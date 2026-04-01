package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.dto.StockReceiptSummaryDTO;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class StockReceiptCard extends JPanel {
    public StockReceiptCard(StockReceiptSummaryDTO receipt, int index, Runnable onViewDetail) {
        setLayout(new BorderLayout(12, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        add(buildLeft(receipt), BorderLayout.WEST);
        add(buildCenter(receipt), BorderLayout.CENTER);
        add(buildRight(index, onViewDetail), BorderLayout.EAST);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, 16, 16);
        g2.setColor(ThemeConfig.BG_CARD);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        g2.setColor(ThemeConfig.BORDER_COLOR);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.dispose();
        super.paintComponent(g);
    }

    private JPanel buildLeft(StockReceiptSummaryDTO r) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        JLabel d = new JLabel(r.receiptDate() != null ? r.receiptDate().format(DateTimeFormatter.ofPattern("dd/MM")) : "N/A");
        d.setFont(ThemeConfig.FONT_H3);
        d.setForeground(ThemeConfig.TEXT_PRIMARY);
        p.add(d);
        return p;
    }

    private JPanel buildCenter(StockReceiptSummaryDTO r) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel title = new JLabel("Phieu #" + r.id());
        title.setFont(ThemeConfig.FONT_H3);
        title.setForeground(ThemeConfig.TEXT_PRIMARY);
        JLabel supplier = new JLabel("Nha cung cap: " + r.supplier());
        supplier.setFont(ThemeConfig.FONT_SMALL);
        supplier.setForeground(ThemeConfig.TEXT_SECONDARY);
        JLabel total = new JLabel(new DecimalFormat("#,##0").format(r.totalImportCost()) + " đ");
        total.setFont(ThemeConfig.FONT_BODY);
        total.setForeground(ThemeConfig.TEXT_SUCCESS);
        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(supplier);
        p.add(Box.createVerticalStrut(4));
        p.add(total);
        return p;
    }

    private JPanel buildRight(int index, Runnable onViewDetail) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JLabel status = new JLabel(statusByIndex(index));
        status.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #0F172A;");
        status.setForeground(statusColor(index));
        status.setBorder(new EmptyBorder(4, 8, 4, 8));
        status.setFont(ThemeConfig.FONT_SMALL);
        JButton detail = new JButton("Xem");
        detail.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #334155;");
        detail.addActionListener(e -> onViewDetail.run());
        p.add(status, BorderLayout.NORTH);
        p.add(detail, BorderLayout.SOUTH);
        return p;
    }

    private String statusByIndex(int i) {
        if (i == 0) return "DRAFT";
        if (i == 1) return "APPROVED";
        return "COMPLETED";
    }

    private Color statusColor(int i) {
        if (i == 0) return ThemeConfig.TEXT_SECONDARY;
        if (i == 1) return ThemeConfig.ACCENT_COLOR;
        return ThemeConfig.TEXT_SUCCESS;
    }
}
