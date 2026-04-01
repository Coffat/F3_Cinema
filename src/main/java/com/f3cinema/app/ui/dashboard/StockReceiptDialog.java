package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.dto.ProductDTO;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.f3cinema.app.dto.StockReceiptItemDTO;
import com.f3cinema.app.service.impl.InventoryServiceImpl;

/**
 * UI Component for Creating Stock Receipts.
 * Strictly adheres to Frontend Style Guide (Midnight Dark Mode, 16px radius, Indigo & Emerald Accents).
 */
public class StockReceiptDialog extends JDialog {
    private JTextField txtSupplier, txtDate;
    private DefaultTableModel tableModel;
    private JTable itemTable;
    private JLabel lblTotalCost;
    private JComboBox<ProductDTO> cbProduct;
    private JSpinner spinQuantity;
    private JSpinner spinPrice;
    private final Runnable onSuccessCallback;
    
    // Yêu cầu 1: List lưu trữ tạm thời các mặt hàng đang nhập
    private final List<StockReceiptItemDTO> currentItems = new ArrayList<>();

    public StockReceiptDialog(JFrame owner, Runnable onSuccessCallback) {
        super(owner, "Khởi Tạo Phiếu Nhập Kho", true);
        this.onSuccessCallback = onSuccessCallback;
        setSize(860, 650);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(ThemeConfig.BG_MAIN);
        
        // 1. Phím tắt ESC để hủy
        setupEscapeKey();

        // Khung chính
        JPanel mainContent = new JPanel(new BorderLayout(0, 24));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(30, 40, 30, 40));

        // 2. Vùng NORTH: Thông tin Master
        mainContent.add(createNorthPanel(), BorderLayout.NORTH);

        // 3. Vùng CENTER: Chi tiết các SP
        mainContent.add(createCenterPanel(), BorderLayout.CENTER);

        // 4. Vùng SOUTH: Tổng tiền và các Button chức năng
        mainContent.add(createSouthPanel(), BorderLayout.SOUTH);

