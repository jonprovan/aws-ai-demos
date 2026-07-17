package com.skillstorm.awsdemos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.transcribe.TranscribeClient;

/** Wires up one AWS SDK client bean per service, all sharing the same region and default credential chain. */
@Configuration
public class AwsClientConfig {

    private final Region region;
    private final DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

    /** Reads the configured AWS region out of application.yml (via AwsDemoProperties). */
    public AwsClientConfig(AwsDemoProperties properties) {
        this.region = Region.of(properties.region());
    }

    /** Client used to upload/download/delete scratch files in S3 for Textract and Transcribe. */
    @Bean
    S3Client s3Client() {
        return S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /** Client used to extract text from uploaded images/PDFs. */
    @Bean
    TextractClient textractClient() {
        return TextractClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /** Client used to synthesize speech audio from text. */
    @Bean
    PollyClient pollyClient() {
        return PollyClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /** Client used to run speech-to-text jobs on uploaded audio/video. */
    @Bean
    TranscribeClient transcribeClient() {
        return TranscribeClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /** Client used to detect labels and text in uploaded images. */
    @Bean
    RekognitionClient rekognitionClient() {
        return RekognitionClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /** Client used to analyze language, sentiment, entities, and key phrases in text. */
    @Bean
    ComprehendClient comprehendClient() {
        return ComprehendClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /** Async client used to invoke the custom Bedrock Agent (InvokeAgent is an event-stream API, async-only). */
    @Bean
    BedrockAgentRuntimeAsyncClient bedrockAgentRuntimeAsyncClient() {
        return BedrockAgentRuntimeAsyncClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
