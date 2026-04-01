package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class PromotionDialog extends JDialog {
    public PromotionDialog(Window owner) {
        super(owner, "Them/Chinh sua khuyen mai", ModalityType.APPLICATION_MODAL);
        setSize(560, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(ThemeConfig.BG_MAIN);
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        main.add(field("Ten khuyen mai *", new JTextField()));
        JComboBox<String> type = new JComboBox<>(new String[]{"VOUCHER_CODE", "PERCENTAGE", "FIXED_AMOUNT", "BUY_X_GET_Y", "POINT_REDEMPTION"});
        main.add(field("Loai khuyen mai *", type));
        main.add(field("Gia tri giam *", new JTextField()));
        main.add(field("Max giam", new JTextField()));
        main.add(field("Ngay bat dau", new JTextField()));
        main.add(field("Ngay ket thuc", new JTextField()));
        main.add(field("Dieu kien toi thieu", new JTextField()));
        main.add(field("Gioi han su dung", new JTextField()));

        JCheckBox autoApply = new JCheckBox("Tu dong ap dung");
        autoApply.setForeground(ThemeConfig.TEXT_PRIMARY);
        autoApply.setOpaque(false);
        autoApply.setFont(ThemeConfig.FONT_BODY);
        main.add(autoApply);
        main.add(Box.createVerticalStrut(12));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton cancel = new JButton("Huy");
        JButton save = new JButton("Luu");
        cancel.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #334155;");
        save.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #6366F1; borderWidth: 0;");
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> dispose());
        actions.add(cancel);
        actions.add(save);
        main.add(actions);
        add(main, BorderLayout.CENTER);
    }

    private JPanel field(String label, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        JLabel l = new JLabel(label);
        l.setFont(ThemeConfig.FONT_BODY);
        l.setForeground(ThemeConfig.TEXT_SECONDARY);
        input.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #1E293B; foreground: #F8FAFC");
        input.setFont(ThemeConfig.FONT_BODY);
        p.add(l, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return p;
    }
}
