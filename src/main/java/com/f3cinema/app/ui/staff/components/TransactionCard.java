package com.f3cinema.app.ui.staff.components;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.dto.transaction.TransactionRowDTO;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class TransactionCard extends JPanel {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");

    public TransactionCard(TransactionRowDTO row, Runnable onView, Runnable onPrint, Runnable onRefund) {
        setLayout(new BorderLayout(12, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 152));
        putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B; borderWidth: 1; borderColor: #334155");

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(230, 0));
        JLabel inv = new JLabel("#" + row.invoiceId());
        inv.setFont(ThemeConfig.FONT_H3);
        inv.setForeground(ThemeConfig.TEXT_PRIMARY);
        JLabel customer = new JLabel(row.customerName() == null ? "Khach le" : row.customerName());
        customer.setFont(ThemeConfig.FONT_BODY);
        customer.setForeground(ThemeConfig.TEXT_SECONDARY);
        JLabel date = new JLabel(row.createdAt() != null ? row.createdAt().format(FMT) : "N/A");
        date.setFont(ThemeConfig.FONT_SMALL);
        date.setForeground(ThemeConfig.TEXT_MUTED);
        left.add(inv);
        left.add(Box.createVerticalStrut(4));
        left.add(customer);
        left.add(Box.createVerticalStrut(4));
        left.add(date);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.add(meta("NV: " + safe(row.staffName())));
        center.add(Box.createVerticalStrut(4));
        center.add(meta("Payment: " + safe(row.paymentStatus() != null ? row.paymentStatus().name() : null)));
        center.add(Box.createVerticalStrut(4));
        center.add(meta("Status: " + safe(row.invoiceStatus() != null ? row.invoiceStatus().name() : null)));

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        JLabel amount = new JLabel(MONEY.format(row.totalAmount()) + " đ");
        amount.setFont(ThemeConfig.FONT_H2);
        amount.setForeground(ThemeConfig.TEXT_SUCCESS);
        amount.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel status = new JLabel(safe(row.invoiceStatus() != null ? row.invoiceStatus().name() : null));
        status.setAlignmentX(Component.RIGHT_ALIGNMENT);
        status.setForeground(statusColor(status.getText()));
        status.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD));
        status.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #0F172A");
        status.setBorder(new EmptyBorder(4, 8, 4, 8));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JButton view = actionBtn("View", onView);
        JButton print = actionBtn("Print", onPrint);
        JButton refund = actionBtn("Refund", onRefund);
        refund.setForeground(ThemeConfig.TEXT_DANGER);
        actions.add(view);
        actions.add(print);
        actions.add(refund);
        right.add(amount);
        right.add(Box.createVerticalStrut(8));
        right.add(status);
        right.add(Box.createVerticalGlue());
        right.add(actions);

        add(left, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }

    private JLabel meta(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeConfig.FONT_SMALL);
        l.setForeground(ThemeConfig.TEXT_SECONDARY);
        return l;
    }

    private JButton actionBtn(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(ThemeConfig.FONT_SMALL);
        b.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #334155; borderWidth: 0;");
        b.addActionListener(e -> action.run());
        return b;
    }

    private String safe(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }

    private Color statusColor(String status) {
        if ("PAID".equals(status)) return ThemeConfig.TEXT_SUCCESS;
        if ("PENDING".equals(status)) return Color.decode("#F59E0B");
        if ("CANCELLED".equals(status)) return ThemeConfig.TEXT_DANGER;
        return ThemeConfig.TEXT_SECONDARY;
    }
}
