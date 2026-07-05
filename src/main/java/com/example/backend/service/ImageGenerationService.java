package com.example.backend.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * fal.ai (flux/schnell) を使った画像生成 (Ruby側の generate_dish_image 相当)。
 */
@Service
public class ImageGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ImageGenerationService.class);
    private static final String FAL_URL = "https://fal.run/fal-ai/flux/schnell";

    private final RestTemplate restTemplate;

    @Value("${app.fal.api-key:}")
    private String falKey;

    public ImageGenerationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateImage(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Key " + falKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of("prompt", prompt), headers);
            JsonNode response = restTemplate.postForObject(FAL_URL, entity, JsonNode.class);
            if (response == null) {
                return null;
            }
            JsonNode urlNode = response.path("images").path(0).path("url");
            return urlNode.isMissingNode() ? null : urlNode.asText();
        } catch (Exception e) {
            log.error("[ImageGenerationService] Image generation error: {}", e.getMessage());
            return null;
        }
    }
}
