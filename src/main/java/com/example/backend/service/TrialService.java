package com.example.backend.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.TrialUsage;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.repository.TrialUsageRepository;
import com.example.backend.security.SecurityUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TrialService {

    private final TrialUsageRepository trialUsageRepository;
    private final OpenAiService openAiService;
    private final ImageGenerationService imageGenerationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TrialService(TrialUsageRepository trialUsageRepository, OpenAiService openAiService,
                         ImageGenerationService imageGenerationService) {
        this.trialUsageRepository = trialUsageRepository;
        this.openAiService = openAiService;
        this.imageGenerationService = imageGenerationService;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> aiSuggestion(String likes, String dislikes) {
        String firebaseUid = SecurityUtil.getFirebaseUid();
        if (firebaseUid == null || firebaseUid.isBlank()) {
            throw new UnauthorizedException("認証情報がありません");
        }

        TrialUsage trialUsage = trialUsageRepository.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> new TrialUsage(firebaseUid));

        if (trialUsage.isLimitExceeded()) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("error", "トライアルの利用回数が上限（" + TrialUsage.TRIAL_LIMIT + "回）に達しました。アカウント登録してご利用ください。");
            body.put("usage_count", trialUsage.getUsageCount());
            body.put("limit", TrialUsage.TRIAL_LIMIT);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
        }

        if (likes == null || likes.isBlank()) {
            throw new BadRequestException("好きなものを入力してください");
        }
        if (dislikes == null || dislikes.isBlank()) {
            throw new BadRequestException("嫌いなものを入力してください");
        }

        String prompt = buildAiPrompt(likes, dislikes);
        String aiResult = openAiService.chat("あなたはプロの料理人です。料理のレシピを正確にJSON形式で返してください。", prompt);

        JsonNode parsed;
        try {
            parsed = objectMapper.readTree(aiResult);
        } catch (Exception e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("error", "レシピの生成に失敗しました");
            body.put("message", "AIからの応答が正しい形式ではありません");
            body.put("response", aiResult != null ? aiResult.substring(0, Math.min(200, aiResult.length())) : null);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
        }

        boolean isNew = trialUsage.getId() == null;
        if (isNew) {
            trialUsage = trialUsageRepository.save(trialUsage);
        }
        trialUsage.increment();
        trialUsage = trialUsageRepository.save(trialUsage);

        String imageUrl = generateDishImage(parsed);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sample", parsed);
        body.put("image_url", imageUrl);
        body.put("usage_count", trialUsage.getUsageCount());
        body.put("limit", TrialUsage.TRIAL_LIMIT);
        return ResponseEntity.ok(body);
    }

    private String generateDishImage(JsonNode parsed) {
        String prompt = buildImagePrompt(parsed);
        return imageGenerationService.generateImage(prompt);
    }

    private String buildImagePrompt(JsonNode parsed) {
        String title = parsed.path("title").asText("");
        StringBuilder ingredients = new StringBuilder();
        JsonNode ingredientsNode = parsed.path("ingredients");
        if (ingredientsNode.isArray()) {
            for (int i = 0; i < ingredientsNode.size(); i++) {
                if (i > 0) {
                    ingredients.append(", ");
                }
                ingredients.append(ingredientsNode.get(i).asText());
            }
        }
        return ("A professional food photograph of " + title + ". "
                + "Made with " + ingredients + ". "
                + "Beautifully plated on a white ceramic dish, "
                + "soft warm natural lighting, shallow depth of field, "
                + "food magazine style, photorealistic, 4K, appetizing, delicious.").trim();
    }

    private String buildAiPrompt(String likes, String dislikes) {
        return """
                好きなものと嫌いなものを元に最適な献立案を1つJSONで返してください。
                出力は必ず純粋なJSONのみを返してください。コードブロックや追加説明は一切含めないでください。
                ▼好きなもの
                %s
                ▼嫌いなもの
                %s

                ▼返す形式（厳守）
                {
                  "title": "string",
                  "reason": "具体的な理由（例：「〇〇さんが好きなので」など、好みに基づいた具体的な理由。）",
                  "time": この料理の調理時間（分単位の整数。材料と調理方法から適切に推定してください）,
                  "budget": この料理の予算（円単位の整数。材料から適切に推定してください）,
                  "ingredients": ["材料1", "材料2"]
                }
                """.formatted(likes, dislikes);
    }
}
