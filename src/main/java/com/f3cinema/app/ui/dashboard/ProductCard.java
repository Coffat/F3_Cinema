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
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class ProductCard extends JPanel {
    private final ProductDTO product;
    private JLabel imageContainer;

    public ProductCard(ProductDTO product, Runnable onEdit, Runnable onAdjust, Runnable onDelete) {
        this.product = product;
        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(200, 280));
        setOpaque(true);
        setBackground(ThemeConfig.BG_CARD);
        putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        setupHoverEffect();
        
        add(buildImageSection(), BorderLayout.NORTH);
        add(buildInfoSection(), BorderLayout.CENTER);
        add(buildActionsSection(onEdit, onAdjust, onDelete), BorderLayout.SOUTH);
    }

    private void setupHoverEffect() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 1; borderColor: " + toHex(getCategoryColor()));
                revalidate();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                putClientProperty(FlatClientProperties.STYLE, "arc: 12");
                revalidate();
                repaint();
            }
        });
    }

    private JPanel buildImageSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 140));
        panel.setOpaque(false);
        
        imageContainer = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(getCategoryColor().getRed(), getCategoryColor().getGreen(), getCategoryColor().getBlue(), 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        imageContainer.setHorizontalAlignment(SwingConstants.CENTER);
        imageContainer.setVerticalAlignment(SwingConstants.CENTER);
        imageContainer.setOpaque(false);
        
        JLabel badge = new JLabel(getCategory());
        badge.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD, 9f));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(getCategoryColor());
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badge.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        
        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(badge);
        
        if (product.imageUrl() != null && !product.imageUrl().isEmpty()) {
            loadProductImage(product.imageUrl());
        } else {
            FlatSVGIcon icon = new FlatSVGIcon(getCategoryIcon(), 64, 64);
            imageContainer.setIcon(icon);
            imageContainer.setForeground(getCategoryColor());
        }
        
        panel.add(imageContainer, BorderLayout.CENTER);
        panel.add(badgeWrapper, BorderLayout.NORTH);
        
        return panel;
    }
    
    private void loadProductImage(String imageUrl) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                try {
                    java.net.URI uri = new java.net.URI(imageUrl);
                    ImageIcon originalIcon = new ImageIcon(uri.toURL());
                    Image img = originalIcon.getImage();
                    
                    int targetWidth = 200;
                    int targetHeight = 140;
                    
                    int originalWidth = img.getWidth(null);
                    int originalHeight = img.getHeight(null);
                    
                    double scale = Math.max((double) targetWidth / originalWidth, (double) targetHeight / originalHeight);
                    int scaledWidth = (int) (originalWidth * scale);
                    int scaledHeight = (int) (originalHeight * scale);
                    
                    Image scaledImage = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaledImage);
                } catch (Exception e) {
                    return null;
                }
            }
            
            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        imageContainer.setIcon(new CoverImageIcon(icon.getImage(), 200, 140, 12));
                    } else {
                        FlatSVGIcon fallback = new FlatSVGIcon(getCategoryIcon(), 64, 64);
                        imageContainer.setIcon(fallback);
                        imageContainer.setForeground(getCategoryColor());
                    }
                } catch (Exception e) {
                    FlatSVGIcon fallback = new FlatSVGIcon(getCategoryIcon(), 64, 64);
                    imageContainer.setIcon(fallback);
                    imageContainer.setForeground(getCategoryColor());
                }
            }
        }.execute();
    }
    
    private static class CoverImageIcon implements Icon {
        private final Image image;
        private final int width;
        private final int height;
        private final int cornerRadius;
        
        public CoverImageIcon(Image image, int width, int height, int cornerRadius) {
            this.image = image;
            this.width = width;
            this.height = height;
            this.cornerRadius = cornerRadius;
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            g2.setClip(new java.awt.geom.RoundRectangle2D.Float(x, y, width, height, cornerRadius, cornerRadius));
            
            int imgWidth = image.getWidth(null);
            int imgHeight = image.getHeight(null);
            
            int drawX = x + (width - imgWidth) / 2;
            int drawY = y + (height - imgHeight) / 2;
            
            g2.drawImage(image, drawX, drawY, imgWidth, imgHeight, null);
            
            g2.dispose();
        }
        
        @Override
        public int getIconWidth() {
            return width;
        }
        
        @Override
        public int getIconHeight() {
            return height;
        }
    }

    private JPanel buildInfoSection() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel name = new JLabel("<html><center>" + product.name() + "</center></html>");
        name.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD, 13f));
        name.setForeground(ThemeConfig.TEXT_PRIMARY);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(name);
        
        panel.add(Box.createVerticalStrut(8));

        JLabel price = new JLabel(format(product.price()));
        price.setFont(ThemeConfig.FONT_H3.deriveFont(Font.BOLD, 17f));
        price.setForeground(ThemeConfig.TEXT_PRIMARY);
        price.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(price);
        
        panel.add(Box.createVerticalStrut(12));

        int currentQty = n(product.currentQuantity());
        int maxQty = Math.max(1, n(product.minThreshold() * 5));
        
        JLabel stockLabel = new JLabel("Tồn: " + currentQty + "/" + maxQty);
        stockLabel.setFont(ThemeConfig.FONT_SMALL.deriveFont(11f));
        stockLabel.setForeground(ThemeConfig.TEXT_SECONDARY);
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(stockLabel);
        
        panel.add(Box.createVerticalStrut(6));

        JProgressBar bar = new JProgressBar(0, maxQty);
        bar.setValue(Math.min(maxQty, currentQty));
        bar.setStringPainted(false);
        bar.setPreferredSize(new Dimension(168, 6));
        bar.setMaximumSize(new Dimension(168, 6));
        bar.setForeground(stockColor());
        bar.setBackground(new Color(255, 255, 255, 10));
        bar.putClientProperty(FlatClientProperties.STYLE, "arc: 3;");
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(bar);

        return panel;
    }

    private JPanel buildActionsSection(Runnable onEdit, Runnable onAdjust, Runnable onDelete) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(200, 48));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 10)),
            new EmptyBorder(8, 16, 8, 16)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 4);
        
        panel.add(createIconButton("icons/edit.svg", "Sửa", ThemeConfig.TEXT_SECONDARY, onEdit), gbc);
        
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 4, 0, 4);
        panel.add(createIconButton("icons/package.svg", "Điều chỉnh", ThemeConfig.TEXT_SECONDARY, onAdjust), gbc);
        
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 4, 0, 0);
        panel.add(createIconButton("icons/trash.svg", "Xóa", ThemeConfig.TEXT_DANGER, onDelete), gbc);
        
        return panel;
    }

    private JButton createIconButton(String iconPath, String tooltip, Color color, Runnable action) {
        JButton btn = new JButton();
        FlatSVGIcon icon = new FlatSVGIcon(iconPath, 18, 18);
        btn.setIcon(icon);
        btn.setToolTipText(tooltip);
        btn.setForeground(color);
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
            }
        });
        
        btn.addActionListener(e -> action.run());
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
    
    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
