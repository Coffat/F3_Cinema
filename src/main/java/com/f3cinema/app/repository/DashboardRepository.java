package com.f3cinema.app.repository;

import com.f3cinema.app.dto.dashboard.DashboardFinance;
import com.f3cinema.app.dto.dashboard.InventoryAlertRow;
import com.f3cinema.app.dto.dashboard.NowShowingRow;

import java.util.List;

/**
 * Read-only aggregations for the operations dashboard (native SQL for performance).
 */
public interface DashboardRepository {

    DashboardFinance loadFinance();

    List<InventoryAlertRow> loadInventoryAlerts();

    List<NowShowingRow> loadNowShowingSchedule();
}
