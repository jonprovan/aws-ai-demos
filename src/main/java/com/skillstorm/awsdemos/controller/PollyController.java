package com.skillstorm.awsdemos.controller;

import com.skillstorm.awsdemos.exception.ErrorMessages;
import com.skillstorm.awsdemos.service.AudioCache;
import com.skillstorm.awsdemos.service.PollyService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PollyController {

    private final PollyService pollyService;
    private final AudioCache audioCache;

    public PollyController(PollyService pollyService, AudioCache audioCache) {
        this.pollyService = pollyService;
        this.audioCache = audioCache;
    }

    @GetMapping("/polly")
    public String form() {
        return "polly";
    }

    @PostMapping("/polly")
    public String synthesize(@RequestParam("text") String text, Model model) {
        try {
            byte[] audio = pollyService.synthesize(text);
            model.addAttribute("audioId", audioCache.store(audio));
        } catch (Exception e) {
            model.addAttribute("error", ErrorMessages.of(e));
        }
        return "polly";
    }

    @GetMapping("/polly/audio/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> audio(@PathVariable String id) {
        byte[] audio = audioCache.get(id);
        if (audio == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"speech.mp3\"")
                .body(audio);
    }
}
