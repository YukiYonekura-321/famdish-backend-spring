package com.example.backend.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.RecipeCreateRequest;
import com.example.backend.dto.RecipeCreateResponse;
import com.example.backend.dto.RecipeDetailResponse;
import com.example.backend.dto.RecipeListItemResponse;
import com.example.backend.dto.RecipeUpdateRequest;
import com.example.backend.entity.Family;
import com.example.backend.entity.Recipe;
import com.example.backend.entity.Stock;
import com.example.backend.entity.Suggestion;
import com.example.backend.entity.User;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.NotFoundException;
import com.example.backend.repository.RecipeRepository;
import com.example.backend.repository.StockRepository;
import com.example.backend.repository.SuggestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final StockRepository stockRepository;
    private final SuggestionRepository suggestionRepository;
    private final UserService userService;
    private final OpenAiService openAiService;
    private final S3UploadService s3UploadService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RecipeService(RecipeRepository recipeRepository, StockRepository stockRepository,
                          SuggestionRepository suggestionRepository, UserService userService,
                          OpenAiService openAiService, S3UploadService s3UploadService) {
        this.recipeRepository = recipeRepository;
        this.stockRepository = stockRepository;
        this.suggestionRepository = suggestionRepository;
        this.userService = userService;
        this.openAiService = openAiService;
        this.s3UploadService = s3UploadService;
    }

    @Transactional
    public Map<String, Object> explain(String dishName, Integer servings, Long suggestionId) {
        if (dishName == null || dishName.isBlank()) {
            throw new BadRequestException("料理名を入力してください");
        }
        if (servings == null) {
            throw new BadRequestException("何人分か入力してください");
        }

        Integer cookingTime = fetchCookingTime(suggestionId);
        String prompt = buildRecipePrompt(dishName, servings, stockList(), cookingTime);
        String aiResult = openAiService.chat("あなたはプロの料理人です。料理のレシピを正確にJSON形式で返してください。", prompt);

        Map<String, Object> body = new LinkedHashMap<>();
        try {
            body.put("recipe", objectMapper.readTree(aiResult));
        } catch (Exception e) {
            body.put("recipe", aiResult);
        }
        return body;
    }

    @Transactional(readOnly = true)
    public List<RecipeListItemResponse> index() {
        return recipeRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(r -> toListItem(r, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipeListItemResponse> family() {
        User currentUser = userService.getCurrentUser();
        Family family = currentUser.getFamily();
        if (family == null) {
            throw new BadRequestException("家族が見つかりません");
        }

        return recipeRepository.findByFamilyIdOrderByCreatedAtDesc(family.getId()).stream()
                .map(r -> toListItem(r, false))
                .toList();
    }

    @Transactional
    public RecipeCreateResponse create(RecipeCreateRequest request) {
        if (request.dishName() == null || request.dishName().isBlank()) {
            throw new BadRequestException("料理名を入力してください");
        }
        User currentUser = userService.getCurrentUser();

        Recipe recipe = new Recipe();
        recipe.setDishName(request.dishName());
        recipe.setProposer(request.proposer());
        recipe.setFamily(currentUser.getFamily());
        recipe.setServings(request.servings());
        if (request.suggestionId() != null) {
            suggestionRepository.findById(request.suggestionId()).ifPresent(recipe::setSuggestion);
        }
        recipe.setMissingIngredients(request.missingIngredients());
        recipe.setCookingTime(request.cookingTime());
        recipe.setSteps(request.steps());
        recipe.setReason(request.reason());
        recipe.setImageUrl(request.imageUrl());

        recipe = recipeRepository.save(recipe);

        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            String s3Url = s3UploadService.upload("recipes", recipe.getId(), null, request.imageUrl());
            if (s3Url != null) {
                recipe.setImageUrl(s3Url);
                recipeRepository.save(recipe);
            }
        }

        return new RecipeCreateResponse(recipe.getId(), "レシピを保存しました");
    }

    @Transactional(readOnly = true)
    public RecipeDetailResponse show(Long id) {
        Recipe recipe = findRecipe(id);
        return new RecipeDetailResponse(
                recipe.getId(),
                recipe.getServings(),
                recipe.getMissingIngredients(),
                recipe.getCookingTime(),
                recipe.getSteps(),
                recipe.getImageUrl()
        );
    }

    @Transactional
    public void update(Long id, RecipeUpdateRequest request) {
        Recipe recipe = findRecipe(id);
        recipe.setServings(request.servings());
        recipe.setMissingIngredients(request.missingIngredients());
        recipe.setCookingTime(request.cookingTime());
        recipe.setSteps(request.steps());
        recipeRepository.save(recipe);
    }

    @Transactional
    public String destroy(Long id) {
        Recipe recipe = findRecipe(id);
        recipeRepository.delete(recipe);
        return "レシピを削除しました";
    }

    private Recipe findRecipe(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("レシピが見つかりません"));
    }

    private List<Map<String, Object>> stockList() {
        User currentUser = userService.getCurrentUser();
        Family family = currentUser.getFamily();
        if (family == null) {
            return List.of();
        }
        return stockRepository.findByFamilyId(family.getId()).stream()
                .map(this::stockToMap)
                .toList();
    }

    private Map<String, Object> stockToMap(Stock stock) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", stock.getName());
        map.put("quantity", stock.getQuantity() != null ? stock.getQuantity().doubleValue() : null);
        map.put("unit", stock.getUnit());
        return map;
    }

    private Integer fetchCookingTime(Long suggestionId) {
        if (suggestionId == null) {
            return null;
        }
        return suggestionRepository.findById(suggestionId)
                .map(Suggestion::getRequests)
                .filter(req -> req != null && req.has("cooking_time"))
                .map(req -> req.get("cooking_time").asInt())
                .orElse(null);
    }

    private RecipeListItemResponse toListItem(Recipe recipe, boolean includeFamily) {
        String familyName = includeFamily && recipe.getFamily() != null ? recipe.getFamily().getName() : null;
        return new RecipeListItemResponse(
                recipe.getId(),
                recipe.getDishName(),
                recipe.getReason(),
                recipe.getServings(),
                recipe.getCookingTime(),
                recipe.getProposer(),
                recipe.getSuggestion() != null ? recipe.getSuggestion().getId() : null,
                recipe.getImageUrl(),
                recipe.getCreatedAt(),
                familyName
        );
    }

    private String buildRecipePrompt(String dishName, Integer servings, List<Map<String, Object>> stockList, Integer cookingTime) {
        ArrayNode stockJson = objectMapper.valueToTree(stockList);
        String cookingTimeLine = cookingTime != null
                ? "制限時間は" + cookingTime + "分以内です。この時間内に完成できるレシピにしてください。"
                : "";

        return """
                「%s」の作り方を%s人分でJSON形式で返してください。
                %s
                出力は必ず純粋なJSONのみを返してください。コードブロック（```）や追加説明は一切含めないでください。

                ▼冷蔵庫の在庫
                %s

                ▼返す形式（厳守）
                {
                  "dish_name": "%s",
                  "servings": %s,
                  "missing_ingredients": [
                    { "name": "食材名", "quantity": "必要量（例：200g、2個）" }
                  ],
                  "cooking_time": 調理時間（分単位の整数）,
                  "steps": [
                    { "step": 1, "description": "手順の説明" },
                    { "step": 2, "description": "手順の説明" }
                  ]
                }

                【missing_ingredients について】
                ・この料理に必要な全食材と冷蔵庫の在庫を比較してください。
                ・冷蔵庫に十分な量がある食材は含めないでください。
                ・冷蔵庫にないか、量が足りない食材のみを「不足分の量」で記載してください。
                ・塩、砂糖、醤油、味噌、酒、みりん、油、こしょう、酢などの基本調味料は常備とみなし、missing_ingredientsに含めないでください。

                【cooking_time について】
                ・下準備から完成までの合計時間を分単位の整数で返してください。
                ・制限時間が指定されている場合は、必ずその時間以内に収まるようにしてください。

                【steps について】
                ・初心者にもわかりやすく、具体的な手順を記載してください。
                ・火加減や時間の目安も含めてください。
                """.formatted(dishName, servings, cookingTimeLine, stockJson, dishName, servings);
    }
}
