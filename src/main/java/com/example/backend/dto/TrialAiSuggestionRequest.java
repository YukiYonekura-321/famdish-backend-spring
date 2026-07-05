package com.example.backend.dto;

/** POST /api/trial/aisuggestion のリクエストボディ */
public record TrialAiSuggestionRequest(String likes, String dislikes) {
}
