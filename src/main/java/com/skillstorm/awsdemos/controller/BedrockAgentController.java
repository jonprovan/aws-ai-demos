package com.skillstorm.awsdemos.controller;

import com.skillstorm.awsdemos.dto.AgentChatRequest;
import com.skillstorm.awsdemos.exception.ErrorMessages;
import com.skillstorm.awsdemos.service.BedrockAgentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/** Serves the custom Bedrock Agent page and the JSON endpoint its JavaScript calls for each chat turn. */
@Controller
public class BedrockAgentController {

    private final BedrockAgentService bedrockAgentService;

    public BedrockAgentController(BedrockAgentService bedrockAgentService) {
        this.bedrockAgentService = bedrockAgentService;
    }

    /** Renders the agent chat page shell; the session id and conversation are handled client-side via JS. */
    @GetMapping("/bedrock/agent")
    String page() {
        return "bedrock-agent";
    }

    /** Runs one turn against the agent under the given session id and returns the reply (or an error message). */
    @PostMapping("/bedrock/agent/api/chat")
    @ResponseBody
    Map<String, String> chat(@RequestBody AgentChatRequest request) {
        try {
            return Map.of("content", bedrockAgentService.sendMessage(request.sessionId(), request.message()));
        } catch (Exception e) {
            return Map.of("error", ErrorMessages.of(e));
        }
    }
}
