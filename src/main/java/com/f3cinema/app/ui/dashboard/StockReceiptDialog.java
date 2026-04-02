package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.common.dialog.BaseAppDialog;
import com.f3cinema.app.ui.common.dialog.DialogStyle;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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
public class StockReceiptDialog extends BaseAppDialog {
    private JTextField txtSupplier, txtDate;
    private DefaultTableModel tableModel;
    private JTable itemTable;
    private JLabel lblTotalCost;
    private JSpinner spinQuantity;
    private JSpinner spinPrice;
    private final Runnable onSuccessCallback;
    
    // Yêu cầu 1: List lưu trữ tạm thời các mặt hàng đang nhập
    private final List<StockReceiptItemDTO> currentItems = new ArrayList<>();

    /** Tránh vòng lặp: setValueAt / addRow kích hoạt TableModelListener → refreshFromTableModel → setValueAt… */
    private boolean ignoreTableModelEvents;

    public StockReceiptDialog(JFrame owner, Runnable onSuccessCallback) {
        super(owner, "Khởi Tạo Phiếu Nhập Kho");
        this.onSuccessCallback = onSuccessCallback;
        setupBaseDialog(980, 650);
        JPanel surface = createSurfacePanel();
        setContentPane(surface);
        
        // 1. Phím tắt ESC để hủy
        setupEscapeKey();

        // Khung chính
        JPanel mainContent = new JPanel(new BorderLayout(0, 24));
        mainContent.setOpaque(false);

        // 2. Vùng NORTH: Thông tin Master
        mainContent.add(createNorthPanel(), BorderLayout.NORTH);

        // 3. Vùng CENTER: Chi tiết các SP
        mainContent.add(createCenterPanel(), BorderLayout.CENTER);

        // 4. Vùng SOUTH: Tổng tiền và các Button chức năng
        mainContent.add(createSouthPanel(), BorderLayout.SOUTH);

        surface.add(mainContent, BorderLayout.CENTER);
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
        
        JLabel lblHeader = DialogStyle.titleLabel("THONG TIN PHIEU NHAP MOI");
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
        
        JLabel lblDetail = new JLabel("CHI TIẾT MẶT HÀNG");
        lblDetail.setFont(ThemeConfig.FONT_H2);
        lblDetail.setForeground(ThemeConfig.TEXT_SECONDARY);
        topCenterItem.add(lblDetail, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        
        JButton btnSelectProduct = new JButton("Chọn sản phẩm");
        btnSelectProduct.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #6366F1; foreground: #FFFFFF; borderWidth: 0;");
        btnSelectProduct.addActionListener(e -> showProductSelectionDialog());
        btnPanel.add(btnSelectProduct);
        
        topCenterItem.add(btnPanel, BorderLayout.EAST);
        
        centerPanel.add(topCenterItem, BorderLayout.NORTH);

        String[] columnNames = {"San pham", "So luong", "Don gia", "Thanh tien"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 2;
            }
        };
        tableModel.addTableModelListener(e -> {
            if (!ignoreTableModelEvents) {
                refreshFromTableModel();
            }
        });

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

        JButton btnCancel = DialogStyle.secondaryButton("Hủy (ESC)");
        btnCancel.addActionListener(e -> dispose());
        btnPanel.add(btnCancel);

        JButton btnSave = DialogStyle.primaryButton("Hoàn tất nhập kho");
        btnSave.addActionListener(e -> saveReceipt());
        btnPanel.add(btnSave);

        southPanel.add(btnPanel, BorderLayout.EAST);

        return southPanel;
    }

    private JPanel createFieldGroup(String labelText, JComponent inputComp) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JLabel lbl = DialogStyle.formLabel(labelText);
        
        // Mặc định thiết kế cho tất cả các TextField
        inputComp.putClientProperty(FlatClientProperties.STYLE, "arc: 12; margin: 10,12,10,12; background: #1E293B; foreground: #F8FAFC; borderColor: #334155");
        DialogStyle.styleInput(inputComp);
        
        if (inputComp instanceof JTextField) {
            ((JTextField) inputComp).setCaretColor(Color.WHITE);
        }
        
