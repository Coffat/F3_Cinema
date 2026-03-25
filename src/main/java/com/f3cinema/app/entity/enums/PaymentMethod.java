package com.f3cinema.app.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CASH("Cash Payment"),
    BANK_TRANSFER("Bank Transfer"),
    MOMO("MoMo E-Wallet");

    private final String label;
}
