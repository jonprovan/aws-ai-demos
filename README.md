# aws-ai-demos

SME Sessions — a Spring Boot app demonstrating AWS AI/ML services: **Textract**, **Polly**, **Transcribe**,
**Rekognition**, **Comprehend**, a **Bedrock** model chat page, and a **Bedrock Agent** chat page. Each
service has its own page; the app calls the AWS API directly and renders the result.

## Prerequisites

- Java 21
- Maven (or use the included `mvnw`/`mvnw.cmd` wrapper)
- AWS credentials available via the default credential chain (env vars, `~/.aws/credentials`, or an IAM role) —
  nothing is hardcoded in the app. This covers Textract, Polly, Transcribe, Rekognition, and Comprehend.
- An S3 bucket the credentials can read/write/delete objects in, used as scratch space for Textract and Transcribe
  (both require input to be read from S3). Configured via `aws.s3.bucket-name` in
  [application.yml](src/main/resources/application.yml) — currently `jon-aws-ai-demos`.
- For the Bedrock chat page: copy [.env.example](.env.example) to `.env` and fill in `BEDROCK_BASE_URL` and
  `BEDROCK_API_KEY` (Bedrock's OpenAI-compatible endpoint + Bearer API key). `.env` is gitignored and loaded
  automatically at startup via `spring-dotenv` — never commit real values.
- For the custom Bedrock Agent page: also fill in `BEDROCK_AGENT_ID` and `BEDROCK_AGENT_ALIAS_ID` in `.env`
  (from the Bedrock Agents console). This path uses the same AWS SigV4 credentials as Textract/Polly/etc., not
  the Bearer API key above.

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
| Bedrock Chat | `/bedrock`     | Typed messages, live conversation, choice of model |
| Bedrock Agent | `/bedrock/agent` | Typed messages, live conversation with a custom Bedrock Agent |

Transcribe runs a real, asynchronous transcription job and polls for completion (up to 5 minutes) before
responding, so that request can take a while — this is a deliberate simplification for demo purposes, not how
you'd want this to work under real concurrent load.

Bedrock Chat talks to Bedrock's OpenAI-compatible REST endpoint directly (Bearer token auth), not the AWS SDK —
a separate integration from the other five pages. The model dropdown is populated live from that endpoint's
`/models` list (falling back to a small hardcoded list if that call fails). The conversation history lives only
in the browser tab (a plain JS array resent in full each turn) — there's no server-side session.

The `/models` endpoint has no field indicating which models actually support `/chat/completions` — some (all
tested Anthropic Claude and OpenAI GPT-5.x models) reject every request with "does not support the
'/v1/chat/completions' API". `BedrockChatService.UNSUPPORTED_FOR_CHAT` hardcodes the list found by directly
probing every available model; it's manual, observed metadata, not derived from anything the API exposes, so
it may need updating if the account's available models change.

Bedrock Agent (`/bedrock/agent`) is reached only via a button on the Bedrock Chat page — it is deliberately
not in the main nav or the model dropdown, since it's a distinct custom agent rather than another selectable
model. Unlike the model-chat page, it's a **stateful** integration: Amazon Bedrock Agents track conversation
memory server-side keyed by a `sessionId`, so the browser generates one id per page load and sends only the
latest message each turn (see `static/js/bedrock-agent-chat.js`), not the full history. It's invoked via the
AWS SDK's `BedrockAgentRuntimeAsyncClient.invokeAgent` (an event-stream API, async-only in the SDK — there is
no synchronous client method for it), using the same SigV4 credentials as the other five AWS-SDK pages.

## IAM Permissions Needed

At minimum, the credentials used need:

- `textract:DetectDocumentText`
- `polly:SynthesizeSpeech`
- `transcribe:StartTranscriptionJob`, `transcribe:GetTranscriptionJob`, `transcribe:DeleteTranscriptionJob`
- `rekognition:DetectLabels`, `rekognition:DetectText`
- `comprehend:DetectDominantLanguage`, `comprehend:DetectSentiment`, `comprehend:DetectEntities`,
  `comprehend:DetectKeyPhrases`
- `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject` scoped to the demo bucket
- `bedrock:InvokeAgent` scoped to the agent/alias ARN, for the Bedrock Agent page

## Notes

- Uploaded files are written to S3 only as scratch input for Textract/Transcribe and are deleted again once the
  AWS call completes.
- All AWS calls run synchronously on the request thread — fine for a single-user demo, not production-ready for
  concurrent traffic.
