package com.example.backend.dto;

/** POST /api/families/assign_cook のレスポンス */
public record AssignCookResponse(Long todayCookId, String todayCookName) {
}
