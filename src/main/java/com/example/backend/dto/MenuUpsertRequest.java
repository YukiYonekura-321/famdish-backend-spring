package com.example.backend.dto;

/** POST /api/menus, PATCH /api/menus/:id のリクエストボディ ({"menu": {"name": "..."}}) */
public record MenuUpsertRequest(MenuAttributesRequest menu) {
}
