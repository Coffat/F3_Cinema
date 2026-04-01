package com.f3cinema.app.ui.dashboard.timeline;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.entity.Room;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.f3cinema.app.ui.dashboard.timeline.TimelineConstants.*;

/**
 * Vertical sidebar listing room names, used as JScrollPane's rowHeaderView
 * to stay synchronized with the timeline scroll position.
 */
public class RoomSidebar extends JPanel {

    public RoomSidebar() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG_MAIN);
        setOpaque(true);
    }

    public void setRooms(List<Room> rooms) {
        removeAll();
        for (int i = 0; i < rooms.size(); i++) {
            add(createRoomCell(rooms.get(i), i));
        }
        revalidate();
        repaint();
    }

    private JPanel createRoomCell(Room room, int index) {
        JPanel cell = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Bottom separator
                g2.setColor(ROW_SEPARATOR);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);

                // Right border accent line
                g2.setColor(BG_ELEVATED);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());

                g2.dispose();
            }
        };
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBackground(index % 2 == 0 ? BG_MAIN : BG_ROW_ALT);
        cell.setOpaque(true);
        cell.setPreferredSize(new Dimension(SIDEBAR_WIDTH, ROW_HEIGHT));
        cell.setMaximumSize(new Dimension(SIDEBAR_WIDTH, ROW_HEIGHT));
        cell.setMinimumSize(new Dimension(SIDEBAR_WIDTH, ROW_HEIGHT));
        cell.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 12));

        cell.add(Box.createVerticalGlue());

        JLabel icon = new JLabel(roomTypeIcon(room));
        icon.setFont(new Font("Inter", Font.PLAIN, 13));
        icon.setForeground(ThemeConfig.TEXT_SECONDARY);
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);
        cell.add(icon);
        cell.add(Box.createVerticalStrut(2));

        JLabel nameLabel = new JLabel(room.getName());
        nameLabel.setFont(FONT_ROOM_NAME);
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cell.add(nameLabel);

        cell.add(Box.createVerticalStrut(2));

        JLabel typeLabel = new JLabel(room.getRoomType().getLabel());
        typeLabel.setFont(FONT_ROOM_TYPE);
        typeLabel.setForeground(TEXT_MUTED);
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cell.add(typeLabel);

        int seats = room.getSeats() != null ? room.getSeats().size() : 0;
        JLabel capacity = new JLabel(seats > 0 ? seats + " seats" : "N/A seats");
        capacity.setFont(new Font("Inter", Font.PLAIN, 10));
        capacity.setForeground(ThemeConfig.TEXT_MUTED);
        capacity.setAlignmentX(Component.LEFT_ALIGNMENT);
        cell.add(capacity);

        cell.add(Box.createVerticalGlue());

        return cell;
    }

    private String roomTypeIcon(Room room) {
        if (room.getRoomType() == null) return "■";
        return switch (room.getRoomType().name()) {
            case "ROOM_IMAX" -> "⬢ IMAX";
            case "ROOM_3D" -> "★ 3D";
            default -> "▦ 2D";
        };
    }

    @Override
    public Dimension getPreferredSize() {
        int totalHeight = getComponentCount() * ROW_HEIGHT;
        return new Dimension(SIDEBAR_WIDTH, Math.max(totalHeight, ROW_HEIGHT));
    }
}
