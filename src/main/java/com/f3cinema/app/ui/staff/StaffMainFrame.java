package com.f3cinema.app.ui.staff;

import com.f3cinema.app.entity.User;
import com.f3cinema.app.ui.admin.SidebarController;
import com.f3cinema.app.ui.staff.ticketing.TicketingFlowPanel;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Modern Main Frame for Cinema Staff with Top Navigation.
 * Optimized with lazy loading for instant startup.
 */
public class StaffMainFrame extends JFrame {

    private final User loggedInUser;
    private StaffNavbarPanel navbarPanel;
    private JPanel contentArea;
    private SidebarController contentController;
    private final Map<String, JPanel> panelCache = new HashMap<>();

    public static final String CARD_TICKETING = "TICKETING";
    public static final String CARD_SEARCH = "SEARCH";
    public static final String CARD_CUSTOMERS = "CUSTOMERS";
    public static final String CARD_TRANSACTIONS = "TRANSACTIONS";

    public StaffMainFrame(User user) {
        this.loggedInUser = user;
        initialize();
    }

    private void initialize() {
        setTitle("F3 Cinema - Phân hệ Nhân viên");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(Color.decode("#0F172A"));
        setLayout(new BorderLayout());

        navbarPanel = new StaffNavbarPanel(loggedInUser);
        add(navbarPanel, BorderLayout.NORTH);

        contentArea = new JPanel(new CardLayout());
        contentArea.setOpaque(false);
        contentArea.putClientProperty(FlatClientProperties.STYLE, "arc: 16");

        add(contentArea, BorderLayout.CENTER);

        contentController = new SidebarController(contentArea);

        navbarPanel.setOnMenuSelected(menuKey -> {
            lazyLoadAndShow(menuKey);
            contentController.handleMenuSelection(menuKey);
        });

        lazyLoadAndShow(CARD_TICKETING);
        contentController.handleMenuSelection(CARD_TICKETING);
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
            case CARD_TICKETING -> new TicketingFlowPanel();
            case CARD_SEARCH -> new SearchShowtimePanel();
            case CARD_CUSTOMERS -> new CustomerPanel();
            case CARD_TRANSACTIONS -> new TransactionHistoryPanel();
            default -> null;
        };
    }
}
