# aws-ai-demos

SME Sessions — a Spring Boot app demonstrating five AWS AI/ML services: **Textract**, **Polly**, **Transcribe**,
**Rekognition**, and **Comprehend**. Each service has its own upload page; the app calls the AWS API directly and
renders the result.

## Prerequisites

- Java 21
- Maven (or use the included `mvnw`/`mvnw.cmd` wrapper)
- AWS credentials available via the default credential chain (env vars, `~/.aws/credentials`, or an IAM role) —
  nothing is hardcoded in the app
- An S3 bucket the credentials can read/write/delete objects in, used as scratch space for Textract and Transcribe
  (both require input to be read from S3). Configured via `aws.s3.bucket-name` in
  [application.yml](src/main/resources/application.yml) — currently `jon-aws-ai-demos`.

## Running

```
mvn spring-boot:run
```

Then open http://localhost:8080.

## Pages

| Service     | Path            | Input                          |
|-------------|-----------------|---------------------------------|
| Textract    | `/textract`     | Image or PDF                    |
| Polly       | `/polly`        | Typed text                      |
| Transcribe  | `/transcribe`   | Audio/video (mp3, mp4, wav, flac, ogg, amr, webm) |
| Rekognition | `/rekognition`  | Image (png/jpg)                 |
| Comprehend  | `/comprehend`   | Typed text or a `.txt` file (≤5,000 bytes) |

Transcribe runs a real, asynchronous transcription job and polls for completion (up to 5 minutes) before
responding, so that request can take a while — this is a deliberate simplification for demo purposes, not how
you'd want this to work under real concurrent load.

## IAM Permissions Needed

At minimum, the credentials used need:

- `textract:DetectDocumentText`
- `polly:SynthesizeSpeech`
- `transcribe:StartTranscriptionJob`, `transcribe:GetTranscriptionJob`, `transcribe:DeleteTranscriptionJob`
- `rekognition:DetectLabels`, `rekognition:DetectText`
- `comprehend:DetectDominantLanguage`, `comprehend:DetectSentiment`, `comprehend:DetectEntities`,
  `comprehend:DetectKeyPhrases`
- `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject` scoped to the demo bucket

## Notes

- Uploaded files are written to S3 only as scratch input for Textract/Transcribe and are deleted again once the
  AWS call completes.
- All AWS calls run synchronously on the request thread — fine for a single-user demo, not production-ready for
  concurrent traffic.
