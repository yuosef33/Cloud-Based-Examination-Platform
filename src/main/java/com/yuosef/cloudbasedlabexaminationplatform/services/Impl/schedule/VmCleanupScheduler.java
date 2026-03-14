package com.yuosef.cloudbasedlabexaminationplatform.services.Impl.schedule;

import com.yuosef.cloudbasedlabexaminationplatform.models.VmInstance;
import com.yuosef.cloudbasedlabexaminationplatform.models.VmStatus;
import com.yuosef.cloudbasedlabexaminationplatform.repository.VmInstanceDao;
import com.yuosef.cloudbasedlabexaminationplatform.services.Impl.TerraformService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class VmCleanupScheduler {
    private final VmInstanceDao vmInstanceDao;
    private final TerraformService terraformService;
    private static final Logger log = LoggerFactory.getLogger(VmCleanupScheduler.class);

    // runs every 5 minutes
    @Scheduled(fixedDelay = 300000)
    public void destroyStoppedVms() {
        // find all STOPPED VMs not updated in last 30 minutes
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);

        List<VmInstance> stoppedVms = vmInstanceDao
                .findByStatusAndUpdatedAtBefore(VmStatus.STOPPED, threshold);

        for (VmInstance vm : stoppedVms) {
            try {
                log.info("Auto-destroying stopped VM: {}", vm.getInstanceId());
                terraformService.destroyVm(vm.getInstanceId());
            } catch (Exception e) {
                log.error("Failed to destroy VM {}: {}", vm.getInstanceId(), e.getMessage());
            }
        }
    }
}
