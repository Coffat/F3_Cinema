package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.entity.Room;
import com.f3cinema.app.entity.enums.RoomType;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

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
    private JButton editButton;
    private JButton mapButton;
    private JButton deleteButton;
    private FlatSVGIcon typeIcon;

    private static final Color C_CARD_BG = ThemeConfig.BG_CARD;
    private static final Color C_TEXT_MAIN = ThemeConfig.TEXT_PRIMARY;
    private static final Color C_TEXT_SUB = ThemeConfig.TEXT_SECONDARY;

    public RoomCard(Room room, int totalSeats, Runnable onEdit, Runnable onDelete, Runnable onViewMap) {
        this.room = room;
        this.totalSeats = totalSeats;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onViewMap = onViewMap;
        initUI();
    }

    private void initUI() {
        setLayout(null);
        setPreferredSize(new Dimension(280, 200));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        RoomVisual visual = getRoomVisual(room.getRoomType());
        typeIcon = new FlatSVGIcon(visual.iconPath(), 18, 18);
        typeIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> visual.accent()));

        animTimer = new Timer(16, e -> {
            double target = isHovered ? 1.0 : 0.0;
            hoverAnim += (target - hoverAnim) * 0.15;
            if (Math.abs(target - hoverAnim) < 0.01) {
                hoverAnim = target;
                animTimer.stop();
            }
            repaint();
            if (getParent() != null) {
                getParent().repaint(getX() - 20, getY() - 20, getWidth() + 40, getHeight() + 40);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { isHovered = true; if (!animTimer.isRunning()) animTimer.start(); }
            @Override public void mouseExited(MouseEvent e) { isHovered = false; if (!animTimer.isRunning()) animTimer.start(); }
            @Override public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) showContextMenu(e);
                else onViewMap.run();
            }
        });

        editButton = buildActionButton("Edit");
        editButton.setBounds(16, 158, 74, 30);
        editButton.addActionListener(e -> onEdit.run());
        add(editButton);

        mapButton = buildActionButton("Seat Map");
        mapButton.setBounds(100, 158, 90, 30);
        mapButton.addActionListener(e -> onViewMap.run());
        add(mapButton);

        deleteButton = buildActionButton("Delete");
        deleteButton.setBounds(200, 158, 74, 30);
        deleteButton.setForeground(ThemeConfig.TEXT_DANGER);
        deleteButton.addActionListener(e -> onDelete.run());
        add(deleteButton);
    }

    private JButton buildActionButton(String text) {
        JButton button = new JButton(text);
        button.setFocusable(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; borderWidth: 0; background: #0F172A; margin: 3,10,3,10;");
        button.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD));
        button.setForeground(ThemeConfig.TEXT_PRIMARY);
        return button;
    }

    private record RoomVisual(Color accent, String iconPath) {}

    private RoomVisual getRoomVisual(RoomType type) {
        if (type == RoomType.ROOM_IMAX) return new RoomVisual(Color.decode("#8B5CF6"), "icons/maximize.svg");
        if (type == RoomType.ROOM_3D) return new RoomVisual(Color.decode("#F59E0B"), "icons/star.svg");
        return new RoomVisual(ThemeConfig.TEXT_SECONDARY, "icons/grid.svg");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        RoomVisual visual = getRoomVisual(room.getRoomType());
        double currentScale = 1.0 + (0.02 * hoverAnim);

        if (hoverAnim > 0) {
            double tx = (w * (currentScale - 1.0)) / 2.0;
            double ty = (h * (currentScale - 1.0)) / 2.0;
            g2.translate(-tx, -ty);
            g2.scale(currentScale, currentScale);
        }

        g2.setColor(new Color(0, 0, 0, (int) (24 + 26 * hoverAnim)));
        g2.fillRoundRect(6, 8, w - 12, h - 12, 20, 20);
        g2.setColor(C_CARD_BG);
        g2.fillRoundRect(0, 0, w, h, 20, 20);

        g2.setColor(new Color(visual.accent().getRed(), visual.accent().getGreen(), visual.accent().getBlue(), 32));
        g2.fillRoundRect(0, 0, w, 52, 20, 20);
        g2.fillRect(0, 26, w, 26);

        g2.setStroke(new BasicStroke((float) (1.0 + hoverAnim)));
        g2.setColor(new Color(visual.accent().getRed(), visual.accent().getGreen(), visual.accent().getBlue(), 120));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);

        typeIcon.paintIcon(this, g2, 16, 17);
        g2.setColor(C_TEXT_MAIN);
        g2.setFont(ThemeConfig.FONT_H3);
        g2.drawString(room.getName(), 42, 31);

        g2.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD));
        g2.setColor(visual.accent());
        String typeLabel = room.getRoomType() != null ? room.getRoomType().getLabel() : "Unknown";
        g2.drawString(typeLabel, w - 104, 31);

        g2.setFont(ThemeConfig.FONT_BODY);
        g2.setColor(C_TEXT_SUB);
        g2.drawString("Capacity", 16, 78);
        g2.drawString(totalSeats + " / " + totalSeats + " seats", 206, 78);
        paintCapacityBar(g2, totalSeats, totalSeats, 16, 86, w - 32);

        g2.drawString("Status:", 16, 120);
        g2.setColor(ThemeConfig.TEXT_SUCCESS);
        g2.fillOval(64, 111, 8, 8);
        g2.drawString("Active", 78, 120);

        g2.dispose();
    }

    private void paintCapacityBar(Graphics2D g2, int current, int total, int x, int y, int width) {
        int height = 8;
        g2.setColor(new Color(100, 100, 100, 50));
        g2.fillRoundRect(x, y, width, height, 4, 4);
        int fillWidth = (int) ((current / (double) Math.max(1, total)) * width);
        g2.setColor(getCapacityColor(current, total));
        g2.fillRoundRect(x, y, fillWidth, height, 4, 4);
    }

    private Color getCapacityColor(int current, int total) {
        double ratio = current / (double) Math.max(1, total);
        if (ratio < 0.5) return ThemeConfig.TEXT_SUCCESS;
        if (ratio < 0.8) return new Color(255, 159, 10);
        return ThemeConfig.TEXT_DANGER;
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
