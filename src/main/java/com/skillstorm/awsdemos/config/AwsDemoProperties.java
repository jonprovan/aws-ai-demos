package com.skillstorm.awsdemos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public record AwsDemoProperties(String region, S3 s3) {

    public record S3(String bucketName) {
    }
}
