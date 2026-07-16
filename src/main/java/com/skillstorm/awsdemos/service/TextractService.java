package com.skillstorm.awsdemos.service;

import com.skillstorm.awsdemos.dto.TextractResult;
import com.skillstorm.awsdemos.exception.AwsDemoException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.S3Object;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class TextractService {

    private final TextractClient textractClient;
    private final S3StorageService s3StorageService;

    public TextractService(TextractClient textractClient, S3StorageService s3StorageService) {
        this.textractClient = textractClient;
        this.s3StorageService = s3StorageService;
    }

    public TextractResult extractText(MultipartFile file) {
        String key = "textract/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        try {
            s3StorageService.upload(key, file.getBytes(), file.getContentType());

            DetectDocumentTextResponse response = textractClient.detectDocumentText(
                    DetectDocumentTextRequest.builder()
                            .document(Document.builder()
                                    .s3Object(S3Object.builder()
                                            .bucket(s3StorageService.bucketName())
                                            .name(key)
                                            .build())
                                    .build())
                            .build());

            List<String> lines = response.blocks().stream()
                    .filter(block -> block.blockType() == BlockType.LINE)
                    .map(Block::text)
                    .toList();

            return new TextractResult(lines, String.join("\n", lines));
        } catch (IOException e) {
            throw new AwsDemoException("Could not read the uploaded file", e);
        } finally {
            s3StorageService.delete(key);
        }
    }
}
