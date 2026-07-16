package com.skillstorm.awsdemos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AwsAiDemosApplication {

    public static void main(String[] args) {
        SpringApplication.run(AwsAiDemosApplication.class, args);
    }
}
