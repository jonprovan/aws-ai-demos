package com.skillstorm.awsdemos.dto;

import java.util.List;
import java.util.Map;

public record ComprehendResult(
        String languageCode,
        String sentiment,
        Map<String, Float> sentimentScores,
        List<Entity> entities,
        List<String> keyPhrases) {

    public record Entity(String text, String type, float score) {
    }
}
