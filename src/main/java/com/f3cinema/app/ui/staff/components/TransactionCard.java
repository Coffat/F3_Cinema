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
        setLayout(new BorderLayout(10, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        // JPanel: avoid borderWidth in STYLE (FlatPanelUI); arc + background only
        putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #1E293B");

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);
        leftCol.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel inv = new JLabel("#" + row.invoiceId());
        inv.setFont(ThemeConfig.FONT_H3);
        inv.setForeground(ThemeConfig.TEXT_PRIMARY);
        inv.setAlignmentX(Component.LEFT_ALIGNMENT);

        String cust = row.customerName() == null ? "Khách lẻ" : row.customerName();
        String when = row.createdAt() != null ? row.createdAt().format(FMT) : "N/A";
        JLabel line2 = new JLabel(cust + "  ·  " + when);
        line2.setFont(ThemeConfig.FONT_SMALL);
        line2.setForeground(ThemeConfig.TEXT_MUTED);
        line2.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftCol.add(inv);
        leftCol.add(Box.createVerticalStrut(2));
        leftCol.add(line2);

        String nv = safe(row.staffName());
        String pay = safe(row.paymentStatus() != null ? row.paymentStatus().name() : null);
        String st = safe(row.invoiceStatus() != null ? row.invoiceStatus().name() : null);
        JLabel metaLine = new JLabel(String.format("NV: %s  ·  Payment: %s  ·  %s", nv, pay, st));
        metaLine.setFont(ThemeConfig.FONT_SMALL);
        metaLine.setForeground(ThemeConfig.TEXT_SECONDARY);
        metaLine.setBorder(new EmptyBorder(0, 8, 0, 8));

        JLabel amount = new JLabel(MONEY.format(row.totalAmount()) + " đ");
        amount.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
        amount.setForeground(ThemeConfig.TEXT_SUCCESS);

        JLabel status = new JLabel(safe(row.invoiceStatus() != null ? row.invoiceStatus().name() : null));
        status.setForeground(statusColor(status.getText()));
        status.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD));
        status.putClientProperty(FlatClientProperties.STYLE, "arc: 6; background: #0F172A");
        status.setBorder(new EmptyBorder(2, 6, 2, 6));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        actions.add(amount);
        actions.add(status);
        actions.add(actionBtn("View", onView));
        actions.add(actionBtn("Print", onPrint));
        JButton refund = actionBtn("Refund", onRefund);
        refund.setForeground(ThemeConfig.TEXT_DANGER);
        actions.add(refund);

        add(leftCol, BorderLayout.WEST);
        add(metaLine, BorderLayout.CENTER);
        add(actions, BorderLayout.EAST);
    }

    private JButton actionBtn(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(ThemeConfig.FONT_SMALL);
        b.setMargin(new Insets(3, 8, 3, 8));
        b.putClientProperty(FlatClientProperties.STYLE, "arc: 6; background: #334155; borderWidth: 0;");
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
