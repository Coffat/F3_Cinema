package com.f3cinema.app.ui.staff.components;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.service.cart.command.AddToCartCommand;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductCard extends JPanel {
    private final ProductDTO product;
    private JLabel imageContainer;

    public ProductCard(ProductDTO product) {
        this.product = product;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(160, 220));
        setOpaque(true);
        setBackground(Color.decode("#1E293B"));
        putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        add(buildImageSection(), BorderLayout.NORTH);
        add(buildInfoSection(), BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #334155");
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #1E293B");
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                new AddToCartCommand(product, 1).execute();
            }
        });
    }
    
    private JPanel buildImageSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(160, 120));
        panel.setOpaque(false);
        
        imageContainer = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(100, 100, 100, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        imageContainer.setHorizontalAlignment(SwingConstants.CENTER);
        imageContainer.setVerticalAlignment(SwingConstants.CENTER);
        imageContainer.setOpaque(false);
        
        if (product.imageUrl() != null && !product.imageUrl().isEmpty()) {
            loadProductImage(product.imageUrl());
        } else {
            FlatSVGIcon icon = new FlatSVGIcon("icons/snacks.svg", 64, 64);
            imageContainer.setIcon(icon);
            imageContainer.setForeground(Color.decode("#6366F1"));
        }
        
        panel.add(imageContainer, BorderLayout.CENTER);
        
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
                    
                    int targetWidth = 160;
                    int targetHeight = 120;
                    
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
                        imageContainer.setIcon(new CoverImageIcon(icon.getImage(), 160, 120, 12));
                    } else {
                        FlatSVGIcon fallback = new FlatSVGIcon("icons/snacks.svg", 64, 64);
                        imageContainer.setIcon(fallback);
                        imageContainer.setForeground(Color.decode("#6366F1"));
                    }
                } catch (Exception e) {
                    FlatSVGIcon fallback = new FlatSVGIcon("icons/snacks.svg", 64, 64);
                    imageContainer.setIcon(fallback);
                    imageContainer.setForeground(Color.decode("#6366F1"));
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
        panel.setBorder(new EmptyBorder(12, 12, 16, 12));

        JLabel nameLabel = new JLabel("<html><center>" + product.name() + "</center></html>");
        nameLabel.setFont(new Font("-apple-system", Font.BOLD, 13));
        nameLabel.setForeground(Color.decode("#F8FAFC"));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(nameLabel);
        
        panel.add(Box.createVerticalStrut(8));
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));
        JLabel priceLabel = new JLabel(currencyFormat.format(product.price()));
        priceLabel.setFont(new Font("-apple-system", Font.BOLD, 15));
        priceLabel.setForeground(Color.decode("#6366F1"));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(priceLabel);

        return panel;
    }
}
