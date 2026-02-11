package com.yuosef.demo1.cloudexaminationplatform.Daos;

import com.yuosef.demo1.cloudexaminationplatform.Models.LabTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabTemplateDao extends JpaRepository<LabTemplate,Long> {
    List<LabTemplate> findByUserId(Long userId);
}
