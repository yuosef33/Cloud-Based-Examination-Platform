package com.yuosef.cloudbasedlabexaminationplatform.config.OnRunTimeCode;

import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplate;
import com.yuosef.cloudbasedlabexaminationplatform.repository.LabTemplateDao;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class LabTemplateConfig {
    @Bean
    CommandLineRunner initBaseAmi(LabTemplateDao repo) {
        return args -> {

            if (!repo.existsByAmiName("Base_AMI")) {
                repo.save(new LabTemplate("Base_AMI", "ami-017a8f63571784296"));
            }

        };
    }
}
