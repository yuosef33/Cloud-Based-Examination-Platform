package com.yuosef.cloudbasedlabexaminationplatform.services.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuosef.cloudbasedlabexaminationplatform.config.JWT.TokenHandler;
import com.yuosef.cloudbasedlabexaminationplatform.models.Dtos.TerraformOutput;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplate;
import com.yuosef.cloudbasedlabexaminationplatform.models.User;
import com.yuosef.cloudbasedlabexaminationplatform.models.VmInstance;
import com.yuosef.cloudbasedlabexaminationplatform.models.VmStatus;
import com.yuosef.cloudbasedlabexaminationplatform.repository.LabTemplateDao;
import com.yuosef.cloudbasedlabexaminationplatform.repository.VmInstanceDao;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateImageRequest;
import software.amazon.awssdk.services.ec2.model.CreateImageResponse;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;

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
    private final String Base_Ami = "Base_AMI";

    public TerraformOutput createNewLabTemplate(User user) throws Exception {

        boolean isAdmin = user.getAuthorities()
                .stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "this is admin privileges only"
            );
        }

        LabTemplate labTemplate = labTemplateDao.findByAmiName(Base_Ami)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Base-Ami not found"));

        String runId = UUID.randomUUID().toString();
        String stateKey = "ec2/" + runId + "/terraform.tfstate";

        File sourceDir = new File("terraform-template");
        File targetDir = new File("terraform-runs/" + runId);

        FileUtils.copyDirectory(sourceDir, targetDir);

        // ---------------- terraform init ----------------

        ProcessBuilder initBuilder = new ProcessBuilder(
                "terraform", "init",
                "-reconfigure",
                "-input=false",
                "-backend-config=key=" + stateKey
        );

        initBuilder.directory(targetDir);
        initBuilder.redirectErrorStream(true);

        Process initProcess = initBuilder.start();

        BufferedReader initReader = new BufferedReader(
                new InputStreamReader(initProcess.getInputStream())
        );

        String line;
        while ((line = initReader.readLine()) != null) {
            System.out.println("TERRAFORM INIT: " + line);
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
                "-var", "instance_type=t3.large",
                "-var", "instance_name="+user.getName()+"_TEMPLATE-VM"
        );

        applyBuilder.directory(targetDir);
        applyBuilder.redirectErrorStream(true);

        Process applyProcess = applyBuilder.start();

        BufferedReader applyReader = new BufferedReader(
                new InputStreamReader(applyProcess.getInputStream())
        );

        while ((line = applyReader.readLine()) != null) {
            System.out.println("TERRAFORM APPLY: " + line);
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


    /**
     *
     * destroy vm
     * @param vmId
     * @throws Exception
     */
    public void destroyVm(String vmId) throws Exception {

        VmInstance vm = vmInstanceDao.findByInstanceId(vmId).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "VM not found"));

        File runDir = new File("terraform-runs/" + vm.getRunId());

        if (!runDir.exists()) {
            log.error("Terraform run folder missing for VM: {} — recreating from S3 state", vmId);

            // recreate the folder and copy terraform template
            File sourceDir = new File("terraform-template");
            FileUtils.copyDirectory(sourceDir, runDir);
        }

        // -------- terraform init --------

        ProcessBuilder initBuilder = new ProcessBuilder(
                "terraform",
                "init",
                "-reconfigure",
                "-backend-config=key=" + vm.getTerraformStateKey(),
                "-input=false"
        );

        initBuilder.directory(runDir);
        initBuilder.redirectErrorStream(true);

        Process initProcess = initBuilder.start();

        BufferedReader initReader = new BufferedReader(
                new InputStreamReader(initProcess.getInputStream())
        );

        String line;
        while ((line = initReader.readLine()) != null) {
            System.out.println("TERRAFORM INIT: " + line);
        }

        int initExit = initProcess.waitFor();

        if (initExit != 0) {
            throw new RuntimeException("Terraform init failed");
        }

        // -------- terraform destroy --------

        ProcessBuilder destroyBuilder = new ProcessBuilder(
                "terraform",
                "destroy",
                "-auto-approve",
                "-input=false" ,
                "-var", "ami_id=" + vm.getLabTemplate().getAmiId(),
                "-var", "instance_type=t3.large",
                "-var", "instance_name=template-vm"
        );

        destroyBuilder.directory(runDir);
        destroyBuilder.redirectErrorStream(true);

        Process destroyProcess = destroyBuilder.start();

        BufferedReader destroyReader = new BufferedReader(
                new InputStreamReader(destroyProcess.getInputStream())
        );

        while ((line = destroyReader.readLine()) != null) {
            System.out.println("TERRAFORM DESTROY: " + line);
        }

        int destroyExit = destroyProcess.waitFor();

        if (destroyExit != 0) {
            throw new RuntimeException("Terraform destroy failed");
        }

        // -------- delete local terraform folder --------

        FileUtils.deleteDirectory(runDir);

        // -------- update DB --------

        vm.setStatus(VmStatus.TERMINATED);
        vmInstanceDao.save(vm);
    }


    @Async
    public void createAmiFromVm(VmInstance vm, String amiName) throws Exception {


        // -------- Step 1: create AMI from instance using AWS SDK --------
        CreateImageRequest createImageRequest = CreateImageRequest.builder()
                .instanceId(vm.getInstanceId())
                .name(amiName)
                .noReboot(true) // reboot for clean AMI
                .build();

        CreateImageResponse createImageResponse = ec2Client.createImage(createImageRequest);
        String newAmiId = createImageResponse.imageId();

        log.info("AMI creation started: {} for instance: {}", newAmiId, vm.getInstanceId());

        // -------- Step 2: wait for AMI to be available --------
        waitForAmiAvailable(newAmiId);

        log.info("AMI available: {}", newAmiId);


        // -------- Step 3: change the EC2 status to stopped so we can destroy it automatically after period using scheduled functions or queries we can add specific condition that if update time not from like 20 minutes if it less than 20 minutes then dont destroy wait to the coming scheduled query    --------
        vm.setStatus(VmStatus.STOPPED);
        vmInstanceDao.save(vm);

        // -------- Step 4: save LabTemplate to DB --------
        User user = vm.getUser();

        LabTemplate labTemplate = LabTemplate.builder()
                .amiName(amiName)
                .amiId(newAmiId)
                .createdBy(user)
                .build();

        labTemplateDao.save(labTemplate);

        log.info("Lab template saved: {}", newAmiId);


    }

    /**
     * Wait for AMI to become available
     * AWS takes a few minutes to create an AMI
     * now it waits just for maximum 600 seconds
     */
    private void waitForAmiAvailable(String amiId) throws InterruptedException {
        log.info("Waiting for AMI {} to become available...", amiId);

        for (int i = 0; i < 30; i++) { // max 10 minutes (30 * 20s)
            DescribeImagesRequest describeRequest = DescribeImagesRequest.builder()
                    .imageIds(amiId)
                    .build();

            DescribeImagesResponse describeResponse = ec2Client.describeImages(describeRequest);

            if (!describeResponse.images().isEmpty()) {
                String state = describeResponse.images().get(0).stateAsString();
                log.info("AMI {} state: {}", amiId, state);

                if ("available".equals(state)) {
                    log.info("AMI {} successfully created  ------------- [CREATED]", amiId);
                    return; // AMI ready
                }

            }

            Thread.sleep(20000); // wait 20 seconds before checking again
        }

         log.error("AMI creation timed out for: " + amiId);
    }


    public VmInstance checkVmExist(String vmId){
        VmInstance vm = vmInstanceDao.findByInstanceId(vmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "VM not found"));
        return vm;
    }
}

