package com.yuosef.demo1.cloudexaminationplatform.Config;

import com.yuosef.demo1.cloudexaminationplatform.Daos.AuthorityDao;
import com.yuosef.demo1.cloudexaminationplatform.Models.Authority;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
@Configuration
public class AuthorityConfig {
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
