package com.f3cinema.app.ui.dashboard;

import javax.swing.*;
import java.awt.*;

/**
 * Common layout and styling for dashboard modules.
 */
public abstract class BaseDashboardModule extends JPanel {

    protected final String title;
    protected final String breadcrumb;

    public BaseDashboardModule(String title, String breadcrumb) {
        this.title = title;
        this.breadcrumb = breadcrumb;
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout(0, 24));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(32, 40, 40, 40));

        // 1. Header Section (Refined Hierarchy)
        JPanel headerPanel = new JPanel(new BorderLayout(12, 4));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 36)); // Larger, Bolder
        lblTitle.setForeground(Color.WHITE);

        JPanel breadcrumbContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        breadcrumbContainer.setOpaque(false);
        JLabel lblBreadcrumb = new JLabel(breadcrumb);
        lblBreadcrumb.setFont(new Font("Inter", Font.PLAIN, 13));
        lblBreadcrumb.setForeground(Color.decode("#6366F1")); // Indigo Accent
        breadcrumbContainer.add(lblBreadcrumb);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(breadcrumbContainer, BorderLayout.SOUTH);

        // 2. Pro Max Glassmorphism Body
        JPanel contentBody = new JPanel() {
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
        contentBody.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(headerPanel, BorderLayout.NORTH);
        add(contentBody, BorderLayout.CENTER);
    }
}
