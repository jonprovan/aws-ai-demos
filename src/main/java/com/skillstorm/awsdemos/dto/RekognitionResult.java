package com.skillstorm.awsdemos.dto;

import java.util.List;

/** Result of a Rekognition call: detected labels (with confidence) and any lines of text found in the image. */
public record RekognitionResult(List<Label> labels, List<String> detectedText) {

    /** A single detected label and how confident Rekognition is in it, as a percentage (0-100). */
    public record Label(String name, double confidence) {
    }
}
