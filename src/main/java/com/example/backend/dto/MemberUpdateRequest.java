package com.example.backend.dto;

/** PATCH /api/members/:id のリクエストボディ */
public record MemberUpdateRequest(MemberAttributesRequest member) {
}
