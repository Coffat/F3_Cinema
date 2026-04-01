package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.service.impl.InventoryServiceImpl;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.common.dialog.BaseAppDialog;
import com.f3cinema.app.ui.common.dialog.DialogStyle;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Standard Dialog to insert new product to the F3 Cinema Warehouse.
 * Strictly adheres to Frontend Style Guide.
 */
public class ProductDialog extends BaseAppDialog {
    private final Runnable onSuccessCallback;
    private JTextField txtName, txtUnit;
    private JComboBox<String> cbCategory;
    private JSpinner spinPrice, spinThreshold;

    public ProductDialog(JFrame owner, Runnable onSuccessCallback) {
        super(owner, "Thêm Mới Sản Phẩm Bán Kho");
        this.onSuccessCallback = onSuccessCallback;
        setupBaseDialog(820, 500);

        JPanel surface = createSurfacePanel();
        setContentPane(surface);
        
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // ------------- HEADER -------------
        JLabel lblHeader = DialogStyle.titleLabel("THÊM SẢN PHẨM KHU VỰC BÁN");
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(lblHeader);
        mainContent.add(Box.createRigidArea(new Dimension(0, 30)));

        // ------------- FORM -------------
        JPanel form = new JPanel(new GridLayout(3, 2, 18, 16));
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

        JPanel filler = new JPanel();
        filler.setOpaque(false);
        form.add(filler);

        mainContent.add(form);
        mainContent.add(Box.createRigidArea(new Dimension(0, 32)));

        // ------------- BUTTONS -------------
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnClose = DialogStyle.secondaryButton("Hủy Lệnh");
        btnClose.addActionListener(e -> dispose());
        btnPanel.add(btnClose);

        JButton btnSave = DialogStyle.primaryButton("Lưu Sản Phẩm");
        btnSave.addActionListener(e -> saveProduct());
        btnPanel.add(btnSave);

        mainContent.add(btnPanel);
        surface.add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createFieldGroup(String labelText, JComponent inputComp) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JLabel lbl = DialogStyle.formLabel(labelText);
        
        DialogStyle.styleInput(inputComp);
        
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
            AppMessageDialogs.showWarning(this, "Thiếu thông tin yêu cầu", "Vui lòng nhập đầy đủ tên sản phẩm và đơn vị tính!");
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
                    AppMessageDialogs.showInfo(ProductDialog.this, "Thành công", "Hệ thống đã thêm thành công sản phẩm mới!");
                    // Trigger UI Table reload from Parent (WarehousePanel)
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                    dispose();
                } catch (Exception e) {
                    // Extract exact origin error (SQL / Constraint / Transaction)
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    AppMessageDialogs.showError(ProductDialog.this, "Kho Hàng", "Lỗi khi lưu dữ liệu sản phẩm mới: \n" + msg);
                }
            }
        }.execute();
    }
}
