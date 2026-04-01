package com.f3cinema.app.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated finance KPIs and chart inputs for the dashboard.
 */
public record DashboardFinance(
        BigDecimal revenueToday,
        long ticketsSoldToday,
        double occupancyPercent,
        long newCustomersToday,
        List<RevenueSeriesPoint> revenueLast7Days,
        BigDecimal ticketRevenue7d,
        BigDecimal fnbRevenue7d
) {
}
