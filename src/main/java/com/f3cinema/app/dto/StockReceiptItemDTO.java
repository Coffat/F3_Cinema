package com.f3cinema.app.dto;

import java.math.BigDecimal;

/**
 * DTO for Stock Receipt Item (Detail).
 */
public record StockReceiptItemDTO(
    Long productId,
    String productName,
    Integer quantity,
    BigDecimal importPrice
) {}
