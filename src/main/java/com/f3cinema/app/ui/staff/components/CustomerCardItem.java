package com.f3cinema.app.ui.staff.components;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class CustomerCardItem extends JPanel {

    private static final Color TEXT_PRIMARY = new Color(0xF8FAFC);
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);

    public CustomerCardItem(String fullName, int points, String tierLabel) {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(260, 122));
        putClientProperty(FlatClientProperties.STYLE,
                "arc: 16; background: #1E293B; borderColor: #334155; borderWidth: 1");

        JLabel nameLabel = new JLabel(fullName == null || fullName.isBlank() ? "Khách hàng chưa đặt tên" : fullName);
        nameLabel.setFont(new Font("Inter", Font.BOLD, 15));
        nameLabel.setForeground(TEXT_PRIMARY);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JLabel tierBadge = new JLabel(tierLabel);
        tierBadge.setFont(new Font("Inter", Font.BOLD, 12));
        tierBadge.setForeground(Color.WHITE);
        tierBadge.setBorder(new EmptyBorder(6, 10, 6, 10));
        tierBadge.putClientProperty(FlatClientProperties.STYLE, "arc: 999; background: " + getTierColor(tierLabel));

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        JLabel pointsLabel = new JLabel(numberFormat.format(Math.max(points, 0)) + " điểm");
        pointsLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        pointsLabel.setForeground(TEXT_SECONDARY);
        pointsLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        bottom.add(tierBadge, BorderLayout.WEST);
        bottom.add(pointsLabel, BorderLayout.EAST);

        add(nameLabel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private String getTierColor(String tierLabel) {
        if ("Thành viên Vàng".equals(tierLabel)) {
            return "#F59E0B";
        }
        if ("Thành viên Bạc".equals(tierLabel)) {
            return "#64748B";
        }
        if ("Thành viên Đồng".equals(tierLabel)) {
            return "#92400E";
        }
        return "#475569";
    }
}
