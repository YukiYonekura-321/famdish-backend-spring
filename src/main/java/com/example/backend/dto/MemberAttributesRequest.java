package com.example.backend.dto;

import java.util.List;

/** member パラメータ (name, likes_attributes, dislikes_attributes) */
public record MemberAttributesRequest(
        String name,
        List<NestedAttributeRequest> likesAttributes,
        List<NestedAttributeRequest> dislikesAttributes
) {
}
