package com.f3cinema.app.ui.dashboard;

import javax.swing.*;
import java.awt.*;

/**
 * Common layout and styling for dashboard modules.
 */
public abstract class BaseDashboardModule extends JPanel {

    protected final String title;
    protected final String breadcrumb;
    protected JPanel contentBody;

    public BaseDashboardModule(String title, String breadcrumb) {
        this.title = title;
        this.breadcrumb = breadcrumb;
        setupUI();
    }

    private void setupUI() {
        // Remove module header to maximize content area
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        // Tighter padding so content fills more of the frame
        setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        // Glassmorphism body
        contentBody = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Deep Glass Background
                g2.setColor(new Color(30, 41, 59, 180)); // Slate 800 Translucent
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                
                // Inner Border Glow
                g2.setStroke(new BasicStroke(1.5f));
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 30), 
                             0, getHeight(), new Color(255, 255, 255, 5)));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 24, 24);
                
                g2.dispose();
            }
        };
        contentBody.setLayout(new BorderLayout());
        contentBody.setOpaque(false);
        contentBody.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(contentBody, BorderLayout.CENTER);
    }
}
