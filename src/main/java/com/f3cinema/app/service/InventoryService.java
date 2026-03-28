package com.f3cinema.app.service;

import com.f3cinema.app.dto.dashboard.InventoryAlertRow;
import com.f3cinema.app.repository.DashboardRepository;
import com.f3cinema.app.repository.DashboardRepositoryImpl;
import lombok.extern.log4j.Log4j2;

import java.util.List;

/**
 * Inventory operations — low-stock alerts for operations dashboard.
 */
@Log4j2
public class InventoryService {

    private static InventoryService instance;

    private final DashboardRepository dashboardRepository = DashboardRepositoryImpl.getInstance();

    private InventoryService() {
    }

    public static synchronized InventoryService getInstance() {
        if (instance == null) {
            instance = new InventoryService();
        }
        return instance;
    }

    public List<InventoryAlertRow> getLowStockAlerts() {
        log.debug("Loading inventory alerts");
        return dashboardRepository.loadInventoryAlerts();
    }
}
