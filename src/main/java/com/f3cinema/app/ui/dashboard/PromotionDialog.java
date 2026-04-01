package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.entity.Voucher;
import com.f3cinema.app.entity.enums.VoucherStatus;
import com.f3cinema.app.entity.enums.VoucherType;
import com.f3cinema.app.service.VoucherService;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.common.dialog.BaseAppDialog;
import com.f3cinema.app.ui.common.dialog.DialogStyle;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PromotionDialog extends BaseAppDialog {
    private final VoucherService voucherService = new VoucherService();
    private final Voucher editingVoucher;
    private final Runnable onSaveCallback;
    
    private JTextField tfCode;
    private JTextField tfDescription;
    private JComboBox<VoucherType> cbVoucherType;
    
    private JTextField tfDiscountPercent;
    private JTextField tfMaxDiscount;
    private JPanel pnPercentageFields;
    
    private JTextField tfDiscountAmount;
    private JPanel pnFixedFields;
    
    private JTextField tfBuyQuantity;
    private JTextField tfGetQuantity;
    private JPanel pnBuyXGetYFields;
    
    private JTextField tfAppliesTo;
    private JPanel pnComboFields;
    
    private JTextField tfMinOrder;
    private JSpinner spValidFrom;
    private JSpinner spValidUntil;
    private JTextField tfUsageLimit;
    private JComboBox<VoucherStatus> cbStatus;
    
    public PromotionDialog(Window owner, Runnable onSaveCallback) {
        this(owner, null, onSaveCallback);
    }
    
    public PromotionDialog(Window owner, Voucher voucher, Runnable onSaveCallback) {
        super(owner, voucher == null ? "Thêm Voucher Mới" : "Chỉnh Sửa Voucher");
        this.editingVoucher = voucher;
        this.onSaveCallback = onSaveCallback;
        setupBaseDialog(900, 700);
        JPanel surface = createSurfacePanel();
        setContentPane(surface);
        buildForm(surface);
        
        if (editingVoucher != null) {
            loadVoucherData();
        } else {
            updateFormFieldsVisibility(VoucherType.PERCENTAGE);
        }
    }
    private void buildForm(JPanel surface) {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setOpaque(false);

        JPanel formContent = new JPanel(new GridBagLayout());
        formContent.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 12, 12);

        tfCode = new JTextField();
        tfDescription = new JTextField();
        cbVoucherType = new JComboBox<>(VoucherType.values());
        cbVoucherType.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof VoucherType) {
                    setText(((VoucherType) value).getDisplayName());
                }
                return this;
            }
        });
        
        cbVoucherType.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateFormFieldsVisibility((VoucherType) e.getItem());
            }
        });
        
        tfDiscountPercent = new JTextField();
        tfMaxDiscount = new JTextField();
        tfDiscountAmount = new JTextField();
        tfBuyQuantity = new JTextField();
        tfGetQuantity = new JTextField();
        tfAppliesTo = new JTextField();
        tfMinOrder = new JTextField();
        tfUsageLimit = new JTextField();
        
        SpinnerDateModel fromModel = new SpinnerDateModel();
        spValidFrom = new JSpinner(fromModel);
        JSpinner.DateEditor fromEditor = new JSpinner.DateEditor(spValidFrom, "dd/MM/yyyy");
        spValidFrom.setEditor(fromEditor);
        
        SpinnerDateModel untilModel = new SpinnerDateModel();
        spValidUntil = new JSpinner(untilModel);
        JSpinner.DateEditor untilEditor = new JSpinner.DateEditor(spValidUntil, "dd/MM/yyyy");
        spValidUntil.setEditor(untilEditor);
        
        cbStatus = new JComboBox<>(VoucherStatus.values());

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 12, 0);
        formContent.add(field("Mã Voucher *", tfCode, "Ví dụ: SUMMER2026"), gbc);

        gbc.gridy = row++;
        formContent.add(field("Mô tả", tfDescription, "Mô tả chi tiết voucher"), gbc);

        gbc.gridy = row++;
        formContent.add(field("Loại Voucher *", cbVoucherType, null), gbc);

        pnPercentageFields = new JPanel(new GridBagLayout());
        pnPercentageFields.setOpaque(false);
        GridBagConstraints gbcPct = new GridBagConstraints();
        gbcPct.fill = GridBagConstraints.HORIZONTAL;
        gbcPct.weightx = 1.0;
        gbcPct.gridx = 0; gbcPct.gridy = 0;
        gbcPct.insets = new Insets(0, 0, 0, 12);
        pnPercentageFields.add(field("Giảm giá (%) *", tfDiscountPercent, "Ví dụ: 25"), gbcPct);
        gbcPct.gridx = 1;
        gbcPct.insets = new Insets(0, 0, 0, 0);
        pnPercentageFields.add(field("Giảm tối đa (₫)", tfMaxDiscount, "Ví dụ: 50000"), gbcPct);

        pnFixedFields = new JPanel(new BorderLayout());
        pnFixedFields.setOpaque(false);
        pnFixedFields.add(field("Số tiền giảm (₫) *", tfDiscountAmount, "Ví dụ: 50000"), BorderLayout.CENTER);

        pnBuyXGetYFields = new JPanel(new GridBagLayout());
        pnBuyXGetYFields.setOpaque(false);
        GridBagConstraints gbcBuy = new GridBagConstraints();
        gbcBuy.fill = GridBagConstraints.HORIZONTAL;
        gbcBuy.weightx = 1.0;
        gbcBuy.gridx = 0; gbcBuy.gridy = 0;
        gbcBuy.insets = new Insets(0, 0, 0, 12);
        pnBuyXGetYFields.add(field("Mua số lượng *", tfBuyQuantity, "Ví dụ: 2"), gbcBuy);
        gbcBuy.gridx = 1;
        gbcBuy.insets = new Insets(0, 0, 0, 0);
        pnBuyXGetYFields.add(field("Tặng số lượng *", tfGetQuantity, "Ví dụ: 1"), gbcBuy);

        pnComboFields = new JPanel(new GridBagLayout());
        pnComboFields.setOpaque(false);
        GridBagConstraints gbcCombo = new GridBagConstraints();
        gbcCombo.fill = GridBagConstraints.HORIZONTAL;
        gbcCombo.weightx = 1.0;
        gbcCombo.gridx = 0; gbcCombo.gridy = 0;
        gbcCombo.insets = new Insets(0, 0, 0, 12);
        pnComboFields.add(field("Giảm giá (%) *", tfDiscountPercent, "Ví dụ: 30"), gbcCombo);
        gbcCombo.gridx = 1;
        gbcCombo.insets = new Insets(0, 0, 0, 0);
        pnComboFields.add(field("Áp dụng cho", tfAppliesTo, "COMBO"), gbcCombo);

        gbc.gridx = 0; gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 12, 0);
        formContent.add(pnPercentageFields, gbc);
        formContent.add(pnFixedFields, gbc);
        formContent.add(pnBuyXGetYFields, gbc);
        formContent.add(pnComboFields, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 12, 12);
        gbc.gridx = 0; gbc.gridy = row;
        formContent.add(field("Đơn tối thiểu (₫)", tfMinOrder, "Ví dụ: 100000"), gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 12, 0);
        formContent.add(field("Giới hạn sử dụng", tfUsageLimit, "Ví dụ: 100"), gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.insets = new Insets(0, 0, 12, 12);
        formContent.add(field("Ngày bắt đầu *", spValidFrom, null), gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 12, 0);
        formContent.add(field("Ngày kết thúc *", spValidUntil, null), gbc);

        if (editingVoucher != null) {
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(0, 0, 12, 0);
            formContent.add(field("Trạng thái", cbStatus, null), gbc);
        }

        main.add(formContent, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton cancel = DialogStyle.secondaryButton("Hủy");
        JButton save = DialogStyle.primaryButton(editingVoucher == null ? "Tạo Voucher" : "Cập Nhật");
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> handleSave());
        actions.add(cancel);
        actions.add(save);
        main.add(actions, BorderLayout.SOUTH);
        
        surface.add(main, BorderLayout.CENTER);
    }
    
    private void updateFormFieldsVisibility(VoucherType type) {
        pnPercentageFields.setVisible(type == VoucherType.PERCENTAGE);
        pnFixedFields.setVisible(type == VoucherType.FIXED_AMOUNT);
        pnBuyXGetYFields.setVisible(type == VoucherType.BUY_X_GET_Y);
        pnComboFields.setVisible(type == VoucherType.COMBO_DISCOUNT);
        
        revalidate();
        repaint();
    }

    private JPanel field(String label, JComponent input, String placeholder) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JLabel l = DialogStyle.formLabel(label);
        DialogStyle.styleInput(input);
        
        if (placeholder != null && input instanceof JTextField) {
            ((JTextField) input).putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        }
        
        p.add(l, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }
    
    private void loadVoucherData() {
        tfCode.setText(editingVoucher.getCode());
        tfCode.setEnabled(false);
        tfDescription.setText(editingVoucher.getDescription());
        
        cbVoucherType.setSelectedItem(editingVoucher.getVoucherType());
        
        if (editingVoucher.getDiscountPercent() != null) {
            tfDiscountPercent.setText(editingVoucher.getDiscountPercent().toString());
        }
        if (editingVoucher.getDiscountAmount() != null) {
            tfDiscountAmount.setText(editingVoucher.getDiscountAmount().toString());
        }
        if (editingVoucher.getMaxDiscount() != null) {
            tfMaxDiscount.setText(editingVoucher.getMaxDiscount().toString());
        }
        if (editingVoucher.getMinOrderAmount() != null) {
            tfMinOrder.setText(editingVoucher.getMinOrderAmount().toString());
        }
        if (editingVoucher.getBuyQuantity() != null) {
            tfBuyQuantity.setText(editingVoucher.getBuyQuantity().toString());
        }
        if (editingVoucher.getGetQuantity() != null) {
            tfGetQuantity.setText(editingVoucher.getGetQuantity().toString());
        }
        if (editingVoucher.getAppliesToCategory() != null) {
            tfAppliesTo.setText(editingVoucher.getAppliesToCategory());
        }
        if (editingVoucher.getUsageLimit() != null) {
            tfUsageLimit.setText(editingVoucher.getUsageLimit().toString());
        }
        
        spValidFrom.setValue(java.sql.Timestamp.valueOf(editingVoucher.getValidFrom()));
        spValidUntil.setValue(java.sql.Timestamp.valueOf(editingVoucher.getValidUntil()));
        cbStatus.setSelectedItem(editingVoucher.getStatus());
        
        updateFormFieldsVisibility(editingVoucher.getVoucherType());
    }
    
    private void handleSave() {
        try {
            String code = tfCode.getText();
            String description = tfDescription.getText();
            VoucherType voucherType = (VoucherType) cbVoucherType.getSelectedItem();
            
            BigDecimal discountPercent = null;
            BigDecimal discountAmount = null;
            BigDecimal maxDiscount = null;
            Integer buyQuantity = null;
            Integer getQuantity = null;
            String appliesToCategory = null;
            
            switch (voucherType) {
                case PERCENTAGE -> {
                    discountPercent = new BigDecimal(tfDiscountPercent.getText().trim());
                    maxDiscount = tfMaxDiscount.getText().isBlank() ? null : 
                        new BigDecimal(tfMaxDiscount.getText().trim());
                }
                case FIXED_AMOUNT -> {
                    discountAmount = new BigDecimal(tfDiscountAmount.getText().trim());
                }
                case BUY_X_GET_Y -> {
                    buyQuantity = Integer.parseInt(tfBuyQuantity.getText().trim());
                    getQuantity = Integer.parseInt(tfGetQuantity.getText().trim());
                }
                case COMBO_DISCOUNT -> {
                    discountPercent = new BigDecimal(tfDiscountPercent.getText().trim());
                    appliesToCategory = tfAppliesTo.getText().isBlank() ? "COMBO" : 
                        tfAppliesTo.getText().trim().toUpperCase();
                }
            }
            
            BigDecimal minOrder = tfMinOrder.getText().isBlank() ? BigDecimal.ZERO : 
                new BigDecimal(tfMinOrder.getText().trim());
            Integer usageLimit = tfUsageLimit.getText().isBlank() ? null : 
                Integer.parseInt(tfUsageLimit.getText().trim());
            
            java.util.Date fromDate = (java.util.Date) spValidFrom.getValue();
            java.util.Date untilDate = (java.util.Date) spValidUntil.getValue();
            LocalDateTime validFrom = new java.sql.Timestamp(fromDate.getTime()).toLocalDateTime();
            LocalDateTime validUntil = new java.sql.Timestamp(untilDate.getTime()).toLocalDateTime();
            
            if (editingVoucher == null) {
                voucherService.createVoucher(code, description, voucherType, discountPercent, 
                    discountAmount, maxDiscount, minOrder, buyQuantity, getQuantity, 
                    appliesToCategory, validFrom, validUntil, usageLimit);
                AppMessageDialogs.showInfo(this, "Thành công", "Tạo voucher thành công!");
            } else {
                VoucherStatus status = (VoucherStatus) cbStatus.getSelectedItem();
                voucherService.updateVoucher(editingVoucher.getId(), code, description, 
                    voucherType, discountPercent, discountAmount, maxDiscount, minOrder, 
                    buyQuantity, getQuantity, appliesToCategory, validFrom, validUntil, 
                    usageLimit, status);
                AppMessageDialogs.showInfo(this, "Thành công", "Cập nhật voucher thành công!");
            }
            
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            dispose();
            
        } catch (NumberFormatException ex) {
            AppMessageDialogs.showError(this, "Giá trị số không hợp lệ. Vui lòng kiểm tra lại.");
        } catch (IllegalArgumentException ex) {
            AppMessageDialogs.showError(this, ex.getMessage());
        } catch (Exception ex) {
            AppMessageDialogs.showError(this, "Lỗi: " + ex.getMessage());
        }
    }
}

