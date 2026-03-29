package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.dto.StockReceiptItemDTO;
import com.f3cinema.app.service.impl.InventoryServiceImpl;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

/**
 * Miniature Dialog adhering to Midnight Theme to pick items for Stock Receipt.
 */
public class AddItemDialog extends JDialog {
    private JComboBox<ProductDTO> cbProduct;
    private JSpinner spinQuantity;
    private JSpinner spinPrice;
    private final Consumer<StockReceiptItemDTO> onItemAdded;

    public AddItemDialog(JDialog owner, Consumer<StockReceiptItemDTO> onItemAdded) {
        super(owner, "Thêm Mặt Hàng Nhập", true);
        this.onItemAdded = onItemAdded;
        setSize(420, 430);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Color.decode("#0F172A")); // Midnight Deep

        setupEscapeKey();

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Tiêu đề thu gọn
        JLabel lblHeader = new JLabel("CHỌN MẶT HÀNG");
        lblHeader.setFont(new Font("Inter", Font.BOLD, 18));
        lblHeader.setForeground(Color.decode("#F8FAFC"));
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(lblHeader);
        mainContent.add(Box.createRigidArea(new Dimension(0, 20)));

        // Khu vực Form nhập liệu
        JPanel form = new JPanel(new GridLayout(3, 1, 0, 16));
        form.setOpaque(false);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 1. Combo Box Sản phẩm
        cbProduct = new JComboBox<>();
        loadProducts(); // Nạp dữ liệu Master
        // Custom combo box renderer để in tên + đơn vị tính
        cbProduct.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ProductDTO) {
                    ProductDTO p = (ProductDTO) value;
                    setText(p.name() + " (" + p.unit() + ") - Mã: " + p.id());
                }
                c.setBackground(isSelected ? Color.decode("#334155") : Color.decode("#1E293B"));
                c.setForeground(Color.WHITE);
                return c;
            }
        });
        form.add(createFieldGroup("Sản phẩm", cbProduct));

        // 2. JSpinner Số lượng
        spinQuantity = new JSpinner(new SpinnerNumberModel(10, 1, 10000, 1));
        form.add(createFieldGroup("Số lượng nhập", spinQuantity));

        // 3. JSpinner Giá nhập
        spinPrice = new JSpinner(new SpinnerNumberModel(40000.0, 0.0, 10000000.0, 500.0));
        form.add(createFieldGroup("Đơn giá nhập (VNĐ)", spinPrice));

        mainContent.add(form);
        mainContent.add(Box.createRigidArea(new Dimension(0, 24)));

        // Khu vực Nút
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnCancel = new JButton("Hủy (ESC)");
        styleButton(btnCancel, "#334155", "#94A3B8");
        btnCancel.addActionListener(e -> dispose());
        btnPanel.add(btnCancel);

        JButton btnAdd = new JButton("Thêm vào phiếu");
        styleButton(btnAdd, "#6366F1", "#FFFFFF");
        btnAdd.addActionListener(e -> submitItem());
        btnPanel.add(btnAdd);

        mainContent.add(btnPanel);
        add(mainContent);
    }

    private void loadProducts() {
        try {
            List<ProductDTO> products = InventoryServiceImpl.getInstance().getAllInventory();
            for (ProductDTO p : products) {
                cbProduct.addItem(p);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối lấy dữ liệu kho!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void submitItem() {
        ProductDTO selected = (ProductDTO) cbProduct.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 danh mục sản phẩm từ hệ thống!", "Cảnh báo Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity = (Integer) spinQuantity.getValue();
        BigDecimal price = BigDecimal.valueOf((Double) spinPrice.getValue());

        // Đóng gói DTO (stock receipt ID chưa có tạm bỏ qua / do Backend lo)
        StockReceiptItemDTO itemDto = new StockReceiptItemDTO(selected.id(), selected.name(), quantity, price);

        // Gọi callback truyền data ra Dialog Ngoài
        onItemAdded.accept(itemDto);
        dispose();
    }

    private void setupEscapeKey() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
    }

    private JPanel createFieldGroup(String labelText, JComponent inputComp) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Inter", Font.PLAIN, 12));
        lbl.setForeground(Color.decode("#94A3B8"));

        if (inputComp instanceof javax.swing.JTextField) {
            inputComp.putClientProperty("FlatLaf.style", "margin: 5,10,5,10");
        } else {
            // Dùng cách an toàn của Swing mặc định để tạo khoảng thở
            inputComp.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    inputComp.getBorder(),
                    javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        }

        // inputComp.putClientProperty(FlatClientProperties.STYLE, "arc: 12; margin:
        // 6,12,6,12; background: #1E293B; foreground: #F8FAFC; borderColor: #334155");
        inputComp.setFont(new Font("Inter", Font.PLAIN, 14));

        p.add(lbl, BorderLayout.NORTH);
        p.add(inputComp, BorderLayout.CENTER);
        return p;
    }

    private void styleButton(JButton btn, String bgHex, String fgHex) {
        btn.setFont(new Font("Inter", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; margin: 8,16,8,16; borderWidth: 0; background: " + bgHex + "; foreground: " + fgHex);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
