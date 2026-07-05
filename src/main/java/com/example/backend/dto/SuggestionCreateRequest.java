package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** POST /api/suggestions のリクエストボディ */
public record SuggestionCreateRequest(
        Object budget,
        Integer cookingTime,
        Integer days,
        @JsonProperty("sgId") Long sgId
) {
}