        add(mainContent);
    }

    private void setupEscapeKey() {
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    private JPanel createNorthPanel() {
        JPanel northPanel = new JPanel(new BorderLayout(0, 16));
        northPanel.setOpaque(false);
        
        JLabel lblHeader = new JLabel("THONG TIN PHIEU NHAP MOI");
        lblHeader.setFont(ThemeConfig.FONT_H1);
        lblHeader.setForeground(ThemeConfig.TEXT_PRIMARY);
        northPanel.add(lblHeader, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(1, 2, 24, 0));
        formPanel.setOpaque(false);

        // Nhà cung cấp
        txtSupplier = new JTextField();
        formPanel.add(createFieldGroup("Nhà cung cấp (Supplier)", txtSupplier));

        // Ngày nhập
        txtDate = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtDate.setEditable(false);
        formPanel.add(createFieldGroup("Ngày nhập (Mặc định hôm nay)", txtDate));

        northPanel.add(formPanel, BorderLayout.CENTER);
        return northPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        JPanel topCenterItem = new JPanel(new BorderLayout());
        topCenterItem.setOpaque(false);
        
        JLabel lblDetail = new JLabel("CHI TIET MAT HANG");
        lblDetail.setFont(ThemeConfig.FONT_H2);
        lblDetail.setForeground(ThemeConfig.TEXT_SECONDARY);
        topCenterItem.add(lblDetail, BorderLayout.WEST);

        JPanel inlineAdd = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        inlineAdd.setOpaque(false);
        cbProduct = new JComboBox<>();
        loadProducts();
        cbProduct.setPreferredSize(new Dimension(230, 34));
        spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        spinPrice = new JSpinner(new SpinnerNumberModel(10000.0, 0.0, 10000000.0, 500.0));
        JButton btnAddItem = new JButton("Them san pham");
        btnAddItem.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #38BDF8; foreground: #0F172A; borderWidth: 0;");
        btnAddItem.addActionListener(e -> addInlineItem());
        inlineAdd.add(cbProduct);
        inlineAdd.add(spinQuantity);
        inlineAdd.add(spinPrice);
        inlineAdd.add(btnAddItem);
        topCenterItem.add(inlineAdd, BorderLayout.EAST);
        
        centerPanel.add(topCenterItem, BorderLayout.NORTH);

        String[] columnNames = {"San pham", "So luong", "Don gia", "Thanh tien"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 2;
            }
        };
        tableModel.addTableModelListener(e -> refreshFromTableModel());

        itemTable = new JTable(tableModel);
        itemTable.setFillsViewportHeight(true);
        itemTable.setBackground(ThemeConfig.BG_CARD);
        itemTable.setForeground(ThemeConfig.TEXT_PRIMARY);
        itemTable.setRowHeight(40);
        itemTable.setShowVerticalLines(false);
        itemTable.setShowHorizontalLines(true);
        itemTable.setGridColor(ThemeConfig.BG_MAIN);
        itemTable.setIntercellSpacing(new Dimension(0, 0));
        itemTable.getTableHeader().setReorderingAllowed(false);
        
        // Header Table Style
        itemTable.getTableHeader().setBackground(ThemeConfig.BG_MAIN);
        itemTable.getTableHeader().setForeground(ThemeConfig.TEXT_SECONDARY);
        itemTable.getTableHeader().setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
        itemTable.getTableHeader().setPreferredSize(new Dimension(0, 45));

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.getViewport().setBackground(ThemeConfig.BG_CARD);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        return centerPanel;
    }

    private JPanel createSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);

        // Hiển thị Tổng tiền
        lblTotalCost = new JLabel("Tong tien nhap: 0 đ");
        lblTotalCost.setFont(ThemeConfig.FONT_H1);
        lblTotalCost.setForeground(ThemeConfig.TEXT_SUCCESS);
        southPanel.add(lblTotalCost, BorderLayout.WEST);

        // Khu vực Nút chức năng
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        btnPanel.setOpaque(false);

        JButton btnCancel = new JButton("Hủy (ESC)");
        btnCancel.setFont(new Font("Inter", Font.BOLD, 14));
        btnCancel.setFocusPainted(false);
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; foreground: #94A3B8; background: #334155; borderWidth: 0; margin: 12,24,12,24");
        btnCancel.addActionListener(e -> dispose());
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPanel.add(btnCancel);

        JButton btnSave = new JButton("Hoàn tất nhập kho");
        btnSave.setFont(new Font("Inter", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; foreground: #FFFFFF; background: #6366F1; borderWidth: 0; margin: 12,24,12,24"); // Indigo
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveReceipt());
        btnPanel.add(btnSave);

        southPanel.add(btnPanel, BorderLayout.EAST);

        return southPanel;
    }

    private JPanel createFieldGroup(String labelText, JComponent inputComp) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ThemeConfig.FONT_BODY);
        lbl.setForeground(ThemeConfig.TEXT_SECONDARY);
        
        // Mặc định thiết kế cho tất cả các TextField
        inputComp.putClientProperty(FlatClientProperties.STYLE, "arc: 12; margin: 10,12,10,12; background: #1E293B; foreground: #F8FAFC; borderColor: #334155");
        inputComp.setFont(ThemeConfig.FONT_BODY);
        
        if (inputComp instanceof JTextField) {
            ((JTextField) inputComp).setCaretColor(Color.WHITE);
        }
        
        p.add(lbl, BorderLayout.NORTH);
        p.add(inputComp, BorderLayout.CENTER);
        return p;
    }

    private void loadProducts() {
        try {
            List<ProductDTO> products = InventoryServiceImpl.getInstance().getAllInventory();
            for (ProductDTO p : products) cbProduct.addItem(p);
            cbProduct.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof ProductDTO p) setText(p.name());
                    return this;
                }
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Khong the tai danh sach san pham.", "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addInlineItem() {
        ProductDTO selected = (ProductDTO) cbProduct.getSelectedItem();
        if (selected == null) return;
        int qty = (Integer) spinQuantity.getValue();
        BigDecimal price = BigDecimal.valueOf((Double) spinPrice.getValue());
        currentItems.add(new StockReceiptItemDTO(selected.id(), selected.name(), qty, price));
        refreshTableAndTotal();
    }

    private void refreshTableAndTotal() {
        tableModel.setRowCount(0);
        
        BigDecimal totalCost = BigDecimal.ZERO;
        for (StockReceiptItemDTO item : currentItems) {
            BigDecimal rowTotal = item.importPrice().multiply(BigDecimal.valueOf(item.quantity()));
            totalCost = totalCost.add(rowTotal);
            
            tableModel.addRow(new Object[]{
                item.productName(),
                item.quantity(),
                item.importPrice(),
                rowTotal
            });
        }
        lblTotalCost.setText(String.format("Tong tien nhap: %,.0f đ", totalCost));
    }

    private void refreshFromTableModel() {
        currentItems.clear();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name = String.valueOf(tableModel.getValueAt(i, 0));
            int qty = Integer.parseInt(String.valueOf(tableModel.getValueAt(i, 1)));
            BigDecimal price = new BigDecimal(String.valueOf(tableModel.getValueAt(i, 2)));
            BigDecimal row = price.multiply(BigDecimal.valueOf(qty));
            tableModel.setValueAt(row, i, 3);
            currentItems.add(new StockReceiptItemDTO(null, name, qty, price));
            total = total.add(row);
        }
        lblTotalCost.setText(String.format("Tong tien nhap: %,.0f đ", total));
    }

    private void saveReceipt() {
        String supplier = txtSupplier.getText().trim();
        if (supplier.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên Nhà cung cấp!", "Thiếu thông tin yêu cầu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Danh sách sản phẩm nhập không được làm trống. Phải thêm ít nhất 1 mặt hàng!", "Thiếu thông tin yêu cầu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal totalImportCost = BigDecimal.ZERO;
        for (StockReceiptItemDTO item : currentItems) {
            totalImportCost = totalImportCost.add(item.importPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }

        com.f3cinema.app.dto.StockReceiptDTO dto = new com.f3cinema.app.dto.StockReceiptDTO(supplier, currentItems, totalImportCost);

        // Async save preventing UI blocking
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                com.f3cinema.app.service.impl.StockReceiptServiceImpl.getInstance().createReceipt(dto);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(StockReceiptDialog.this, "Hệ thống ghi nhận việc nhập kho thành công!", "Hoàn tất thao tác", JOptionPane.INFORMATION_MESSAGE);
                    // Trigger UI Table reload from Parent (WarehousePanel) for BOTH TABS
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                    dispose();
                } catch (Exception e) {
                    // Extract exact origin error (SQL / Constraint / Transaction)
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(StockReceiptDialog.this, "Lỗi khi thiết lập tạo mới phiếu nhập: \n" + msg, "Kho Hàng", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
