package com.skillstorm.awsdemos.dto;

/** One turn in a Bedrock chat conversation: role is "user" or "assistant". */
public record ChatMessage(String role, String content) {
}
