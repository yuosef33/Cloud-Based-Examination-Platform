package com.yuosef.demo1.cloudexaminationplatform.sitting;



import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "management-token")
public class TokenConfig {
    private String secret;
    private Duration duration;

    public TokenConfig() {
    }

    public TokenConfig(String secret, Duration duration) {
        this.secret = secret;
        this.duration = duration;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }
}
