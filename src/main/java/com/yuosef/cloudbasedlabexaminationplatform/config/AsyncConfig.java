package com.yuosef.cloudbasedlabexaminationplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);    // 10 VMs running at same time
        executor.setMaxPoolSize(20);     // burst up to 20
        executor.setQueueCapacity(50);   // queue if more than 20
        executor.setThreadNamePrefix("vm-collect-");
        executor.initialize();
        return executor;
    }
}