package com.f3cinema.app.dto.dashboard;

import java.math.BigDecimal;

/**
 * Top movie ranking row for dashboard widget.
 */
public record TopMovieRow(
        int rank,
        String movieTitle,
        long ticketsSold,
        BigDecimal revenue
) {
}
