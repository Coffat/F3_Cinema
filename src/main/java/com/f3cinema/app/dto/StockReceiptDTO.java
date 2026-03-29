package com.f3cinema.app.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Stock Receipt (Master).
 */
public record StockReceiptDTO(
    String supplier,
    List<StockReceiptItemDTO> items,
    BigDecimal totalImportCost
) {}
