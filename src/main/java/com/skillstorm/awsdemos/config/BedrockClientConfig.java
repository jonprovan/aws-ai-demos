package com.skillstorm.awsdemos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

/** Wires up an HTTP client pre-configured for Bedrock's OpenAI-compatible endpoint (base URL + Bearer auth). */
@Configuration
public class BedrockClientConfig {

    /** Builds a RestClient with the configured base URL and Authorization header already set for every call. */
    @Bean
    RestClient bedrockRestClient(BedrockProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .build();
    }
}
