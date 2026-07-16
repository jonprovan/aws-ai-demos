package com.skillstorm.awsdemos.controller;

import com.skillstorm.awsdemos.exception.ErrorMessages;
import com.skillstorm.awsdemos.service.ComprehendService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Handles the Comprehend demo page: accepts typed text or a .txt upload and runs the text-analysis request. */
@Controller
public class ComprehendController {

    private final ComprehendService comprehendService;

    public ComprehendController(ComprehendService comprehendService) {
        this.comprehendService = comprehendService;
    }

    /** Renders the empty text-entry form. */
    @GetMapping("/comprehend")
    String form() {
        return "comprehend";
    }

    /** Resolves whichever input was submitted (file wins over typed text), runs Comprehend, and shows the result. */
    @PostMapping("/comprehend")
    String analyze(
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Model model) {
        try {
            String content = resolveText(text, file);
            model.addAttribute("submittedText", content);
            model.addAttribute("result", comprehendService.analyzeText(content));
        } catch (Exception e) {
            model.addAttribute("error", ErrorMessages.of(e));
        }
        return "comprehend";
    }

    /** Prefers the uploaded .txt file's contents when present, otherwise falls back to the typed text field. */
    private String resolveText(String text, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
        return text;
    }
}
