package com.skillstorm.awsdemos.service;

import com.skillstorm.awsdemos.dto.RekognitionResult;
import com.skillstorm.awsdemos.exception.AwsDemoException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectTextRequest;
import software.amazon.awssdk.services.rekognition.model.Image;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Service
public class RekognitionService {

    private final RekognitionClient rekognitionClient;

    public RekognitionService(RekognitionClient rekognitionClient) {
        this.rekognitionClient = rekognitionClient;
    }

    public RekognitionResult analyzeImage(MultipartFile file) {
        Image image;
        try {
            image = Image.builder().bytes(SdkBytes.fromByteArray(file.getBytes())).build();
        } catch (IOException e) {
            throw new AwsDemoException("Could not read the uploaded image", e);
        }

        List<RekognitionResult.Label> labels = rekognitionClient
                .detectLabels(DetectLabelsRequest.builder().image(image).maxLabels(15).minConfidence(70F).build())
                .labels()
                .stream()
                .map(label -> new RekognitionResult.Label(label.name(), label.confidence()))
                .sorted(Comparator.comparingDouble(RekognitionResult.Label::confidence).reversed())
                .toList();

        List<String> detectedText = rekognitionClient
                .detectText(DetectTextRequest.builder().image(image).build())
                .textDetections()
                .stream()
                .filter(detection -> detection.typeAsString().equals("LINE"))
                .map(detection -> detection.detectedText())
                .toList();

        return new RekognitionResult(labels, detectedText);
    }
}
