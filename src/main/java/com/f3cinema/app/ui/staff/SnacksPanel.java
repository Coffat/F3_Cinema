package com.f3cinema.app.ui.staff;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.service.cart.CartManager;
import com.f3cinema.app.service.cart.CartObserver;
import com.f3cinema.app.service.cart.command.ClearCartCommand;
import com.f3cinema.app.service.impl.InventoryServiceImpl;
import com.f3cinema.app.service.payment.CashPaymentStrategy;
import com.f3cinema.app.service.payment.MomoPaymentStrategy;
import com.f3cinema.app.service.payment.PaymentContext;
import com.f3cinema.app.ui.components.WrapLayout;
import com.f3cinema.app.ui.dashboard.BaseDashboardModule;
import com.f3cinema.app.ui.staff.components.CartItemPanel;
import com.f3cinema.app.ui.staff.components.ProductCard;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Modern implementation of the Concessions Tab.
 * Integrates Observer Pattern (CartManager), Command Pattern (Add/Remove), and Strategy Pattern (Checkout).
 */
public class SnacksPanel extends BaseDashboardModule implements CartObserver {

    private JPanel productsContainer;
    private JPanel cartItemsContainer;
    private JLabel lblTotal;
    private FlatButton btnCheckout;
    private JComboBox<String> cbPaymentMethod;

    public SnacksPanel() {
        super("Bắp nước", "Trang chủ / Bắp nước");
        initUI();
        CartManager.getInstance().addObserver(this);
        loadProducts();
    }

    private void initUI() {
        JPanel mainContent = new JPanel(new BorderLayout(20, 0));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(10, 20, 20, 20));

        // LEFT: Products Grid Mode
        productsContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
        productsContainer.setOpaque(false);

