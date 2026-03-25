package com.f3cinema.app.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvoiceStatus {
    PENDING("Pending Payment"),
    PAID("Fully Paid"),
    CANCELLED("Cancelled");

    private final String label;
}
