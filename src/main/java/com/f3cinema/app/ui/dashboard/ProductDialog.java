package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.dto.ProductDTO;
import com.f3cinema.app.service.InventoryService;
import com.f3cinema.app.service.impl.InventoryServiceImpl;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.common.dialog.BaseAppDialog;
import com.f3cinema.app.ui.common.dialog.DialogStyle;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Refactored ProductDialog — Unified Form for Create and Update Product.
 * Adheres to Frontend Style Guide and Professional UI/UX Standards.
 */
public class ProductDialog extends BaseAppDialog {
    private final Long productId;
    private final Runnable onSuccessCallback;
    private final InventoryService inventoryService = InventoryServiceImpl.getInstance();

    private JTextField txtName, txtUnit;
    private JComboBox<String> cbCategory;
    private JSpinner spinPrice, spinThreshold;
    private JLabel lblImagePreview;
    private JButton btnSave;
    private String selectedImagePath;
    private String currentImageUrl;
    
    private static final String IMAGE_DIR = "src/main/resources/images/products/";
    private static final Color BG_SURFACE = new Color(0x1E293B); // Slate 800

    public ProductDialog(JFrame owner, Long productId, Runnable onSuccessCallback) {
        super(owner, productId == null ? "Thêm Mới Sản Phẩm" : "Cập Nhật Sản Phẩm");
        this.productId = productId;
        this.onSuccessCallback = onSuccessCallback;

        setupBaseDialog(820, 520);
        initComponents();
        setupEvents();
        
        if (productId != null) {
            bindData();
        }
    }

    private void initComponents() {
        JPanel surface = createSurfacePanel();
        surface.setBackground(BG_SURFACE);
        setContentPane(surface);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // ------------- HEADER -------------
        String headerText = productId == null ? "THÊM SẢN PHẨM KHU VỰC BÁN" : "CHỈNH SỬA THÔNG TIN SẢN PHẨM";
        JLabel lblHeader = DialogStyle.titleLabel(headerText);
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(lblHeader);
        mainContent.add(Box.createRigidArea(new Dimension(0, 30)));

        // ------------- FORM -------------
        JPanel form = new JPanel(new GridLayout(4, 2, 24, 20));
        form.setOpaque(false);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(createFieldGroup("Tên sản phẩm", txtName = new JTextField()));
        cbCategory = new JComboBox<>(new String[]{"SNACK", "DRINK", "COMBO"});
        form.add(createFieldGroup("Danh mục", cbCategory));

        spinPrice = new JSpinner(new SpinnerNumberModel(45000.0, 0.0, 10000000.0, 5000.0));
        form.add(createFieldGroup("Giá bán (VNĐ)", spinPrice));

        form.add(createFieldGroup("Đơn vị tính", txtUnit = new JTextField()));

        spinThreshold = new JSpinner(new SpinnerNumberModel(20, 0, 10000, 1));
        form.add(createFieldGroup("Ngưỡng báo tồn (Min Threshold)", spinThreshold));

        form.add(createImageUploadPanel());

        mainContent.add(form);
        mainContent.add(Box.createRigidArea(new Dimension(0, 40)));

        // ------------- BUTTONS -------------
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnClose = DialogStyle.secondaryButton("Hủy Lệnh");
        btnClose.addActionListener(e -> dispose());
        btnPanel.add(btnClose);

        btnSave = DialogStyle.primaryButton(productId == null ? "Lưu Sản Phẩm" : "Cập Nhật");
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #6366F1; foreground: #FFFFFF;");
        btnSave.addActionListener(e -> handleSave());
        btnPanel.add(btnSave);

        mainContent.add(btnPanel);
        surface.add(mainContent, BorderLayout.CENTER);
    }

    private void setupEvents() {
        // ESC to close
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Enter to Save
        getRootPane().setDefaultButton(btnSave);
    }

