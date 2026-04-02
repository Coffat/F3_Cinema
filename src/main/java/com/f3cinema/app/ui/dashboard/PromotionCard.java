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
    private final Runnable onEdit;
    private final Runnable onDuplicate;
    private final Runnable onDeactivate;
    private boolean isHovered = false;
    
    private static final int CARD_WIDTH = 500;
    private static final int CARD_HEIGHT = 140;
    private static final int STUB_WIDTH = 100;
    private static final int NOTCH_RADIUS = 12;
    private static final int CARD_ARC = 20;
    
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
        this.onEdit = onEdit;
        this.onDuplicate = onDuplicate;
        this.onDeactivate = onDeactivate;
        
        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        setupHoverEffect();
        add(buildLeftStub(), BorderLayout.WEST);
        add(buildRightContent(), BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int yOffset = isHovered ? -4 : 0;
        g2.translate(0, yOffset);
        
        g2.setColor(ThemeConfig.BG_CARD);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_ARC, CARD_ARC);
        
        g2.setColor(ThemeConfig.BG_MAIN);
        g2.fillOval(STUB_WIDTH - NOTCH_RADIUS, 15 - NOTCH_RADIUS, NOTCH_RADIUS * 2, NOTCH_RADIUS * 2);
        g2.fillOval(STUB_WIDTH - NOTCH_RADIUS, getHeight() - 27 - NOTCH_RADIUS, NOTCH_RADIUS * 2, NOTCH_RADIUS * 2);
        
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4, 6}, 0));
        Color dashedColor = new Color(
            ThemeConfig.BORDER_COLOR.getRed(),
            ThemeConfig.BORDER_COLOR.getGreen(),
            ThemeConfig.BORDER_COLOR.getBlue(),
            100
        );
        g2.setColor(dashedColor);
        g2.drawLine(STUB_WIDTH, 20, STUB_WIDTH, getHeight() - 20);
        
        g2.dispose();
        super.paintComponent(g);
    }

    private void setupHoverEffect() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    private JPanel buildLeftStub() {
        JPanel stub = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color color1 = typeColor(item.type());
                Color color2 = darkenColor(color1, 0.8f);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, color1,
                    0, getHeight(), color2
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                g2.dispose();
            }
        };
        stub.setPreferredSize(new Dimension(STUB_WIDTH, CARD_HEIGHT));
        stub.setLayout(new GridBagLayout());
        stub.setOpaque(false);
        
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        
        JPanel iconWrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = 52;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillOval(x, y, size, size);
                
                g2.setColor(new Color(255, 255, 255, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x, y, size, size);
                
                g2.dispose();
            }
        };
        iconWrapper.setOpaque(false);
        iconWrapper.setPreferredSize(new Dimension(60, 60));
        iconWrapper.setLayout(new GridBagLayout());
        
        FlatSVGIcon icon = new FlatSVGIcon(getTypeIcon(), 32, 32);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> Color.WHITE));
        JLabel iconLabel = new JLabel(icon);
        iconWrapper.add(iconLabel);
        
        JLabel typeLabel = new JLabel(getTypeLabel());
        typeLabel.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD, 11f));
        typeLabel.setForeground(new Color(255, 255, 255, 230));
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        content.add(iconWrapper);
        content.add(Box.createVerticalStrut(6));
        content.add(typeLabel);
        
        stub.add(content);
        return stub;
    }
    
    private Color darkenColor(Color color, float factor) {
        return new Color(
            Math.max((int)(color.getRed() * factor), 0),
            Math.max((int)(color.getGreen() * factor), 0),
            Math.max((int)(color.getBlue() * factor), 0)
        );
    }

    private JPanel buildRightContent() {
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BorderLayout(0, 0));
        right.setBorder(new EmptyBorder(18, 24, 18, 24));
        
        JPanel topSection = new JPanel(new BorderLayout(8, 0));
        topSection.setOpaque(false);
        topSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        
        JLabel nameLabel = new JLabel(item.name());
        nameLabel.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD, 13f));
        nameLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        
        JLabel statusBadge = createStatusBadge();
        
        topSection.add(nameLabel, BorderLayout.WEST);
        topSection.add(statusBadge, BorderLayout.EAST);
        
        JPanel middleSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        middleSection.setOpaque(false);
        
        JLabel discountLabel = new JLabel(item.discountLabel());
        discountLabel.setFont(ThemeConfig.FONT_H2.deriveFont(Font.BOLD, 26f));
        discountLabel.setForeground(typeColor(item.type()));
        
        JLabel codeBadge = createCodeBadge();
        
        middleSection.add(discountLabel);
        middleSection.add(codeBadge);
        
        JPanel bottomSection = new JPanel(new BorderLayout(10, 0));
        bottomSection.setOpaque(false);
        bottomSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        
        JPanel datePanel = buildDatePanel();
        JPanel progressPanel = buildCompactProgress();
        JPanel actionsPanel = buildActions();
        
        bottomSection.add(datePanel, BorderLayout.WEST);
        bottomSection.add(progressPanel, BorderLayout.CENTER);
        bottomSection.add(actionsPanel, BorderLayout.EAST);
        
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(topSection);
        wrapper.add(Box.createVerticalStrut(8));
        wrapper.add(middleSection);
        wrapper.add(Box.createVerticalGlue());
        wrapper.add(bottomSection);
        
        right.add(wrapper, BorderLayout.CENTER);
        return right;
    }
    
    private JLabel createCodeBadge() {
        JLabel badge = new JLabel("  " + item.code() + "  ");
        badge.setFont(new Font("Courier New", Font.BOLD, 11));
        badge.setForeground(typeColor(item.type()));
        badge.setOpaque(true);
        badge.setBackground(new Color(
            typeColor(item.type()).getRed(),
            typeColor(item.type()).getGreen(),
            typeColor(item.type()).getBlue(),
            25
        ));
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(
                typeColor(item.type()).getRed(),
                typeColor(item.type()).getGreen(),
                typeColor(item.type()).getBlue(),
                100
            ), 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        badge.putClientProperty(FlatClientProperties.STYLE, "arc: 6;");
        return badge;
    }
    
    private JPanel buildDatePanel() {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), item.endDate());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yy");
        String dateText = fmt.format(item.startDate()) + " - " + fmt.format(item.endDate());
        
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);
        
        FlatSVGIcon calIconSvg = new FlatSVGIcon("icons/calendar.svg", 12, 12);
        calIconSvg.setColorFilter(new FlatSVGIcon.ColorFilter(c -> ThemeConfig.TEXT_MUTED));
        JLabel calIcon = new JLabel(calIconSvg);
        panel.add(calIcon);
        
        JLabel dateLabel = new JLabel(dateText);
        dateLabel.setFont(ThemeConfig.FONT_SMALL.deriveFont(11f));
        dateLabel.setForeground(ThemeConfig.TEXT_MUTED);
        panel.add(dateLabel);
        
        if (daysLeft >= 0 && daysLeft <= 7 && "ACTIVE".equals(item.status())) {
            JLabel warningLabel = new JLabel("(còn " + daysLeft + " ngày)");
            warningLabel.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD, 10f));
            warningLabel.setForeground(Color.decode("#F59E0B"));
            panel.add(warningLabel);
        }
        
        return panel;
    }
    
    private JPanel buildCompactProgress() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        
        int percentage = item.limit() > 0 ? (item.used() * 100 / item.limit()) : 0;
        
        JLabel percentLabel = new JLabel(percentage + "%");
        percentLabel.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD, 10f));
        percentLabel.setForeground(getUsageColor(percentage));
        
        int max = Math.max(item.limit(), 1);
        JProgressBar bar = new JProgressBar(0, max);
        bar.setValue(Math.min(item.used(), max));
        bar.setStringPainted(false);
        bar.setPreferredSize(new Dimension(120, 6));
        bar.setMaximumSize(new Dimension(120, 6));
        bar.setForeground(getUsageColor(percentage));
        bar.setBackground(new Color(255, 255, 255, 10));
        bar.putClientProperty(FlatClientProperties.STYLE, "arc: 3;");
        
        panel.add(bar, BorderLayout.CENTER);
        panel.add(percentLabel, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setOpaque(false);
        
        actions.add(createIconButton("icons/edit.svg", ThemeConfig.TEXT_SECONDARY, onEdit));
        actions.add(createIconButton("icons/copy.svg", ThemeConfig.TEXT_SECONDARY, onDuplicate));
        actions.add(createIconButton("icons/trash.svg", ThemeConfig.TEXT_DANGER, onDeactivate));
        
        return actions;
    }

    private JButton createIconButton(String iconPath, Color color, Runnable action) {
        JButton btn = new JButton();
        FlatSVGIcon icon = new FlatSVGIcon(iconPath, 15, 15);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> color));
        btn.setIcon(icon);
        btn.setForeground(color);
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 6;");
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(new Color(255, 255, 255, 6));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
            }
        });
        
        btn.addActionListener(e -> action.run());
        return btn;
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

    private String getTypeIcon() {
        return switch (item.type()) {
            case "PERCENTAGE" -> "icons/percent.svg";
            case "FIXED_AMOUNT" -> "icons/tag.svg";
            case "BUY_X_GET_Y" -> "icons/gift.svg";
            case "COMBO_DISCOUNT" -> "icons/package.svg";
            case "POINT_REDEMPTION" -> "icons/star.svg";
            default -> "icons/ticket.svg";
        };
    }

    private String getTypeLabel() {
        return switch (item.type()) {
            case "PERCENTAGE" -> "Giảm %";
            case "FIXED_AMOUNT" -> "Giảm cố định";
            case "BUY_X_GET_Y" -> "Mua X tặng Y";
            case "COMBO_DISCOUNT" -> "Combo";
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
            case "COMBO_DISCOUNT" -> Color.decode("#8B5CF6");
            case "POINT_REDEMPTION" -> Color.decode("#3B82F6");
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
}
