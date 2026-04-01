package com.f3cinema.app.dto.transaction;

import com.f3cinema.app.entity.enums.InvoiceStatus;
import com.f3cinema.app.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRowDTO(
        Long invoiceId,
        LocalDateTime createdAt,
        String customerName,
        String customerPhone,
        String staffName,
        BigDecimal totalAmount,
        InvoiceStatus invoiceStatus,
        PaymentStatus paymentStatus
) {
}
