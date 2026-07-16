package com.skillstorm.awsdemos.controller;

import com.skillstorm.awsdemos.exception.ErrorMessages;
import com.skillstorm.awsdemos.service.RekognitionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/** Handles the Rekognition demo page: shows the upload form and runs the image-analysis request. */
@Controller
public class RekognitionController {

    private final RekognitionService rekognitionService;

    public RekognitionController(RekognitionService rekognitionService) {
        this.rekognitionService = rekognitionService;
    }

    /** Renders the empty upload form. */
    @GetMapping("/rekognition")
    String form() {
        return "rekognition";
    }

    /** Validates a file was chosen, runs it through Rekognition, and puts the result (or error) back on the page. */
    @PostMapping("/rekognition")
    String analyze(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please choose an image to upload");
            return "rekognition";
        }
        try {
            model.addAttribute("result", rekognitionService.analyzeImage(file));
        } catch (Exception e) {
            model.addAttribute("error", ErrorMessages.of(e));
        }
        return "rekognition";
    }
}
