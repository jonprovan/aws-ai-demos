package com.skillstorm.awsdemos.dto;

import java.util.List;

public record TextractResult(List<String> lines, String fullText) {
}
