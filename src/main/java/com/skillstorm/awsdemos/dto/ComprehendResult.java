package com.skillstorm.awsdemos.dto;

import java.util.List;
import java.util.Map;

/** Combined result of the four Comprehend calls run against one piece of text. */
public record ComprehendResult(
        String languageCode,
        String sentiment,
        Map<String, Float> sentimentScores,
        List<Entity> entities,
        List<String> keyPhrases) {

    /** A single named entity Comprehend found, its type (e.g. PERSON, LOCATION), and confidence score (0-1). */
    public record Entity(String text, String type, float score) {
    }
}
