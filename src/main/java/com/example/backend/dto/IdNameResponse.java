package com.example.backend.dto;

/** {id, name} だけを持つ軽量表現 (Like, Dislike, Menu, Member の一覧などで共通利用) */
public record IdNameResponse(Long id, String name) {
}
