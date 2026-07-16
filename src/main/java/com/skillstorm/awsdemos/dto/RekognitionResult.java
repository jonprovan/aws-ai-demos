package com.skillstorm.awsdemos.dto;

import java.util.List;

public record RekognitionResult(List<Label> labels, List<String> detectedText) {

    public record Label(String name, double confidence) {
    }
}
