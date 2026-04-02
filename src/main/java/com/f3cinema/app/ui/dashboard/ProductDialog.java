package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.service.impl.InventoryServiceImpl;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.common.dialog.BaseAppDialog;
import com.f3cinema.app.ui.common.dialog.DialogStyle;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Standard Dialog to insert new product to the F3 Cinema Warehouse.
 * Strictly adheres to Frontend Style Guide.
 */
public class ProductDialog extends BaseAppDialog {
    private final Runnable onSuccessCallback;
    private JTextField txtName, txtUnit;
    private JComboBox<String> cbCategory;
    private JSpinner spinPrice, spinThreshold;
    private JLabel lblImagePreview;
    private String selectedImagePath;
    private static final String IMAGE_DIR = "src/main/resources/images/products/";

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
        JPanel form = new JPanel(new GridLayout(4, 2, 18, 16));
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

        form.add(createImageUploadPanel());

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

    private JPanel createImageUploadPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        JLabel lbl = DialogStyle.formLabel("Hình ảnh sản phẩm");

        JPanel contentPanel = new JPanel(new BorderLayout(12, 0));
        contentPanel.setOpaque(false);

        lblImagePreview = new JLabel();
        lblImagePreview.setPreferredSize(new Dimension(80, 80));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));
        lblImagePreview.setBackground(new Color(30, 41, 59));
        lblImagePreview.setOpaque(true);
        lblImagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagePreview.setVerticalAlignment(SwingConstants.CENTER);
        lblImagePreview.setText("No Image");

        JButton btnSelectImage = new JButton("Chọn ảnh");
        btnSelectImage.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #334155; foreground: #F8FAFC; borderWidth: 0;");
        btnSelectImage.addActionListener(e -> selectImage());

        contentPanel.add(lblImagePreview, BorderLayout.WEST);
        contentPanel.add(btnSelectImage, BorderLayout.CENTER);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn hình ảnh sản phẩm");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedImagePath = selectedFile.getAbsolutePath();
            loadImagePreview(selectedImagePath);
        }
    }

    private void loadImagePreview(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                return;
            }

            BufferedImage originalImage = javax.imageio.ImageIO.read(imageFile);
            if (originalImage == null) {
                return;
            }

            int targetWidth = 80;
            int targetHeight = 80;
            Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);

            ImageIcon icon = new ImageIcon(scaledImage);
            lblImagePreview.setIcon(icon);
            lblImagePreview.setText(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        String imageUrl = null;
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            try {
                File imageDir = new File(IMAGE_DIR);
                if (!imageDir.exists()) {
                    imageDir.mkdirs();
                }

                String extension = "";
                int dotIndex = selectedImagePath.lastIndexOf('.');
                if (dotIndex > 0) {
                    extension = selectedImagePath.substring(dotIndex);
                }

                String newFileName = UUID.randomUUID().toString() + extension;
                File destFile = new File(IMAGE_DIR + newFileName);
                Files.copy(new File(selectedImagePath).toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                imageUrl = "images/products/" + newFileName;
            } catch (IOException e) {
                e.printStackTrace();
                AppMessageDialogs.showWarning(this, "Cảnh báo", "Không thể lưu ảnh, sản phẩm sẽ được lưu không có ảnh.");
            }
        }

        ProductDTO dto = new ProductDTO(null, normalizedName, price, unit, imageUrl, 0, threshold);

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
