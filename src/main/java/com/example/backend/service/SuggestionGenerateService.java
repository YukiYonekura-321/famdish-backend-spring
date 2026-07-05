package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.Member;
import com.example.backend.entity.Stock;
import com.example.backend.entity.Suggestion;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.StockRepository;
import com.example.backend.repository.SuggestionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Rails の SuggestionGenerateJob (ActiveJob/SolidQueue) 相当。
 * @Async でバックグラウンド実行する。
 */
@Service
public class SuggestionGenerateService {

    private static final Logger log = LoggerFactory.getLogger(SuggestionGenerateService.class);
    private static final int MAX_IMAGE_RETRY = 3;

    private final SuggestionRepository suggestionRepository;
    private final MemberRepository memberRepository;
    private final StockRepository stockRepository;
    private final OpenAiService openAiService;
    private final ImageGenerationService imageGenerationService;
    private final S3UploadService s3UploadService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SuggestionGenerateService(SuggestionRepository suggestionRepository,
                                      MemberRepository memberRepository,
                                      StockRepository stockRepository,
                                      OpenAiService openAiService,
                                      ImageGenerationService imageGenerationService,
                                      S3UploadService s3UploadService) {
        this.suggestionRepository = suggestionRepository;
        this.memberRepository = memberRepository;
        this.stockRepository = stockRepository;
        this.openAiService = openAiService;
        this.imageGenerationService = imageGenerationService;
        this.s3UploadService = s3UploadService;
    }

