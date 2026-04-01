package com.f3cinema.app.entity.enums;

import lombok.Getter;

/**
 * Types of voucher discount strategies.
 */
@Getter
public enum VoucherType {
    PERCENTAGE("Giảm theo %"),
    FIXED_AMOUNT("Giảm cố định"),
    BUY_X_GET_Y("Mua X tặng Y"),
    COMBO_DISCOUNT("Giảm combo");

    private final String displayName;

    VoucherType(String displayName) {
        this.displayName = displayName;
    }
}
