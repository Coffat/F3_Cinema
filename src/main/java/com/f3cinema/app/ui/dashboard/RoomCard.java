package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.entity.Room;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoomCard extends JPanel {

    private final Room room;
    private final int totalSeats;
    private final Runnable onEdit;
    private final Runnable onDelete;
    private final Runnable onViewMap;

    private boolean isHovered = false;
    private double hoverAnim = 0.0;
    private Timer animTimer;

    private static final Color C_CARD_BG = new Color(30, 41, 59, 220);
    private static final Color C_ACCENT = Color.decode("#6366F1");
    private static final Color C_TEXT_MAIN = Color.decode("#F8FAFC");
    private static final Color C_TEXT_SUB = Color.decode("#94A3B8");

    public RoomCard(Room room, int totalSeats, Runnable onEdit, Runnable onDelete, Runnable onViewMap) {
        this.room = room;
        this.totalSeats = totalSeats;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onViewMap = onViewMap;
        initUI();
    }

    private void initUI() {
        setPreferredSize(new Dimension(260, 170));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        animTimer = new Timer(16, e -> {
            double target = isHovered ? 1.0 : 0.0;
            hoverAnim += (target - hoverAnim) * 0.15;
            if (Math.abs(target - hoverAnim) < 0.01) {
                hoverAnim = target;
                animTimer.stop();
            }
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { isHovered = true; if (!animTimer.isRunning()) animTimer.start(); }
            @Override public void mouseExited(MouseEvent e) { isHovered = false; if (!animTimer.isRunning()) animTimer.start(); }
            @Override public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) showContextMenu(e);
                else onViewMap.run();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        double currentScale = 1.0 + (0.03 * hoverAnim);

        if (hoverAnim > 0) {
            double tx = (w * (currentScale - 1.0)) / 2.0;
            double ty = (h * (currentScale - 1.0)) / 2.0;
            g2.translate(-tx, -ty);
            g2.scale(currentScale, currentScale);
        }

        g2.setColor(C_CARD_BG);
        g2.fillRoundRect(2, 2, w - 4, h - 4, 20, 20);

        // Header Background Stripe
        g2.setColor(new Color(99, 102, 241, 40));
        g2.fillRoundRect(2, 2, w - 4, 50, 20, 20);
        g2.fillRect(2, 30, w - 4, 22);

        // Border Glow
        g2.setStroke(new BasicStroke((float)(1.2 + hoverAnim)));
        Color borderStart = new Color(255, 255, 255, 30);
        int r = (int)(borderStart.getRed() + (C_ACCENT.getRed() - borderStart.getRed()) * hoverAnim);
        int cg = (int)(borderStart.getGreen() + (C_ACCENT.getGreen() - borderStart.getGreen()) * hoverAnim);
        int b = (int)(borderStart.getBlue() + (C_ACCENT.getBlue() - borderStart.getBlue()) * hoverAnim);
        int a = (int)(borderStart.getAlpha() + (C_ACCENT.getAlpha() - borderStart.getAlpha()) * hoverAnim);
        g2.setColor(new Color(r, cg, b, a));
        g2.drawRoundRect(2, 2, w - 4, h - 4, 20, 20);

        // Content
        g2.setColor(C_TEXT_MAIN);
        g2.setFont(new Font("Inter", Font.BOLD, 16));
        g2.drawString(room.getName(), 16, 32);

        g2.setFont(new Font("Inter", Font.PLAIN, 13));
        g2.setColor(C_TEXT_SUB);
        g2.drawString("Loại: " + room.getRoomType().getLabel(), 16, 80);
        g2.drawString("Tổng số ghế: " + totalSeats, 16, 105);
        
        g2.setFont(new Font("Inter", Font.ITALIC, 11));
        g2.setColor(new Color(99, 102, 241, 150));
        g2.drawString("• Chuột trái: Sắp xếp ghế", 16, 135);
        g2.drawString("• Chuột phải: Menu tùy chọn", 16, 152);

        g2.dispose();
    }

    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        menu.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
        JMenuItem mapItem = new JMenuItem("Sơ đồ ghế"); mapItem.addActionListener(al -> onViewMap.run());
        JMenuItem editItem = new JMenuItem("Sửa"); editItem.addActionListener(al -> onEdit.run());
        JMenuItem deleteItem = new JMenuItem("Xóa"); deleteItem.setForeground(Color.decode("#F43F5E")); 
        deleteItem.addActionListener(al -> onDelete.run());
        
        menu.add(mapItem); menu.addSeparator(); menu.add(editItem); menu.add(deleteItem);
        menu.show(this, e.getX(), e.getY());
    }
}
