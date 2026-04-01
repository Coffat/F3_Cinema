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
    private Image backgroundImage;

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

        if (product.imageUrl() != null && !product.imageUrl().isEmpty()) {
            loadProductImage(product.imageUrl());
        }
        
        add(buildOverlayContent(), BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(BorderFactory.createLineBorder(Color.decode("#6366F1"), 2, true));
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(null);
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                new AddToCartCommand(product, 1).execute();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
        
        if (backgroundImage != null) {
            int imgWidth = backgroundImage.getWidth(null);
            int imgHeight = backgroundImage.getHeight(null);
            
            double scale = Math.max((double) getWidth() / imgWidth, (double) getHeight() / imgHeight);
            int scaledWidth = (int) (imgWidth * scale);
            int scaledHeight = (int) (imgHeight * scale);
            
            int x = (getWidth() - scaledWidth) / 2;
            int y = (getHeight() - scaledHeight) / 2;
            
            g2.drawImage(backgroundImage, x, y, scaledWidth, scaledHeight, null);
            
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        } else {
            g2.setColor(Color.decode("#1E293B"));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            
            FlatSVGIcon icon = new FlatSVGIcon("icons/snacks.svg");
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> new Color(99, 102, 241, 60)));
            icon.paintIcon(this, g2, (getWidth() - 80) / 2, (getHeight() - 80) / 2);
        }
        
        g2.dispose();
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
                    repaint();
                } catch (Exception e) {
                    backgroundImage = null;
                    repaint();
                }
            }
        }.execute();
    }
    
    private JPanel buildOverlayContent() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(12, 12, 16, 12));
        
        panel.add(Box.createVerticalGlue());

        JLabel nameLabel = new JLabel("<html><center>" + product.name() + "</center></html>");
        nameLabel.setFont(new Font("-apple-system", Font.BOLD, 13));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(nameLabel);
        
        panel.add(Box.createVerticalStrut(8));
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));
        JLabel priceLabel = new JLabel(currencyFormat.format(product.price()));
        priceLabel.setFont(new Font("-apple-system", Font.BOLD, 16));
        priceLabel.setForeground(Color.WHITE);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(priceLabel);
        
        panel.add(Box.createVerticalStrut(8));

        return panel;
    }
}
