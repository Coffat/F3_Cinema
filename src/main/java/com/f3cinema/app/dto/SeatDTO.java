package com.f3cinema.app.dto;

public record SeatDTO(
    Long id,
    String rowChar,
    int number,
    SeatType seatType,
    double price,
    boolean isSold
) {
    public enum SeatType {
        NORMAL, VIP, SWEETBOX
    }
}
