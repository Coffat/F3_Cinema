package com.f3cinema.app;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.ui.LoginFrame;
import com.formdev.flatlaf.FlatLaf;
import javax.swing.SwingUtilities;

/**
 * F3 Cinema Management System — Application Entry Point.
 */
public class App {
    public static void main(String[] args) {
        // Bypass HTTP 429 or 403 when loading images from Wikipedia/Wikimedia by setting a custom User-Agent
        System.setProperty("http.agent", "F3CinemaApp/1.0 (Java Swing; +https://github.com)");

        // Set FlatLaf default font before initializing ThemeConfig
        FlatLaf.setPreferredFontFamily("-apple-system");

        Thread.ofVirtual().start(() -> {
            HibernateUtil.getSessionFactory();
        });

        SwingUtilities.invokeLater(() -> {
            ThemeConfig.setup();

            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
