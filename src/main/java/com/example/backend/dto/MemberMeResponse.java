package com.example.backend.dto;

/** GET /api/members/me のレスポンス */
public record MemberMeResponse(
        Long familyId,
        String familyName,
        String username,
        MemberIdResponse member
) {
}
