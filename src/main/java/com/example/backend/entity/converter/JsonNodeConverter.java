package com.example.backend.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.NullNode;

/**
 * Postgres の json カラム (Rails の t.json) と Java の {@link JsonNode} を相互変換する。
 */
@Converter
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode attribute) {
        if (attribute == null || attribute.isNull()) {
            return null;
        }
        return OBJECT_MAPPER.writeValueAsString(attribute);
    }

    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return NullNode.getInstance();
        }
        return OBJECT_MAPPER.readTree(dbData);
    }
}
