package com.example.backend.dto;

import java.util.List;

/** GET /api/members のレスポンス項目 */
public record MemberSummaryResponse(
        Long id,
        String name,
        List<IdNameResponse> likes,
        List<IdNameResponse> dislikes,
        MemberUserResponse user,
        List<IdNameResponse> menus
) {
}
