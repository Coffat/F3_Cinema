package com.f3cinema.app.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Single day revenue for dashboard line chart.
 */
public record RevenueSeriesPoint(LocalDate day, BigDecimal amount) {
}
