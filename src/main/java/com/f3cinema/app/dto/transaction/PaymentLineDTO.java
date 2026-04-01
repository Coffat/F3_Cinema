package com.f3cinema.app.dto.transaction;

import com.f3cinema.app.entity.enums.PaymentMethod;
import com.f3cinema.app.entity.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentLineDTO(
        Long paymentId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String transactionId
) {
}
