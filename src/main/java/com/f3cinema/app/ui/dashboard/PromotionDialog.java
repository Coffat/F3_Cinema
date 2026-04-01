package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.ui.common.dialog.BaseAppDialog;
import com.f3cinema.app.ui.common.dialog.DialogStyle;

import javax.swing.*;
import java.awt.*;

public class PromotionDialog extends BaseAppDialog {
    public PromotionDialog(Window owner) {
        super(owner, "Them/Chinh sua khuyen mai");
        setupBaseDialog(860, 520);
        JPanel surface = createSurfacePanel();
        setContentPane(surface);
        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setOpaque(false);

        JPanel formContent = new JPanel(new GridBagLayout());
        formContent.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 16);

        JPanel nameField = field("Ten khuyen mai *", new JTextField());
        JComboBox<String> type = new JComboBox<>(new String[]{"VOUCHER_CODE", "PERCENTAGE", "FIXED_AMOUNT", "BUY_X_GET_Y", "POINT_REDEMPTION"});
        JPanel typeField = field("Loai khuyen mai *", type);
        JPanel valueField = field("Gia tri giam *", new JTextField());
        JPanel maxField = field("Max giam", new JTextField());
        JPanel startField = field("Ngay bat dau", new JTextField());
        JPanel endField = field("Ngay ket thuc", new JTextField());
        JPanel minField = field("Dieu kien toi thieu", new JTextField());
        JPanel limitField = field("Gioi han su dung", new JTextField());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        formContent.add(nameField, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 0, 16);
        gbc.gridx = 0;
        gbc.gridy = 1;
        formContent.add(typeField, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        formContent.add(valueField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 16);
        formContent.add(maxField, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        formContent.add(minField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 16);
        formContent.add(startField, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        formContent.add(endField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 16);
        formContent.add(limitField, gbc);

        JCheckBox autoApply = new JCheckBox("Tu dong ap dung");
        autoApply.setForeground(ThemeConfig.TEXT_PRIMARY);
        autoApply.setOpaque(false);
        autoApply.setFont(ThemeConfig.FONT_BODY);
        gbc.gridx = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        formContent.add(autoApply, gbc);
        main.add(formContent, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton cancel = DialogStyle.secondaryButton("Huy");
        JButton save = DialogStyle.primaryButton("Luu");
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> dispose());
        actions.add(cancel);
        actions.add(save);
        main.add(actions, BorderLayout.SOUTH);
        surface.add(main, BorderLayout.CENTER);
    }

    private JPanel field(String label, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        JLabel l = DialogStyle.formLabel(label);
        DialogStyle.styleInput(input);
        p.add(l, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return p;
    }
}
