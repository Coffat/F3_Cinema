package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.entity.Voucher;
import com.f3cinema.app.entity.enums.VoucherStatus;
import com.f3cinema.app.service.VoucherService;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PromotionPanel extends BaseDashboardModule {
    private final VoucherService voucherService = new VoucherService();
    private final List<PromotionCard.PromotionItem> promotions = new ArrayList<>();
    private JPanel cards;
    private JLabel statActive;
    private JLabel statUsage;
    private JLabel statDiscount;
    private JLabel statRoi;
    private final boolean isReadOnly;

    public PromotionPanel() {
        this(false);
    }
    
    public PromotionPanel(boolean isReadOnly) {
        super(isReadOnly ? "Danh sách Khuyến mãi" : "Quản lý Voucher", isReadOnly ? "Home > Promotions" : "Home > Vouchers");
        this.isReadOnly = isReadOnly;
        loadVouchersFromDatabase();
        initUI();
    }

    private void loadVouchersFromDatabase() {
        promotions.clear();
        try {
            List<Voucher> vouchers = voucherService.getAllVouchers();
            for (Voucher v : vouchers) {
                LocalDate startDate = v.getValidFrom().toLocalDate();
                LocalDate endDate = v.getValidUntil().toLocalDate();
                
                String status = mapVoucherStatus(v, startDate, endDate);
                String type = v.getVoucherType().name();
                String discountLabel = formatDiscountLabel(v);
                
                int used = v.getUsageCount() != null ? v.getUsageCount() : 0;
                int limit = v.getUsageLimit() != null ? v.getUsageLimit() : 0;
                
                promotions.add(new PromotionCard.PromotionItem(
                    v.getDescription() != null && !v.getDescription().isBlank() ? 
                        v.getDescription() : "Voucher " + v.getCode(),
                    v.getCode(),
                    type,
                    status,
                    discountLabel,
                    startDate,
                    endDate,
                    used,
                    limit
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String formatDiscountLabel(Voucher v) {
        return switch (v.getVoucherType()) {
            case PERCENTAGE -> {
                String label = v.getDiscountPercent() + "%";
                if (v.getMaxDiscount() != null) {
                    label += " (max " + formatMoney(v.getMaxDiscount().longValue()) + ")";
                }
                yield label;
            }
            case FIXED_AMOUNT -> 
                v.getDiscountAmount() != null ? formatMoney(v.getDiscountAmount().longValue()) : "0đ";
            case BUY_X_GET_Y -> 
                String.format("Mua %d tặng %d", 
                    v.getBuyQuantity() != null ? v.getBuyQuantity() : 0,
                    v.getGetQuantity() != null ? v.getGetQuantity() : 0);
            case COMBO_DISCOUNT -> {
                String label = v.getDiscountPercent() + "% combo";
                if (v.getMaxDiscount() != null) {
                    label += " (max " + formatMoney(v.getMaxDiscount().longValue()) + ")";
                }
                yield label;
            }
        };
    }
    
    private String mapVoucherStatus(Voucher v, LocalDate startDate, LocalDate endDate) {
        LocalDate now = LocalDate.now();
        if (v.getStatus() != VoucherStatus.ACTIVE) return "EXPIRED";
        if (now.isBefore(startDate)) return "SCHEDULED";
        if (now.isAfter(endDate)) return "EXPIRED";
        return "ACTIVE";
    }
    
    private String formatMoney(long amount) {
        if (amount >= 1000) {
            return (amount / 1000) + "k";
        }
        return amount + "đ";
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
        control.add(left, BorderLayout.WEST);

        if (!isReadOnly) {
            JButton add = new JButton("Thêm khuyến mãi");
            add.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #6366F1; borderWidth: 0;");
            add.setForeground(Color.WHITE);
            add.addActionListener(e -> new PromotionDialog(SwingUtilities.getWindowAncestor(this), () -> refreshVouchers()).setVisible(true));
            control.add(add, BorderLayout.EAST);
        }
        
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
        
        com.formdev.flatlaf.extras.FlatSVGIcon svgIcon = new com.formdev.flatlaf.extras.FlatSVGIcon(iconPath, 24, 24);
        svgIcon.setColorFilter(new com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter(c -> ThemeConfig.ACCENT_COLOR));
        JLabel icon = new JLabel(svgIcon);
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
        cards = new JPanel(new com.f3cinema.app.util.WrapLayout(FlowLayout.LEFT, 20, 20));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(0, 24, 16, 24));
        for (PromotionCard.PromotionItem p : promotions) {
            cards.add(new PromotionCard(p, isReadOnly,
                    () -> handleEdit(p),
                    () -> handleDuplicate(p),
                    () -> handleDeactivate(p)));
        }
        JScrollPane scroll = new JScrollPane(cards);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(ThemeConfig.BG_MAIN);
        return scroll;
    }
    
    private void handleEdit(PromotionCard.PromotionItem item) {
        try {
            Voucher voucher = voucherService.searchVouchers(item.code()).stream()
                .filter(v -> v.getCode().equals(item.code()))
                .findFirst()
                .orElse(null);
            
            if (voucher != null) {
                new PromotionDialog(SwingUtilities.getWindowAncestor(this), voucher, () -> refreshVouchers()).setVisible(true);
            }
        } catch (Exception e) {
            AppMessageDialogs.showError(this, "Không thể chỉnh sửa: " + e.getMessage());
        }
    }
    
    private void handleDuplicate(PromotionCard.PromotionItem item) {
        try {
            Voucher original = voucherService.searchVouchers(item.code()).stream()
                .filter(v -> v.getCode().equals(item.code()))
                .findFirst()
                .orElse(null);

            if (original == null) {
                AppMessageDialogs.showError(this, "Không tìm thấy voucher gốc.");
                return;
            }

            // Sinh mã mới: thêm "COPY_" + 4 ký tự cuối timestamp để tránh trùng
            String newCode = "COPY_" + original.getCode();
            if (newCode.length() > 45) {
                newCode = "CPY" + original.getCode().substring(0, Math.min(original.getCode().length(), 42));
            }
            // Đảm bảo unique bằng cách thêm số ngẫu nhiên nếu code đã tồn tại
            String finalCode = newCode;
            int attempt = 0;
            while (attempt < 100) {
                try {
                    voucherService.createVoucher(
                        finalCode,
                        (original.getDescription() != null ? original.getDescription() : "") + " (nhân bản)",
                        original.getVoucherType(),
                        original.getDiscountPercent(),
                        original.getDiscountAmount(),
                        original.getMaxDiscount(),
                        original.getMinOrderAmount(),
                        original.getBuyQuantity(),
                        original.getGetQuantity(),
                        original.getAppliesToCategory(),
                        original.getValidFrom(),
                        original.getValidUntil(),
                        original.getUsageLimit()
                    );
                    AppMessageDialogs.showInfo(this, "Thành công", "Đã nhân bản voucher thành: " + finalCode);
                    refreshVouchers();
                    return;
                } catch (IllegalArgumentException ex) {
                    if (ex.getMessage() != null && ex.getMessage().contains("đã tồn tại")) {
                        // Thử code khác
                        attempt++;
                        finalCode = newCode + attempt;
                    } else {
                        throw ex;
                    }
                }
            }
            AppMessageDialogs.showError(this, "Không thể nhân bản: mã voucher đã bị trùng quá nhiều lần.");
        } catch (Exception e) {
            AppMessageDialogs.showError(this, "Lỗi nhân bản: " + e.getMessage());
        }
    }
    
    private void handleDeactivate(PromotionCard.PromotionItem item) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn vô hiệu hóa voucher " + item.code() + "?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Voucher voucher = voucherService.searchVouchers(item.code()).stream()
                    .filter(v -> v.getCode().equals(item.code()))
                    .findFirst()
                    .orElse(null);
                
                if (voucher != null) {
                    voucherService.deleteVoucher(voucher.getId());
                    AppMessageDialogs.showInfo(this, "Thành công", "Đã vô hiệu hóa voucher.");
                    refreshVouchers();
                }
            } catch (Exception e) {
                AppMessageDialogs.showError(this, "Lỗi: " + e.getMessage());
            }
        }
    }
    
    private void refreshVouchers() {
        loadVouchersFromDatabase();
        cards.removeAll();
        for (PromotionCard.PromotionItem p : promotions) {
            cards.add(new PromotionCard(p, isReadOnly,
                    () -> handleEdit(p),
                    () -> handleDuplicate(p),
                    () -> handleDeactivate(p)));
        }
        refreshStats();
        cards.revalidate();
        cards.repaint();
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
