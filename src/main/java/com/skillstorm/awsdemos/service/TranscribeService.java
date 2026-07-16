package com.skillstorm.awsdemos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillstorm.awsdemos.dto.TranscribeResult;
import com.skillstorm.awsdemos.exception.AwsDemoException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.DeleteTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.GetTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.Media;
import software.amazon.awssdk.services.transcribe.model.MediaFormat;
import software.amazon.awssdk.services.transcribe.model.StartTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJob;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJobStatus;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/** Runs an uploaded audio/video file through a real Transcribe job and returns the resulting transcript. */
@Service
public class TranscribeService {

    private static final Duration POLL_INTERVAL = Duration.ofSeconds(3);
    private static final Duration MAX_WAIT = Duration.ofMinutes(5);

    private final TranscribeClient transcribeClient;
    private final S3StorageService s3StorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TranscribeService(TranscribeClient transcribeClient, S3StorageService s3StorageService) {
        this.transcribeClient = transcribeClient;
        this.s3StorageService = s3StorageService;
    }

    /**
     * Uploads the file to S3 (Transcribe requires its input there), starts an async transcription
     * job with language auto-detection, blocks polling until it finishes or times out, reads the
     * transcript JSON Transcribe wrote back to the same bucket, and parses out the transcript text.
     * Cleans up the input/output S3 objects and the Transcribe job itself in all cases.
     */
    public TranscribeResult transcribe(MultipartFile file) {
        String jobName = "demo-" + UUID.randomUUID();
        String extension = extensionOf(file.getOriginalFilename());
        String inputKey = "transcribe/" + jobName + "." + extension;
        String outputKey = jobName + ".json";
        boolean jobStarted = false;

        try {
            s3StorageService.upload(inputKey, file.getBytes(), file.getContentType());

            transcribeClient.startTranscriptionJob(StartTranscriptionJobRequest.builder()
                    .transcriptionJobName(jobName)
                    .media(Media.builder()
                            .mediaFileUri("s3://" + s3StorageService.bucketName() + "/" + inputKey)
                            .build())
                    .mediaFormat(mediaFormatFor(extension))
                    .identifyLanguage(true)
                    .outputBucketName(s3StorageService.bucketName())
                    .build());
            jobStarted = true;

            TranscriptionJob job = pollUntilDone(jobName);
            if (job.transcriptionJobStatus() == TranscriptionJobStatus.FAILED) {
                throw new AwsDemoException("Transcription job failed: " + job.failureReason());
            }

            return new TranscribeResult(readTranscriptText(outputKey));
        } catch (IOException e) {
            throw new AwsDemoException("Could not read the uploaded file", e);
        } finally {
            s3StorageService.delete(inputKey);
            if (jobStarted) {
                s3StorageService.delete(outputKey);
                try {
                    transcribeClient.deleteTranscriptionJob(
                            DeleteTranscriptionJobRequest.builder().transcriptionJobName(jobName).build());
                } catch (RuntimeException ignored) {
                    // best-effort cleanup; job may already be gone
                }
            }
        }
    }

    /** Polls GetTranscriptionJob every POLL_INTERVAL until status is COMPLETED/FAILED, or throws past MAX_WAIT. */
    private TranscriptionJob pollUntilDone(String jobName) {
        Instant deadline = Instant.now().plus(MAX_WAIT);
        while (true) {
            TranscriptionJob job = transcribeClient
                    .getTranscriptionJob(GetTranscriptionJobRequest.builder().transcriptionJobName(jobName).build())
                    .transcriptionJob();

            if (job.transcriptionJobStatus() == TranscriptionJobStatus.COMPLETED
                    || job.transcriptionJobStatus() == TranscriptionJobStatus.FAILED) {
                return job;
            }
            if (Instant.now().isAfter(deadline)) {
                throw new AwsDemoException("Transcription job timed out after " + MAX_WAIT.toMinutes() + " minutes");
            }
            try {
                Thread.sleep(POLL_INTERVAL.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AwsDemoException("Interrupted while waiting for the transcription job", e);
            }
        }
    }

    /** Downloads the completed job's transcript JSON from S3 and pulls out the plain-text transcript field. */
    private String readTranscriptText(String outputKey) {
        try {
            JsonNode root = objectMapper.readTree(s3StorageService.download(outputKey));
            return root.path("results").path("transcripts").get(0).path("transcript").asText();
        } catch (IOException e) {
            throw new AwsDemoException("Could not parse transcript output", e);
        }
    }

    /** Pulls the lowercase file extension off a filename, or throws if there isn't one. */
    private String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new AwsDemoException("Uploaded file has no recognizable extension");
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    /** Maps a file extension to the Transcribe MediaFormat enum, or throws if it's not one we support. */
    private MediaFormat mediaFormatFor(String extension) {
        return switch (extension) {
            case "mp3" -> MediaFormat.MP3;
            case "mp4" -> MediaFormat.MP4;
            case "wav" -> MediaFormat.WAV;
            case "flac" -> MediaFormat.FLAC;
            case "ogg" -> MediaFormat.OGG;
            case "amr" -> MediaFormat.AMR;
            case "webm" -> MediaFormat.WEBM;
            default -> throw new AwsDemoException("Unsupported audio/video format: ." + extension);
        };
    }
}
