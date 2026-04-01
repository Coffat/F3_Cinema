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

/**
 * Modern Card displaying a Product to be added to the cart.
 */
public class ProductCard extends JPanel {

    private final ProductDTO product;
    private boolean isHovered = false;

    public ProductCard(ProductDTO product) {
        this.product = product;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 10));
        setPreferredSize(new Dimension(160, 200));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B"); // Slate 800
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // SVG Icon Placeholder
        JLabel imageLabel = new JLabel(new FlatSVGIcon("icons/snacks.svg", 64, 64));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Product Name
        JLabel nameLabel = new JLabel("<html><div style='text-align: center; width: 120px'>" + product.name() + "</div></html>");
        nameLabel.setFont(new Font("-apple-system", Font.BOLD, 14));
        nameLabel.setForeground(Color.decode("#F8FAFC")); // Slate 50
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Product Price
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        JLabel priceLabel = new JLabel(currencyFormat.format(product.price()));
        priceLabel.setFont(new Font("-apple-system", Font.PLAIN, 13));
        priceLabel.setForeground(Color.decode("#6366F1")); // Indigo 500
        priceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setOpaque(false);
        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);

        add(imageLabel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);

        // Hover Effect and Click Handler
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #334155"); // Slate 700
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B"); // Slate 800
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Command Pattern Trigger
                new AddToCartCommand(product, 1).execute();
            }
        });
    }
}
