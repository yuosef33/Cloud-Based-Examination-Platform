package com.yuosef.cloudbasedlabexaminationplatform.repository;

import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplate;
import com.yuosef.cloudbasedlabexaminationplatform.models.User;
import com.yuosef.cloudbasedlabexaminationplatform.models.VmInstance;
import com.yuosef.cloudbasedlabexaminationplatform.models.VmStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface VmInstanceDao extends JpaRepository<VmInstance, Long> {
    Optional<VmInstance> findByInstanceId(String instanceId);
    List<VmInstance> findByStatusAndUpdatedAtBefore(VmStatus status, LocalDateTime time);
    Optional<VmInstance> findByUserAndLabTemplateAndStatus(
            User user,
            LabTemplate labTemplate,
            VmStatus status
    );
    List<VmInstance> findByLabTemplateAndStatus(LabTemplate labTemplate, VmStatus status);

}
