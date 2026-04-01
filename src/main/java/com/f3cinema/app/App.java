package com.f3cinema.app;

import com.f3cinema.app.config.HibernateUtil;
import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.ui.LoginFrame;
import javax.swing.SwingUtilities;

/**
 * F3 Cinema Management System — Application Entry Point.
 */
public class App {  
    public static void main(String[] args) {
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
