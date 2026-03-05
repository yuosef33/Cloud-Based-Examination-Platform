package com.yuosef.cloudbasedlabexaminationplatform.repository;

import com.yuosef.cloudbasedlabexaminationplatform.models.OAuthCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
@Repository
public interface OAuthCodeDao extends JpaRepository<OAuthCode, Long> {
    Optional<OAuthCode> findByCode(String code);
    @Modifying
    @Transactional
    @Query("DELETE FROM OAuthCode o WHERE o.email = :email")
    void deleteByEmail(String email);
}
