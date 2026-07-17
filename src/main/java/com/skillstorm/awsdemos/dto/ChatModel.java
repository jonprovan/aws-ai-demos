package com.skillstorm.awsdemos.dto;

/** One selectable entry in the model dropdown on the Bedrock chat page: the real id plus a readable label. */
public record ChatModel(String id, String name) {
}
