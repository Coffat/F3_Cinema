package com.f3cinema.app.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Product and its corresponding Inventory.
 * Pattern: DTO Pattern using Java 21 Record.
 */
public record ProductDTO(
    Long id,
    String name,
    BigDecimal price,
    String unit,
    Integer currentQuantity,
    Integer minThreshold
) {}
