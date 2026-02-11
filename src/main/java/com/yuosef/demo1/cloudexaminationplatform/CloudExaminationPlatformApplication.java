package com.yuosef.demo1.cloudexaminationplatform;

import com.yuosef.demo1.cloudexaminationplatform.sitting.TokenConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TokenConfig.class)
public class CloudExaminationPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudExaminationPlatformApplication.class, args);
    }

}
