package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.dto.ProductDTO;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class ProductCard extends JPanel {
    private final ProductDTO product;
    private Image backgroundImage;
    private Image cachedCardImage;
    private int cachedWidth = -1;
    private int cachedHeight = -1;

    public ProductCard(ProductDTO product, Runnable onEdit, Runnable onAdjust, Runnable onDelete) {
        this.product = product;
        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(200, 280));
        setOpaque(true);
        setBackground(ThemeConfig.BG_CARD);
        putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        setupHoverEffect();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                invalidateCardCache();
            }
        });
        
        if (product.imageUrl() != null && !product.imageUrl().isEmpty()) {
            loadProductImage(product.imageUrl());
        }
        
        add(buildOverlayContent(), BorderLayout.CENTER);
        
        boolean hasActions = onEdit != null || onAdjust != null || onDelete != null;
        if (hasActions) {
            add(buildActionsSection(onEdit, onAdjust, onDelete), BorderLayout.SOUTH);
        } else {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    new com.f3cinema.app.service.cart.command.AddToCartCommand(product, 1).execute();
                }
            });
        }
    }
    
    public ProductCard(ProductDTO product) {
        this(product, null, null, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Image cardBg = getOrCreateCardBackground();
        if (cardBg != null) {
            g.drawImage(cardBg, 0, 0, null);
        }
    }

    private Image getOrCreateCardBackground() {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }
        if (cachedCardImage != null && cachedWidth == width && cachedHeight == height) {
            return cachedCardImage;
        }

        java.awt.image.BufferedImage composed = new java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = composed.createGraphics();
        ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ig.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        ig.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, width, height, 12, 12));

        if (backgroundImage != null) {
            int imgWidth = backgroundImage.getWidth(null);
            int imgHeight = backgroundImage.getHeight(null);
            if (imgWidth > 0 && imgHeight > 0) {
                double scale = Math.max((double) width / imgWidth, (double) height / imgHeight);
                int scaledWidth = (int) Math.ceil(imgWidth * scale);
                int scaledHeight = (int) Math.ceil(imgHeight * scale);
                int drawX = (width - scaledWidth) / 2;
                int drawY = (height - scaledHeight) / 2;
                ig.drawImage(backgroundImage, drawX, drawY, scaledWidth, scaledHeight, null);
            } else {
                ig.setColor(ThemeConfig.BG_CARD);
                ig.fillRect(0, 0, width, height);
            }
            ig.setColor(new Color(0, 0, 0, 120));
            ig.fillRect(0, 0, width, height);
        } else {
            Color category = getCategoryColor();
            ig.setColor(new Color(category.getRed(), category.getGreen(), category.getBlue(), 30));
            ig.fillRect(0, 0, width, height);
            FlatSVGIcon icon = new FlatSVGIcon(getCategoryIcon(), 100, 100);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c ->
                    new Color(category.getRed(), category.getGreen(), category.getBlue(), 40)));
            icon.paintIcon(this, ig, (width - 100) / 2, (height - 100) / 2);
        }
        ig.dispose();

        cachedCardImage = composed;
        cachedWidth = width;
        cachedHeight = height;
        return cachedCardImage;
    }

    private void invalidateCardCache() {
        cachedCardImage = null;
        cachedWidth = -1;
        cachedHeight = -1;
    }
    
    private void setupHoverEffect() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(BorderFactory.createLineBorder(getCategoryColor(), 2, true));
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(null);
                repaint();
            }
        });
    }
    
    private void loadProductImage(String imageUrl) {
        new SwingWorker<Image, Void>() {
            @Override
            protected Image doInBackground() throws Exception {
                try {
                    java.net.URI uri = new java.net.URI(imageUrl);
                    ImageIcon originalIcon = new ImageIcon(uri.toURL());
                    return originalIcon.getImage();
                } catch (Exception e) {
                    return null;
                }
            }
            
            @Override
            protected void done() {
                try {
                    backgroundImage = get();
                    invalidateCardCache();
                    repaint();
                } catch (Exception e) {
                    backgroundImage = null;
                    invalidateCardCache();
                    repaint();
                }
            }
        }.execute();
    }
    
    private JPanel buildOverlayContent() {
        JPanel overlay = new JPanel();
        overlay.setLayout(new BorderLayout());
        overlay.setOpaque(false);
        
        JPanel topSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        topSection.setOpaque(false);
        
        JLabel badge = new JLabel(getCategory());
        badge.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD, 9f));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(getCategoryColor());
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badge.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        topSection.add(badge);
        
        JPanel infoSection = new JPanel();
        infoSection.setOpaque(false);
        infoSection.setLayout(new BoxLayout(infoSection, BoxLayout.Y_AXIS));
        infoSection.setBorder(new EmptyBorder(12, 16, 12, 16));
        
        infoSection.add(Box.createVerticalGlue());

        JLabel name = new JLabel("<html><center>" + product.name() + "</center></html>");
        name.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD, 13f));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoSection.add(name);
        
        infoSection.add(Box.createVerticalStrut(8));

        JLabel price = new JLabel(format(product.price()));
        price.setFont(ThemeConfig.FONT_H3.deriveFont(Font.BOLD, 18f));
        price.setForeground(Color.WHITE);
        price.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoSection.add(price);
        
        infoSection.add(Box.createVerticalStrut(12));

        int currentQty = n(product.currentQuantity());
        int maxQty = Math.max(1, n(product.minThreshold() * 5));
        
        JLabel stockLabel = new JLabel("Tồn: " + currentQty + "/" + maxQty);
        stockLabel.setFont(ThemeConfig.FONT_SMALL.deriveFont(11f));
        stockLabel.setForeground(new Color(255, 255, 255, 200));
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoSection.add(stockLabel);
        
        infoSection.add(Box.createVerticalStrut(6));

        JProgressBar bar = new JProgressBar(0, maxQty);
        bar.setValue(Math.min(maxQty, currentQty));
        bar.setStringPainted(false);
        bar.setPreferredSize(new Dimension(168, 6));
        bar.setMaximumSize(new Dimension(168, 6));
        bar.setForeground(stockColor());
        bar.setBackground(new Color(255, 255, 255, 30));
        bar.putClientProperty(FlatClientProperties.STYLE, "arc: 3;");
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoSection.add(bar);
        
        infoSection.add(Box.createVerticalStrut(8));
        
        overlay.add(topSection, BorderLayout.NORTH);
        overlay.add(infoSection, BorderLayout.CENTER);
        
        return overlay;
    }

    private JPanel buildActionsSection(Runnable onEdit, Runnable onAdjust, Runnable onDelete) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(Color.decode("#0F172A"));
        panel.setPreferredSize(new Dimension(200, 48));
        panel.setBorder(new EmptyBorder(8, 16, 8, 16));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 4);
        
        panel.add(createIconButton("icons/edit.svg", "Sửa", Color.WHITE, onEdit), gbc);
        
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 4, 0, 4);
        panel.add(createIconButton("icons/package.svg", "Điều chỉnh", Color.WHITE, onAdjust), gbc);
        
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 4, 0, 0);
        panel.add(createIconButton("icons/trash.svg", "Xóa", Color.decode("#EF4444"), onDelete), gbc);
        
        return panel;
    }

    private JButton createIconButton(String iconPath, String tooltip, Color color, Runnable action) {
        JButton btn = new JButton();
        FlatSVGIcon icon = new FlatSVGIcon(iconPath, 18, 18);
        btn.setIcon(icon);
        btn.setToolTipText(tooltip);
        btn.setForeground(color);
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setMinimumSize(new Dimension(36, 36));
        btn.setMaximumSize(new Dimension(36, 36));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(true);
        btn.setBackground(new Color(255, 255, 255, 40));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(255, 255, 255, 40));
            }
        });
        
        btn.addActionListener(e -> {
            if (action != null) action.run();
        });
        return btn;
    }

    private String getCategoryIcon() {
        return switch (getCategory()) {
            case "DRINK" -> "icons/droplet.svg";
            case "COMBO" -> "icons/package.svg";
            default -> "icons/popcorn.svg";
        };
    }

    private Color stockColor() {
        int qty = n(product.currentQuantity());
        int max = Math.max(1, n(product.minThreshold() * 5));
        double r = qty / (double) max;
        if (qty == 0) return Color.decode("#EF4444");
        if (r < 0.2) return Color.decode("#EF4444");
        if (r < 0.5) return Color.decode("#F59E0B");
        return Color.decode("#10B981");
    }

    private String getCategory() {
        String n = product.name() == null ? "" : product.name().toLowerCase();
        if (n.contains("combo")) return "COMBO";
        if (n.contains("nước") || n.contains("nuoc") || n.contains("coca") || n.contains("sprite") || n.contains("fanta") || n.contains("pepsi") || n.contains("tea")) return "DRINK";
        return "SNACK";
    }

    private Color getCategoryColor() {
        return switch (getCategory()) {
            case "DRINK" -> Color.decode("#3B82F6");
            case "COMBO" -> Color.decode("#8B5CF6");
            default -> Color.decode("#F59E0B");
        };
    }

    private String format(BigDecimal v) {
        return new DecimalFormat("#,##0").format(v) + "đ";
    }

    private int n(Integer v) {
        return v == null ? 0 : v;
    }
}
