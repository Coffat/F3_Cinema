package com.f3cinema.app.service;

import com.f3cinema.app.dto.dashboard.DashboardFinance;
import com.f3cinema.app.repository.DashboardRepository;
import com.f3cinema.app.repository.DashboardRepositoryImpl;
import lombok.extern.log4j.Log4j2;

/**
 * Invoice-related business logic and dashboard finance aggregates.
 */
@Log4j2
public class InvoiceService {

    private static InvoiceService instance;

    private final DashboardRepository dashboardRepository = DashboardRepositoryImpl.getInstance();

    private InvoiceService() {
    }

    public static synchronized InvoiceService getInstance() {
        if (instance == null) {
            instance = new InvoiceService();
        }
        return instance;
    }

    /**
     * KPIs, 7-day revenue series, and ticket vs F&amp;B mix for the dashboard.
     */
    public DashboardFinance getDashboardFinance() {
        log.debug("Loading dashboard finance aggregates");
        return dashboardRepository.loadFinance();
    }
}
