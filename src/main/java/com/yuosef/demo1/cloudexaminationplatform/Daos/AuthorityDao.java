package com.yuosef.demo1.cloudexaminationplatform.Daos;

import com.yuosef.demo1.cloudexaminationplatform.Models.Authority;
import com.yuosef.demo1.cloudexaminationplatform.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorityDao extends JpaRepository<Authority,Long> {
    Authority findByUserRole(String userRole);
    boolean existsByUserRole(String userRole);

}
