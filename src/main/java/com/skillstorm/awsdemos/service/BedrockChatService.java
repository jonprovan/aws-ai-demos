package com.skillstorm.awsdemos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillstorm.awsdemos.dto.ChatMessage;
import com.skillstorm.awsdemos.dto.ChatModel;
import com.skillstorm.awsdemos.exception.AwsDemoException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Talks to Bedrock's OpenAI-compatible REST endpoint: listing models and running chat completions. */
@Service
public class BedrockChatService {

    /** Vendor id prefix -> a properly capitalized display name, since the API only gives us the raw id. */
    private static final Map<String, String> VENDOR_NAMES = Map.ofEntries(
            Map.entry("anthropic", "Anthropic"),
            Map.entry("openai", "OpenAI"),
            Map.entry("google", "Google"),
            Map.entry("meta", "Meta"),
            Map.entry("amazon", "Amazon"),
            Map.entry("mistral", "Mistral AI"),
            Map.entry("qwen", "Qwen"),
            Map.entry("deepseek", "DeepSeek"),
            Map.entry("moonshotai", "Moonshot AI"),
            Map.entry("minimax", "MiniMax"),
            Map.entry("nvidia", "NVIDIA"),
            Map.entry("xai", "xAI"),
            Map.entry("zai", "Z.ai"),
            Map.entry("writer", "Writer"));

    /** Tokens that should render fully uppercase rather than title-cased (e.g. "oss" -> "OSS", not "Oss"). */
    private static final Set<String> KNOWN_ACRONYMS = Set.of("gpt", "oss", "glm", "llm", "vl", "it");

    /** Matches version-style tokens like "4" and "5" that should be rejoined as "4.5" rather than "4 5". */
    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d+");

    /**
     * Models confirmed (by directly probing /chat/completions for each available model) to reject
     * every request with "does not support the '/v1/chat/completions' API" — the API exposes no
     * metadata for this, so there's no way to detect it other than hardcoding what was observed.
     * Re-check this list if the account's available models change.
     */
    private static final Set<String> UNSUPPORTED_FOR_CHAT = Set.of(
            "anthropic.claude-haiku-4-5",
            "anthropic.claude-opus-4-7",
            "anthropic.claude-opus-4-8",
            "anthropic.claude-sonnet-5",
            "openai.gpt-5.4",
            "openai.gpt-5.4-2026-03-05",
            "openai.gpt-5.5",
            "openai.gpt-5.5-2026-04-23",
            "openai.gpt-5.6-luna",
            "openai.gpt-5.6-sol",
            "openai.gpt-5.6-terra");

    /** Used only if the /models call fails, so the chat page still works. All four confirmed to support chat/completions. */
    private static final List<ChatModel> FALLBACK_MODELS = List.of(
            toChatModel("openai.gpt-oss-20b"),
            toChatModel("google.gemma-3-4b-it"),
            toChatModel("qwen.qwen3-32b"),
            toChatModel("deepseek.v3.2"));

    private final RestClient bedrockRestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BedrockChatService(RestClient bedrockRestClient) {
        this.bedrockRestClient = bedrockRestClient;
    }

