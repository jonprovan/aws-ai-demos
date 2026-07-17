package com.skillstorm.awsdemos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds the "bedrock.*" keys (sourced from .env via spring-dotenv) into typed config. */
@ConfigurationProperties(prefix = "bedrock")
public record BedrockProperties(String baseUrl, String apiKey, Agent agent) {

    public record Agent(String id, String aliasId) {
    }
}
