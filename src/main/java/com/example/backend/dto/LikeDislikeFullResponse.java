package com.example.backend.dto;

import java.time.LocalDateTime;

public record LikeDislikeFullResponse(
        Long id,
        String name,
        Long memberId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
