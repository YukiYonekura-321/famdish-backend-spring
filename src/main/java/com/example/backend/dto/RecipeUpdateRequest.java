package com.example.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record RecipeUpdateRequest(
        Integer servings,
        JsonNode missingIngredients,
        Integer cookingTime,
        JsonNode steps
) {
}
