package com.yuosef.cloudbasedlabexaminationplatform.services.Impl.schedule;

import com.yuosef.cloudbasedlabexaminationplatform.models.Lab;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabStatus;
import com.yuosef.cloudbasedlabexaminationplatform.models.VmInstance;
import com.yuosef.cloudbasedlabexaminationplatform.models.VmStatus;
import com.yuosef.cloudbasedlabexaminationplatform.repository.LabDao;
import com.yuosef.cloudbasedlabexaminationplatform.repository.VmInstanceDao;
import com.yuosef.cloudbasedlabexaminationplatform.services.Impl.TerraformService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LabScheduler {
    private final LabDao labDao;
    private final TaskScheduler taskScheduler;
    private final VmInstanceDao vmInstanceDao;
    private final TerraformService terraformService;


    @PostConstruct
    public void scheduleExistingLabs() {
        log.info("Server started — rescheduling existing labs...");

        // find all labs that are not finished yet
        List<Lab> unfinishedLabs = labDao.findByStatusIn((
                List.of(LabStatus.CREATED, LabStatus.RUNNING)
        ));

        for (Lab lab : unfinishedLabs) {
            scheduleLab(lab);
        }

        log.info("Rescheduled {} labs", unfinishedLabs.size());
    }

    public void scheduleLab(Lab lab) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = lab.getLabStartTime().plus(lab.getLabDuration());
        LocalDateTime now2 = LocalDateTime.now();
        log.info("Current server time: {}", now2);  // ← add this
        log.info("Lab start time: {}", lab.getLabStartTime());
        // -------- Alarm 1: start the lab --------
        if (lab.getStatus() == LabStatus.CREATED) {
            if (lab.getLabStartTime().isAfter(now)) {
                // start time is in the future  schedule normally
                taskScheduler.schedule(
                        () -> startLab(lab.getId()),
                        lab.getLabStartTime().atZone(ZoneId.systemDefault()).toInstant()
                );
                log.info("Scheduled START for lab '{}' at {} (instant: {})",
                        lab.getLabName(),
                        lab.getLabStartTime(),
                        lab.getLabStartTime().atZone(ZoneId.systemDefault()).toInstant()
                );
            } else {
                // start time already passed ( server was down)
                // start it immediately
                log.info("Lab '{}' start time already passed — starting now",
                        lab.getLabName());
                startLab(lab.getId());
            }
        }

        // -------- Alarm 2: finish the lab --------
        if (endTime.isAfter(now)) {
            // end time is in the future schedule normally
            taskScheduler.schedule(
                    () -> finishLab(lab.getId()),
                    endTime.atZone(ZoneId.systemDefault()).toInstant()
            );
            log.info("Scheduled FINISH for lab '{}' at {}",
                    lab.getLabName(), endTime);
        } else {
            // end time already passed (server was down)
            // finish it immediately
            log.info("Lab '{}' end time already passed — finishing now",
                    lab.getLabName());
            finishLab(lab.getId());
        }
    }

    @Transactional
    public void startLab(Long labId) {
        labDao.findById(labId).ifPresent(lab -> {

            // double check it's still CREATED — avoid running twice
            if (lab.getStatus() != LabStatus.CREATED) {
                log.warn("Lab {} is already {}, skipping start",
                        labId, lab.getStatus());
                return;
            }

            lab.setStatus(LabStatus.RUNNING);
            labDao.save(lab);

            log.info(" Lab STARTED: '{}'", lab.getLabName());
        });
    }

    /**
     * Called exactly at labStartTime + labDuration.
     * Changes lab status from RUNNING → FINISHED
     */
    @Transactional
    public void finishLab(Long labId) {
        labDao.findById(labId).ifPresent(lab -> {

            // double check it's still RUNNING — avoid finishing twice
            if (lab.getStatus() != LabStatus.RUNNING) {
                log.warn("Lab {} is already {}, skipping finish",
                        labId, lab.getStatus());
                return;
            }
            List<VmInstance> runningVms = vmInstanceDao
                    .findAllByLabAndStatus(lab, VmStatus.RUNNING);

            for (VmInstance vm : runningVms) {
                try {
                    terraformService.stopInstance(vm.getInstanceId());
                    vm.setStatus(VmStatus.WAITING);
                    vmInstanceDao.save(vm);
                    log.info("Stopped VM: {} → WAITING for lab: {}",
                            vm.getInstanceId(), lab.getLabName());
                } catch (Exception e) {
                    log.error("Failed to stop VM {}: {}", vm.getInstanceId(), e.getMessage());
                }
            }

            lab.setStatus(LabStatus.FINISHED);
            labDao.save(lab);
            log.info("Lab FINISHED: '{}'", lab.getLabName());
        });

    }


}
