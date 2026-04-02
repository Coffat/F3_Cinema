package com.f3cinema.app.ui.common.dialog;

import com.f3cinema.app.config.ThemeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public abstract class BaseAppDialog extends JDialog {
    private static final int DEFAULT_PAD_TOP = 28;
    private static final int DEFAULT_PAD_LEFT = 36;
    private static final int DEFAULT_PAD_BOTTOM = 28;
    private static final int DEFAULT_PAD_RIGHT = 36;

    protected BaseAppDialog(Window owner, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
    }

    protected final void setupBaseDialog(int width, int height) {
        setUndecorated(true);
        setSize(width, height);
        setLocationRelativeTo(getOwner());
        setBackground(new Color(0, 0, 0, 0));
    }

    /** Giống setupBaseDialog nhưng không setSize — dùng pack() sau khi dựng nội dung. */
    protected final void setupUndecoratedNoFixedSize() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
    }

    protected final JPanel createSurfacePanel(int paddingTop, int paddingLeft, int paddingBottom, int paddingRight) {
        JPanel surface = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59, 230));
                g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 24, 24);
                g2.setStroke(new BasicStroke(1.2f));
                g2.setColor(new Color(255, 255, 255, 25));
                g2.drawRoundRect(0, 0, getWidth() - 7, getHeight() - 7, 24, 24);
                g2.setColor(ThemeConfig.ACCENT_COLOR);
                g2.fillRoundRect(0, 0, getWidth() - 6, 4, 4, 4);
                g2.dispose();
            }
        };
        surface.setOpaque(false);
        surface.setBorder(new EmptyBorder(paddingTop, paddingLeft, paddingBottom, paddingRight));
        return surface;
    }

    protected final JPanel createSurfacePanel() {
        return createSurfacePanel(DEFAULT_PAD_TOP, DEFAULT_PAD_LEFT, DEFAULT_PAD_BOTTOM, DEFAULT_PAD_RIGHT);
    }
}
