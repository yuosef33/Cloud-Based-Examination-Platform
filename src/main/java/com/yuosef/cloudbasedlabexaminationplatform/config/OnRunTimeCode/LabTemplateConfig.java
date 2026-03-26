package com.yuosef.cloudbasedlabexaminationplatform.config.OnRunTimeCode;

import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplate;
import com.yuosef.cloudbasedlabexaminationplatform.models.LabTemplateStatus;
import com.yuosef.cloudbasedlabexaminationplatform.repository.LabTemplateDao;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


@Configuration
public class LabTemplateConfig {
    @Bean
    CommandLineRunner initBaseAmi(LabTemplateDao repo) {
        return args -> {

            if (!repo.existsByAmiName("Base_AMI")) {
                repo.save(new LabTemplate("Base_AMI", "ami-0989a23df83ccca69", LabTemplateStatus.AVAILABLE));
            }

        };
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("lab-scheduler-");
        scheduler.initialize();
        return scheduler;
    }
}