    /**
     * Calls the /models endpoint, keeps only models marked "available" for this account and known to
     * support the chat/completions API (see UNSUPPORTED_FOR_CHAT), and returns them alphabetized by
     * display name. Falls back to a small hardcoded list if the call fails or returns nothing usable,
     * so the dropdown is never empty.
     */
    public List<ChatModel> listModels() {
        List<ChatModel> models;
        try {
            JsonNode root = bedrockRestClient.get()
                    .uri("/models")
                    .retrieve()
                    .body(JsonNode.class);

            models = new ArrayList<>();
            for (JsonNode entry : root.path("data")) {
                String id = entry.path("id").asText(null);
                String status = entry.path("status").asText("available");
                if (id != null && "available".equals(status) && !UNSUPPORTED_FOR_CHAT.contains(id)) {
                    models.add(toChatModel(id));
                }
            }
            if (models.isEmpty()) {
                models = FALLBACK_MODELS;
            }
        } catch (Exception e) {
            models = FALLBACK_MODELS;
        }
        return models.stream()
                .sorted(Comparator.comparing(ChatModel::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Sends the chosen model plus the full conversation history to /chat/completions and returns just
     * the assistant's reply text out of the first choice.
     */
    public String sendMessage(String model, List<ChatMessage> messages) {
        try {
            JsonNode root = bedrockRestClient.post()
                    .uri("/chat/completions")
                    .body(Map.of("model", model, "messages", messages))
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new AwsDemoException("Bedrock returned a response with no message content");
            }
            return content.asText();
        } catch (RestClientResponseException e) {
            throw new AwsDemoException(extractErrorMessage(e), e);
        }
    }

    /** Pulls the human-readable message out of Bedrock's OpenAI-style {"error": {"message": ...}} body. */
    private String extractErrorMessage(RestClientResponseException e) {
        try {
            JsonNode root = objectMapper.readTree(e.getResponseBodyAsByteArray());
            String message = root.path("error").path("message").asText(null);
            return message != null ? message : e.getMessage();
        } catch (Exception parseFailure) {
            return e.getMessage();
        }
    }

    private static ChatModel toChatModel(String id) {
        return new ChatModel(id, displayName(id));
    }

    /**
     * Best-effort readable label for a raw model id like "openai.gpt-oss-20b" -> "OpenAI GPT OSS 20B".
     * The API has no display-name field, so this is derived purely from the id: the part before the
     * first "." is treated as the vendor, everything after is split on "-" into words, adjacent
     * number-only words are rejoined with "." (so "4-5" reads as "4.5"), known acronyms are fully
     * uppercased, size suffixes like "20b" get their trailing letter uppercased, and everything else
     * is title-cased.
     */
    private static String displayName(String id) {
        int dot = id.indexOf('.');
        if (dot < 0) {
            return id;
        }
        String vendorPrefix = id.substring(0, dot);
        String slug = id.substring(dot + 1);
        String vendorName = VENDOR_NAMES.getOrDefault(vendorPrefix, capitalize(vendorPrefix));

        String[] rawWords = slug.split("-");
        List<String> words = new ArrayList<>();
        for (int i = 0; i < rawWords.length; i++) {
            if (!DIGITS_ONLY.matcher(rawWords[i]).matches()) {
                words.add(rawWords[i]);
                continue;
            }
            int runEnd = i;
            while (runEnd < rawWords.length && DIGITS_ONLY.matcher(rawWords[runEnd]).matches()) {
                runEnd++;
            }
            // Exactly two adjacent numbers (e.g. "4-5") read as a version like "4.5"; three or more
            // (e.g. a "2026-03-05" date) are left hyphenated as originally written.
            if (runEnd - i == 2) {
                words.add(rawWords[i] + "." + rawWords[i + 1]);
            } else {
                words.add(String.join("-", Arrays.copyOfRange(rawWords, i, runEnd)));
            }
            i = runEnd - 1;
        }

        StringBuilder name = new StringBuilder(vendorName);
        for (String word : words) {
            name.append(' ').append(formatWord(word));
        }
        return name.toString();
    }

    private static String formatWord(String word) {
        if (KNOWN_ACRONYMS.contains(word.toLowerCase(Locale.ROOT))) {
            return word.toUpperCase(Locale.ROOT);
        }
        String titled = capitalize(word);
        int len = titled.length();
        if (len >= 2 && Character.isLetter(titled.charAt(len - 1)) && Character.isDigit(titled.charAt(len - 2))) {
            return titled.substring(0, len - 1) + Character.toUpperCase(titled.charAt(len - 1));
        }
        return titled;
    }

    private static String capitalize(String word) {
        if (word.isEmpty()) {
            return word;
        }
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase(Locale.ROOT);
    }
}
