package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class PromotionCard extends JPanel {
    private final PromotionItem item;
    
    public record PromotionItem(
            String name,
            String code,
            String type,
            String status,
            String discountLabel,
            LocalDate startDate,
            LocalDate endDate,
            int used,
            int limit
    ) {}

    public PromotionCard(PromotionItem item, Runnable onEdit, Runnable onDuplicate, Runnable onDeactivate) {
        this.item = item;
        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(340, 200));
        setOpaque(true);
        setBackground(ThemeConfig.BG_CARD);
        setBorder(new EmptyBorder(24, 24, 20, 24));
        putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        setupHoverEffect();
        add(buildContent(onEdit, onDuplicate, onDeactivate), BorderLayout.CENTER);
    }

    private void setupHoverEffect() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(typeColor(item.type()), 1),
                    new EmptyBorder(23, 23, 19, 23)
                ));
                revalidate();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(new EmptyBorder(24, 24, 20, 24));
                revalidate();
                repaint();
            }
        });
    }

    private JPanel buildContent(Runnable onEdit, Runnable onDuplicate, Runnable onDeactivate) {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(16));
        content.add(buildMainInfo());
        content.add(Box.createVerticalStrut(16));
        content.add(buildProgress());
        content.add(Box.createVerticalGlue());
        content.add(Box.createVerticalStrut(12));
        content.add(buildActions(onEdit, onDuplicate, onDeactivate));

        return content;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        
        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftSide.setOpaque(false);
        
        FlatSVGIcon icon = new FlatSVGIcon(getTypeIcon(), 18, 18);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setForeground(typeColor(item.type()));
        leftSide.add(iconLabel);
        
        JLabel typeLabel = new JLabel(getTypeLabel());
        typeLabel.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD, 11f));
        typeLabel.setForeground(typeColor(item.type()));
        leftSide.add(typeLabel);
        
        JLabel statusBadge = createStatusBadge();
        
        header.add(leftSide, BorderLayout.WEST);
        header.add(statusBadge, BorderLayout.EAST);
        
        return header;
    }

    private JLabel createStatusBadge() {
        JLabel badge = new JLabel(getStatusLabel());
        badge.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD, 9f));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(statusColor(item.status()));
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));
        badge.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        return badge;
    }

    private JPanel buildMainInfo() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel name = new JLabel("<html>" + item.name() + "</html>");
        name.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD, 15f));
        name.setForeground(ThemeConfig.TEXT_PRIMARY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(name);
        
        panel.add(Box.createVerticalStrut(8));
        
        JPanel codeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        codeRow.setOpaque(false);
        codeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel codeIcon = new JLabel(new FlatSVGIcon("icons/ticket.svg", 14, 14));
        codeIcon.setForeground(ThemeConfig.TEXT_SECONDARY);
        codeRow.add(codeIcon);
        
        JLabel code = new JLabel(item.code());
        code.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD, 12f));
        code.setForeground(typeColor(item.type()));
        codeRow.add(code);
        
        panel.add(codeRow);
        panel.add(Box.createVerticalStrut(12));
        
        JLabel discount = new JLabel(item.discountLabel());
        discount.setFont(ThemeConfig.FONT_H2.deriveFont(Font.BOLD, 22f));
        discount.setForeground(typeColor(item.type()));
        discount.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(discount);
        
        panel.add(Box.createVerticalStrut(8));
        
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), item.endDate());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yy");
        String dateText = fmt.format(item.startDate()) + " - " + fmt.format(item.endDate());
        
        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        dateRow.setOpaque(false);
        dateRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel calIcon = new JLabel(new FlatSVGIcon("icons/calendar.svg", 12, 12));
        calIcon.setForeground(ThemeConfig.TEXT_MUTED);
        dateRow.add(calIcon);
        
        JLabel dateLabel = new JLabel(dateText);
        dateLabel.setFont(ThemeConfig.FONT_SMALL.deriveFont(11f));
        dateLabel.setForeground(ThemeConfig.TEXT_MUTED);
        dateRow.add(dateLabel);
        
        if (daysLeft >= 0 && daysLeft <= 7 && "ACTIVE".equals(item.status())) {
            JLabel warningLabel = new JLabel("(còn " + daysLeft + " ngày)");
            warningLabel.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD, 11f));
            warningLabel.setForeground(Color.decode("#F59E0B"));
            dateRow.add(warningLabel);
        }
        
        panel.add(dateRow);
        
        return panel;
    }

    private JPanel buildProgress() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        
        int percentage = item.limit() > 0 ? (item.used() * 100 / item.limit()) : 0;
        
        JLabel usageLabel = new JLabel(item.used() + "/" + item.limit());
        usageLabel.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD, 11f));
        usageLabel.setForeground(ThemeConfig.TEXT_SECONDARY);
        
        JLabel percentLabel = new JLabel(percentage + "%");
        percentLabel.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD, 11f));
        percentLabel.setForeground(getUsageColor(percentage));
        
        header.add(usageLabel, BorderLayout.WEST);
        header.add(percentLabel, BorderLayout.EAST);
        
        int max = Math.max(item.limit(), 1);
        JProgressBar bar = new JProgressBar(0, max);
        bar.setValue(Math.min(item.used(), max));
        bar.setStringPainted(false);
        bar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 8));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        bar.setForeground(getUsageColor(percentage));
        bar.setBackground(new Color(255, 255, 255, 10));
        bar.putClientProperty(FlatClientProperties.STYLE, "arc: 4;");
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(header);
        panel.add(Box.createVerticalStrut(6));
        panel.add(bar);
        
        return panel;
    }

    private JPanel buildActions(Runnable onEdit, Runnable onDuplicate, Runnable onDeactivate) {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        actions.setOpaque(false);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        
        actions.add(createIconButton("icons/edit.svg", ThemeConfig.TEXT_SECONDARY, onEdit));
        actions.add(createIconButton("icons/copy.svg", ThemeConfig.TEXT_SECONDARY, onDuplicate));
        actions.add(createIconButton("icons/trash.svg", ThemeConfig.TEXT_DANGER, onDeactivate));
        
        return actions;
    }

    private JButton createIconButton(String iconPath, Color color, Runnable action) {
        JButton btn = new JButton();
        FlatSVGIcon icon = new FlatSVGIcon(iconPath, 16, 16);
        btn.setIcon(icon);
        btn.setForeground(color);
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 6;");
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(new Color(255, 255, 255, 10));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
            }
        });
        
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private String getTypeIcon() {
        return switch (item.type()) {
            case "PERCENTAGE" -> "icons/trending-up.svg";
            case "FIXED_AMOUNT" -> "icons/dollar.svg";
            case "BUY_X_GET_Y" -> "icons/gift.svg";
            case "POINT_REDEMPTION" -> "icons/star.svg";
            default -> "icons/gift.svg";
        };
    }

    private String getTypeLabel() {
        return switch (item.type()) {
            case "PERCENTAGE" -> "Giảm %";
            case "FIXED_AMOUNT" -> "Giảm cố định";
            case "BUY_X_GET_Y" -> "Mua X tặng Y";
            case "POINT_REDEMPTION" -> "Đổi điểm";
            default -> item.type();
        };
    }

    private String getStatusLabel() {
        return switch (item.status()) {
            case "ACTIVE" -> "Đang chạy";
            case "SCHEDULED" -> "Sắp diễn ra";
            case "EXPIRED" -> "Đã kết thúc";
            default -> item.status();
        };
    }

    private Color getUsageColor(int percentage) {
        if (percentage >= 90) return Color.decode("#EF4444");
        if (percentage >= 70) return Color.decode("#F59E0B");
        return Color.decode("#10B981");
    }

    private Color typeColor(String type) {
        return switch (type) {
            case "PERCENTAGE" -> Color.decode("#10B981");
            case "FIXED_AMOUNT" -> Color.decode("#F59E0B");
            case "BUY_X_GET_Y" -> Color.decode("#EC4899");
            case "POINT_REDEMPTION" -> Color.decode("#8B5CF6");
            default -> ThemeConfig.ACCENT_COLOR;
        };
    }

    private Color statusColor(String status) {
        return switch (status) {
            case "ACTIVE" -> Color.decode("#10B981");
            case "SCHEDULED" -> ThemeConfig.ACCENT_COLOR;
            case "EXPIRED" -> Color.decode("#64748B");
            default -> ThemeConfig.TEXT_MUTED;
        };
    }
    
    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
