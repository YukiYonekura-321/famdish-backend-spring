package com.example.backend.dto;

/** POST /api/families/assign_cook のリクエストボディ */
public record AssignCookRequest(Long memberId) {
}
