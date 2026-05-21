package com.predix.compliance;

import com.predix.compliance.config.ComplianceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties(ComplianceProperties.class)
@EnableAsync
public class CompliancePolicyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompliancePolicyApplication.class, args);
    }
}
