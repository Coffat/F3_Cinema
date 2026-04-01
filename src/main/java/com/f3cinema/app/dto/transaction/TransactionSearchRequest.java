package com.f3cinema.app.dto.transaction;

import com.f3cinema.app.entity.enums.InvoiceStatus;
import com.f3cinema.app.entity.enums.PaymentStatus;

import java.time.LocalDate;

public record TransactionSearchRequest(
        String keyword,
        LocalDate fromDate,
        LocalDate toDate,
        InvoiceStatus invoiceStatus,
        PaymentStatus paymentStatus,
        Long staffId,
        int offset,
        int limit
) {
    public TransactionSearchRequest {
        if (offset < 0) {
            offset = 0;
        }
        if (limit <= 0) {
            limit = 20;
        }
    }
}
