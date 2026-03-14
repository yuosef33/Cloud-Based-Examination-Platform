package com.yuosef.cloudbasedlabexaminationplatform.config.OnRunTimeCode;

import com.yuosef.cloudbasedlabexaminationplatform.repository.AuthorityDao;
import com.yuosef.cloudbasedlabexaminationplatform.models.Authority;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
@Configuration
public class AuthorityConfig {
    /// this class works at runtime ro create USER & ADMIN roles
    @Bean
    CommandLineRunner initRoles(AuthorityDao repo) {
        return args -> {

            if (!repo.existsByUserRole("USER")) {
                repo.save(new Authority(null, "USER", new ArrayList<>()));
            }

            if (!repo.existsByUserRole("ADMIN")) {
                repo.save(new Authority(null, "ADMIN", new ArrayList<>()));
            }
        };
    }
}
