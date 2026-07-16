package com.lab.governance.lab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GovernanceLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovernanceLabApplication.class, args);
    }
}
