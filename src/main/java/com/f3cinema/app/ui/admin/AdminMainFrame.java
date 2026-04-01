package com.f3cinema.app.ui.admin;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.entity.User;
import com.f3cinema.app.ui.dashboard.*;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;

/**
 * AdminMainFrame — Modern Sidebar Navigation architecture for F3 Cinema Admin Suite.
 * Core UI for HCMUTE Project (2026 Standards).
 */
public class AdminMainFrame extends JFrame {

    private final User loggedInUser;
    private NavbarPanel navbarPanel;
    private JPanel contentArea;
    private SidebarController sidebarController;

    private static final String CARD_DASHBOARD  = "DASHBOARD";
    private static final String CARD_MOVIES     = "MOVIES";
    private static final String CARD_ROOMS      = "ROOMS";
    private static final String CARD_SHOWTIMES  = "SHOWTIMES";
    private static final String CARD_STAFF      = "STAFF";
    private static final String CARD_WAREHOUSE  = "WAREHOUSE";
    private static final String CARD_PROMOTION  = "PROMOTION";
    private static final String CARD_STATISTICS = "STATISTICS";

    public AdminMainFrame(User user) {
        this.loggedInUser = user;
        // 1. Setup Theme
        ThemeConfig.setup();
        initialize();
    }

    private void initialize() {
        setTitle("F3 Cinema — Admin Suite");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1366, 860);
        setMinimumSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở full màn hình mặc định
        getContentPane().setBackground(Color.decode("#0F172A")); // Slate 900
        setLayout(new BorderLayout());

        // 2. NORTH: Top Navigation
        navbarPanel = new NavbarPanel(loggedInUser);
        add(navbarPanel, BorderLayout.NORTH);

        // 3. CENTER: Content (CardLayout)
        contentArea = new JPanel(new CardLayout());
        contentArea.setOpaque(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // Apply global arc styling to content area (if needed for glass windows inside)
        contentArea.putClientProperty(FlatClientProperties.STYLE, "arc: 16");

        add(contentArea, BorderLayout.CENTER);

        // 4. Controller & Module Injection
        sidebarController = new SidebarController(contentArea);
        injectModules();

        // Bind Navbar events to Controller
        navbarPanel.setOnMenuSelected(sidebarController::handleMenuSelection);

        // Set initial card
        sidebarController.handleMenuSelection(CARD_DASHBOARD);
    }

    private void injectModules() {
        // Zero-latency module initialization
        contentArea.add(new DashboardPanel(), CARD_DASHBOARD);
        contentArea.add(new MoviePanel(), CARD_MOVIES);
        contentArea.add(new RoomPanel(), CARD_ROOMS);
        contentArea.add(new ShowtimePanel(), CARD_SHOWTIMES);
        contentArea.add(new StaffPanel(), CARD_STAFF);
        contentArea.add(new WarehousePanel(), CARD_WAREHOUSE);
        contentArea.add(new PromotionPanel(), CARD_PROMOTION);
        contentArea.add(new StatisticsPanel(), CARD_STATISTICS);
    }
}
