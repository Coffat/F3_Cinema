package com.f3cinema.app.ui.staff.ticketing.step4;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.entity.enums.PaymentMethod;
import com.f3cinema.app.service.TicketingService;
import com.f3cinema.app.service.VoucherService;
import com.f3cinema.app.service.cart.CartManager;
import com.f3cinema.app.service.impl.TicketingServiceImpl;
import com.f3cinema.app.service.payment.MomoPaymentService;
import com.f3cinema.app.service.impl.TransactionHistoryServiceImpl;
import com.f3cinema.app.util.pdf.InvoiceExportService;
import com.f3cinema.app.ui.staff.ticketing.TicketOrderState;
import com.f3cinema.app.ui.staff.ticketing.TicketingFlowPanel;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.common.dialog.MomoQrDialog;
import com.f3cinema.app.ui.staff.ticketing.components.CustomerLookupPanel;
import com.f3cinema.app.ui.staff.ticketing.components.OrderSummaryCard;
import com.f3cinema.app.ui.staff.ticketing.components.PointRedemptionPanel;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Step 4: Professional payment panel with optimal layout.
 * Full-width order details, tabbed discount section, proper spacing.
 */
public class PaymentPanel extends JPanel {

    private static final Color TEXT_PRIMARY = new Color(0xF8FAFC);
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);
    private static final Color ACCENT_PRIMARY = new Color(0x6366F1);
    private static final Color SUCCESS = new Color(0x10B981);

    private final TicketingFlowPanel navigator;
    private final TicketOrderState state;
    private final TicketingService ticketingService;
    private final VoucherService voucherService;

    private CustomerLookupPanel customerLookupPanel;
    private PointRedemptionPanel pointRedemptionPanel;
    private JRadioButton rbDiscountPoints;
    private JRadioButton rbDiscountVoucher;
    private ButtonGroup discountModeGroup;
    private JPanel discountContentPanel;
    private JTextField txtManualVoucher;
    private JLabel lblVoucherStatus;
    private ButtonGroup paymentMethodGroup;
    private JRadioButton rbPayCash;
    private JRadioButton rbPayCard;
    private String selectedMethod = "CASH";
    private OrderSummaryCard summaryCard;
    private JButton btnConfirm;
    private JPanel orderDetailsContent;

    public PaymentPanel(TicketingFlowPanel navigator) {
        this.navigator = navigator;
        this.state = TicketOrderState.getInstance();
        this.ticketingService = TicketingServiceImpl.getInstance();
        this.voucherService = new VoucherService();

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        initUI();
        attachListeners();
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
        contentBody.setLayout(new BorderLayout(20, 20));
        contentBody.setOpaque(false);
        contentBody.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topSection = createTopSection();
        contentBody.add(topSection, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(680);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOpaque(false);

        splitPane.setLeftComponent(createMainArea());
        splitPane.setRightComponent(createRightSidebar());

        contentBody.add(splitPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottomBar.setOpaque(false);

        btnConfirm = new JButton("XÁC NHẬN THANH TOÁN");
        FlatSVGIcon iconCheck = new FlatSVGIcon("icons/ticket.svg", 18, 18);
        iconCheck.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
        btnConfirm.setIcon(iconCheck);
        btnConfirm.setFont(new Font("Inter", Font.BOLD, 16));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setBackground(SUCCESS);
        btnConfirm.setPreferredSize(new Dimension(300, 52));
        btnConfirm.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0;");
        btnConfirm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConfirm.addActionListener(e -> confirmPayment());

        bottomBar.add(btnConfirm);
        contentBody.add(bottomBar, BorderLayout.SOUTH);

        add(contentBody, BorderLayout.CENTER);
    }

    private JPanel createTopSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel infoWrapper = new JPanel();
        infoWrapper.setLayout(new BoxLayout(infoWrapper, BoxLayout.Y_AXIS));
        infoWrapper.setOpaque(false);

        JLabel lblStep = new JLabel("Bước 4/4");
        lblStep.setFont(new Font("Inter", Font.PLAIN, 13));
        lblStep.setForeground(TEXT_SECONDARY);
        lblStep.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Thanh toán");
        FlatSVGIcon icon = new FlatSVGIcon("icons/credit-card.svg", 22, 22);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> TEXT_PRIMARY));
        lblTitle.setIcon(icon);
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

    private JScrollPane createMainArea() {
        JPanel mainArea = new JPanel();
        mainArea.setLayout(new BoxLayout(mainArea, BoxLayout.Y_AXIS));
        mainArea.setOpaque(false);

        customerLookupPanel = new CustomerLookupPanel();
        mainArea.add(customerLookupPanel);
        mainArea.add(Box.createVerticalStrut(20));

        mainArea.add(createOrderDetailsPanel());

        JScrollPane scrollPane = new JScrollPane(mainArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    private JPanel createOrderDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B");
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel lblTitle = new JLabel("Chi tiết đơn hàng");
        FlatSVGIcon icon = new FlatSVGIcon("icons/box.svg", 18, 18);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> TEXT_PRIMARY));
        lblTitle.setIcon(icon);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 17));
        lblTitle.setForeground(TEXT_PRIMARY);

        orderDetailsContent = new JPanel();
        orderDetailsContent.setLayout(new BoxLayout(orderDetailsContent, BoxLayout.Y_AXIS));
        orderDetailsContent.setOpaque(false);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(orderDetailsContent, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDiscountSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B");
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JLabel lblTitle = new JLabel("Giảm giá");
        FlatSVGIcon icon = new FlatSVGIcon("icons/gift.svg", 16, 16);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> TEXT_PRIMARY));
        lblTitle.setIcon(icon);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);

        rbDiscountPoints = new JRadioButton("Đổi điểm");
        rbDiscountVoucher = new JRadioButton("Nhập mã voucher");
        discountModeGroup = new ButtonGroup();
        discountModeGroup.add(rbDiscountPoints);
        discountModeGroup.add(rbDiscountVoucher);
        rbDiscountPoints.setSelected(true);

        styleDiscountRadio(rbDiscountPoints);
        styleDiscountRadio(rbDiscountVoucher);

        pointRedemptionPanel = new PointRedemptionPanel();
        pointRedemptionPanel.setBorder(new EmptyBorder(14, 4, 4, 4));
        JPanel manualVoucherPanel = createManualVoucherPanel();
        discountContentPanel = new JPanel(new CardLayout());
        discountContentPanel.setOpaque(false);
        discountContentPanel.add(pointRedemptionPanel, "POINTS");
        discountContentPanel.add(manualVoucherPanel, "VOUCHER");

        rbDiscountPoints.addActionListener(e -> {
            CardLayout cl = (CardLayout) discountContentPanel.getLayout();
            cl.show(discountContentPanel, "POINTS");
            clearManualVoucher();
        });
        rbDiscountVoucher.addActionListener(e -> {
            CardLayout cl = (CardLayout) discountContentPanel.getLayout();
            cl.show(discountContentPanel, "VOUCHER");
            state.setPointRedemption(null);
        });

        panel.add(lblTitle, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        JPanel choiceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        choiceRow.setOpaque(false);
        choiceRow.add(rbDiscountPoints);
        choiceRow.add(rbDiscountVoucher);
        body.add(choiceRow, BorderLayout.NORTH);
        body.add(discountContentPanel, BorderLayout.CENTER);
        panel.add(body, BorderLayout.CENTER);

        return panel;
    }

    private void styleDiscountRadio(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setFont(new Font("Inter", Font.PLAIN, 13));
        rb.setForeground(TEXT_PRIMARY);
        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JPanel createManualVoucherPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 4, 4, 4));

        JLabel lblDesc = new JLabel("Nhập mã giảm giá");
        lblDesc.setFont(new Font("Inter", Font.PLAIN, 12));
        lblDesc.setForeground(TEXT_SECONDARY);
        lblDesc.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(lblDesc);
        panel.add(Box.createVerticalStrut(10));

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        txtManualVoucher = new JTextField();
        txtManualVoucher.setFont(new Font("Inter", Font.PLAIN, 13));
        txtManualVoucher.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "VD: F3CINEMA20");
        txtManualVoucher.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; background: #0F172A; borderColor: #334155;");
        txtManualVoucher.setForeground(TEXT_PRIMARY);
        txtManualVoucher.setCaretColor(TEXT_PRIMARY);

        JButton btnApply = new JButton("Áp dụng");
        btnApply.setFont(new Font("Inter", Font.BOLD, 12));
        btnApply.setForeground(Color.WHITE);
        btnApply.setBackground(ACCENT_PRIMARY);
        btnApply.setPreferredSize(new Dimension(90, 40));
        btnApply.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0;");
        btnApply.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnApply.addActionListener(e -> applyManualVoucher());

        inputRow.add(txtManualVoucher, BorderLayout.CENTER);
        inputRow.add(btnApply, BorderLayout.EAST);

        lblVoucherStatus = new JLabel(" ");
        lblVoucherStatus.setFont(new Font("Inter", Font.PLAIN, 11));
        lblVoucherStatus.setBorder(new EmptyBorder(8, 0, 0, 0));
        lblVoucherStatus.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(inputRow);
        panel.add(lblVoucherStatus);

        return panel;
    }

    private JPanel createPaymentMethodPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(true);
        panel.setBackground(new Color(0x1E293B));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x334155), 1, true),
                new EmptyBorder(18, 18, 18, 18)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel lblMethod = new JLabel("Phương thức");
        lblMethod.setFont(new Font("Inter", Font.BOLD, 15));
        lblMethod.setForeground(TEXT_PRIMARY);

        paymentMethodGroup = new ButtonGroup();
        rbPayCash = createPaymentMethodRadio("Tiền mặt", "CASH");
        rbPayMomo = createPaymentMethodRadio("MoMo", "MOMO_TEST");
        rbPayCard = createPaymentMethodRadio("Thẻ NH", "CARD");
        paymentMethodGroup.add(rbPayCash);
        paymentMethodGroup.add(rbPayMomo);
        paymentMethodGroup.add(rbPayCard);

        JPanel row = new JPanel(new GridLayout(1, 3, 4, 0));
        row.setOpaque(false);
        row.add(rbPayCash);
        row.add(rbPayMomo);
        row.add(rbPayCard);

        syncPaymentMethodRadiosFromState();

        panel.add(lblMethod, BorderLayout.NORTH);
        panel.add(row, BorderLayout.CENTER);

        return panel;
    }

    private JRadioButton createPaymentMethodRadio(String label, String method) {
        JRadioButton rb = new JRadioButton(label);
        rb.setFont(new Font("Inter", Font.BOLD, 12));
        rb.setForeground(TEXT_PRIMARY);
        rb.setOpaque(false);
        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rb.addActionListener(e -> {
            if (rb.isSelected()) {
                selectedMethod = method;
                state.setPaymentMethod(method);
            }
        });
        return rb;
    }

    private JRadioButton rbPayMomo;

    private void syncPaymentMethodRadiosFromState() {
        String m = state.getPaymentMethod() != null ? state.getPaymentMethod() : "CASH";
        selectedMethod = m;
        if ("MOMO_TEST".equals(m) || "BANK_TRANSFER".equals(m)) {
            if (rbPayMomo != null) rbPayMomo.setSelected(true);
        } else if ("CARD".equals(m)) {
            if (rbPayCard != null) rbPayCard.setSelected(true);
        } else {
            if (rbPayCash != null) rbPayCash.setSelected(true);
        }
    }

    private JScrollPane createRightSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(360, 0));

        summaryCard = new OrderSummaryCard();
        sidebar.add(summaryCard);
        sidebar.add(Box.createVerticalStrut(16));

        sidebar.add(createDiscountSection());
        sidebar.add(Box.createVerticalStrut(16));

        sidebar.add(createPaymentMethodPanel());

        JScrollPane scrollPane = new JScrollPane(sidebar);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    private void attachListeners() {
        state.addPropertyChangeListener("selectedSeats", this::onStateChanged);
        state.addPropertyChangeListener("snacksTotal", this::onStateChanged);
        state.addPropertyChangeListener("discount", this::onStateChanged);
        state.addPropertyChangeListener("grandTotal", this::onStateChanged);
    }

    public void onStepActivated() {
        // Sync once from cart source before rendering details.
        state.setSnacksFromCartManager(CartManager.getInstance().getItems());
        refreshOrderDetails();
        if (rbPayCash != null) {
            syncPaymentMethodRadiosFromState();
        }
    }

    private void onStateChanged(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(this::refreshOrderDetails);
    }

    private void refreshOrderDetails() {
        if (orderDetailsContent == null) return;
        
        orderDetailsContent.removeAll();

        boolean hasSeats = !state.getSelectedSeats().isEmpty();
        boolean hasShowtime = state.getShowtimeId() != null;

        if (hasShowtime) {
            String movieTitle = state.getMovieTitle() != null ? state.getMovieTitle() : "N/A";
            String roomName = state.getRoomName() != null ? state.getRoomName() : "N/A";
            String startTime = state.getStartTime() != null ? state.getStartTime() : "N/A";
            
            orderDetailsContent.add(createOrderRow("Thông tin phim", 
                    movieTitle, "video"));
            orderDetailsContent.add(Box.createVerticalStrut(10));
            
            orderDetailsContent.add(createOrderRow("Phòng & Giờ chiếu",
                    roomName + " • " + startTime, "map-pin"));
            orderDetailsContent.add(Box.createVerticalStrut(10));

            orderDetailsContent.add(createDivider());
            orderDetailsContent.add(Box.createVerticalStrut(10));
        }

        if (hasSeats) {
            for (var seat : state.getSelectedSeats()) {
                orderDetailsContent.add(createOrderRow("Ghế " + seat.name(),
                        formatPrice(seat.price()), "ticket"));
                orderDetailsContent.add(Box.createVerticalStrut(8));
            }
        }

        if (state.hasSnacks()) {
            if (hasSeats) {
                orderDetailsContent.add(createDivider());
                orderDetailsContent.add(Box.createVerticalStrut(10));
            }
            
            for (Map.Entry<ProductDTO, Integer> entry : state.getSnacksCart().entrySet()) {
                ProductDTO product = entry.getKey();
                int qty = entry.getValue();
                BigDecimal itemTotal = product.price().multiply(BigDecimal.valueOf(qty));
                
                String snackInfo = product.name() + " x" + qty;
                orderDetailsContent.add(createOrderRow(snackInfo,
                        formatPrice(itemTotal), "popcorn"));
                orderDetailsContent.add(Box.createVerticalStrut(8));
            }
        }

        if (!hasSeats && !state.hasSnacks()) {
            JLabel lblEmpty = new JLabel("Chưa có sản phẩm nào");
            lblEmpty.setFont(new Font("Inter", Font.ITALIC, 14));
            lblEmpty.setForeground(TEXT_SECONDARY);
            lblEmpty.setAlignmentX(LEFT_ALIGNMENT);
            orderDetailsContent.add(lblEmpty);
        }

        orderDetailsContent.revalidate();
        orderDetailsContent.repaint();
    }

    private JPanel createOrderRow(String label, String value, String iconName) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        FlatSVGIcon icon = new FlatSVGIcon("icons/" + iconName + ".svg", 20, 20);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> ACCENT_PRIMARY));
        JLabel iconLabel = new JLabel(icon);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        lblLabel.setForeground(TEXT_SECONDARY);
        lblLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblValue = new JLabel("<html>" + value.replace("\n", "<br>") + "</html>");
        lblValue.setFont(new Font("Inter", Font.BOLD, 14));
        lblValue.setForeground(TEXT_PRIMARY);
        lblValue.setAlignmentX(LEFT_ALIGNMENT);

        textPanel.add(lblLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblValue);

        row.add(iconLabel, BorderLayout.WEST);
        row.add(textPanel, BorderLayout.CENTER);

        return row;
    }

    private JSeparator createDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 15));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private void applyManualVoucher() {
        String code = txtManualVoucher.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            showVoucherError("Vui lòng nhập mã voucher!");
            return;
        }

        state.setPointRedemption(null);

        BigDecimal seatTotal = state.getSeatTotal();
        BigDecimal snacksTotal = state.getSnacksTotal();
        BigDecimal currentTotal = seatTotal.add(snacksTotal);

        Map<String, Object> context = new HashMap<>();
        context.put("seatCount", state.getSelectedSeatIds().size());
        context.put("seatTotal", seatTotal);
        context.put("snacksCart", state.getSnacksCartByProductId());
        context.put("snacksTotal", snacksTotal);
        context.put("comboTotal", calculateComboTotal());
        context.put("orderAmount", currentTotal);

        try {
            BigDecimal discount = voucherService.applyVoucher(code, currentTotal, context);
            showVoucherSuccess("Giảm " + formatPrice(discount));
            state.setDiscount(discount);
        } catch (IllegalArgumentException ex) {
            showVoucherError(ex.getMessage());
            state.setDiscount(BigDecimal.ZERO);
        } catch (Exception ex) {
            showVoucherError("Lỗi hệ thống. Vui lòng thử lại.");
            state.setDiscount(BigDecimal.ZERO);
        }
    }
    
    private BigDecimal calculateComboTotal() {
        Map<Long, Integer> snacksCart = state.getSnacksCartByProductId();
        if (snacksCart == null || snacksCart.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal comboTotal = BigDecimal.ZERO;
        Map<ProductDTO, Integer> cartItems = CartManager.getInstance().getItems();
        
        for (Map.Entry<ProductDTO, Integer> entry : cartItems.entrySet()) {
            ProductDTO product = entry.getKey();
            if (product != null && product.name().toLowerCase().contains("combo")) {
                comboTotal = comboTotal.add(
                    product.price().multiply(BigDecimal.valueOf(entry.getValue()))
                );
            }
        }
        
        return comboTotal;
    }

    private void clearManualVoucher() {
        if (txtManualVoucher != null) {
            txtManualVoucher.setText("");
            lblVoucherStatus.setText(" ");
        }
    }

    private void showVoucherSuccess(String message) {
        FlatSVGIcon checkIcon = new FlatSVGIcon("icons/check.svg", 12, 12);
        checkIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> SUCCESS));
        lblVoucherStatus.setIcon(checkIcon);
        lblVoucherStatus.setText(message);
        lblVoucherStatus.setForeground(SUCCESS);
    }

    private void showVoucherError(String message) {
        lblVoucherStatus.setIcon(null);
        lblVoucherStatus.setText("✗ " + message);
        lblVoucherStatus.setForeground(new Color(0xEF4444));
    }

    private void confirmPayment() {
        if (!state.hasSeats() && !state.hasSnacks()) {
            AppMessageDialogs.showError(this, "Lỗi", "Vui lòng chọn ít nhất một sản phẩm (ghế hoặc bắp nước)!");
            return;
        }

        String confirmMsg = buildConfirmMessage();
        if (!AppMessageDialogs.confirmYesNo(this, "Xác nhận thanh toán", confirmMsg)) return;

        if ("MOMO_TEST".equals(selectedMethod) || "BANK_TRANSFER".equals(selectedMethod)) {
            startMomoPaymentFlow();
            return;
        }

        PaymentMethod pm = resolveUiPaymentMethod(selectedMethod);
        runStandardBooking(pm, null);
    }

    private void startMomoPaymentFlow() {
        // Generate an orderId that resembles F3 invoice code (F3-yyyyMMdd-XXX) to avoid looking completely different.
        // We append a random salt to guarantee uniqueness required by MoMo's strict policy.
        String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String salt = String.format("%04d", System.currentTimeMillis() % 10000);
        String orderId = "F3-" + datePart + "-" + salt;
        String orderInfo = "Thanh toan don " + orderId;
        try {
            MomoPaymentService momo = MomoPaymentService.loadFromClasspath();
            MomoPaymentService.MomoInitResponse init = momo.createTestQr(state.getGrandTotal(), orderInfo, orderId);

            String qrUrl = init.bestQrUrl();
            if (qrUrl == null || qrUrl.isBlank()) {
                throw new IllegalStateException("MoMo không trả về QR/payUrl hợp lệ.");
            }

            MomoQrDialog.AutoCheckFn autoCheck = () -> momo.queryStatus(init.orderId(), init.requestId()).paid();
            boolean confirmed = MomoQrDialog.showAndConfirm(
                    this,
                    qrUrl,
                    formatPrice(state.getGrandTotal()),
                    init.orderId(),
                    autoCheck,
                    momo.autoCheckTimeoutSeconds(),
                    momo.autoCheckPollSeconds()
            );
            if (!confirmed) {
                return;
            }

            MomoPaymentService.MomoQueryResult latest = momo.queryStatus(init.orderId(), init.requestId());
            String transactionRef = latest.transactionId() != null && !latest.transactionId().isBlank()
                    ? latest.transactionId()
                    : init.orderId();

            runStandardBooking(PaymentMethod.BANK_TRANSFER, transactionRef);
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            AppMessageDialogs.showError(this, "MoMo Test", "Không khởi tạo được thanh toán MoMo: " + root.getMessage());
        }
    }

    private void runStandardBooking(PaymentMethod paymentMethod, String externalTxnId) {
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang xử lý...");

        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                Long customerId = state.hasCustomer() ? state.getCustomer().getId() : null;

                return ticketingService.bookSeatsWithLoyalty(
                        state.getShowtimeId(),
                        state.getSelectedSeatIds(),
                        state.getSnacksCartByProductId(),
                        customerId,
                        state.getSelectedTier(),
                        paymentMethod,
                        externalTxnId
                );
            }

            @Override
            protected void done() {
                try {
                    Long invoiceId = get();
                    showSuccessDialog(invoiceId);

                    CartManager.getInstance().clearCart();
                    if (customerLookupPanel != null) customerLookupPanel.reset();
                    if (pointRedemptionPanel != null) pointRedemptionPanel.reset();
                    clearManualVoucher();
                    navigator.reset();

                } catch (Exception e) {
                    Throwable root = e;
                    while (root.getCause() != null && root.getCause() != root) {
                        root = root.getCause();
                    }
                    AppMessageDialogs.showError(PaymentPanel.this, "Lỗi",
                            "Lỗi khi thanh toán: " + root.getMessage());
                } finally {
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("XÁC NHẬN THANH TOÁN");
                }
            }
        }.execute();
    }

    private static PaymentMethod resolveUiPaymentMethod(String ui) {
        if ("MOMO_TEST".equals(ui) || "BANK_TRANSFER".equals(ui)) {
            return PaymentMethod.BANK_TRANSFER;
        }
        if ("CARD".equals(ui)) {
            return PaymentMethod.CARD;
        }
        return PaymentMethod.CASH;
    }

    private String buildConfirmMessage() {
        StringBuilder msg = new StringBuilder();
        msg.append("Xác nhận thanh toán ").append(formatPrice(state.getGrandTotal())).append("?\n\n");
        
        if (state.hasCustomer()) {
            msg.append("Khách hàng: ").append(state.getCustomer().getFullName()).append("\n");
            if (state.getPointsToRedeem() > 0) {
                msg.append("Điểm sử dụng: ").append(String.format("%,d", state.getPointsToRedeem())).append(" điểm\n");
            }
        }
        
        msg.append("Phương thức: ").append(getPaymentMethodLabel());
        
        return msg.toString();
    }

    private void showSuccessDialog(Long invoiceId) {
        String displayCode = "#" + invoiceId;
        try {
            var d = TransactionHistoryServiceImpl.getInstance().getTransactionDetail(invoiceId);
            if (d.invoiceCode() != null && !d.invoiceCode().isBlank()) {
                displayCode = d.invoiceCode();
            }
        } catch (Exception ignored) {
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Thanh toán thành công", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(460, 320);
        dialog.setLocationRelativeTo(this);
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(new Color(15, 23, 42));
        JLabel check = new JLabel("✓", SwingConstants.CENTER);
        check.setFont(new Font("Inter", Font.BOLD, 52));
        check.setForeground(SUCCESS);
        String finalDisplayCode = displayCode;
        JLabel info = new JLabel("<html><div style='text-align:center'>Thanh toán thành công<br/><b>" + finalDisplayCode + "</b><br/><span style='color:#94a3b8;font-size:12px'>ID: #" + invoiceId + "</span><br/>Tổng tiền: " + formatPrice(state.getGrandTotal()) + "</div></html>", SwingConstants.CENTER);
        info.setForeground(TEXT_PRIMARY);
        info.setFont(new Font("Inter", Font.PLAIN, 14));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actions.setOpaque(false);
        JButton btnPrint = new JButton("In hóa đơn (PDF)");
        JButton btnContinue = new JButton("Bán vé tiếp");
        btnPrint.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #334155;");
        btnContinue.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #6366F1; borderWidth: 0;");
        btnPrint.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            String safeName = finalDisplayCode.replaceAll("[^A-Za-z0-9\\-]", "_");
            chooser.setSelectedFile(new File("hoa-don-" + safeName + ".pdf"));
            if (chooser.showSaveDialog(dialog) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            Path target = chooser.getSelectedFile().toPath();
            btnPrint.setEnabled(false);
            new SwingWorker<Path, Void>() {
                @Override
                protected Path doInBackground() throws Exception {
                    var detail = TransactionHistoryServiceImpl.getInstance().getTransactionDetail(invoiceId);
                    return InvoiceExportService.getInstance().exportInvoice(detail, target);
                }

                @Override
                protected void done() {
                    btnPrint.setEnabled(true);
                    try {
                        Path saved = get();
                        AppMessageDialogs.showInfo(dialog, "Thành công", "Đã xuất PDF:\n" + saved.toAbsolutePath());
                    } catch (Exception ex) {
                        Throwable root = ex;
                        while (root.getCause() != null && root.getCause() != root) {
                            root = root.getCause();
                        }
                        AppMessageDialogs.showError(dialog, "Không in được", root.getMessage());
                    }
                }
            }.execute();
        });
        btnContinue.addActionListener(e -> dialog.dispose());
        actions.add(btnPrint);
        actions.add(btnContinue);
        root.add(check, BorderLayout.NORTH);
        root.add(info, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private String getPaymentMethodLabel() {
        if ("MOMO_TEST".equals(selectedMethod) || "BANK_TRANSFER".equals(selectedMethod)) return "MoMo/CK";
        if ("CARD".equals(selectedMethod)) return "Thẻ ngân hàng";
        return "Tiền mặt";
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0 VNĐ";
        return String.format("%,.0f VNĐ", price.doubleValue());
    }
}
