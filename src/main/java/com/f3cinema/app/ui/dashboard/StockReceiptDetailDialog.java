package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.dto.StockReceiptDTO;
import com.f3cinema.app.dto.StockReceiptItemDTO;
import com.f3cinema.app.ui.common.dialog.BaseAppDialog;
import com.f3cinema.app.ui.common.dialog.DialogStyle;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;

public class StockReceiptDetailDialog extends BaseAppDialog {

    private final String receiptIdText;
    private final StockReceiptDTO receiptDTO;

    public StockReceiptDetailDialog(JFrame parent, String receiptIdText, StockReceiptDTO receiptDTO) {
        super(parent, "Chi Tiết Phiếu Nhập: " + receiptIdText);
        this.receiptIdText = receiptIdText;
        this.receiptDTO = receiptDTO;
        initUI();
    }

    private void initUI() {
        setupBaseDialog(700, 500);
        JPanel surface = createSurfacePanel();
        surface.setLayout(new BorderLayout(0, 16));
        setContentPane(surface);

        // Bắt phím ESC để đóng
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // Vùng trên: Thông tin Master
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        topPanel.setOpaque(false);

        JLabel lblHeader = new JLabel("Phiếu Nhập: " + receiptIdText);
        lblHeader.setFont(new Font("Inter", Font.BOLD, 22));
        lblHeader.setForeground(Color.decode("#F8FAFC"));

        JLabel lblSupplier = new JLabel("Nhà Cung Cấp: " + receiptDTO.supplier());
        lblSupplier.setFont(new Font("Inter", Font.PLAIN, 16));
        lblSupplier.setForeground(Color.decode("#94A3B8"));

        JPanel headerInfo = new JPanel(new GridLayout(2, 1, 0, 8));
        headerInfo.setOpaque(false);
        headerInfo.add(lblHeader);
        headerInfo.add(lblSupplier);
        topPanel.add(headerInfo);

        surface.add(topPanel, BorderLayout.NORTH);

        // Vùng giữa: JTable liệt kê chi tiết
        String[] columns = { "Tên Sản Phẩm", "Số Lượng", "Giá Nhập", "Thành Tiền" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (StockReceiptItemDTO item : receiptDTO.items()) {
            java.math.BigDecimal lineTotal = item.importPrice().multiply(java.math.BigDecimal.valueOf(item.quantity()));
            model.addRow(new Object[] {
                    item.productName(),
                    item.quantity(),
                    String.format("%,.0f đ", item.importPrice()),
                    String.format("%,.0f đ", lineTotal)
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.decode("#1E293B"));
        table.setForeground(Color.decode("#F8FAFC"));
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setGridColor(Color.decode("#0F172A"));
        table.getTableHeader().setBackground(Color.decode("#0F172A"));
        table.getTableHeader().setForeground(Color.decode("#94A3B8"));
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);

        // Padding Cell
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); // Cell Padding
                c.setBackground(Color.decode("#1E293B"));
                c.setForeground(Color.decode("#F8FAFC"));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.decode("#1E293B"));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        surface.add(scrollPane, BorderLayout.CENTER);

        // Vùng dưới: Tổng Tiền và Nút Đóng
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JLabel lblTotal = new JLabel("Tổng Cộng: " + String.format("%,.0f đ", receiptDTO.totalImportCost()));
        lblTotal.setFont(new Font("Inter", Font.BOLD, 22));
        lblTotal.setForeground(Color.decode("#10B981")); // Emerald Green
        bottomPanel.add(lblTotal, BorderLayout.WEST);

        JButton btnClose = DialogStyle.secondaryButton("Đóng");
        btnClose.setPreferredSize(new Dimension(120, 45));
        btnClose.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnClose);
        bottomPanel.add(btnPanel, BorderLayout.EAST);

        surface.add(bottomPanel, BorderLayout.SOUTH);
    }
}
