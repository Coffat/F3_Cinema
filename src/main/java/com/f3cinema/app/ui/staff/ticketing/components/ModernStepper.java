package com.f3cinema.app.ui.staff.ticketing.components;

import com.f3cinema.app.config.ThemeConfig;

import javax.swing.*;
import java.awt.*;

public class ModernStepper extends JPanel {
    private final String[] stepLabels;
    private int currentStep = 1;

    public ModernStepper(String... labels) {
        this.stepLabels = labels;
        setOpaque(false);
        setPreferredSize(new Dimension(0, 74));
    }

    public void setCurrentStep(int step) {
        this.currentStep = Math.max(1, Math.min(step, stepLabels.length));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int count = stepLabels.length;
        int stepWidth = (getWidth() - 40) / Math.max(1, count);
        int y = 24;
        for (int i = 0; i < count; i++) {
            int x = 20 + i * stepWidth;
            if (i > 0) {
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(i + 1 <= currentStep ? ThemeConfig.ACCENT_COLOR : ThemeConfig.BORDER_COLOR);
                g2.drawLine(x - stepWidth + 32, y, x - 8, y);
            }
            Color circle = (i + 1 <= currentStep) ? ThemeConfig.ACCENT_COLOR : ThemeConfig.BORDER_COLOR;
            g2.setColor(circle);
            g2.fillOval(x - 16, y - 16, 32, 32);
            g2.setColor(Color.WHITE);
            g2.setFont(ThemeConfig.FONT_BODY);
            String text = (i + 1 < currentStep) ? "✓" : String.valueOf(i + 1);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, x - fm.stringWidth(text) / 2, y + fm.getAscent() / 2 - 1);
            g2.setColor(i + 1 <= currentStep ? ThemeConfig.TEXT_PRIMARY : ThemeConfig.TEXT_SECONDARY);
            g2.setFont(ThemeConfig.FONT_SMALL);
            fm = g2.getFontMetrics();
            g2.drawString(stepLabels[i], x - fm.stringWidth(stepLabels[i]) / 2, y + 30);
        }
        g2.dispose();
    }
}
