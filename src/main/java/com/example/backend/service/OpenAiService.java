package com.example.backend.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * OpenAI Chat Completions API 呼び出し (Ruby の ruby-openai gem 相当)。
 */
@Service
public class OpenAiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);
    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    private final RestTemplate restTemplate;

    @Value("${app.openai.api-key:}")
    private String apiKey;

    public OpenAiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** system + user のシンプルなチャット補完 */
    public String chat(String systemPrompt, String userPrompt) {
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );
        return complete(messages, null);
    }

    /** 画像URL付きのVision問い合わせ (SuggestionGenerateJob#image_matches_dish? 相当) */
    public String chatWithImage(String imageUrl, String text, int maxTokens) {
        List<Map<String, Object>> content = List.of(
                Map.of("type", "image_url", "image_url", Map.of("url", imageUrl)),
                Map.of("type", "text", "text", text)
        );
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "user", "content", content)
        );
        return complete(messages, maxTokens);
    }

    private String complete(List<Map<String, Object>> messages, Integer maxTokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = maxTokens != null
                ? Map.of("model", MODEL, "messages", messages, "max_tokens", maxTokens)
                : Map.of("model", MODEL, "messages", messages, "temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        JsonNode response = restTemplate.postForObject(CHAT_COMPLETIONS_URL, entity, JsonNode.class);

        if (response == null) {
            return null;
        }
        JsonNode content = response.path("choices").path(0).path("message").path("content");
        return content.isMissingNode() ? null : content.asText();
    }
}
