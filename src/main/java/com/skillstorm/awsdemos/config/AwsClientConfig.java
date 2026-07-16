package com.skillstorm.awsdemos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.transcribe.TranscribeClient;

@Configuration
public class AwsClientConfig {

    private final Region region;
    private final DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

    public AwsClientConfig(AwsDemoProperties properties) {
        this.region = Region.of(properties.region());
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public TextractClient textractClient() {
        return TextractClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public PollyClient pollyClient() {
        return PollyClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public TranscribeClient transcribeClient() {
        return TranscribeClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public RekognitionClient rekognitionClient() {
        return RekognitionClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public ComprehendClient comprehendClient() {
        return ComprehendClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