    private void bindData() {
        new SwingWorker<ProductDTO, Void>() {
            @Override
            protected ProductDTO doInBackground() {
                List<ProductDTO> all = inventoryService.getAllInventory();
                return all.stream()
                        .filter(p -> p.id().equals(productId))
                        .findFirst()
                        .orElse(null);
            }

            @Override
            protected void done() {
                try {
                    ProductDTO dto = get();
                    if (dto != null) {
                        String cleanName = dto.name();
                        if (cleanName.contains("] ")) {
                            cleanName = cleanName.substring(cleanName.indexOf("] ") + 2);
                        }
                        txtName.setText(cleanName);
                        txtUnit.setText(dto.unit());
                        spinPrice.setValue(dto.price().doubleValue());
                        spinThreshold.setValue(dto.minThreshold());
                        
                        // Set category from name prefix or logic
                        if (dto.name().contains("[SNACK]")) cbCategory.setSelectedItem("SNACK");
                        else if (dto.name().contains("[DRINK]")) cbCategory.setSelectedItem("DRINK");
                        else if (dto.name().contains("[COMBO]")) cbCategory.setSelectedItem("COMBO");
                        
                        currentImageUrl = dto.imageUrl();
                        if (currentImageUrl != null) {
                            loadImagePreview(IMAGE_DIR.replace("src/main/resources/", "") + currentImageUrl);
                        }
                    }
                } catch (Exception e) {
                    AppMessageDialogs.showError(ProductDialog.this, "Lỗi", "Không thể tải thông tin sản phẩm.");
                }
            }
        }.execute();
    }

    private JPanel createFieldGroup(String labelText, JComponent inputComp) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JLabel lbl = DialogStyle.formLabel(labelText);
        DialogStyle.styleInput(inputComp);
        if (inputComp instanceof JTextField tf) {
            tf.setCaretColor(Color.WHITE);
        }
        p.add(lbl, BorderLayout.NORTH);
        p.add(inputComp, BorderLayout.CENTER);
        return p;
    }

    private JPanel createImageUploadPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        JLabel lbl = DialogStyle.formLabel("Hình ảnh minh họa");

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        contentPanel.setOpaque(false);

        lblImagePreview = new JLabel("No Image");
        lblImagePreview.setPreferredSize(new Dimension(80, 80));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));
        lblImagePreview.setBackground(new Color(0x0F172A));
        lblImagePreview.setOpaque(true);
        lblImagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagePreview.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        JButton btnSelectImage = new JButton("Duyệt File...");
        btnSelectImage.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #334155; foreground: #F8FAFC; borderWidth: 0;");
        btnSelectImage.addActionListener(e -> selectImage());

        contentPanel.add(lblImagePreview);
        contentPanel.add(btnSelectImage);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "jpeg", "png"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImagePath = fileChooser.getSelectedFile().getAbsolutePath();
            loadImagePreview(selectedImagePath);
        }
    }

    private void loadImagePreview(String path) {
        try {
            File file = new File(path.startsWith("images/") ? "src/main/resources/" + path : path);
            if (!file.exists()) return;
            BufferedImage img = javax.imageio.ImageIO.read(file);
            if (img != null) {
                Image scaled = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                lblImagePreview.setIcon(new ImageIcon(scaled));
                lblImagePreview.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSave() {
        String name = txtName.getText().trim();
        String unit = txtUnit.getText().trim();
        String category = (String) cbCategory.getSelectedItem();

        if (name.isEmpty() || unit.isEmpty()) {
            AppMessageDialogs.showWarning(this, "Yêu cầu", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        BigDecimal price = BigDecimal.valueOf((Double) spinPrice.getValue());
        Integer threshold = (Integer) spinThreshold.getValue();
        String normalizedName = name.startsWith("[" + category + "]") ? name : ("[" + category + "] " + name);

        String imageUrl = currentImageUrl;
        if (selectedImagePath != null) {
            try {
                File dir = new File(IMAGE_DIR);
                if (!dir.exists()) dir.mkdirs();
                String ext = selectedImagePath.substring(selectedImagePath.lastIndexOf("."));
                String newName = UUID.randomUUID().toString() + ext;
                Files.copy(new File(selectedImagePath).toPath(), new File(IMAGE_DIR + newName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                imageUrl = "images/products/" + newName;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ProductDTO dto = new ProductDTO(productId, normalizedName, price, unit, imageUrl, 0, threshold);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (productId == null) inventoryService.addProduct(dto);
                else inventoryService.updateProduct(dto);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    AppMessageDialogs.showInfo(ProductDialog.this, "Thành công", 
                            productId == null ? "Đã thêm sản phẩm mới!" : "Đã cập nhật thông tin sản phẩm!");
                    if (onSuccessCallback != null) onSuccessCallback.run();
                    dispose();
                } catch (Exception e) {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    AppMessageDialogs.showError(ProductDialog.this, "Lỗi", msg);
                }
            }
        }.execute();
    }
}
