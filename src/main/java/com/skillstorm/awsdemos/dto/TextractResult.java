package com.skillstorm.awsdemos.dto;

import java.util.List;

/** Result of a Textract call: each detected line of text, plus all lines joined for display. */
public record TextractResult(List<String> lines, String fullText) {
}
