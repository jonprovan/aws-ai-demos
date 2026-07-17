package com.skillstorm.awsdemos.dto;

import java.util.List;

/** Body of a POST to /bedrock/api/chat: the model to use plus the full conversation so far. */
public record ChatRequest(String model, List<ChatMessage> messages) {
}
