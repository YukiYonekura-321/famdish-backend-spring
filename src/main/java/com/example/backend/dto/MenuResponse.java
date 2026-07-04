package com.example.backend.dto;

/** GET /api/menus のレスポンス項目 */
public record MenuResponse(Long id, String name, IdNameResponse member) {
}
