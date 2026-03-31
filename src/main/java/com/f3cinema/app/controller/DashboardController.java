package com.f3cinema.app.controller;

import com.f3cinema.app.dto.dashboard.DashboardSnapshot;
import com.f3cinema.app.service.InventoryService;
import com.f3cinema.app.service.InvoiceService;
import com.f3cinema.app.service.MovieService;

/**
 * Aggregates dashboard data from domain services (no EDT access).
 */
public class DashboardController {

    private final InvoiceService invoiceService;
    private final InventoryService inventoryService;
    private final MovieService movieService;

    public DashboardController() {
        this(InvoiceService.getInstance(), InventoryService.getInstance(), MovieService.getInstance());
    }

    public DashboardController(InvoiceService invoiceService,
                               InventoryService inventoryService,
                               MovieService movieService) {
        this.invoiceService = invoiceService;
        this.inventoryService = inventoryService;
        this.movieService = movieService;
    }

    public DashboardSnapshot loadSnapshot() {
        return new DashboardSnapshot(
                invoiceService.getDashboardFinance(),
                inventoryService.getLowStockAlerts(),
                movieService.getNowShowingSchedule()
        );
    }
}
