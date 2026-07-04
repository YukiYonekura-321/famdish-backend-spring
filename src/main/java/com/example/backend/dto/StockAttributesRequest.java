package com.example.backend.dto;

import java.math.BigDecimal;

public record StockAttributesRequest(String name, BigDecimal quantity, String unit) {
}
