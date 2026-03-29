package com.f3cinema.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockReceiptSummaryDTO(
    Long id,
    String supplier,
    BigDecimal totalImportCost,
    LocalDateTime receiptDate
) {}
