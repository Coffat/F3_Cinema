package com.f3cinema.app.ui.staff.components;

import com.f3cinema.app.config.ThemeConfig;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class CustomerCardItem extends JPanel {

    public CustomerCardItem(String fullName, int points, String tierLabel) {
        this(fullName, "-", points, tierLabel);
    }

    public CustomerCardItem(String fullName, String phone, int points, String tierLabel) {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setPreferredSize(new Dimension(285, 188));
        setBorder(new EmptyBorder(14, 14, 14, 14));
        putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B; borderWidth: 1; borderColor: #334155");

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel tierBadge = createTierBadge(tierLabel);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        JLabel pointsLabel = new JLabel(numberFormat.format(Math.max(points, 0)) + " pts");
        pointsLabel.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD));
        pointsLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        top.add(tierBadge, BorderLayout.WEST);
        top.add(pointsLabel, BorderLayout.EAST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel nameLabel = new JLabel(fullName == null || fullName.isBlank() ? "Khach hang chua dat ten" : fullName);
        nameLabel.setFont(ThemeConfig.FONT_H3);
        nameLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        JLabel phoneLabel = new JLabel(phone == null || phone.isBlank() ? "-" : phone);
        phoneLabel.setFont(ThemeConfig.FONT_BODY);
        phoneLabel.setForeground(ThemeConfig.TEXT_SECONDARY);
        center.add(nameLabel);
        center.add(Box.createVerticalStrut(4));
        center.add(phoneLabel);
        center.add(Box.createVerticalStrut(10));
        center.add(buildTierProgress(points, tierLabel));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        actions.add(actionButton("Lich su"));
        actions.add(actionButton("Tich diem"));
        actions.add(actionButton("Sua"));

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private JButton actionButton(String text) {
        JButton b = new JButton(text);
        b.setFont(ThemeConfig.FONT_SMALL);
        b.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #334155; borderWidth: 0;");
        b.setForeground(ThemeConfig.TEXT_PRIMARY);
        return b;
    }

    private JLabel createTierBadge(String tierLabel) {
        JLabel badge = new JLabel(normalizeTier(tierLabel));
        badge.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD));
        badge.setForeground(Color.WHITE);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badge.putClientProperty(FlatClientProperties.STYLE, "arc: 999; background: " + tierColor(normalizeTier(tierLabel)));
        return badge;
    }

    private JComponent buildTierProgress(int points, String tierLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        int next = nextTierPoint(normalizeTier(tierLabel));
        if (next <= 0) {
            JLabel max = new JLabel("Da dat hang cao nhat");
            max.setFont(ThemeConfig.FONT_SMALL);
            max.setForeground(ThemeConfig.TEXT_SECONDARY);
            panel.add(max);
            return panel;
        }
        JProgressBar bar = new JProgressBar(0, next);
        bar.setValue(Math.min(points, next));
        bar.setForeground(Color.decode(tierColor(normalizeTier(tierLabel))));
        bar.setBackground(new Color(100, 100, 100, 45));
        bar.setStringPainted(false);
        JLabel lbl = new JLabel("Con " + Math.max(0, next - points) + " diem de len hang");
        lbl.setFont(ThemeConfig.FONT_SMALL);
        lbl.setForeground(ThemeConfig.TEXT_SECONDARY);
        panel.add(bar);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lbl);
        return panel;
    }

    private String normalizeTier(String tierLabel) {
        if (tierLabel == null) return "MEMBER";
        if (tierLabel.contains("Vang")) return "GOLD";
        if (tierLabel.contains("Bac")) return "SILVER";
        if (tierLabel.contains("Dong")) return "MEMBER";
        return tierLabel.toUpperCase(Locale.ROOT);
    }

    private int nextTierPoint(String tier) {
        return switch (tier) {
            case "MEMBER" -> 1000;
            case "SILVER" -> 5000;
            case "GOLD" -> 10000;
            default -> -1;
        };
    }

    private String tierColor(String tier) {
        return switch (tier) {
            case "SILVER" -> "#C0C0C0";
            case "GOLD" -> "#F59E0B";
            case "PLATINUM" -> "#8B5CF6";
            default -> "#64748B";
        };
    }
}
