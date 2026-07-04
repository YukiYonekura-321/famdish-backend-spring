package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/** POST /api/members のレスポンス (Rails の member.as_json(include: [:likes, :dislikes, family:...]) 相当) */
public record MemberCreateResponse(
        Long id,
        String name,
        Long familyId,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<LikeDislikeFullResponse> likes,
        List<LikeDislikeFullResponse> dislikes,
        FamilySummaryResponse family
) {
}
