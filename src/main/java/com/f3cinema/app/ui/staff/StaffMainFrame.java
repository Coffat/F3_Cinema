package com.f3cinema.app.ui.staff;

import com.f3cinema.app.entity.User;
import com.f3cinema.app.ui.admin.SidebarController;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;

/**
 * Modern Main Frame for Cinema Staff with Top Navigation.
 */
public class StaffMainFrame extends JFrame {

    private final User loggedInUser;
    private StaffNavbarPanel navbarPanel;
    private JPanel contentArea;
    private SidebarController contentController;

    // Card Constants
    public static final String CARD_TICKETING = "TICKETING";
    public static final String CARD_SNACKS = "SNACKS";
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
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở full màn hình mặc định
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 1. Root Layout
        getContentPane().setBackground(Color.decode("#0F172A")); // Slate 900
        setLayout(new BorderLayout());

        // 2. NORTH: Top Navigation
        navbarPanel = new StaffNavbarPanel(loggedInUser);
        add(navbarPanel, BorderLayout.NORTH);

        // 3. CENTER: Content
        contentArea = new JPanel(new CardLayout());
        contentArea.setOpaque(false);
        contentArea.putClientProperty(FlatClientProperties.STYLE, "arc: 16");

        add(contentArea, BorderLayout.CENTER);

        // 4. Controller & Injection
        contentController = new SidebarController(contentArea);
        injectModules();

        // Bind Navbar events
        navbarPanel.setOnMenuSelected(contentController::handleMenuSelection);

        // Set initial card
        contentController.handleMenuSelection(CARD_TICKETING);
    }

    private void injectModules() {
        contentArea.add(new TicketingPanel(), CARD_TICKETING);
        contentArea.add(new SnacksPanel(), CARD_SNACKS);
        contentArea.add(new SearchShowtimePanel(), CARD_SEARCH);
        contentArea.add(new CustomerPanel(), CARD_CUSTOMERS);
        contentArea.add(new TransactionHistoryPanel(), CARD_TRANSACTIONS);
    }
}
