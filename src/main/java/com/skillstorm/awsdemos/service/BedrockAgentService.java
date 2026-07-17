package com.skillstorm.awsdemos.service;

import com.skillstorm.awsdemos.config.BedrockProperties;
import com.skillstorm.awsdemos.exception.AwsDemoException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeAgentRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeAgentResponseHandler;

import java.util.concurrent.CompletionException;

/** Talks to the custom Amazon Bedrock Agent via the InvokeAgent event-stream API. */
@Service
public class BedrockAgentService {

    private final BedrockAgentRuntimeAsyncClient bedrockAgentRuntimeAsyncClient;
    private final BedrockProperties properties;

    public BedrockAgentService(BedrockAgentRuntimeAsyncClient bedrockAgentRuntimeAsyncClient, BedrockProperties properties) {
        this.bedrockAgentRuntimeAsyncClient = bedrockAgentRuntimeAsyncClient;
        this.properties = properties;
    }

    /**
     * Sends one message under the given session id and blocks until the agent's full reply has
     * streamed back. The agent tracks conversation memory server-side keyed by sessionId, so only the
     * latest message is sent here, not the full history (unlike the model-chat page).
     */
    public String sendMessage(String sessionId, String message) {
        StringBuilder reply = new StringBuilder();

        InvokeAgentRequest request = InvokeAgentRequest.builder()
                .agentId(properties.agent().id())
                .agentAliasId(properties.agent().aliasId())
                .sessionId(sessionId)
                .inputText(message)
                .build();

        InvokeAgentResponseHandler handler = InvokeAgentResponseHandler.builder()
                .subscriber(InvokeAgentResponseHandler.Visitor.builder()
                        .onChunk(chunk -> reply.append(chunk.bytes().asUtf8String()))
                        .build())
                .build();

        try {
            bedrockAgentRuntimeAsyncClient.invokeAgent(request, handler).join();
        } catch (CompletionException e) {
            // Unwrap so ErrorMessages.of() can recognize the real AWS exception type underneath.
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw new AwsDemoException("Agent invocation failed", e.getCause());
        }

        if (reply.isEmpty()) {
            throw new AwsDemoException("The agent returned an empty response");
        }
        return reply.toString();
    }
}
