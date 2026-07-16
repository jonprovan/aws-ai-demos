package com.skillstorm.awsdemos.controller;

import com.skillstorm.awsdemos.exception.ErrorMessages;
import com.skillstorm.awsdemos.service.TextractService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/** Handles the Textract demo page: shows the upload form and runs the extract-text request. */
@Controller
public class TextractController {

    private final TextractService textractService;

    public TextractController(TextractService textractService) {
        this.textractService = textractService;
    }

    /** Renders the empty upload form. */
    @GetMapping("/textract")
    String form() {
        return "textract";
    }

    /** Validates a file was chosen, runs it through Textract, and puts the result (or error) back on the page. */
    @PostMapping("/textract")
    String analyze(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please choose a file to upload");
            return "textract";
        }
        try {
            model.addAttribute("result", textractService.extractText(file));
        } catch (Exception e) {
            model.addAttribute("error", ErrorMessages.of(e));
        }
        return "textract";
    }
}
