package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.dto.StockReceiptDTO;
import com.f3cinema.app.dto.StockReceiptSummaryDTO;
import com.f3cinema.app.service.impl.InventoryServiceImpl;
import com.f3cinema.app.service.impl.StockReceiptServiceImpl;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class WarehousePanel extends BaseDashboardModule {

    private JPanel quickStatsRow;
    private JPanel productsContainer;
    private JPanel receiptsTimelineContainer;
    private JLabel totalProductsValue;
    private JLabel lowStockValue;
    private JLabel outStockValue;
    private JLabel totalValueLabel;

    private SwingWorker<List<ProductDTO>, Void> inventoryWorker;
    private SwingWorker<List<StockReceiptSummaryDTO>, Void> historyWorker;
    
    // Java 21 Virtual Thread Executor for Zero Latency UX
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public WarehousePanel() {
        super("Kho & Sản phẩm", "Home > Warehouse & Products");
        initUI();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false);
        tabbedPane.putClientProperty(FlatClientProperties.STYLE,
                "tabHeight: 48; tabArc: 12; tabSelectionHeight: 3; tabSelectionArc: 0; tabSeparatorColor: #334155");
        tabbedPane.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
        tabbedPane.setForeground(ThemeConfig.TEXT_PRIMARY);

        FlatSVGIcon productsIcon = new FlatSVGIcon("icons/box.svg", 20, 20);
        productsIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> ThemeConfig.TEXT_PRIMARY));
        FlatSVGIcon receiptIcon = new FlatSVGIcon("icons/clipboard.svg", 20, 20);
        receiptIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> ThemeConfig.TEXT_PRIMARY));

        tabbedPane.addTab("Sản phẩm", productsIcon, createProductTab());
        tabbedPane.addTab("Phiếu nhập kho", receiptIcon, createHistoryTab());

        JButton btnAddProduct = new JButton("Thêm Sản phẩm");
        stylePrimaryButton(btnAddProduct);
        btnAddProduct.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD, 12f));
        btnAddProduct.setPreferredSize(new Dimension(112, 28));
        btnAddProduct.setMinimumSize(new Dimension(112, 28));
        btnAddProduct.setMaximumSize(new Dimension(112, 28));
        btnAddProduct.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0; focusWidth: 0; innerFocusWidth: 0");
        btnAddProduct.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            ProductDialog dialog = new ProductDialog((JFrame) window, null, this::loadInventoryData);
            dialog.setVisible(true);
        });
        JPanel trailing = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        trailing.setOpaque(false);
        trailing.add(btnAddProduct);
        tabbedPane.putClientProperty("JTabbedPane.trailingComponent", trailing);
        tabbedPane.addChangeListener(e -> btnAddProduct.setVisible(tabbedPane.getSelectedIndex() == 0));
        btnAddProduct.setVisible(true);

        contentBody.setLayout(new BorderLayout(0, 16));
        contentBody.setBackground(ThemeConfig.BG_MAIN);
        quickStatsRow = buildQuickStats();
        contentBody.add(quickStatsRow, BorderLayout.NORTH);
        contentBody.add(tabbedPane, BorderLayout.CENTER);

        loadAllData();
    }

    private JPanel buildQuickStats() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        totalProductsValue = new JLabel("0");
        lowStockValue = new JLabel("0");
        outStockValue = new JLabel("0");
        totalValueLabel = new JLabel("0");
        row.add(createMiniStatCard("Tổng sản phẩm", totalProductsValue, "icons/box.svg", ThemeConfig.ACCENT_COLOR));
        row.add(createMiniStatCard("Sắp hết hàng", lowStockValue, "icons/alert.svg", Color.decode("#F59E0B")));
        row.add(createMiniStatCard("Hết hàng", outStockValue, "icons/x-circle.svg", ThemeConfig.TEXT_DANGER));
        row.add(createMiniStatCard("Tổng giá trị", totalValueLabel, "icons/dollar.svg", ThemeConfig.TEXT_SUCCESS));
        return row;
    }

    private JPanel createMiniStatCard(String label, JLabel value, String icon, Color accent) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(ThemeConfig.BG_CARD);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16");
        FlatSVGIcon svgIcon = new FlatSVGIcon(icon, 28, 28);
        svgIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> accent));
        JLabel iconLabel = new JLabel(svgIcon);
        value.setFont(ThemeConfig.FONT_H1);
        value.setForeground(ThemeConfig.TEXT_PRIMARY);
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeConfig.FONT_SMALL);
        lbl.setForeground(ThemeConfig.TEXT_SECONDARY);
        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(value);
        t.add(lbl);
        card.add(iconLabel, BorderLayout.WEST);
        card.add(t, BorderLayout.CENTER);
        return card;
    }

    private JPanel createProductTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        productsContainer = new JPanel(new com.f3cinema.app.util.WrapLayout(FlowLayout.LEFT, 24, 24));
        productsContainer.setOpaque(true);
        productsContainer.setBackground(ThemeConfig.BG_MAIN);
        productsContainer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(true);
        inner.setBackground(ThemeConfig.BG_MAIN);
        inner.add(productsContainer, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(inner);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(ThemeConfig.BG_MAIN);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);

        JButton btnImportStock = new JButton("Nhập kho");
        stylePrimaryButton(btnImportStock);
        btnImportStock.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            StockReceiptDialog dialog = new StockReceiptDialog((JFrame) window, this::loadAllData);
            dialog.setVisible(true);
        });
        topPanel.add(btnImportStock);

        panel.add(topPanel, BorderLayout.NORTH);
        receiptsTimelineContainer = new JPanel();
        receiptsTimelineContainer.setLayout(new BoxLayout(receiptsTimelineContainer, BoxLayout.Y_AXIS));
        receiptsTimelineContainer.setOpaque(false);
        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(true);
        inner.setBackground(ThemeConfig.BG_MAIN);
        inner.add(receiptsTimelineContainer, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(inner);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeConfig.BG_MAIN);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void loadAllData() {
        loadInventoryData();
        loadHistoryData();
    }

    public void loadInventoryData() {
        if (inventoryWorker != null && !inventoryWorker.isDone()) {
            inventoryWorker.cancel(true);
        }

        inventoryWorker = new SwingWorker<List<ProductDTO>, Void>() {
            @Override
            protected List<ProductDTO> doInBackground() throws Exception {
                return InventoryServiceImpl.getInstance().getAllInventory();
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    List<ProductDTO> products = get();
                    renderProductCards(products);
                    updateStats(products);
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    AppMessageDialogs.showError(WarehousePanel.this, "Lỗi", "Lỗi tải dữ liệu Hệ thống: " + msg);
                }
            }
        };
        inventoryWorker.execute();
    }

    public void loadHistoryData() {
        if (receiptsTimelineContainer == null)
            return;

        if (historyWorker != null && !historyWorker.isDone()) {
            historyWorker.cancel(true);
        }

        historyWorker = new SwingWorker<List<StockReceiptSummaryDTO>, Void>() {
            @Override
            protected List<StockReceiptSummaryDTO> doInBackground() throws Exception {
                return StockReceiptServiceImpl.getInstance().getAllReceipts();
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    renderReceiptTimeline(get());
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    AppMessageDialogs.showError(WarehousePanel.this, "Lỗi", "Lỗi tải dữ liệu Lịch sử Nhập kho: " + msg);
                }
            }
        };
        historyWorker.execute();
    }

    private void renderProductCards(List<ProductDTO> products) {
        productsContainer.removeAll();
        for (ProductDTO p : products) {
            ProductCard card = new ProductCard(
                    p,
                    () -> openEditDialog(p.id()),
                    () -> openAdjustStockDialog(p.id(), p.name(), p.unit()),
                    () -> handleDeleteProduct(p.id(), p.name()));
            productsContainer.add(card);
        }
        productsContainer.revalidate();
        productsContainer.repaint();
    }

    private void openEditDialog(Long productId) {
        Window window = SwingUtilities.getWindowAncestor(this);
        ProductDialog dialog = new ProductDialog((JFrame) window, productId, this::loadInventoryData);
        dialog.setVisible(true);
    }

    /**
     * Quick Stock Adjustment — Add quantity directly with a small validated input.
     */
    private void openAdjustStockDialog(Long id, String name, String unit) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        JLabel lbl = new JLabel("Nhập số lượng " + unit + " muốn cộng thêm vào kho cho '" + name + "':");
        lbl.setFont(ThemeConfig.FONT_SMALL);
        lbl.setForeground(ThemeConfig.TEXT_PRIMARY);
        
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spinner.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #0F172A; foreground: #FFFFFF;");
        
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(spinner, BorderLayout.CENTER);
        
        int choice = JOptionPane.showConfirmDialog(this, panel, "Cập Nhật Tồn Kho Nhanh", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (choice == JOptionPane.OK_OPTION) {
            int quantity = (Integer) spinner.getValue();
            setLoadingState(true);
            
            CompletableFuture.runAsync(() -> {
                try {
                    InventoryServiceImpl.getInstance().addStock(id, quantity);
                    SwingUtilities.invokeLater(() -> {
                        setLoadingState(false);
                        AppMessageDialogs.showInfo(WarehousePanel.this, "Hoàn Tất", 
                                "Đã nhập thành công " + quantity + " " + unit + " cho " + name);
                        loadInventoryData();
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        setLoadingState(false);
                        AppMessageDialogs.showError(WarehousePanel.this, "Lỗi", e.getMessage());
                    });
                }
            }, virtualThreadExecutor);
        }
    }

    /**
     * Optimized Delete Logic — Zero Latency UX with Java 21 Virtual Threads.
     */
    private void handleDeleteProduct(Long id, String name) {
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn ngừng kinh doanh sản phẩm '" + name + "'?\nLưu ý: Sản phẩm sẽ bị ẩn khỏi danh sách bán nhưng vẫn lưu lại trong lịch sử hóa đơn.",
                "Xác nhận ngừng kinh doanh", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            setLoadingState(true);

            CompletableFuture.runAsync(() -> {
                try {
                    InventoryServiceImpl.getInstance().deleteProduct(id);
                    SwingUtilities.invokeLater(() -> {
                        setLoadingState(false);
                        AppMessageDialogs.showInfo(WarehousePanel.this, "Hệ Thống", "Đã ngừng kinh doanh sản phẩm: " + name);
                        loadInventoryData();
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        setLoadingState(false);
                        String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        AppMessageDialogs.showError(WarehousePanel.this, "Lỗi Hệ Thống", "Không thể xóa sản phẩm: " + msg);
                    });
                }
            }, virtualThreadExecutor);
        }
    }

    private void setLoadingState(boolean isLoading) {
        contentBody.setEnabled(!isLoading);
        if (isLoading) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
        repaint();
    }

    private void renderReceiptTimeline(List<StockReceiptSummaryDTO> receipts) {
        receiptsTimelineContainer.removeAll();
        AtomicInteger index = new AtomicInteger(0);
        for (StockReceiptSummaryDTO r : receipts) {
            int idx = index.getAndIncrement();
            StockReceiptCard card = new StockReceiptCard(r, idx, () -> openReceiptDetail(r.id()));
            card.setAlignmentX(Component.CENTER_ALIGNMENT);
            receiptsTimelineContainer.add(wrapReceiptRow(card));
            if (idx < receipts.size() - 1) {
                receiptsTimelineContainer.add(createTimelineConnector());
            }
        }
        receiptsTimelineContainer.revalidate();
        receiptsTimelineContainer.repaint();
    }

    private JPanel wrapReceiptRow(JComponent card) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getMaximumSize().height));
        row.add(Box.createHorizontalGlue());
        row.add(card);
        row.add(Box.createHorizontalGlue());
        return row;
    }

    private JPanel createTimelineConnector() {
        JPanel connector = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(ThemeConfig.BORDER_COLOR);
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5, 5}, 0));
                int cx = getWidth() / 2;
                g2.drawLine(cx, 0, cx, getHeight());
                g2.dispose();
            }
        };
        connector.setOpaque(false);
        connector.setAlignmentX(Component.CENTER_ALIGNMENT);
        connector.setPreferredSize(new Dimension(0, 20));
        connector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return connector;
    }

    private void openReceiptDetail(Long receiptId) {
        Window window = SwingUtilities.getWindowAncestor(this);
        new SwingWorker<StockReceiptDTO, Void>() {
            @Override
            protected StockReceiptDTO doInBackground() throws Exception {
                return StockReceiptServiceImpl.getInstance().getReceiptDetails(receiptId);
            }

            @Override
            protected void done() {
                try {
                    StockReceiptDTO detailDTO = get();
                    String receiptIdText = "PN-" + String.format("%04d", receiptId);
                    StockReceiptDetailDialog dialog = new StockReceiptDetailDialog((JFrame) window, receiptIdText, detailDTO);
                    dialog.setVisible(true);
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    AppMessageDialogs.showError(WarehousePanel.this, "Lỗi", "Không thể tải chi tiết phiếu nhập: " + msg);
                }
            }
        }.execute();
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(ThemeConfig.ACCENT_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 40));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0;");
    }

    private void updateStats(List<ProductDTO> products) {
        int total = products.size();
        int low = 0;
        int out = 0;
        BigDecimal value = BigDecimal.ZERO;
        for (ProductDTO p : products) {
            int qty = p.currentQuantity() == null ? 0 : p.currentQuantity();
            int threshold = p.minThreshold() == null ? 0 : p.minThreshold();
            if (qty == 0) out++;
            else if (qty <= threshold) low++;
            value = value.add(p.price().multiply(BigDecimal.valueOf(qty)));
        }
        totalProductsValue.setText(String.valueOf(total));
        lowStockValue.setText(String.valueOf(low));
        outStockValue.setText(String.valueOf(out));
        totalValueLabel.setText(new DecimalFormat("#,##0").format(value));
    }
}
