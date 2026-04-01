package com.f3cinema.app.dto.transaction;

import com.f3cinema.app.entity.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TransactionDetailDTO(
        Long invoiceId,
        LocalDateTime createdAt,
        InvoiceStatus invoiceStatus,
        String customerName,
        String customerPhone,
        String staffName,
        BigDecimal totalAmount,
        Integer pointsUsed,
        Integer pointsEarned,
        List<TicketLineDTO> tickets,
        List<SnackLineDTO> snacks,
        List<PaymentLineDTO> payments
) {
}
