package com.f3cinema.app.ui.staff.components;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.service.cart.command.UpdateQuantityCommand;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Component displaying a single line item in the Cart.
 */
public class CartItemPanel extends JPanel {

    private final ProductDTO product;
    private final int quantity;

    public CartItemPanel(ProductDTO product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 0, 8, 0));

        // Name and Price
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(product.name());
        nameLabel.setFont(new Font("-apple-system", Font.BOLD, 13));
        nameLabel.setForeground(Color.decode("#F8FAFC"));
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        JLabel priceLabel = new JLabel(currencyFormat.format(product.price()));
        priceLabel.setFont(new Font("-apple-system", Font.PLAIN, 12));
        priceLabel.setForeground(Color.decode("#94A3B8"));

        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);

        // Quantity Controls
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        qtyPanel.setOpaque(false);

        FlatButton btnMinus = new FlatButton();
        btnMinus.setText("-");
        btnMinus.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #334155; foreground: #F8FAFC; focusWidth: 0;");
        btnMinus.setPreferredSize(new Dimension(28, 28));
        btnMinus.addActionListener(e -> new UpdateQuantityCommand(product, quantity - 1).execute());

        JLabel qtyLabel = new JLabel(String.valueOf(quantity), SwingConstants.CENTER);
        qtyLabel.setFont(new Font("-apple-system", Font.BOLD, 14));
        qtyLabel.setForeground(Color.WHITE);
        qtyLabel.setPreferredSize(new Dimension(30, 28));

        FlatButton btnPlus = new FlatButton();
        btnPlus.setText("+");
        btnPlus.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #334155; foreground: #F8FAFC; focusWidth: 0;");
        btnPlus.setPreferredSize(new Dimension(28, 28));
        btnPlus.addActionListener(e -> new UpdateQuantityCommand(product, quantity + 1).execute());

        qtyPanel.add(btnMinus);
        qtyPanel.add(qtyLabel);
        qtyPanel.add(btnPlus);

        add(infoPanel, BorderLayout.CENTER);
        add(qtyPanel, BorderLayout.EAST);
    }
}
