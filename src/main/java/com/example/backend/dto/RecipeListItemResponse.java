package com.example.backend.dto;

import java.time.LocalDateTime;

public record RecipeListItemResponse(
        Long id,
        String dishName,
        String reason,
        Integer servings,
        Integer cookingTime,
        Long proposerId,
        Long suggestionId,
        String imageUrl,
        LocalDateTime createdAt,
        String familyName
) {
}
