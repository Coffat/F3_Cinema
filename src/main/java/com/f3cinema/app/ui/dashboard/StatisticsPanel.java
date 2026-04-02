package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class StatisticsPanel extends BaseDashboardModule {
    public StatisticsPanel() {
        super("Thống kê", "Home > Statistics");
        initUI();
    }

    private void initUI() {
        contentBody.setLayout(new BorderLayout(0, 16));
        contentBody.setBackground(ThemeConfig.BG_MAIN);
        contentBody.add(buildTopFilters(), BorderLayout.NORTH);
        contentBody.add(buildMain(), BorderLayout.CENTER);
    }

    private JPanel buildTopFilters() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));
        JPanel control = new JPanel(new BorderLayout(12, 0));
        control.setBackground(ThemeConfig.BG_CARD);
        control.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        control.putClientProperty(FlatClientProperties.STYLE, "arc: 20");
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        String[] presets = {"Hom nay", "Hom qua", "Tuan nay", "Thang nay", "30 ngay"};
        for (String preset : presets) {
            JButton btn = new JButton(preset);
            btn.setForeground(ThemeConfig.TEXT_PRIMARY);
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "arc: 12; background: #1E293B; foreground: #F8FAFC; borderWidth: 0;");
            left.add(btn);
        }
        JComboBox<String> range = new JComboBox<>(new String[]{"Tuy chinh", "7 ngay", "30 ngay", "Quy nay", "Nam nay"});
        JComboBox<String> metric = new JComboBox<>(new String[]{"Doanh thu", "Ve ban", "Ti le lap day"});
        JButton exportPdf = new JButton("Export PDF");
        JButton exportExcel = new JButton("Export Excel");
        JButton print = new JButton("Print");
        exportPdf.setForeground(ThemeConfig.TEXT_PRIMARY);
        exportExcel.setForeground(ThemeConfig.TEXT_PRIMARY);
        print.setForeground(ThemeConfig.TEXT_PRIMARY);
        exportPdf.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; background: #334155; foreground: #F8FAFC; borderWidth: 0;");
        exportExcel.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; background: #334155; foreground: #F8FAFC; borderWidth: 0;");
        print.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; background: #334155; foreground: #F8FAFC; borderWidth: 0;");
        JCheckBox compare = new JCheckBox("So sanh ky truoc");
        compare.setOpaque(false);
        compare.setForeground(ThemeConfig.TEXT_SECONDARY);
        left.add(range);
        left.add(metric);
        left.add(compare);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(exportPdf);
        right.add(exportExcel);
        right.add(print);
        control.add(left, BorderLayout.WEST);
        control.add(right, BorderLayout.EAST);
        top.add(control, BorderLayout.CENTER);
        return top;
    }

    private JPanel buildMain() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(0, 24, 16, 24));
        main.add(buildStats());
        main.add(Box.createVerticalStrut(16));
        main.add(buildRevenueChartsRow());
        main.add(Box.createVerticalStrut(16));
        main.add(buildMoviePerformanceRow());
        main.add(Box.createVerticalStrut(16));
        main.add(buildCustomerAnalyticsRow());
        JScrollPane scroll = new JScrollPane(main);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(ThemeConfig.BG_MAIN);
        return wrap(scroll);
    }

    private JPanel wrap(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.add(stat("Doanh thu", "1.24B", ThemeConfig.TEXT_SUCCESS));
        row.add(stat("Ve da ban", "18,420", ThemeConfig.ACCENT_COLOR));
        row.add(stat("Lap day TB", "72%", ThemeConfig.TEXT_PRIMARY));
        row.add(stat("Khach moi", "1,281", ThemeConfig.TEXT_SECONDARY));
        return row;
    }

    private JPanel stat(String title, String value, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(ThemeConfig.BG_CARD);
        p.putClientProperty(FlatClientProperties.STYLE, "arc: 16");
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JLabel v = new JLabel(value);
        v.setFont(ThemeConfig.FONT_H1);
        v.setForeground(color);
        JLabel t = new JLabel(title);
        t.setFont(ThemeConfig.FONT_SMALL);
        t.setForeground(ThemeConfig.TEXT_SECONDARY);
        p.add(v, BorderLayout.CENTER);
        p.add(t, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildRevenueChartsRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.gridy = 0;
        c.gridx = 0; c.weightx = 0.70; c.insets = new Insets(0, 0, 0, 8);
        row.add(chartCard("Xu huong doanh thu"), c);
        c.gridx = 1; c.weightx = 0.30; c.insets = new Insets(0, 8, 0, 0);
        row.add(chartCard("Nguon doanh thu"), c);
        return row;
    }

    private JPanel buildMoviePerformanceRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        row.add(chartCard("Top phim doanh thu"));
        row.add(chartCard("Genre distribution"));
        return row;
    }

    private JPanel buildCustomerAnalyticsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        row.add(chartCard("New vs Returning"));
        row.add(chartCard("Tier distribution"));
        row.add(chartCard("Top spenders"));
        return row;
    }

    private JPanel chartCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ThemeConfig.BG_CARD);
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16");
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel t = new JLabel(title);
        t.setFont(ThemeConfig.FONT_H3);
        t.setForeground(ThemeConfig.TEXT_PRIMARY);
        JLabel ph = new JLabel("Chart area", SwingConstants.CENTER);
        ph.setForeground(ThemeConfig.TEXT_MUTED);
        ph.setFont(ThemeConfig.FONT_BODY);
        card.add(t, BorderLayout.NORTH);
        card.add(ph, BorderLayout.CENTER);
        return card;
    }
}
