package com.yuosef.cloudbasedlabexaminationplatform.services.Impl;

import com.yuosef.cloudbasedlabexaminationplatform.models.Lab;
import com.yuosef.cloudbasedlabexaminationplatform.models.VmInstance;
import com.yuosef.cloudbasedlabexaminationplatform.models.VmStatus;
import com.yuosef.cloudbasedlabexaminationplatform.repository.LabDao;
import com.yuosef.cloudbasedlabexaminationplatform.repository.VmInstanceDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class FileCollectionService {

    private final SsmClient ssmClient;
    private final LabDao labDao;
    private final VmInstanceDao vmInstanceDao;
    private final TerraformService terraformService;
    private final Executor taskExecutor;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    public FileCollectionService(SsmClient ssmClient, LabDao labDao,
                                 VmInstanceDao vmInstanceDao,
                                 TerraformService terraformService,
                                 @Qualifier("taskExecutor") Executor taskExecutor) {
        this.ssmClient = ssmClient;
        this.labDao = labDao;
        this.vmInstanceDao = vmInstanceDao;
        this.terraformService = terraformService;
        this.taskExecutor = taskExecutor;
    }

    @Async
    public void collectAllStudentFiles(Long labId) {
        Lab lab = labDao.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found: " + labId));

        List<VmInstance> waitingVms = vmInstanceDao
                .findAllByLabAndStatus(lab, VmStatus.WAITING);

        if (waitingVms.isEmpty()) {
            log.warn("No WAITING VMs found for lab: {}", labId);
            lab.setCollected(true);
            labDao.save(lab);
            return;
        }

        log.info("Starting parallel file collection for {} VMs in lab: {}",
                waitingVms.size(), lab.getLabName());

        List<CompletableFuture<Void>> futures = waitingVms.stream()
                .map(vm -> CompletableFuture.runAsync(
                        () -> collectSingleVm(vm, lab),
                        taskExecutor
                ))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // reload to avoid stale entity
        Lab freshLab = labDao.findById(labId)
                .orElseThrow(() -> new RuntimeException("Lab not found"));
        freshLab.setCollected(true);
        labDao.save(freshLab);
        log.info("Collection done for lab: {}", lab.getLabName());
    }

    private void collectSingleVm(VmInstance vm, Lab lab) {
        try {
            terraformService.startInstance(vm.getInstanceId());
            waitForSsmReady(vm.getInstanceId());

            // get osType from VM's template
            String osType = vm.getLabTemplate().getOsType().name();

            String commandId = sendCollectionCommand(
                    vm.getInstanceId(),
                    lab.getId(),
                    vm.getUser().getId(),
                    lab.getFileDirectory(),
                    osType
            );

            waitForCommandCompletion(vm.getInstanceId(), commandId);
            terraformService.destroyVmWithSdk(vm.getInstanceId());
            vm.setStatus(VmStatus.TERMINATED);
            vmInstanceDao.save(vm);
            log.info("VM {} collected and terminated", vm.getInstanceId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Collection interrupted for VM {}", vm.getInstanceId());
        } catch (Exception e) {
            log.error("Collection failed for VM {}: {}",
                    vm.getInstanceId(), e.getMessage());
        }
    }

    private String buildWindowsScript(String sourceDir, Long labId, Long studentId) {
        return String.format("""
            $sourcePath = "%s"
            $destination = "s3://%s/labs/%s/%s/"
            if (Test-Path $sourcePath) {
                aws s3 cp $sourcePath $destination --recursive
            }
            """,
                sourceDir, bucketName, labId, studentId
        );
    }

    private String buildLinuxScript(String sourceDir, Long labId, Long studentId) {
        return String.format("""
            SOURCE="%s"
            DEST="s3://%s/labs/%s/%s/"
            if [ -d "$SOURCE" ]; then
                aws s3 cp "$SOURCE" "$DEST" --recursive
            fi
            """,
                sourceDir, bucketName, labId, studentId
        );
    }

    private String sendCollectionCommand(String instanceId, Long labId,
                                         Long studentId, String sourceDir,
                                         String osType) {

        boolean isLinux = "LINUX".equalsIgnoreCase(osType);

        // different document and script based on OS
        String documentName = isLinux
                ? "AWS-RunShellScript"
                : "AWS-RunPowerShellScript";

        String script = isLinux
                ? buildLinuxScript(sourceDir, labId, studentId)
                : buildWindowsScript(sourceDir, labId, studentId);

        SendCommandResponse response = ssmClient.sendCommand(
                SendCommandRequest.builder()
                        .instanceIds(instanceId)
                        .documentName(documentName)
                        .parameters(Map.of("commands", List.of(script)))
                        .timeoutSeconds(300)
                        .build()
        );

        String commandId = response.command().commandId();
        log.info("SSM {} command sent: {} for instance: {}", osType, commandId, instanceId);
        return commandId;
    }

    private void waitForSsmReady(String instanceId) throws InterruptedException {
        log.info("Waiting for SSM on: {}", instanceId);
        for (int i = 0; i < 20; i++) {
            try {
                DescribeInstanceInformationResponse response =
                        ssmClient.describeInstanceInformation(
                                DescribeInstanceInformationRequest.builder()
                                        .filters(InstanceInformationStringFilter.builder()
                                                .key("InstanceIds")
                                                .values(instanceId)
                                                .build())
                                        .build()
                        );

                // ADD THIS
                log.info("SSM check attempt {}/20 — found {} instances for {}",
                        i + 1,
                        response.instanceInformationList().size(),
                        instanceId);

                if (!response.instanceInformationList().isEmpty()) {
                    log.info("SSM ready on: {}", instanceId);
                    return;
                }
            } catch (Exception e) {
                log.error("SSM check error attempt {}/20: {}", i + 1, e.getMessage());
            }
            Thread.sleep(15000);
        }
        throw new RuntimeException("SSM never became ready for: " + instanceId);
    }
    private void waitForCommandCompletion(String instanceId,
                                          String commandId) throws InterruptedException {
        log.info("Waiting for command: {}", commandId);
        for (int i = 0; i < 20; i++) {
            Thread.sleep(15000);
            try {
                GetCommandInvocationResponse response =
                        ssmClient.getCommandInvocation(
                                GetCommandInvocationRequest.builder()
                                        .instanceId(instanceId)
                                        .commandId(commandId)
                                        .build()
                        );
                String status = response.statusAsString();
                log.info("Command {} status: {}", commandId, status);
                if (status.equals("Success")) return;
                if (status.equals("Failed") || status.equals("Cancelled")) {
                    throw new RuntimeException("SSM command failed");
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Error checking command: {}", e.getMessage());
            }
        }
        throw new RuntimeException("Command timed out: " + commandId);
    }
}