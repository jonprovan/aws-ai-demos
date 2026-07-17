package com.skillstorm.awsdemos.controller;

import com.skillstorm.awsdemos.dto.ChatModel;
import com.skillstorm.awsdemos.dto.ChatRequest;
import com.skillstorm.awsdemos.exception.ErrorMessages;
import com.skillstorm.awsdemos.service.BedrockChatService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/** Serves the Bedrock chat page and the two JSON endpoints its JavaScript calls (model list, chat turn). */
@Controller
public class BedrockChatController {

    private final BedrockChatService bedrockChatService;

    public BedrockChatController(BedrockChatService bedrockChatService) {
        this.bedrockChatService = bedrockChatService;
    }

    /** Renders the chat page shell; the model list and conversation are loaded client-side via JS. */
    @GetMapping("/bedrock")
    String page() {
        return "bedrock";
    }

    /** Returns the models available for the dropdown. */
    @GetMapping("/bedrock/api/models")
    @ResponseBody
    List<ChatModel> models() {
        return bedrockChatService.listModels();
    }

    /** Runs one chat turn: sends the selected model and full history, returns the reply (or an error message). */
    @PostMapping("/bedrock/api/chat")
    @ResponseBody
    Map<String, String> chat(@RequestBody ChatRequest request) {
        try {
            return Map.of("content", bedrockChatService.sendMessage(request.model(), request.messages()));
        } catch (Exception e) {
            return Map.of("error", ErrorMessages.of(e));
        }
    }
}
