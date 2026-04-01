package com.f3cinema.app.dto.dashboard;

import java.util.List;

/**
 * Full dashboard payload assembled for the UI layer.
 */
public record DashboardSnapshot(
        DashboardFinance finance,
        List<InventoryAlertRow> inventoryAlerts,
        List<NowShowingRow> nowShowing
) {
}
