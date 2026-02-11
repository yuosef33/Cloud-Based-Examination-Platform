package com.yuosef.demo1.cloudexaminationplatform.Daos;

import com.yuosef.demo1.cloudexaminationplatform.Models.Lab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabDao extends JpaRepository<Lab, Long> {

}
