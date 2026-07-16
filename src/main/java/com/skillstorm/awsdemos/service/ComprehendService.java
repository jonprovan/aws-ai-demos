package com.skillstorm.awsdemos.service;

import com.skillstorm.awsdemos.dto.ComprehendResult;
import com.skillstorm.awsdemos.exception.AwsDemoException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.DetectDominantLanguageRequest;
import software.amazon.awssdk.services.comprehend.model.DetectEntitiesRequest;
import software.amazon.awssdk.services.comprehend.model.DetectKeyPhrasesRequest;
import software.amazon.awssdk.services.comprehend.model.DetectSentimentRequest;
import software.amazon.awssdk.services.comprehend.model.DominantLanguage;
import software.amazon.awssdk.services.comprehend.model.KeyPhrase;
import software.amazon.awssdk.services.comprehend.model.SentimentScore;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Runs a block of text through Comprehend's language, sentiment, entity, and key-phrase detection. */
@Service
public class ComprehendService {

    private static final int MAX_BYTES = 5000;

    private final ComprehendClient comprehendClient;

    public ComprehendService(ComprehendClient comprehendClient) {
        this.comprehendClient = comprehendClient;
    }

    /**
     * Validates the text is present and within Comprehend's 5,000-byte synchronous-API limit,
     * detects the dominant language, then uses that language code to run sentiment, entity, and
     * key-phrase detection, combining all four results into one object.
     */
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
                .map(DominantLanguage::languageCode)
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
                .map(KeyPhrase::text)
                .toList();

        return new ComprehendResult(
                languageCode,
                sentimentResponse.sentimentAsString(),
                sentimentScores,
                entities,
                keyPhrases);
    }
}
