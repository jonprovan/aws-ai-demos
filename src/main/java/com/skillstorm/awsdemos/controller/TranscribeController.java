package com.skillstorm.awsdemos.controller;

import com.skillstorm.awsdemos.exception.ErrorMessages;
import com.skillstorm.awsdemos.service.TranscribeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class TranscribeController {

    private final TranscribeService transcribeService;

    public TranscribeController(TranscribeService transcribeService) {
        this.transcribeService = transcribeService;
    }

    @GetMapping("/transcribe")
    public String form() {
        return "transcribe";
    }

    @PostMapping("/transcribe")
    public String transcribe(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please choose an audio or video file to upload");
            return "transcribe";
        }
        try {
            model.addAttribute("result", transcribeService.transcribe(file));
        } catch (Exception e) {
            model.addAttribute("error", ErrorMessages.of(e));
        }
        return "transcribe";
    }
}
