package com.f3cinema.app.dto.transaction;

import com.f3cinema.app.entity.enums.InvoiceStatus;
import com.f3cinema.app.entity.enums.PaymentStatus;

public record TransactionActionResultDTO(
        Long invoiceId,
        InvoiceStatus invoiceStatus,
        PaymentStatus paymentStatus,
        String message
) {
}
