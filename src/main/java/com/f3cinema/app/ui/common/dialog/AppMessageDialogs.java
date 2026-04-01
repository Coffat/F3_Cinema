package com.f3cinema.app.ui.common.dialog;

import com.f3cinema.app.config.ThemeConfig;

import javax.swing.*;
import java.awt.*;

public final class AppMessageDialogs {
    private AppMessageDialogs() {
    }

    public static void showInfo(Component parent, String message) {
        showInfo(parent, "Thông báo", message);
    }

    public static void showInfo(Component parent, String title, String message) {
        withTheme(() -> JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE));
    }

    public static void showWarning(Component parent, String message) {
        showWarning(parent, "Cảnh báo", message);
    }

    public static void showWarning(Component parent, String title, String message) {
        withTheme(() -> JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE));
    }

    public static void showError(Component parent, String message) {
        showError(parent, "Lỗi", message);
    }

    public static void showError(Component parent, String title, String message) {
        withTheme(() -> JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE));
    }

    public static boolean confirmYesNo(Component parent, String title, String message) {
        final int[] result = {JOptionPane.NO_OPTION};
        withTheme(() -> result[0] = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE));
        return result[0] == JOptionPane.YES_OPTION;
    }

    public static String promptInput(Component parent, String title, String message) {
        final String[] value = new String[1];
        withTheme(() -> value[0] = JOptionPane.showInputDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE));
        return value[0];
    }

    private static void withTheme(Runnable action) {
        Color oldBg = UIManager.getColor("OptionPane.background");
        Color oldMsgBg = UIManager.getColor("Panel.background");
        Color oldFg = UIManager.getColor("OptionPane.messageForeground");
        Font oldFont = UIManager.getFont("OptionPane.messageFont");
        Font oldBtnFont = UIManager.getFont("OptionPane.buttonFont");
        try {
            UIManager.put("OptionPane.background", ThemeConfig.BG_CARD);
            UIManager.put("Panel.background", ThemeConfig.BG_CARD);
            UIManager.put("OptionPane.messageForeground", ThemeConfig.TEXT_PRIMARY);
            UIManager.put("OptionPane.messageFont", ThemeConfig.FONT_BODY);
            UIManager.put("OptionPane.buttonFont", ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
            action.run();
        } finally {
            UIManager.put("OptionPane.background", oldBg);
            UIManager.put("Panel.background", oldMsgBg);
            UIManager.put("OptionPane.messageForeground", oldFg);
            UIManager.put("OptionPane.messageFont", oldFont);
            UIManager.put("OptionPane.buttonFont", oldBtnFont);
        }
    }
}
