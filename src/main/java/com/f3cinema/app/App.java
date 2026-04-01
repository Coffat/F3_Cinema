package com.f3cinema.app;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.ui.LoginFrame;
import javax.swing.SwingUtilities;

/**
 * F3 Cinema Management System — Application Entry Point.
 */
public class App {  
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. Initialize Modern Midnight theme BEFORE any UI is created
            ThemeConfig.setup();

            // 2. Seed Database with initial data (Disabled: using Docker init.sql instead)
            // com.f3cinema.app.config.DatabaseSeeder.seed();

            // 3. Launch Login Screen
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
