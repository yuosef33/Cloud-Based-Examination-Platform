package com.yuosef.demo1.cloudexaminationplatform.Daos;

import com.yuosef.demo1.cloudexaminationplatform.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User,Long> {
    User findUserByEmail(String email);
}
