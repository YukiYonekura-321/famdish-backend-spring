package com.example.backend.dto;

import java.time.LocalDateTime;

public record InvitationCreateResponse(String token, String inviteUrl, LocalDateTime expiresAt) {
}
