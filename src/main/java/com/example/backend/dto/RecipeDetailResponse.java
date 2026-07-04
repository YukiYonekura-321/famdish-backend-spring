package com.example.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record RecipeDetailResponse(
        Long id,
        Integer servings,
        JsonNode missingIngredients,
        Integer cookingTime,
        JsonNode steps,
        String imageUrl
) {
}