    @Async
    public void generate(Long suggestionId, Long familyId, String feedbackJson, JsonNode constraints, int days) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId).orElse(null);
        if (suggestion == null) {
            return;
        }

        try {
            suggestion.setStatus(Suggestion.PROCESSING);
            suggestionRepository.saveAndFlush(suggestion);

            String prompt = buildPrompt(familyId, feedbackJson, constraints, days);
            log.info("[SuggestionGenerateService] Prompt:\n{}", prompt);

            String aiResult = openAiService.chat("あなたは料理の献立提案AIです。", prompt);
            JsonNode parsed = objectMapper.readTree(aiResult);

            String representativeImageUrl;
            if (parsed.isArray()) {
                representativeImageUrl = null;
                for (JsonNode dayParsed : parsed) {
                    ObjectNode dayObj = (ObjectNode) dayParsed;
                    if (!"料理は作れません".equals(textOrNull(dayParsed, "title"))) {
                        String tmpImageUrl = generateDishImageWithRetry(dayParsed);
                        if (tmpImageUrl != null) {
                            String suffix = "_day" + (dayParsed.has("day") ? dayParsed.get("day").asText() : dayParsed.path("day_number").asText());
                            String s3Url = s3UploadService.upload("suggestions", suggestion.getId(), suffix, tmpImageUrl);
                            dayObj.put("image_url", s3Url);
                        } else {
                            dayObj.putNull("image_url");
                        }
                    } else {
                        dayObj.putNull("image_url");
                    }
                    if (representativeImageUrl == null && !dayObj.path("image_url").isNull()) {
                        representativeImageUrl = dayObj.path("image_url").asText(null);
                    }
                }
            } else {
                ObjectNode obj = (ObjectNode) parsed;
                if (!"料理は作れません".equals(textOrNull(parsed, "title"))) {
                    String tmpImageUrl = generateDishImageWithRetry(parsed);
                    if (tmpImageUrl != null) {
                        String s3Url = s3UploadService.upload("suggestions", suggestion.getId(), null, tmpImageUrl);
                        obj.put("image_url", s3Url);
                        representativeImageUrl = s3Url;
                    } else {
                        obj.putNull("image_url");
                        representativeImageUrl = null;
                    }
                } else {
                    obj.putNull("image_url");
                    representativeImageUrl = null;
                }
            }

            suggestion.setAiRawJson(objectMapper.writeValueAsString(parsed));
            suggestion.setStatus(Suggestion.COMPLETED);
            suggestion.setImageUrl(representativeImageUrl);
            suggestionRepository.save(suggestion);
        } catch (Exception e) {
            log.error("[SuggestionGenerateService] Failed: {}", e.getMessage(), e);
            suggestion.setStatus(Suggestion.FAILED);
            suggestionRepository.save(suggestion);
        }
    }

    private String generateDishImageWithRetry(JsonNode parsed) {
        String title = textOrNull(parsed, "title");
        String lastUrl = null;

        for (int attempt = 0; attempt < MAX_IMAGE_RETRY; attempt++) {
            String imageUrl = imageGenerationService.generateImage(buildImagePrompt(title));
            lastUrl = imageUrl;
            if (imageUrl == null || imageUrl.isBlank()) {
                continue;
            }
            if (imageMatchesDish(imageUrl, title)) {
                log.info("[SuggestionGenerateService] Image verified for '{}' on attempt {}", title, attempt + 1);
                return imageUrl;
            }
            log.warn("[SuggestionGenerateService] Image mismatch for '{}' on attempt {}, retrying...", title, attempt + 1);
        }
        log.warn("[SuggestionGenerateService] Max retries ({}) reached for '{}', using last generated image", MAX_IMAGE_RETRY, title);
        return lastUrl;
    }

    private boolean imageMatchesDish(String imageUrl, String title) {
        try {
            String question = "この画像は「" + title + "」という料理を撮影したものですか？料理の写真であり、かつそれが「"
                    + title + "」であれば「YES」、そうでなければ「NO」とだけ答えてください。";
            String answer = openAiService.chatWithImage(imageUrl, question, 10);
            String normalized = answer != null ? answer.strip().toUpperCase() : null;
            log.info("[SuggestionGenerateService] Image verification for '{}': {}", title, normalized);
            return normalized != null && normalized.contains("YES");
        } catch (Exception e) {
            log.error("[SuggestionGenerateService] Image verification error: {}", e.getMessage());
            return true;
        }
    }

    private String buildImagePrompt(String title) {
        return ("A professional food photograph of " + title + ". "
                + "Beautifully plated on a white ceramic dish, "
                + "soft warm natural lighting, shallow depth of field, "
                + "food magazine style, photorealistic, 4K, appetizing, delicious.").trim();
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    // ── プロンプト構築 ──

    private String buildPrompt(Long familyId, String feedbackJson, JsonNode constraints, int days) {
        List<Member> members = memberRepository.findByFamilyId(familyId);
        List<Stock> stocks = stockRepository.findByFamilyId(familyId);

        ArrayNode likes = objectMapper.createArrayNode();
        ArrayNode dislikes = objectMapper.createArrayNode();
        for (Member member : members) {
            ObjectNode likeEntry = objectMapper.createObjectNode();
            likeEntry.put("name", member.getName());
            ArrayNode likeNames = objectMapper.createArrayNode();
            member.getLikes().forEach(l -> likeNames.add(l.getName()));
            likeEntry.set("likes", likeNames);
            likes.add(likeEntry);

            ObjectNode dislikeEntry = objectMapper.createObjectNode();
            dislikeEntry.put("name", member.getName());
            ArrayNode dislikeNames = objectMapper.createArrayNode();
            member.getDislikes().forEach(d -> dislikeNames.add(d.getName()));
            dislikeEntry.set("dislikes", dislikeNames);
            dislikes.add(dislikeEntry);
        }

        ArrayNode stockList = objectMapper.createArrayNode();
        for (Stock stock : stocks) {
            ObjectNode entry = objectMapper.createObjectNode();
            entry.put("name", stock.getName());
            entry.put("quantity", stock.getQuantity() != null ? stock.getQuantity().doubleValue() : null);
            entry.put("unit", stock.getUnit());
            stockList.add(entry);
        }

        List<String> constraintLines = buildConstraintLines(constraints);

        if (days > 1) {
            return buildMultiDayPrompt(likes, dislikes, stockList, feedbackJson, constraintLines, days);
        }
        return buildSingleDayPrompt(likes, dislikes, stockList, feedbackJson, constraintLines);
    }

    private List<String> buildConstraintLines(JsonNode constraints) {
        List<String> lines = new ArrayList<>();
        if (constraints != null) {
            JsonNode budget = constraints.path("budget");
            if (!budget.isMissingNode() && !budget.isNull()) {
                lines.add("・予算: " + budget.asText() + "円以内");
            }
            JsonNode cookingTime = constraints.path("cookingTime");
            if (cookingTime.isMissingNode()) {
                cookingTime = constraints.path("cooking_time");
            }
            if (!cookingTime.isMissingNode() && !cookingTime.isNull()) {
                lines.add("・調理時間: " + cookingTime.asText() + "分以内");
            }
        }
        return lines;
    }

    private String buildSingleDayPrompt(ArrayNode likes, ArrayNode dislikes, ArrayNode stockList, String feedbackJson,
                                         List<String> constraintLines) {
        String constraintText = constraintLines.isEmpty() ? "特になし" : constraintLines.get(0);
        return """
                家族の好み・在庫・制約条件をもとに、献立案を1つJSONで返してください。
                家族の好みと在庫を優先して提案してください。
                出力は必ず純粋なJSONのみを返してください。コードブロックや追加説明は一切含めないでください。

                【判断ルール】
                ・塩、砂糖、醤油、味噌、酒、みりん、油、こしょう、酢などの基本調味料は家庭に常備されているものとし、在庫になくても使用して構いません。
                ・在庫にある食材をなるべく活用してください。
                ・ただし、主要な食材（肉、魚、野菜、米、麺など）が在庫に全くない場合は「料理は作れません」を返してください。
                ・予算や調理時間の制約が指定されている場合、それを明らかに超える料理しか作れない場合は「料理は作れません」を返してください。
                ・制約を満たす献立が可能な場合は、必ず献立を提案してください。

                ▼家族の好み
                好き：%s
                嫌い：%s

                ▼冷蔵庫の在庫（なるべく在庫を活用してください。基本調味料は常備とみなします）
                %s

                ▼制約条件
                %s

                ▼過去のフィードバック
                %s

                ▼返す形式（厳守）
                制約条件を検証し、主要食材の不足・予算超過・時間超過で献立が不可能な場合のみ、以下のいずれかを返してください。

                【在庫が足りない場合】
                {
                  "title": "料理は作れません",
                  "reason": "在庫がありません",
                  "ingredients": ["必要な材料1", "必要な材料2"]
                }

                【調理時間が足りない場合】
                {
                  "title": "料理は作れません",
                  "reason": "調理時間が〇〇分足りません",
                  "ingredients": ["必要な材料1", "必要な材料2"]
                }

                【制約条件を満たす場合】
                {
                  "title": "string",
                  "reason": "具体的な理由（例：「〇〇さんが好きなので」「〇〇の在庫を活用して」など、家族の好みや在庫に基づいた具体的な理由。「制約条件を満たしています」のような当たり前の理由は禁止）",
                  "time": この料理の調理時間（分単位の整数。材料と調理方法から適切に推定してください）,
                  "budget": この料理の予算（円単位の整数。材料から適切に推定してください）,
                  "ingredients": ["材料1", "材料2"]
                }
                """.formatted(likes, dislikes, stockList, constraintText, feedbackJson);
    }

    private String buildMultiDayPrompt(ArrayNode likes, ArrayNode dislikes, ArrayNode stockList, String feedbackJson,
                                        List<String> constraintLines, int days) {
        String constraintText = constraintLines.isEmpty() ? "特になし" : constraintLines.get(0);
        return """
                家族の好み・在庫・制約条件をもとに、%d日分の献立案をJSONの配列で返してください。
                家族の好みと在庫を優先して提案してください。
                出力は必ず純粋なJSONのみを返してください。コードブロックや追加説明は一切含めないでください。

                【判断ルール】
                ・塩、砂糖、醤油、味噌、酒、みりん、油、こしょう、酢などの基本調味料は家庭に常備されているものとし、在庫になくても使用して構いません。
                ・在庫にある食材をなるべく活用してください。
                ・ただし、主要な食材（肉、魚、野菜、米、麺など）が在庫に全くない場合は「料理は作れません」を返してください。
                ・予算や調理時間の制約が指定されている場合、それを明らかに超える料理しか作れない場合は「料理は作れません」を返してください。
                ・制約を満たす献立が可能な場合は、必ず献立を提案してください。
                ・%d日分の献立が重複しないよう、バリエーション豊かに提案してください。
                ・在庫を考慮し、%d日間で効率的に使い切れるよう工夫してください。

                ▼家族の好み
                好き：%s
                嫌い：%s

                ▼冷蔵庫の在庫（なるべく在庫を活用してください。基本調味料は常備とみなします）
                %s

                ▼制約条件（1日あたり）
                %s

                ▼過去のフィードバック
                %s

                ▼返す形式（厳守）
                制約条件を検証し、主要食材の不足・予算超過・時間超過で献立が不可能な場合のみ、以下のいずれかを返してください。

                【在庫が足りない場合】
                [
                  {
                    "day": 1,
                    "title": "料理は作れません",
                    "reason": "在庫がありません",
                    "ingredients": ["必要な材料1", "必要な材料2"]
                  }
                ]

                【調理時間が足りない場合】
                [
                  {
                    "day": 1,
                    "title": "料理は作れません",
                    "reason": "調理時間が〇〇分足りません",
                    "ingredients": ["必要な材料1", "必要な材料2"]
                  }
                ]

                【制約条件を満たす場合】%d日分の異なる献立を返してください
                [
                  {
                    "day": 1,
                    "title": "string",
                    "reason": "具体的な理由（例：「〇〇さんが好きなので」「〇〇の在庫を活用して」など、家族の好みや在庫に基づいた具体的な理由。「制約条件を満たしています」のような当たり前の理由は禁止）",
                    "time": この日の料理の調理時間（分単位の整数。材料と調理方法から適切に推定してください）,
                    "budget": この日の料理の予算（円単位の整数。材料から適切に推定してください）,
                    "ingredients": ["材料1", "材料2"]
                  },
                  {
                    "day": 2,
                    "title": "string",
                    "reason": "具体的な理由",
                    "time": この日の料理の調理時間（分単位の整数）,
                    "budget": この日の料理の予算（円単位の整数）,
                    "ingredients": ["材料1", "材料2"]
                  }
                ]
                """.formatted(days, days, days, likes, dislikes, stockList, constraintText, feedbackJson, days);
    }
}
