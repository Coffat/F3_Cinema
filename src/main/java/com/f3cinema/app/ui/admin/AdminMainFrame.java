package com.f3cinema.app.ui.admin;

import com.f3cinema.app.entity.User;
import com.f3cinema.app.ui.dashboard.*;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * AdminMainFrame — Modern Sidebar Navigation architecture for F3 Cinema Admin Suite.
 * Core UI for HCMUTE Project (2026 Standards).
 * Optimized with lazy loading for instant startup.
 */
public class AdminMainFrame extends JFrame {

    private final User loggedInUser;
    private NavbarPanel navbarPanel;
    private JPanel contentArea;
    private SidebarController sidebarController;
    private final Map<String, JPanel> panelCache = new HashMap<>();

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
        initialize();
    }

    private void initialize() {
        setTitle("F3 Cinema — Admin Suite");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1366, 860);
        setMinimumSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.decode("#0F172A"));
        setLayout(new BorderLayout());

        navbarPanel = new NavbarPanel(loggedInUser);
        add(navbarPanel, BorderLayout.NORTH);

        contentArea = new JPanel(new CardLayout());
        contentArea.setOpaque(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        contentArea.putClientProperty(FlatClientProperties.STYLE, "arc: 16");

        add(contentArea, BorderLayout.CENTER);

        sidebarController = new SidebarController(contentArea);

        navbarPanel.setOnMenuSelected(menuKey -> {
            lazyLoadAndShow(menuKey);
            sidebarController.handleMenuSelection(menuKey);
        });

        lazyLoadAndShow(CARD_DASHBOARD);
    }

    private void lazyLoadAndShow(String menuKey) {
        if (!panelCache.containsKey(menuKey)) {
            JPanel panel = createPanelForMenu(menuKey);
            if (panel != null) {
                panelCache.put(menuKey, panel);
                contentArea.add(panel, menuKey);
            }
        }
    }

    private JPanel createPanelForMenu(String menuKey) {
        return switch (menuKey) {
            case CARD_DASHBOARD -> new DashboardPanel();
            case CARD_MOVIES -> new MoviePanel();
            case CARD_ROOMS -> new RoomPanel();
            case CARD_SHOWTIMES -> new ShowtimePanel();
            case CARD_STAFF -> new StaffPanel();
            case CARD_WAREHOUSE -> new WarehousePanel();
            case CARD_PROMOTION -> new PromotionPanel();
            case CARD_STATISTICS -> new StatisticsPanel();
            default -> null;
        };
    }
}
