package com.f3cinema.app.ui.dashboard;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

/**
 * UI Component for Creating Stock Receipts.
 * Strictly adheres to Frontend Style Guide (Midnight Dark Mode, 16px radius, Indigo & Emerald Accents).
 */
public class StockReceiptDialog extends JDialog {
    private JTextField txtSupplier, txtDate;
    private DefaultTableModel tableModel;
    private JTable itemTable;
    private JLabel lblTotalCost;
    private final Runnable onSuccessCallback;
    
    // Yêu cầu 1: List lưu trữ tạm thời các mặt hàng đang nhập
    private final List<StockReceiptItemDTO> currentItems = new ArrayList<>();

    public StockReceiptDialog(JFrame owner, Runnable onSuccessCallback) {
        super(owner, "Khởi Tạo Phiếu Nhập Kho", true);
        this.onSuccessCallback = onSuccessCallback;
        setSize(860, 650);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Color.decode("#0F172A")); // Slate 900 Background
        
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
        
        JLabel lblHeader = new JLabel("THÔNG TIN PHIẾU NHẬP MỚI");
        lblHeader.setFont(new Font("Inter", Font.BOLD, 22));
        lblHeader.setForeground(Color.decode("#F8FAFC"));
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

        // Header Tab Chi Tiết + Nút Thêm SP
        JPanel topCenterItem = new JPanel(new BorderLayout());
        topCenterItem.setOpaque(false);
        
        JLabel lblDetail = new JLabel("CHI TIẾT MẶT HÀNG");
        lblDetail.setFont(new Font("Inter", Font.BOLD, 16));
        lblDetail.setForeground(Color.decode("#94A3B8"));
        topCenterItem.add(lblDetail, BorderLayout.WEST);

        JButton btnAddItem = new JButton("+ Thêm SP vào phiếu");
        btnAddItem.setFont(new Font("Inter", Font.BOLD, 13));
        btnAddItem.setFocusPainted(false);
        btnAddItem.putClientProperty(FlatClientProperties.STYLE, "arc: 12; foreground: #1E293B; background: #38BDF8; borderWidth: 0; margin: 6,16,6,16");
        btnAddItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Yêu cầu 2: Call sub-dialog when clicked
        btnAddItem.addActionListener(e -> {
            AddItemDialog dialog = new AddItemDialog(this, itemDto -> {
                currentItems.add(itemDto);
                refreshTableAndTotal();
            });
            dialog.setVisible(true);
        });
        
        topCenterItem.add(btnAddItem, BorderLayout.EAST);
        
        centerPanel.add(topCenterItem, BorderLayout.NORTH);

        // Danh sách dạng Bảng (Table)
        String[] columnNames = {"STT", "Tên Sản phẩm", "Số lượng", "Giá nhập", "Thành tiền"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        itemTable = new JTable(tableModel);
        itemTable.setFillsViewportHeight(true);
        itemTable.setBackground(Color.decode("#1E293B"));
        itemTable.setForeground(Color.decode("#F8FAFC"));
        itemTable.setRowHeight(40);
        itemTable.setShowVerticalLines(false);
        itemTable.setShowHorizontalLines(true);
        itemTable.setGridColor(Color.decode("#0F172A"));
        itemTable.setIntercellSpacing(new Dimension(0, 0));
        itemTable.getTableHeader().setReorderingAllowed(false);
        
        // Header Table Style
        itemTable.getTableHeader().setBackground(Color.decode("#0F172A"));
        itemTable.getTableHeader().setForeground(Color.decode("#94A3B8"));
        itemTable.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        itemTable.getTableHeader().setPreferredSize(new Dimension(0, 45));

        // Custom Cell Renderer
        itemTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12)); // Padding Cột
                
                if (isSelected) {
                    c.setBackground(Color.decode("#334155"));
                } else {
                    c.setBackground(Color.decode("#1E293B")); // Background Form Đen Slate 800
                }
                c.setForeground(Color.decode("#F8FAFC")); 
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.getViewport().setBackground(Color.decode("#1E293B"));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        return centerPanel;
    }

    private JPanel createSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);

        // Hiển thị Tổng tiền
        lblTotalCost = new JLabel("Tổng tiền nhập: 0 đ");
        lblTotalCost.setFont(new Font("Inter", Font.BOLD, 22));
        lblTotalCost.setForeground(Color.decode("#10B981")); // Emerald (Xanh lá mạnh nổi bật theo yêu cầu)
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
        lbl.setFont(new Font("Inter", Font.PLAIN, 13));
        lbl.setForeground(Color.decode("#94A3B8"));
        
        // Mặc định thiết kế cho tất cả các TextField
        inputComp.putClientProperty(FlatClientProperties.STYLE, "arc: 12; margin: 10,12,10,12; background: #1E293B; foreground: #F8FAFC; borderColor: #334155");
        inputComp.setFont(new Font("Inter", Font.PLAIN, 15));
        
        if (inputComp instanceof JTextField) {
            ((JTextField) inputComp).setCaretColor(Color.WHITE);
        }
        
        p.add(lbl, BorderLayout.NORTH);
        p.add(inputComp, BorderLayout.CENTER);
        return p;
    }

    // Yêu cầu 3 & 4: Cập nhật Table và Tổng tiền
    private void refreshTableAndTotal() {
        tableModel.setRowCount(0); // clear UI rows
        
        BigDecimal totalCost = BigDecimal.ZERO;
        int index = 1;
        
        for (StockReceiptItemDTO item : currentItems) {
            BigDecimal rowTotal = item.importPrice().multiply(BigDecimal.valueOf(item.quantity()));
            totalCost = totalCost.add(rowTotal);
            
            tableModel.addRow(new Object[]{
                index++,
                item.productName(),
                item.quantity(),
                String.format("%,.0f đ", item.importPrice()),
                String.format("%,.0f đ", rowTotal)
            });
        }
        
        lblTotalCost.setText(String.format("Tổng tiền nhập: %,.0f đ", totalCost));
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
