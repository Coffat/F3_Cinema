package com.f3cinema.app.ui.staff.ticketing.step3;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.service.cart.CartManager;
import com.f3cinema.app.service.cart.CartObserver;
import com.f3cinema.app.service.impl.InventoryServiceImpl;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.staff.components.CartItemPanel;
import com.f3cinema.app.ui.dashboard.ProductCard;
import com.f3cinema.app.ui.staff.ticketing.TicketOrderState;
import com.f3cinema.app.ui.staff.ticketing.TicketingFlowPanel;
import com.f3cinema.app.ui.staff.ticketing.components.OrderSummaryCard;
import com.f3cinema.app.util.WrapLayout;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Step 3: Snacks selection with cinema-standard layout.
 * Left: Product grid | Right: Cart + Order summary.
 */
public class SnacksSelectionPanel extends JPanel implements CartObserver {

    private static final Color TEXT_PRIMARY = new Color(0xF8FAFC);
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);
    private static final Color ACCENT_PRIMARY = new Color(0x6366F1);
    private static final Color BG_SURFACE = new Color(0x1E293B);

    private final TicketingFlowPanel navigator;
    private final TicketOrderState state;
    private final CartManager cartManager;

    private JPanel productsContainer;
    private JPanel cartItemsContainer;
    private OrderSummaryCard summaryCard;
    private JButton btnNext;
    private JButton btnSkip;

    public SnacksSelectionPanel(TicketingFlowPanel navigator) {
        this.navigator = navigator;
        this.state = TicketOrderState.getInstance();
        this.cartManager = CartManager.getInstance();

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        initUI();
        cartManager.addObserver(this);
        loadProducts();
    }

    private void initUI() {
        JPanel contentBody = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59, 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setStroke(new BasicStroke(1.5f));
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 30),
                        0, getHeight(), new Color(255, 255, 255, 5)));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 24, 24);
                g2.dispose();
            }
        };
        contentBody.setLayout(new BorderLayout(20, 16));
        contentBody.setOpaque(false);
        contentBody.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topSection = createTopSection();
        contentBody.add(topSection, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(750);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOpaque(false);

        splitPane.setLeftComponent(createProductsArea());
        splitPane.setRightComponent(createRightSidebar());

        contentBody.add(splitPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottomBar.setOpaque(false);

        btnSkip = new JButton("Bỏ qua >");
        btnSkip.setFont(new Font("Inter", Font.PLAIN, 14));
        btnSkip.setForeground(TEXT_PRIMARY);
        btnSkip.setBackground(BG_SURFACE);
        btnSkip.setPreferredSize(new Dimension(130, 42));
        btnSkip.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0;");
        btnSkip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSkip.addActionListener(e -> navigator.nextStep());

        btnNext = new JButton("Tiếp tục >");
        btnNext.setFont(new Font("Inter", Font.BOLD, 15));
        btnNext.setForeground(Color.WHITE);
        btnNext.setBackground(ACCENT_PRIMARY);
        btnNext.setPreferredSize(new Dimension(160, 44));
        btnNext.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0;");
        btnNext.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNext.addActionListener(e -> navigator.nextStep());

        bottomBar.add(btnSkip);
        bottomBar.add(btnNext);
        contentBody.add(bottomBar, BorderLayout.SOUTH);

        add(contentBody, BorderLayout.CENTER);
    }

    private JPanel createTopSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel infoWrapper = new JPanel();
        infoWrapper.setLayout(new BoxLayout(infoWrapper, BoxLayout.Y_AXIS));
        infoWrapper.setOpaque(false);

        JLabel lblStep = new JLabel("Bước 3/4");
        lblStep.setFont(new Font("Inter", Font.PLAIN, 13));
        lblStep.setForeground(TEXT_SECONDARY);
        lblStep.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Thêm bắp nước (tùy chọn)");
        FlatSVGIcon popcornIcon = new FlatSVGIcon("icons/popcorn.svg", 22, 22);
        popcornIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> TEXT_PRIMARY));
        lblTitle.setIcon(popcornIcon);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);

        infoWrapper.add(lblStep);
        infoWrapper.add(Box.createVerticalStrut(4));
        infoWrapper.add(lblTitle);

        JButton btnBack = new JButton("Quay lại");
        FlatSVGIcon backIcon = new FlatSVGIcon("icons/arrow-left.svg", 14, 14);
        backIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> TEXT_PRIMARY));
        btnBack.setIcon(backIcon);
        btnBack.setFont(new Font("Inter", Font.PLAIN, 13));
        btnBack.setForeground(TEXT_PRIMARY);
        btnBack.setPreferredSize(new Dimension(120, 36));
        btnBack.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; background: #1E293B; borderWidth: 0;");
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> navigator.previousStep());

        panel.add(infoWrapper, BorderLayout.WEST);
        panel.add(btnBack, BorderLayout.EAST);

        return panel;
    }

    private JPanel createProductsArea() {
        JPanel area = new JPanel(new BorderLayout());
        area.setOpaque(false);

        productsContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));
        productsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(productsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        area.add(scrollPane, BorderLayout.CENTER);

        return area;
    }

    private JPanel createRightSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 16));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(340, 0));

        JPanel cartPanel = createCartPanel();
        summaryCard = new OrderSummaryCard();

        sidebar.add(cartPanel, BorderLayout.CENTER);
        sidebar.add(summaryCard, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B");
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel lblCartTitle = new JLabel("🛒 Giỏ hàng");
        lblCartTitle.setFont(new Font("Inter", Font.BOLD, 16));
        lblCartTitle.setForeground(TEXT_PRIMARY);

        cartItemsContainer = new JPanel();
        cartItemsContainer.setLayout(new BoxLayout(cartItemsContainer, BoxLayout.Y_AXIS));
        cartItemsContainer.setOpaque(false);

        JScrollPane scrollCart = new JScrollPane(cartItemsContainer);
        scrollCart.setBorder(BorderFactory.createEmptyBorder());
        scrollCart.setOpaque(false);
        scrollCart.getViewport().setOpaque(false);
        scrollCart.getVerticalScrollBar().setUnitIncrement(12);
        scrollCart.setPreferredSize(new Dimension(0, 250));

        panel.add(lblCartTitle, BorderLayout.NORTH);
        panel.add(scrollCart, BorderLayout.CENTER);

        return panel;
    }

    private void loadProducts() {
        productsContainer.removeAll();
        JLabel lblLoading = new JLabel("Đang tải sản phẩm...", SwingConstants.CENTER);
        lblLoading.setForeground(TEXT_PRIMARY);
        lblLoading.setFont(new Font("Inter", Font.ITALIC, 14));
        productsContainer.add(lblLoading);
        productsContainer.revalidate();
        productsContainer.repaint();

        new SwingWorker<List<ProductDTO>, Void>() {
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
                    AppMessageDialogs.showError(SnacksSelectionPanel.this, "Lỗi", "Lỗi tải sản phẩm: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void onStepActivated() {
        syncCartToState();
    }

    @Override
    public void onCartUpdated() {
        SwingUtilities.invokeLater(() -> {
            cartItemsContainer.removeAll();
            Map<ProductDTO, Integer> items = cartManager.getItems();

            if (items.isEmpty()) {
                JLabel lblEmpty = new JLabel("Giỏ hàng trống");
                lblEmpty.setForeground(TEXT_SECONDARY);
                lblEmpty.setFont(new Font("Inter", Font.ITALIC, 13));
                cartItemsContainer.add(lblEmpty);
            } else {
                for (Map.Entry<ProductDTO, Integer> entry : items.entrySet()) {
                    cartItemsContainer.add(new CartItemPanel(entry.getKey(), entry.getValue()));
                    cartItemsContainer.add(Box.createVerticalStrut(8));
                }
            }

            cartItemsContainer.revalidate();
            cartItemsContainer.repaint();

            syncCartToState();
        });
    }

    private void syncCartToState() {
        state.setSnacksFromCartManager(cartManager.getItems());
    }
}
