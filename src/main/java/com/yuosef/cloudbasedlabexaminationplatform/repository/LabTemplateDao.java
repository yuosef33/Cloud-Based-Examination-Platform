package com.yuosef.cloudbasedlabexaminationplatform.repository;

import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplate;
import com.yuosef.cloudbasedlabexaminationplatform.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabTemplateDao extends JpaRepository<LabTemplate,Long> {
    List<LabTemplate> findAllByCreatedBy(User user);
    Optional<LabTemplate> findByAmiName(String amiName);
    boolean existsByAmiName(String amiName);
}