        p.add(lbl, BorderLayout.NORTH);
        p.add(inputComp, BorderLayout.CENTER);
        return p;
    }

    private void refreshTableAndTotal() {
        ignoreTableModelEvents = true;
        try {
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
        } finally {
            ignoreTableModelEvents = false;
        }
    }

    private void refreshFromTableModel() {
        ignoreTableModelEvents = true;
        try {
            List<StockReceiptItemDTO> previousItems = new ArrayList<>(currentItems);
            currentItems.clear();
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String name = String.valueOf(tableModel.getValueAt(i, 0));
                int qty = Integer.parseInt(String.valueOf(tableModel.getValueAt(i, 1)));
                BigDecimal price = new BigDecimal(String.valueOf(tableModel.getValueAt(i, 2)));
                BigDecimal row = price.multiply(BigDecimal.valueOf(qty));
                tableModel.setValueAt(row, i, 3);
                Long productId = i < previousItems.size() ? previousItems.get(i).productId() : null;
                currentItems.add(new StockReceiptItemDTO(productId, name, qty, price));
                total = total.add(row);
            }
            lblTotalCost.setText(String.format("Tong tien nhap: %,.0f đ", total));
        } finally {
            ignoreTableModelEvents = false;
        }
    }

    private void saveReceipt() {
        String supplier = txtSupplier.getText().trim();
        if (supplier.isEmpty()) {
            AppMessageDialogs.showWarning(this, "Thiếu thông tin yêu cầu", "Vui lòng nhập tên Nhà cung cấp!");
            return;
        }

        if (currentItems.isEmpty()) {
            AppMessageDialogs.showWarning(this, "Thiếu thông tin yêu cầu", "Danh sách sản phẩm nhập không được làm trống. Phải thêm ít nhất 1 mặt hàng!");
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
                    AppMessageDialogs.showInfo(StockReceiptDialog.this, "Hoàn tất thao tác", "Hệ thống ghi nhận việc nhập kho thành công!");
                    // Trigger UI Table reload from Parent (WarehousePanel) for BOTH TABS
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                    dispose();
                } catch (Exception e) {
                    // Extract exact origin error (SQL / Constraint / Transaction)
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    AppMessageDialogs.showError(StockReceiptDialog.this, "Kho Hàng", "Lỗi khi thiết lập tạo mới phiếu nhập: \n" + msg);
                }
            }
        }.execute();
    }

    private void showProductSelectionDialog() {
        new StockProductPickerDialog(this).setVisible(true);
    }

    /**
     * Cùng lớp vỏ với các dialog F3 ({@link BaseAppDialog} + surface).
     * Tách hàng số lượng/đơn giá và hàng nút — trước đây gom trong một {@link FlowLayout} nên nút bị cắt/chồng.
     */
    private final class StockProductPickerDialog extends BaseAppDialog {

        StockProductPickerDialog(Window owner) {
            super(owner, "Chọn sản phẩm nhập kho");
            setupBaseDialog(720, 560);
            setLocationRelativeTo(owner);
            JPanel surface = createSurfacePanel();
            setContentPane(surface);

            JPanel main = new JPanel(new BorderLayout(0, 20));
            main.setOpaque(false);

            JLabel lblHeader = DialogStyle.titleLabel("CHỌN SẢN PHẨM");
            main.add(lblHeader, BorderLayout.NORTH);

            List<ProductDTO> allProducts = InventoryServiceImpl.getInstance().getAllInventory();
            String[] productColumnNames = {"", "Sản phẩm", "Tồn kho", "Giá bán"};
            Object[][] productData = new Object[allProducts.size()][4];
            for (int i = 0; i < allProducts.size(); i++) {
                ProductDTO p = allProducts.get(i);
                productData[i][0] = Boolean.FALSE;
                productData[i][1] = p;
                productData[i][2] = p.currentQuantity() != null ? p.currentQuantity() : 0;
                productData[i][3] = p.price();
            }

            DefaultTableModel productTableModel = new DefaultTableModel(productData, productColumnNames) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) {
                        return Boolean.class;
                    }
                    return super.getColumnClass(columnIndex);
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 0;
                }
            };

            JTable productTable = new JTable(productTableModel);
            productTable.setBackground(ThemeConfig.BG_CARD);
            productTable.setForeground(ThemeConfig.TEXT_PRIMARY);
            productTable.setRowHeight(40);
            productTable.setShowVerticalLines(false);
            productTable.setShowHorizontalLines(true);
            productTable.setIntercellSpacing(new Dimension(0, 0));
            productTable.setGridColor(new Color(51, 65, 85, 160));
            productTable.getTableHeader().setBackground(ThemeConfig.BG_MAIN);
            productTable.getTableHeader().setForeground(ThemeConfig.TEXT_SECONDARY);
            productTable.getTableHeader().setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
            productTable.getTableHeader().setPreferredSize(new Dimension(0, 44));
            productTable.getColumnModel().getColumn(0).setMaxWidth(44);
            productTable.getColumnModel().getColumn(2).setPreferredWidth(88);
            productTable.getColumnModel().getColumn(3).setPreferredWidth(110);

            productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (column == 1 && value instanceof ProductDTO p) {
                        setText(p.name());
                    }
                    if (isSelected) {
                        setBackground(new Color(99, 102, 241, 50));
                    } else {
                        setBackground(ThemeConfig.BG_CARD);
                    }
                    return c;
                }
            });

            JScrollPane scrollPane = new JScrollPane(productTable);
            scrollPane.getViewport().setBackground(ThemeConfig.BG_CARD);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setOpaque(false);
            productTable.setPreferredScrollableViewportSize(new Dimension(640, 280));

            main.add(scrollPane, BorderLayout.CENTER);

            JSpinner spinQty = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
            JSpinner spinPrice = new JSpinner(new SpinnerNumberModel(10000.0, 0.0, 10000000.0, 500.0));
            DialogStyle.styleInput(spinQty);
            DialogStyle.styleInput(spinPrice);
            spinQty.setPreferredSize(new Dimension(120, 42));
            spinPrice.setPreferredSize(new Dimension(140, 42));

            JPanel spinRow = new JPanel(new GridBagLayout());
            spinRow.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 0, 10);
            gbc.gridx = 0;
            gbc.gridy = 0;
            spinRow.add(DialogStyle.formLabel("Số lượng nhập"), gbc);
            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 0, 28);
            spinRow.add(spinQty, gbc);
            gbc.gridx = 2;
            gbc.insets = new Insets(0, 0, 0, 10);
            spinRow.add(DialogStyle.formLabel("Đơn giá nhập"), gbc);
            gbc.gridx = 3;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.weightx = 1.0;
            spinRow.add(spinPrice, gbc);

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
            btnRow.setOpaque(false);
            JButton btnCancel = DialogStyle.secondaryButton("Hủy");
            btnCancel.addActionListener(e -> dispose());
            JButton btnAdd = DialogStyle.primaryButton("Thêm vào phiếu");
            btnAdd.addActionListener(e -> {
                int selectedCount = 0;
                for (int i = 0; i < productTableModel.getRowCount(); i++) {
                    if (Boolean.TRUE.equals(productTableModel.getValueAt(i, 0))) {
                        selectedCount++;
                    }
                }
                if (selectedCount == 0) {
                    AppMessageDialogs.showWarning(StockProductPickerDialog.this, "Thiếu lựa chọn",
                            "Vui lòng chọn ít nhất một sản phẩm!");
                    return;
                }

                int qty = (Integer) spinQty.getValue();
                BigDecimal price = BigDecimal.valueOf((Double) spinPrice.getValue());

                for (int i = 0; i < productTableModel.getRowCount(); i++) {
                    if (Boolean.TRUE.equals(productTableModel.getValueAt(i, 0))) {
                        ProductDTO p = (ProductDTO) productTableModel.getValueAt(i, 1);
                        currentItems.add(new StockReceiptItemDTO(p.id(), p.name(), qty, price));
                    }
                }
                refreshTableAndTotal();
                dispose();
            });
            btnRow.add(btnCancel);
            btnRow.add(btnAdd);

            JPanel south = new JPanel();
            south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
            south.setOpaque(false);
            south.add(spinRow);
            south.add(Box.createVerticalStrut(16));
            south.add(btnRow);

            main.add(south, BorderLayout.SOUTH);

            surface.add(main, BorderLayout.CENTER);

            KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "CLOSE_PICKER");
            getRootPane().getActionMap().put("CLOSE_PICKER", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
        }
    }
}
