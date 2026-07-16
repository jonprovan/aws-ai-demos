package com.skillstorm.awsdemos.service;

import com.skillstorm.awsdemos.dto.ComprehendResult;
import com.skillstorm.awsdemos.exception.AwsDemoException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.DetectDominantLanguageRequest;
import software.amazon.awssdk.services.comprehend.model.DetectEntitiesRequest;
import software.amazon.awssdk.services.comprehend.model.DetectKeyPhrasesRequest;
import software.amazon.awssdk.services.comprehend.model.DetectSentimentRequest;
import software.amazon.awssdk.services.comprehend.model.SentimentScore;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ComprehendService {

    private static final int MAX_BYTES = 5000;

    private final ComprehendClient comprehendClient;

    public ComprehendService(ComprehendClient comprehendClient) {
        this.comprehendClient = comprehendClient;
    }

    public ComprehendResult analyzeText(String text) {
        if (text == null || text.isBlank()) {
            throw new AwsDemoException("Please provide some text to analyze");
        }
        if (text.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new AwsDemoException("Text is too long for this demo (Comprehend's synchronous APIs cap input at 5,000 bytes UTF-8)");
        }

        String languageCode = comprehendClient
                .detectDominantLanguage(DetectDominantLanguageRequest.builder().text(text).build())
                .languages()
                .stream()
                .findFirst()
                .map(l -> l.languageCode())
                .orElse("en");

        var sentimentResponse = comprehendClient.detectSentiment(
                DetectSentimentRequest.builder().text(text).languageCode(languageCode).build());

        SentimentScore scores = sentimentResponse.sentimentScore();
        Map<String, Float> sentimentScores = new LinkedHashMap<>();
        sentimentScores.put("Positive", scores.positive());
        sentimentScores.put("Negative", scores.negative());
        sentimentScores.put("Neutral", scores.neutral());
        sentimentScores.put("Mixed", scores.mixed());

        var entities = comprehendClient
                .detectEntities(DetectEntitiesRequest.builder().text(text).languageCode(languageCode).build())
                .entities()
                .stream()
                .map(e -> new ComprehendResult.Entity(e.text(), e.typeAsString(), e.score()))
                .toList();

        var keyPhrases = comprehendClient
                .detectKeyPhrases(DetectKeyPhrasesRequest.builder().text(text).languageCode(languageCode).build())
                .keyPhrases()
                .stream()
                .map(kp -> kp.text())
                .toList();

        return new ComprehendResult(
                languageCode,
                sentimentResponse.sentimentAsString(),
                sentimentScores,
                entities,
                keyPhrases);
    }
}
