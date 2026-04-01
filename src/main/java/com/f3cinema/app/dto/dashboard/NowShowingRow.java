package com.f3cinema.app.dto.dashboard;

import java.time.LocalDateTime;

/**
 * Showtime row with computed status for operations display.
 */
public record NowShowingRow(String movieTitle, LocalDateTime startTime, LocalDateTime endTime, String statusLabel) {
}
