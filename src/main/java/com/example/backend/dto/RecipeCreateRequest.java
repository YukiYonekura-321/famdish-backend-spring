package com.example.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record RecipeCreateRequest(
        String dishName,
        Long proposer,
        Integer servings,
        Long suggestionId,
        JsonNode missingIngredients,
        Integer cookingTime,
        JsonNode steps,
        String reason,
        String imageUrl
) {
}

