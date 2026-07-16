package com.skillstorm.awsdemos.dto;

/** Result of a Transcribe job: the plain-text transcript pulled from the completed job's output. */
public record TranscribeResult(String transcriptText) {
}
