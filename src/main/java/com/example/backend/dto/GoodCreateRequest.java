package com.example.backend.dto;

/** POST /api/goods, POST /api/goods/create_suggestion のリクエストボディ */
public record GoodCreateRequest(Long menuId, Long suggestionId, GoodNestedAttributes good) {

    public Long resolveMenuId() {
        return menuId != null ? menuId : (good != null ? good.menuId() : null);
    }

    public Long resolveSuggestionId() {
        return suggestionId != null ? suggestionId : (good != null ? good.suggestionId() : null);
    }
}