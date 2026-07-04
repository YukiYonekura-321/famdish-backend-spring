package com.example.backend.dto;

/** POST /api/members のリクエストボディ */
public record MemberCreateRequest(
        Long familyId,
        FamilyNameRequest family,
        Boolean linkUser,
        MemberAttributesRequest member
) {
}
