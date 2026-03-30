package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.service.impl.InventoryServiceImpl;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class WarehousePanel extends BaseDashboardModule {

    private JTable productTable;
    private DefaultTableModel productTableModel;
    private JTable historyTable;
    private DefaultTableModel historyModel;

    private SwingWorker<List<ProductDTO>, Void> inventoryWorker;
    private SwingWorker<List<com.f3cinema.app.dto.StockReceiptSummaryDTO>, Void> historyWorker;

    public WarehousePanel() {
        super("Kho & Sản phẩm", "Home > Warehouse & Products");
        initUI();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false);
        tabbedPane.putClientProperty("JTabbedPane.showTabSeparators", true);
        tabbedPane.setFont(new Font("Inter", Font.BOLD, 14));
        tabbedPane.setForeground(Color.WHITE); // Theo chuẩn giao diện
        UIManager.put("TabbedPane.selectedBackground", Color.decode("#6366F1"));

        // Tab 1: Sản phẩm & Tồn kho
        tabbedPane.addTab("Sản phẩm & Tồn kho", createProductTab());

        // Tab 2: Lịch sử nhập kho
        tabbedPane.addTab("Lịch sử nhập kho", createHistoryTab());

        contentBody.setLayout(new BorderLayout());
        contentBody.add(tabbedPane, BorderLayout.CENTER);

        // Load data on start
        loadAllData();
    }

    private JPanel createProductTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Nút thêm sản phẩm
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);

        JButton btnAddProduct = new JButton("+ Thêm Sản phẩm");
        stylePrimaryButton(btnAddProduct);
        btnAddProduct.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            ProductDialog dialog = new ProductDialog((JFrame) window, this::loadInventoryData);
            dialog.setVisible(true);
        });
        topPanel.add(btnAddProduct);

        panel.add(topPanel, BorderLayout.NORTH);

        // Bảng dữ liệu sản phẩm
        String[] columnNames = { "ID", "Tên sản phẩm", "Đơn giá", "Đơn vị tính", "Tồn kho", "Ngưỡng tối thiểu" };
        productTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = createStyledTable(productTableModel);
        JScrollPane scrollPane = createStyledScrollPane(productTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Nút nhập kho
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);

        JButton btnImportStock = new JButton("+ Nhập kho");
        stylePrimaryButton(btnImportStock);
        btnImportStock.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            StockReceiptDialog dialog = new StockReceiptDialog((JFrame) window, this::loadAllData);
            dialog.setVisible(true);
        });
        topPanel.add(btnImportStock);

        panel.add(topPanel, BorderLayout.NORTH);

        // Bảng dữ liệu lịch sử
        String[] columnNames = { "Mã Phiếu", "Nhà Cung Cấp", "Tổng Tiền", "Ngày Nhập", "Hành Động" };
        historyModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = createStyledTable(historyModel);
        setupHistoryTableActions(historyTable);
        JScrollPane scrollPane = createStyledScrollPane(historyTable);
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

        // Sử dụng SwingWorker để trách giật lag UI Thread
        inventoryWorker = new SwingWorker<List<ProductDTO>, Void>() {
            @Override
            protected List<ProductDTO> doInBackground() throws Exception {
                // Background thread: Call DB
                return InventoryServiceImpl.getInstance().getAllInventory();
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    List<ProductDTO> products = get();
                    productTableModel.setRowCount(0); // clear
                    for (ProductDTO dto : products) {
                        productTableModel.addRow(new Object[] {
                                dto.id(),
                                dto.name(),
                                String.format("%,.0f đ", dto.price()),
                                dto.unit(),
                                dto.currentQuantity(),
                                dto.minThreshold()
                        });
                    }
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(WarehousePanel.this,
                            "Lỗi tải dữ liệu Hệ thống: " + msg,
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        inventoryWorker.execute();
    }

    public void loadHistoryData() {
        if (historyModel == null)
            return;

        if (historyWorker != null && !historyWorker.isDone()) {
            historyWorker.cancel(true);
        }

        historyWorker = new SwingWorker<List<com.f3cinema.app.dto.StockReceiptSummaryDTO>, Void>() {
            @Override
            protected List<com.f3cinema.app.dto.StockReceiptSummaryDTO> doInBackground() throws Exception {
                return com.f3cinema.app.service.impl.StockReceiptServiceImpl.getInstance().getAllReceipts();
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    List<com.f3cinema.app.dto.StockReceiptSummaryDTO> receipts = get();
                    historyModel.setRowCount(0);
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                            .ofPattern("dd/MM/yyyy HH:mm");
                    for (com.f3cinema.app.dto.StockReceiptSummaryDTO dto : receipts) {
                        historyModel.addRow(new Object[] {
                                "PN-" + String.format("%04d", dto.id()),
                                dto.supplier(),
                                String.format("%,.0f đ", dto.totalImportCost()),
                                dto.receiptDate() != null ? dto.receiptDate().format(formatter) : "N/A",
                                "Xem chi tiết"
                        });
                    }
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(WarehousePanel.this,
                            "Lỗi tải dữ liệu Lịch sử Nhập kho: " + msg,
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        historyWorker.execute();
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.decode("#1E293B"));
        table.setForeground(Color.decode("#F8FAFC"));
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.decode("#0F172A"));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setReorderingAllowed(false);

        // Header Style
        table.getTableHeader().setBackground(Color.decode("#0F172A"));
        table.getTableHeader().setForeground(Color.decode("#94A3B8"));
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        // Custom Cell Renderer for Row Hover / Stripe
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12)); // Cell Padding

                if (isSelected) {
                    c.setBackground(Color.decode("#334155"));
                } else {
                    c.setBackground(Color.decode("#1E293B"));
                }

                // Highlight Low Inventory with Red Color
                if (column == 4) {
                    try {
                        int qty = Integer.parseInt(value.toString());
                        int threshold = Integer.parseInt(table.getValueAt(row, 5).toString());
                        if (qty <= threshold)
                            c.setForeground(Color.decode("#F43F5E")); // Danger
                        else
                            c.setForeground(Color.decode("#F8FAFC"));
                    } catch (Exception ignored) {
                    }
                } else if (!isSelected) {
                    c.setForeground(Color.decode("#F8FAFC"));
                }

                return c;
            }
        });

        return table;
    }

    private JScrollPane createStyledScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.decode("#1E293B"));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        return scrollPane;
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(Color.decode("#6366F1")); // Indigo 500
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 45));
        btn.putClientProperty("JButton.buttonType", "roundRect");
    }

    private void setupHistoryTableActions(JTable table) {
        // Vẽ Nút bấm vào cột thứ 4 ("Hành Động")
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JPanel container = new JPanel(new GridBagLayout());
                container.setBackground(isSelected ? Color.decode("#334155") : Color.decode("#1E293B"));

                JButton btn = new JButton(value != null ? value.toString() : "Xem chi tiết");
                btn.setBackground(Color.decode("#38BDF8")); // Bầu trời xanh nhạt
                btn.setForeground(Color.decode("#0F172A")); // Chữ cực đậm
                btn.setFont(new Font("Inter", Font.BOLD, 12));
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                container.add(btn);
                return container;
            }
        });

        // Bắt sự kiện Click vào ô
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (col == 4 && row >= 0) {
                    String receiptIdText = (String) table.getValueAt(row, 0);
                    Long receiptId = Long.parseLong(receiptIdText.replace("PN-", ""));
                    
                    Window window = SwingUtilities.getWindowAncestor(WarehousePanel.this);
                    new SwingWorker<com.f3cinema.app.dto.StockReceiptDTO, Void>() {
                        @Override
                        protected com.f3cinema.app.dto.StockReceiptDTO doInBackground() throws Exception {
                            return com.f3cinema.app.service.impl.StockReceiptServiceImpl.getInstance().getReceiptDetails(receiptId);
                        }

                        @Override
                        protected void done() {
                            try {
                                com.f3cinema.app.dto.StockReceiptDTO detailDTO = get();
                                StockReceiptDetailDialog dialog = new StockReceiptDetailDialog((JFrame) window, receiptIdText, detailDTO);
                                dialog.setVisible(true);
                            } catch (Exception ex) {
                                String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                                JOptionPane.showMessageDialog(WarehousePanel.this, 
                                    "Không thể tải chi tiết phiếu nhập: " + msg, 
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }.execute();
                }
            }
        });

        // Chuột Hover lên ô Biến thành Hình Bàn Tay
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col == 4) {
                    table.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }
}
