package com.yuosef.cloudbasedlabexaminationplatform.repository;

import com.yuosef.cloudbasedlabexaminationplatform.models.Lab;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface LabDao extends JpaRepository<Lab, Long> {
    List<Lab> findByStatusAndLabStartTimeBefore(LabStatus status, LocalDateTime time);
    List<Lab> findByStatus(LabStatus status);

    List<Lab> findByStatusIn(Collection<LabStatus> statuses);
}
