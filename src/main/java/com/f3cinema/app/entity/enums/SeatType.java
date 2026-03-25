package com.f3cinema.app.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeatType {
    NORMAL("Normal Seat"),
    VIP("VIP Premium"),
    SWEETBOX("Sweetbox for Couples");

    private final String label;
}
