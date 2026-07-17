package com.skillstorm.awsdemos.dto;

/** Body of a POST to /bedrock/agent/api/chat: the browser-generated session id plus the latest message. */
public record AgentChatRequest(String sessionId, String message) {
}
