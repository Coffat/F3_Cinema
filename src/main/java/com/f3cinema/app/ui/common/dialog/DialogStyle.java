package com.f3cinema.app.ui.common.dialog;

import com.f3cinema.app.config.ThemeConfig;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public final class DialogStyle {
    private DialogStyle() {
    }

    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Inter", Font.BOLD, 22));
        lbl.setForeground(ThemeConfig.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel formLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ThemeConfig.FONT_BODY);
        lbl.setForeground(ThemeConfig.TEXT_SECONDARY);
        return lbl;
    }

    public static JLabel errorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(ThemeConfig.FONT_SMALL);
        lbl.setForeground(ThemeConfig.TEXT_DANGER);
        return lbl;
    }

    public static void styleInput(JComponent input) {
        input.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; focusWidth: 2; innerFocusWidth: 0; " +
                        "background: #1E293B; foreground: #F8FAFC; borderColor: #334155;");
        input.setFont(new Font("Inter", Font.PLAIN, 15));
        input.setPreferredSize(new Dimension(0, 42));
        if (input instanceof JTextField textField) {
            textField.setCaretColor(Color.WHITE);
        }
    }

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; borderWidth: 0; margin: 8,24,8,24; background: #6366F1;");
        btn.putClientProperty(FlatClientProperties.MINIMUM_WIDTH, 0);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setForeground(ThemeConfig.TEXT_SECONDARY);
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; borderWidth: 0; margin: 8,24,8,24; background: #334155;");
        btn.putClientProperty(FlatClientProperties.MINIMUM_WIDTH, 0);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
