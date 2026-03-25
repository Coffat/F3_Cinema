package com.f3cinema.app.ui.admin;

import java.awt.CardLayout;
import javax.swing.JPanel;

/**
 * Controller for managing sidebar navigation events and content switching.
 */
public class SidebarController {

    private final JPanel contentArea;
    private final CardLayout cardLayout;

    public SidebarController(JPanel contentArea) {
        this.contentArea = contentArea;
        this.cardLayout = (CardLayout) contentArea.getLayout();
    }

    /**
     * Handles switching the view based on menu identifier.
     * @param menuId Unique ID of the menu item (e.g., DASHBOARD, MOVIES).
     */
    public void handleMenuSelection(String menuId) {
        // Zero-latency card switching
        cardLayout.show(contentArea, menuId);
    }
}
