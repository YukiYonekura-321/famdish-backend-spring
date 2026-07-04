package com.example.backend.dto;

import jakarta.validation.Valid;

/** POST /api/contacts のリクエストボディ ({"contact": {...}}) */
public record ContactCreateRequest(@Valid ContactAttributesRequest contact) {
}
