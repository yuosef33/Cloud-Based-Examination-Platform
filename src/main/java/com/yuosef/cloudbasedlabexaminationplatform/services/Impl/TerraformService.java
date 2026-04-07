package com.yuosef.cloudbasedlabexaminationplatform.services.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuosef.cloudbasedlabexaminationplatform.models.*;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.TerraformOutput;
import com.yuosef.cloudbasedlabexaminationplatform.repository.LabTemplateDao;
import com.yuosef.cloudbasedlabexaminationplatform.repository.VmInstanceDao;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TerraformService {
    private static final Logger log = LoggerFactory.getLogger(TerraformService.class);
    private final VmInstanceDao vmInstanceDao;
    private final LabTemplateDao labTemplateDao;
    private final Ec2Client ec2Client;
    public static final String Base_Ami = "Base_AMI";

    private static final String TEMPLATE_DIR = "terraform-template";
    private static final String PRE_INIT_DIR = "terraform-pre-init";
    private static final String RUNS_DIR = "terraform-runs";
    private static final String PLUGIN_CACHE_DIR = "terraform-plugin-cache";

    /**
     * Called ONCE on app startup — pre-initializes terraform so every
     * EC2 creation skips the slow init step
     */
    @PostConstruct
    public void preInitTerraform() {
        try {
            // create plugin cache dir — providers downloaded once and reused
            new File(PLUGIN_CACHE_DIR).mkdirs();
            log.info("Terraform plugin cache dir ready: {}", PLUGIN_CACHE_DIR);

            File preInitDir = new File(PRE_INIT_DIR);

            // if already pre-initialized → skip
            if (new File(preInitDir, ".terraform").exists()) {
                log.info("Terraform already pre-initialized skipping");
                return;
            }

            log.info("Pre-initializing Terraform... this runs only once");

            // copy template to pre-init dir
            FileUtils.copyDirectory(new File(TEMPLATE_DIR), preInitDir);

            // run init once with plugin cache
            ProcessBuilder initBuilder = new ProcessBuilder(
                    "terraform", "init",
                    "-reconfigure",
                    "-input=false",
                    "-backend-config=key=ec2/pre-init/terraform.tfstate"
            );

            // use plugin cache — downloads providers once, reuses forever
            initBuilder.environment().put("TF_PLUGIN_CACHE_DIR",
                    new File(PLUGIN_CACHE_DIR).getAbsolutePath());
            initBuilder.directory(preInitDir);
            initBuilder.redirectErrorStream(true);

            Process process = initBuilder.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                log.info("TERRAFORM PRE-INIT: {}", line);
            }

            int exit = process.waitFor();
            if (exit != 0) {
                log.error("Terraform pre-init failed!");
                return;
            }

            log.info("Terraform pre-initialized successfully ");

        } catch (Exception e) {
            log.error("Failed to pre-initialize Terraform: {}", e.getMessage());
        }
    }

    public TerraformOutput createEc2(User user, String amiName) throws Exception {

        LabTemplate labTemplate = labTemplateDao.findByAmiName(amiName)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "AMI not found: " + amiName));

        String runId = UUID.randomUUID().toString();
        String stateKey = "ec2/" + runId + "/terraform.tfstate";

        File targetDir = new File(RUNS_DIR + "/" + runId);

        // copy from PRE-INIT dir — already has .terraform folder
        // this means we SKIP the slow init step entirely
        FileUtils.copyDirectory(new File(PRE_INIT_DIR), targetDir);
        log.info("Copied pre-init dir for runId: {}", runId);

        // ---------------- terraform init (fast — just reconfigures backend) ----------------
        // we still need to run init to point to the new state key
        // but it's fast now because providers are already cached
        ProcessBuilder initBuilder = new ProcessBuilder(
                "terraform", "init",
                "-reconfigure",
                "-input=false",
                "-backend-config=key=" + stateKey
        );

        // use plugin cache — no downloading needed
        initBuilder.environment().put("TF_PLUGIN_CACHE_DIR",
                new File(PLUGIN_CACHE_DIR).getAbsolutePath());
        initBuilder.directory(targetDir);
        initBuilder.redirectErrorStream(true);

        Process initProcess = initBuilder.start();
        BufferedReader initReader = new BufferedReader(
                new InputStreamReader(initProcess.getInputStream()));

        String line;
        while ((line = initReader.readLine()) != null) {
            log.info("TERRAFORM INIT: {}", line);
        }

        int initExit = initProcess.waitFor();
        if (initExit != 0) {
            throw new RuntimeException("Terraform init failed");
        }

        // ---------------- terraform apply ----------------
        ProcessBuilder applyBuilder = new ProcessBuilder(
                "terraform", "apply",
                "-auto-approve",
                "-input=false",
                "-var", "ami_id=" + labTemplate.getAmiId(),
                "-var", "instance_type=t3.2xlarge",
                "-var", "instance_name=" + user.getName() + "-" + runId.substring(0, 8) + "-VM"
        );

        applyBuilder.environment().put("TF_PLUGIN_CACHE_DIR",
                new File(PLUGIN_CACHE_DIR).getAbsolutePath());
        applyBuilder.directory(targetDir);
        applyBuilder.redirectErrorStream(true);

        Process applyProcess = applyBuilder.start();
        BufferedReader applyReader = new BufferedReader(
                new InputStreamReader(applyProcess.getInputStream()));

        while ((line = applyReader.readLine()) != null) {
            log.info("TERRAFORM APPLY: {}", line);
        }

        int applyExit = applyProcess.waitFor();
        if (applyExit != 0) {
            throw new RuntimeException("Terraform apply failed");
        }

        // ---------------- terraform output ----------------
        ProcessBuilder outputBuilder = new ProcessBuilder(
                "terraform", "output", "-json"
        );

        outputBuilder.directory(targetDir);
        Process outputProcess = outputBuilder.start();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(outputProcess.getInputStream());

        String ip = node.get("public_ip").get("value").asText();
        String id = node.get("instance_id").get("value").asText();

        // ---------------- save instance ----------------
        VmInstance vmInstance = VmInstance.builder()
                .instanceId(id)
                .publicIp(ip)
                .labTemplate(labTemplate)
                .status(VmStatus.RUNNING)
                .user(user)
                .vncPort(5900)
                .terraformStateKey(stateKey)
                .runId(runId)
                .build();

        vmInstanceDao.save(vmInstance);

        return new TerraformOutput(ip, id);
    }

    public void destroyVm(String vmId) throws Exception {
        VmInstance vm = vmInstanceDao.findByInstanceId(vmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "VM not found"));

        File runDir = new File(RUNS_DIR + "/" + vm.getRunId());

        if (!runDir.exists()) {
            log.error("Run folder missing — recreating from S3 state: {}", vmId);
            FileUtils.copyDirectory(new File(PRE_INIT_DIR), runDir);
        }

        // -------- terraform init --------
        ProcessBuilder initBuilder = new ProcessBuilder(
                "terraform", "init",
                "-reconfigure",
                "-backend-config=key=" + vm.getTerraformStateKey(),
                "-input=false"
        );

        initBuilder.environment().put("TF_PLUGIN_CACHE_DIR",
                new File(PLUGIN_CACHE_DIR).getAbsolutePath());
        initBuilder.directory(runDir);
        initBuilder.redirectErrorStream(true);

        Process initProcess = initBuilder.start();
        BufferedReader initReader = new BufferedReader(
                new InputStreamReader(initProcess.getInputStream()));

        String line;
        while ((line = initReader.readLine()) != null) {
            log.info("TERRAFORM INIT: {}", line);
        }

        int initExit = initProcess.waitFor();
        if (initExit != 0) throw new RuntimeException("Terraform init failed");

        // -------- terraform destroy --------
        ProcessBuilder destroyBuilder = new ProcessBuilder(
                "terraform", "destroy",
                "-auto-approve",
                "-input=false",
                "-var", "ami_id=" + vm.getLabTemplate().getAmiId(),
                "-var", "instance_type=t3.large",
                "-var", "instance_name=template-vm"
        );

        destroyBuilder.environment().put("TF_PLUGIN_CACHE_DIR",
                new File(PLUGIN_CACHE_DIR).getAbsolutePath());
        destroyBuilder.directory(runDir);
        destroyBuilder.redirectErrorStream(true);

        Process destroyProcess = destroyBuilder.start();
        BufferedReader destroyReader = new BufferedReader(
                new InputStreamReader(destroyProcess.getInputStream()));

        while ((line = destroyReader.readLine()) != null) {
            log.info("TERRAFORM DESTROY: {}", line);
        }

        int destroyExit = destroyProcess.waitFor();
        if (destroyExit != 0) throw new RuntimeException("Terraform destroy failed");

        FileUtils.deleteDirectory(runDir);

        vm.setStatus(VmStatus.TERMINATED);
        vmInstanceDao.save(vm);
    }

    @Async
    public void createAmiFromVm(VmInstance vm, String amiName) throws Exception {
        CreateImageRequest createImageRequest = CreateImageRequest.builder()
                .instanceId(vm.getInstanceId())
                .name(amiName)
                .noReboot(true)
                .build();

        CreateImageResponse createImageResponse = ec2Client.createImage(createImageRequest);
        String newAmiId = createImageResponse.imageId();

        log.info("AMI creation started: {} for instance: {}", newAmiId, vm.getInstanceId());

        LabTemplate labTemplate = LabTemplate.builder()
                .amiName(amiName)
                .amiId(newAmiId)
                .createdBy(vm.getUser())
                .labTemplateStatus(LabTemplateStatus.PENDING)
                .build();

        LabTemplate savedLabTemplate = labTemplateDao.save(labTemplate);

        waitForAmiAvailable(newAmiId);

        log.info("AMI available: {}", newAmiId);
        savedLabTemplate.setLabTemplateStatus(LabTemplateStatus.AVAILABLE);
        labTemplateDao.save(savedLabTemplate);

        vm.setStatus(VmStatus.STOPPED);
        vmInstanceDao.save(vm);

        log.info("Lab template saved: {}", newAmiId);
    }

    private void waitForAmiAvailable(String amiId) throws InterruptedException {
        log.info("Waiting for AMI {} to become available...", amiId);

        for (int i = 0; i < 30; i++) {
            DescribeImagesRequest describeRequest = DescribeImagesRequest.builder()
                    .imageIds(amiId)
                    .build();

            DescribeImagesResponse describeResponse = ec2Client.describeImages(describeRequest);

            if (!describeResponse.images().isEmpty()) {
                String state = describeResponse.images().get(0).stateAsString();
                log.info("AMI {} state: {}", amiId, state);

                if ("available".equals(state)) {
                    log.info("AMI {} successfully created ", amiId);
                    return;
                }
            }

            Thread.sleep(20000);
        }

        log.error("AMI creation timed out for: {}", amiId);
    }

    public VmInstance checkVmExist(String vmId) {
        return vmInstanceDao.findByInstanceId(vmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "VM not found"));
    }

    public void stopInstance(String instanceId) {
        StopInstancesRequest request = StopInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();
        ec2Client.stopInstances(request);
        log.info("Stopped instance: {}", instanceId);
    }


    /**
     *
     * -----------------AWS SDK Functions-------------
     */


    /**
     * AWS SDK alternative to Terraform createEc2
     * Much faster — ~3-5 seconds vs ~20-40 seconds with Terraform
     * No state files, no init, no apply
     */
    public TerraformOutput createEc2WithSdk(User user, String amiName,Lab lab) throws Exception {

        LabTemplate labTemplate = labTemplateDao.findByAmiName(amiName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "AMI not found: " + amiName));

        // -------- create EC2 instance --------
        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .imageId(labTemplate.getAmiId())
                .instanceType(InstanceType.T3_2_XLARGE)
                .minCount(1)
                .maxCount(1)
                .keyName("Ec2-Base")
                .securityGroupIds("sg-0687cc28f63bd584a")
                .tagSpecifications(TagSpecification.builder()
                        .resourceType(ResourceType.INSTANCE)
                        .tags(Tag.builder()
                                .key("Name")
                                .value(user.getName() + "-" + UUID.randomUUID().toString().substring(0, 8) + "-VM")
                                .build())
                        .build())
                .build();

        RunInstancesResponse runResponse = ec2Client.runInstances(runRequest);
        String instanceId = runResponse.instances().get(0).instanceId();

        log.info("EC2 instance created: {}", instanceId);

        // -------- wait for instance to be running and get public IP --------
        String publicIp = waitForInstanceRunning(instanceId);

        log.info("EC2 instance running: {} with IP: {}", instanceId, publicIp);

        // -------- save to DB --------
        VmInstance vmInstance = VmInstance.builder()
                .instanceId(instanceId)
                .publicIp(publicIp)
                .labTemplate(labTemplate)
                .status(VmStatus.RUNNING)
                .user(user)
                .vncPort(5900)
                .terraformStateKey(null)  // no terraform state
                .runId(null)              // no terraform run
                .lab(lab)
                .build();

        vmInstanceDao.save(vmInstance);

        return new TerraformOutput(publicIp, instanceId);
    }

    /**
     * Wait for EC2 instance to be in running state and have a public IP
     * AWS takes ~10-15 seconds to start an instance
     */
    private String waitForInstanceRunning(String instanceId) throws InterruptedException {
        log.info("Waiting for instance {} to be running...", instanceId);

        for (int i = 0; i < 20; i++) { // max 3 minutes (20 * 10s)
            Thread.sleep(10000); // wait 10 seconds

            DescribeInstancesRequest describeRequest = DescribeInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();

            DescribeInstancesResponse describeResponse = ec2Client.describeInstances(describeRequest);

            if (!describeResponse.reservations().isEmpty()) {
                Instance instance = describeResponse.reservations()
                        .get(0).instances().get(0);

                String state = instance.state().nameAsString();
                log.info("Instance {} state: {}", instanceId, state);

                if ("running".equals(state) && instance.publicIpAddress() != null) {
                    return instance.publicIpAddress();
                }
            }
        }

        throw new RuntimeException("Instance failed to start: " + instanceId);
    }

    /**
     * AWS SDK alternative to Terraform destroyVm
     * Terminates EC2 instance directly — no state files needed
     */
    public void destroyVmWithSdk(String vmId) {
        VmInstance vm = vmInstanceDao.findByInstanceId(vmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "VM not found"));

        // -------- terminate instance --------
        TerminateInstancesRequest terminateRequest = TerminateInstancesRequest.builder()
                .instanceIds(vm.getInstanceId())
                .build();

        ec2Client.terminateInstances(terminateRequest);
        log.info("Terminated instance: {}", vm.getInstanceId());

        // -------- update DB --------
        vm.setStatus(VmStatus.TERMINATED);
        vmInstanceDao.save(vm);
    }


}
