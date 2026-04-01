package com.f3cinema.app.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketLineDTO(
        String movieTitle,
        String roomName,
        LocalDateTime startTime,
        String seatLabel,
        BigDecimal finalPrice
) {
}
