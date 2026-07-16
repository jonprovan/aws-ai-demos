package com.skillstorm.awsdemos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds the "aws.*" keys from application.yml (region, S3 bucket name) into typed config. */
@ConfigurationProperties(prefix = "aws")
public record AwsDemoProperties(String region, S3 s3) {

    public record S3(String bucketName) {
    }
}
