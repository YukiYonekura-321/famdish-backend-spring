package com.example.backend.dto;

/** POST/PATCH /api/stocks のリクエストボディ ({"stock": {...}}) */
public record StockUpsertRequest(StockAttributesRequest stock) {
}
