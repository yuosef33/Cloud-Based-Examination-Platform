package com.yuosef.cloudbasedlabexaminationplatform.repository;

import com.yuosef.cloudbasedlabexaminationplatform.models.Lab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabDao extends JpaRepository<Lab, Long> {

}