        JScrollPane scrollProducts = new JScrollPane(productsContainer);
        scrollProducts.setBorder(null);
        scrollProducts.setOpaque(false);
        scrollProducts.getViewport().setOpaque(false);
        scrollProducts.getVerticalScrollBar().setUnitIncrement(16);
        scrollProducts.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "showButtons: false; thumbInsets: 0,0,0,0; width: 6;");

        // RIGHT: Cart Panel Container
        JPanel cartPanel = new JPanel(new BorderLayout(0, 15));
        cartPanel.setPreferredSize(new Dimension(350, 0));
        cartPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B"); // Slate 800
        cartPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Right Header
        JPanel cartHeader = new JPanel(new BorderLayout());
        cartHeader.setOpaque(false);
        JLabel lblCartTitle = new JLabel("Giỏ Hàng");
        lblCartTitle.setFont(new Font("Inter", Font.BOLD, 18));
        lblCartTitle.setForeground(Color.WHITE);
        
        FlatButton btnClear = new FlatButton();
        btnClear.setText("Xóa tất cả");
        btnClear.putClientProperty(FlatClientProperties.STYLE, "foreground: #F43F5E; background: null; hoverBackground: #334155");
        btnClear.addActionListener(e -> new ClearCartCommand().execute());
        
        cartHeader.add(lblCartTitle, BorderLayout.WEST);
        cartHeader.add(btnClear, BorderLayout.EAST);

        // Right Items List
        cartItemsContainer = new JPanel();
        cartItemsContainer.setLayout(new BoxLayout(cartItemsContainer, BoxLayout.Y_AXIS));
        cartItemsContainer.setOpaque(false);

        JScrollPane scrollCart = new JScrollPane(cartItemsContainer);
        scrollCart.setBorder(null);
        scrollCart.setOpaque(false);
        scrollCart.getViewport().setOpaque(false);

        // Right Footer Checkout
        JPanel cartFooter = new JPanel(new BorderLayout(0, 15));
        cartFooter.setOpaque(false);

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setOpaque(false);
        JLabel lblTotalLabel = new JLabel("Tổng tiền:");
        lblTotalLabel.setFont(new Font("Inter", Font.BOLD, 16));
        lblTotalLabel.setForeground(Color.decode("#94A3B8"));
        
        lblTotal = new JLabel("0 ₫");
        lblTotal.setFont(new Font("Inter", Font.BOLD, 22));
        lblTotal.setForeground(Color.decode("#6366F1"));
        
        totalPanel.add(lblTotalLabel, BorderLayout.WEST);
        totalPanel.add(lblTotal, BorderLayout.EAST);

        cbPaymentMethod = new JComboBox<>(new String[]{"Tiền mặt", "Momo"});
        cbPaymentMethod.putClientProperty(FlatClientProperties.STYLE, "arc: 10; padding: 5, 10, 5, 10");

        btnCheckout = new FlatButton();
        btnCheckout.setText("THANH TOÁN");
        btnCheckout.setFont(new Font("Inter", Font.BOLD, 14));
        btnCheckout.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #6366F1; foreground: #FFFFFF; hoverBackground: #4F46E5");
        btnCheckout.setPreferredSize(new Dimension(0, 45));
        btnCheckout.addActionListener(e -> processCheckout());

        JPanel checkoutActionPanel = new JPanel(new BorderLayout(0, 10));
        checkoutActionPanel.setOpaque(false);
        checkoutActionPanel.add(cbPaymentMethod, BorderLayout.NORTH);
        checkoutActionPanel.add(btnCheckout, BorderLayout.CENTER);

        cartFooter.add(totalPanel, BorderLayout.NORTH);
        cartFooter.add(checkoutActionPanel, BorderLayout.SOUTH);

        cartPanel.add(cartHeader, BorderLayout.NORTH);
        cartPanel.add(scrollCart, BorderLayout.CENTER);
        cartPanel.add(cartFooter, BorderLayout.SOUTH);

        mainContent.add(scrollProducts, BorderLayout.CENTER);
        mainContent.add(cartPanel, BorderLayout.EAST);

        contentBody.add(mainContent, BorderLayout.CENTER);
        onCartUpdated(); // Initial state setup
    }

    private void loadProducts() {
        productsContainer.removeAll();
        JLabel lblLoading = new JLabel("Đang tải dữ liệu...", SwingConstants.CENTER);
        lblLoading.setForeground(Color.WHITE);
        productsContainer.add(lblLoading);
        productsContainer.revalidate();
        productsContainer.repaint();

        SwingWorker<List<ProductDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ProductDTO> doInBackground() throws Exception {
                return InventoryServiceImpl.getInstance().getAllInventory();
            }

            @Override
            protected void done() {
                try {
                    List<ProductDTO> products = get();
                    productsContainer.removeAll();
                    for (ProductDTO product : products) {
                        productsContainer.add(new ProductCard(product));
                    }
                    productsContainer.revalidate();
                    productsContainer.repaint();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SnacksPanel.this, "Lỗi tải dữ liệu bắp nước: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void onCartUpdated() {
        cartItemsContainer.removeAll();
        Map<ProductDTO, Integer> items = CartManager.getInstance().getItems();
        
        for (Map.Entry<ProductDTO, Integer> entry : items.entrySet()) {
            cartItemsContainer.add(new CartItemPanel(entry.getKey(), entry.getValue()));
        }
        
        BigDecimal total = CartManager.getInstance().getTotalPrice();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));
        lblTotal.setText(currencyFormat.format(total));

        btnCheckout.setEnabled(!items.isEmpty());

        cartItemsContainer.revalidate();
        cartItemsContainer.repaint();
    }

    private void processCheckout() {
        if (CartManager.getInstance().getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng rỗng!");
            return;
        }

        PaymentContext context = new PaymentContext();
        String selectedMethod = (String) cbPaymentMethod.getSelectedItem();
        
        if ("Momo".equals(selectedMethod)) {
            context.setPaymentStrategy(new MomoPaymentStrategy());
        } else {
            context.setPaymentStrategy(new CashPaymentStrategy());
        }

        com.f3cinema.app.entity.Invoice tempInvoice = new com.f3cinema.app.entity.Invoice();
        tempInvoice.setId((long) (Math.random() * 10000));
        tempInvoice.setTotalAmount(CartManager.getInstance().getTotalPrice());
        
        // Strategy Execution
        context.processPayment(tempInvoice);
        
        // Complete flow by executing ClearCart Command
        new ClearCartCommand().execute();
    }
}
