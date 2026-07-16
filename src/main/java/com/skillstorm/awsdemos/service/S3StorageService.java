package com.skillstorm.awsdemos.service;

import com.skillstorm.awsdemos.config.AwsDemoProperties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/** Thin wrapper around the demo S3 bucket, used by Textract and Transcribe as scratch storage for uploads. */
@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucketName;

    /** Reads the configured bucket name out of application.yml (via AwsDemoProperties). */
    public S3StorageService(S3Client s3Client, AwsDemoProperties properties) {
        this.s3Client = s3Client;
        this.bucketName = properties.s3().bucketName();
    }

    /** Returns the configured bucket name, e.g. so callers can build an s3:// URI for it. */
    public String bucketName() {
        return bucketName;
    }

    /** Writes the given bytes to the bucket under key, overwriting anything already there. */
    public void upload(String key, byte[] content, String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(content));
    }

    /** Reads the full contents of the object at key back into memory. */
    public byte[] download(String key) {
        return s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build())
                .asByteArray();
    }

    /** Deletes the object at key; used to clean up scratch uploads once an AWS call finishes. */
    public void delete(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build());
    }
}
