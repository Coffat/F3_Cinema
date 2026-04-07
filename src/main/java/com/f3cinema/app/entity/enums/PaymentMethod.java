package com.f3cinema.app.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CASH("Tiền mặt"),
    BANK_TRANSFER("Chuyển khoản"),
    CARD("Thẻ ngân hàng");

    private final String label;
}
