package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** POST /api/suggestions/:id/feedback のリクエストボディ */
public record SuggestionFeedbackRequest(
        @JsonProperty("chosenOption") String chosenOption,
        @JsonProperty("feedbackNote") String feedbackNote
) {
}

