package com.f3cinema.app.dto;

import java.time.LocalDateTime;

public record ShowtimeSummaryDTO(
    Long showtimeId,
    String movieTitle,
    String roomName,
    LocalDateTime startTime,
    double basePrice
) {}
