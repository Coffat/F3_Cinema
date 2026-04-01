package com.f3cinema.app.dto.transaction;

import java.math.BigDecimal;

public record SnackLineDTO(
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
