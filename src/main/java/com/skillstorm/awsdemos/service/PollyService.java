package com.skillstorm.awsdemos.service;

import com.skillstorm.awsdemos.exception.AwsDemoException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.VoiceId;

import java.io.IOException;
import java.io.InputStream;

@Service
public class PollyService {

    private final PollyClient pollyClient;

    public PollyService(PollyClient pollyClient) {
        this.pollyClient = pollyClient;
    }

    public byte[] synthesize(String text) {
        if (text == null || text.isBlank()) {
            throw new AwsDemoException("Please provide some text to synthesize");
        }

        try (InputStream audio = pollyClient.synthesizeSpeech(SynthesizeSpeechRequest.builder()
                .text(text)
                .voiceId(VoiceId.JOANNA)
                .outputFormat(OutputFormat.MP3)
                .build())) {
            return audio.readAllBytes();
        } catch (IOException e) {
            throw new AwsDemoException("Could not read synthesized audio from Polly", e);
        }
    }
}
