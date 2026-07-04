package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** likes_attributes / dislikes_attributes の1要素 (ActiveRecord の accepts_nested_attributes_for 相当) */
public record NestedAttributeRequest(
        Long id,
        String name,
        @JsonProperty("_destroy") Boolean destroy
) {
}
