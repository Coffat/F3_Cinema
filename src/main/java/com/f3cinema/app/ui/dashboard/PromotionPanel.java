package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PromotionPanel extends BaseDashboardModule {
    private final List<PromotionCard.PromotionItem> promotions = new ArrayList<>();
    private JPanel cards;
    private JLabel statActive;
    private JLabel statUsage;
    private JLabel statDiscount;
    private JLabel statRoi;

    public PromotionPanel() {
        super("Khuyến mãi", "Home > Promotions");
        seedData();
        initUI();
    }

    private void initUI() {
        contentBody.setLayout(new BorderLayout(0, 16));
        contentBody.setBackground(ThemeConfig.BG_MAIN);
        contentBody.add(buildStatsBar(), BorderLayout.NORTH);
        contentBody.add(buildGrid(), BorderLayout.CENTER);
        refreshStats();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 24, 8, 24));
        JPanel control = new JPanel(new BorderLayout(12, 0));
        control.setBackground(ThemeConfig.BG_CARD);
        control.putClientProperty(FlatClientProperties.STYLE, "arc: 20");
        control.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JTextField search = new JTextField();
        search.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo mã KM / tên...");
        search.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #0F172A;");
        JComboBox<String> status = new JComboBox<>(new String[]{"Tất cả", "Đang chạy", "Sắp diễn ra", "Đã kết thúc"});
        status.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #0F172A;");
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(search);
        left.add(status);
        JButton add = new JButton("Thêm khuyến mãi");
        add.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #6366F1; borderWidth: 0;");
        add.addActionListener(e -> new PromotionDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true));
        control.add(left, BorderLayout.WEST);
        control.add(add, BorderLayout.EAST);
        toolbar.add(control, BorderLayout.CENTER);
        return toolbar;
    }

    private JPanel buildStatsBar() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 24, 0, 24));
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        statActive = statVal();
        statUsage = statVal();
        statDiscount = statVal();
        statRoi = statVal();
        row.add(statCard("Khuyến mãi", statActive, "icons/gift.svg"));
        row.add(statCard("Lượt sử dụng", statUsage, "icons/user-check.svg"));
        row.add(statCard("Tổng giảm giá", statDiscount, "icons/dollar.svg"));
        row.add(statCard("ROI", statRoi, "icons/trending-up.svg"));
        wrapper.add(row, BorderLayout.NORTH);
        wrapper.add(buildToolbar(), BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel statCard(String label, JLabel value, String iconPath) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(ThemeConfig.BG_CARD);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16");
        
        JPanel iconWrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color iconBg = new Color(ThemeConfig.ACCENT_COLOR.getRed(), ThemeConfig.ACCENT_COLOR.getGreen(), ThemeConfig.ACCENT_COLOR.getBlue(), 20);
                g2.setColor(iconBg);
                g2.fillRoundRect(0, 0, 48, 48, 12, 12);
                
                g2.dispose();
            }
        };
        iconWrapper.setOpaque(false);
        iconWrapper.setPreferredSize(new Dimension(48, 48));
        iconWrapper.setLayout(new GridBagLayout());
        
        JLabel icon = new JLabel(new com.formdev.flatlaf.extras.FlatSVGIcon(iconPath, 24, 24));
        icon.setForeground(ThemeConfig.ACCENT_COLOR);
        iconWrapper.add(icon);
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeConfig.FONT_SMALL);
        lbl.setForeground(ThemeConfig.TEXT_SECONDARY);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(value);
        text.add(lbl);
        card.add(iconWrapper, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JLabel statVal() {
        JLabel l = new JLabel("0");
        l.setFont(ThemeConfig.FONT_H2);
        l.setForeground(ThemeConfig.TEXT_PRIMARY);
        return l;
    }

    private JComponent buildGrid() {
        cards = new JPanel(new com.f3cinema.app.util.WrapLayout(FlowLayout.LEFT, 24, 24));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(0, 24, 16, 24));
        for (PromotionCard.PromotionItem p : promotions) {
            cards.add(new PromotionCard(p,
                    () -> new PromotionDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true),
                    () -> AppMessageDialogs.showInfo(this, "Đã nhân bản " + p.code()),
                    () -> AppMessageDialogs.showInfo(this, "Đã tắt " + p.code())));
        }
        JScrollPane scroll = new JScrollPane(cards);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(ThemeConfig.BG_MAIN);
        return scroll;
    }

    private void seedData() {
        promotions.clear();
        promotions.add(new PromotionCard.PromotionItem("Giảm 25% combo bắp nước", "SUMMER25", "PERCENTAGE", "ACTIVE", "25% (max 50,000đ)", LocalDate.now().minusDays(3), LocalDate.now().plusDays(12), 67, 100));
        promotions.add(new PromotionCard.PromotionItem("Giảm 40k cho tài khoản mới", "NEWUSER", "FIXED_AMOUNT", "ACTIVE", "40,000đ", LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), 45, 150));
        promotions.add(new PromotionCard.PromotionItem("Happy hour mua 2 tặng 1", "FLASH50", "BUY_X_GET_Y", "SCHEDULED", "Buy 2 Get 1", LocalDate.now().plusDays(2), LocalDate.now().plusDays(14), 0, 80));
        promotions.add(new PromotionCard.PromotionItem("Đổi điểm lấy voucher", "POINT500", "POINT_REDEMPTION", "EXPIRED", "500 điểm -> 30k", LocalDate.now().minusDays(30), LocalDate.now().minusDays(3), 78, 80));
    }

    private void refreshStats() {
        long active = promotions.stream().filter(p -> "ACTIVE".equals(p.status())).count();
        int usage = promotions.stream().mapToInt(PromotionCard.PromotionItem::used).sum();
        statActive.setText(String.valueOf(active));
        statUsage.setText(String.valueOf(usage));
        statDiscount.setText("12.5M");
        statRoi.setText("+24.5%");
    }
}
