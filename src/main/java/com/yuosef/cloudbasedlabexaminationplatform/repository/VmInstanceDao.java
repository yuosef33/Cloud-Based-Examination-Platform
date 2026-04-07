package com.yuosef.cloudbasedlabexaminationplatform.repository;

import com.yuosef.cloudbasedlabexaminationplatform.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface VmInstanceDao extends JpaRepository<VmInstance, Long> {
    Optional<VmInstance> findByInstanceId(String instanceId);
    List<VmInstance> findByStatusAndUpdatedAtBefore(VmStatus status, LocalDateTime time);
    Optional<VmInstance> findByUserAndLabAndStatus(
            User user,
            Lab lab,
            VmStatus status
    );
    List<VmInstance> findByLabTemplateAndStatus(LabTemplate labTemplate, VmStatus status);
    List<VmInstance> findAllByLabAndStatus(Lab lab, VmStatus status);

}
