package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.service.impl.InventoryServiceImpl;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Standard Dialog to insert new product to the F3 Cinema Warehouse.
 * Strictly adheres to Frontend Style Guide.
 */
public class ProductDialog extends JDialog {
    private final Runnable onSuccessCallback;
    private JTextField txtName, txtUnit;
    private JComboBox<String> cbCategory;
    private JSpinner spinPrice, spinThreshold;

    public ProductDialog(JFrame owner, Runnable onSuccessCallback) {
        super(owner, "Thêm Mới Sản Phẩm Bán Kho", true);
        this.onSuccessCallback = onSuccessCallback;
        setSize(520, 560);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(ThemeConfig.BG_MAIN);
        
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ------------- HEADER -------------
        JLabel lblHeader = new JLabel("THÊM SẢN PHẨM KHU VỰC BÁN");
        lblHeader.setFont(ThemeConfig.FONT_H1);
        lblHeader.setForeground(ThemeConfig.TEXT_PRIMARY);
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(lblHeader);
        mainContent.add(Box.createRigidArea(new Dimension(0, 30)));

        // ------------- FORM -------------
        JPanel form = new JPanel(new GridLayout(5, 1, 0, 16));
        form.setOpaque(false);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(createFieldGroup("Tên sản phẩm (VD: Bắp rang bơ phô mai)", txtName = new JTextField()));
        cbCategory = new JComboBox<>(new String[]{"SNACK", "DRINK", "COMBO"});
        form.add(createFieldGroup("Danh mục", cbCategory));
        
        spinPrice = new JSpinner(new SpinnerNumberModel(45000.0, 0.0, 1000000.0, 5000.0));
        form.add(createFieldGroup("Giá bán (VNĐ)", spinPrice));

        form.add(createFieldGroup("Đơn vị tính (VD: Ly, Hộp, Combo)", txtUnit = new JTextField()));

        spinThreshold = new JSpinner(new SpinnerNumberModel(20, 1, 10000, 1));
        form.add(createFieldGroup("Mức cảnh báo tồn kho (Min Threshold)", spinThreshold));

        mainContent.add(form);
        mainContent.add(Box.createRigidArea(new Dimension(0, 32)));

        // ------------- BUTTONS -------------
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnClose = new JButton("Hủy Lệnh");
        btnClose.setFont(new Font("Inter", Font.BOLD, 14));
        btnClose.setFocusPainted(false);
        btnClose.putClientProperty(FlatClientProperties.STYLE, "arc: 12; foreground: #F8FAFC; background: #334155; borderWidth: 0; margin: 10,24,10,24");
        btnClose.addActionListener(e -> dispose());
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPanel.add(btnClose);

        JButton btnSave = new JButton("Lưu Sản Phẩm");
        btnSave.setFont(new Font("Inter", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; foreground: #FFFFFF; background: #6366F1; borderWidth: 0; margin: 10,24,10,24");
        btnSave.addActionListener(e -> saveProduct());
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPanel.add(btnSave);

        mainContent.add(btnPanel);
        add(mainContent);
    }

    private JPanel createFieldGroup(String labelText, JComponent inputComp) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ThemeConfig.FONT_BODY);
        lbl.setForeground(ThemeConfig.TEXT_SECONDARY);
        
        inputComp.putClientProperty(FlatClientProperties.STYLE, "arc: 12; margin: 10,12,10,12; background: #1E293B; foreground: #F8FAFC; borderColor: #334155");
        inputComp.setFont(ThemeConfig.FONT_BODY);
        
        if (inputComp instanceof JTextField) {
            ((JTextField) inputComp).setCaretColor(Color.WHITE);
        }
        
        p.add(lbl, BorderLayout.NORTH);
        p.add(inputComp, BorderLayout.CENTER);
        return p;
    }

    private void saveProduct() {
        String name = txtName.getText().trim();
        String unit = txtUnit.getText().trim();
        String category = cbCategory != null ? String.valueOf(cbCategory.getSelectedItem()) : "SNACK";

        if (name.isEmpty() || unit.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên sản phẩm và đơn vị tính!", "Thiếu thông tin yêu cầu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal price = BigDecimal.valueOf((Double) spinPrice.getValue());
        Integer threshold = (Integer) spinThreshold.getValue();

        String normalizedName = name.startsWith("[" + category + "]") ? name : ("[" + category + "] " + name);
        ProductDTO dto = new ProductDTO(null, normalizedName, price, unit, null, 0, threshold);

        // Async save preventing UI blocking
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                InventoryServiceImpl.getInstance().addProduct(dto);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(ProductDialog.this, "Hệ thống đã thêm thành công sản phẩm mới!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    // Trigger UI Table reload from Parent (WarehousePanel)
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                    dispose();
                } catch (Exception e) {
                    // Extract exact origin error (SQL / Constraint / Transaction)
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(ProductDialog.this, "Lỗi khi lưu dữ liệu sản phẩm mới: \n" + msg, "Kho Hàng", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
