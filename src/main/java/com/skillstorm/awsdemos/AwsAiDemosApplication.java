package com.skillstorm.awsdemos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AwsAiDemosApplication {

    /** Boots the embedded Tomcat server and Spring context; entry point run when the jar is launched. */
    public static void main(String[] args) {
        SpringApplication.run(AwsAiDemosApplication.class, args);
    }
}
