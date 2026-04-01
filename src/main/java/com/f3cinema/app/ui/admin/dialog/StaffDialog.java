package com.f3cinema.app.ui.admin.dialog;

import com.f3cinema.app.config.ThemeConfig;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * StaffDialog — Add / Edit staff account.
 * Password field is optional; blank means keep existing (edit) or default "1" (add).
 */
public class StaffDialog extends JDialog {

    private static final Color C_BG_SURFACE = new Color(30, 41, 59, 220);
    private static final Color C_ACCENT = ThemeConfig.ACCENT_COLOR;
    private static final Color C_DANGER = ThemeConfig.TEXT_DANGER;
    private static final Color C_TEXT_PRIMARY = ThemeConfig.TEXT_PRIMARY;
    private static final Color C_TEXT_HINT = ThemeConfig.TEXT_SECONDARY;
    private static final Color C_BORDER = new Color(255, 255, 255, 25);

    private final boolean editMode;
    private JTextField txtUsername;
    private JTextField txtFullName;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JLabel lblPasswordStrength;
    private JLabel lblError;
    private JButton btnSave;
    private boolean saved = false;

    public StaffDialog(Window owner, boolean editMode) {
        super(owner, editMode ? "Chỉnh sửa nhân viên" : "Thêm nhân viên", ModalityType.APPLICATION_MODAL);
        this.editMode = editMode;
        initUI();
        setupKeyBindings();
    }

    public boolean isSaved() {
        return saved;
    }

    public String getUsername() {
        return txtUsername.getText().trim();
    }

    public String getFullName() {
        return txtFullName.getText().trim();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword()).trim();
    }

    public void setInitialValues(String username, String fullName) {
        txtUsername.setText(username == null ? "" : username);
        txtFullName.setText(fullName == null ? "" : fullName);
    }

    public void setError(String message) {
        lblError.setText(message == null ? " " : ("⚠ " + message));
    }

    public void setOnSave(Runnable onSave) {
        getRootPane().setDefaultButton(btnSave);
        btnSave.addActionListener(e -> onSave.run());
    }

    public void markSavedAndClose() {
        saved = true;
        dispose();
    }

    private void initUI() {
        setUndecorated(true);
        setSize(520, 460);
        setLocationRelativeTo(getOwner());
        setBackground(new Color(0, 0, 0, 0));

        JPanel glass = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 24, 24);
                g2.setStroke(new BasicStroke(1.2f));
                g2.setColor(C_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 7, getHeight() - 7, 24, 24);
                g2.setColor(C_ACCENT);
                g2.fillRoundRect(0, 0, getWidth() - 6, 4, 4, 4);
                g2.dispose();
            }
        };
        glass.setOpaque(false);
        glass.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        JLabel lblTitle = new JLabel(editMode ? "Chỉnh sửa Nhân viên" : "Thêm Nhân viên");
        lblTitle.setFont(ThemeConfig.FONT_H1);
        lblTitle.setForeground(C_TEXT_PRIMARY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;

        gbc.insets = new Insets(10, 0, 4, 0);
        gbc.gridy = 0;
        form.add(buildLabel("Username *"), gbc);
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.gridy = 1;
        txtUsername = new JTextField();
        styleTextField(txtUsername, "Ví dụ: staff06");
        form.add(txtUsername, gbc);

        gbc.insets = new Insets(6, 0, 4, 0);
        gbc.gridy = 2;
        form.add(buildLabel("Họ tên *"), gbc);
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.gridy = 3;
        txtFullName = new JTextField();
        styleTextField(txtFullName, "Ví dụ: Cinema Staff 06");
        form.add(txtFullName, gbc);

        gbc.insets = new Insets(6, 0, 4, 0);
        gbc.gridy = 4;
        form.add(buildLabel(editMode ? "Mật khẩu mới (để trống nếu không đổi)" : "Mật khẩu (để trống = 1)"), gbc);
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.gridy = 5;
        txtPassword = new JPasswordField();
        styleTextField(txtPassword, "");
        form.add(txtPassword, gbc);

        gbc.insets = new Insets(0, 0, 8, 0);
        gbc.gridy = 6;
        JPanel pwOptions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pwOptions.setOpaque(false);
        chkShowPassword = new JCheckBox("Hiện mật khẩu");
        chkShowPassword.setOpaque(false);
        chkShowPassword.setForeground(C_TEXT_HINT);
        chkShowPassword.setFont(ThemeConfig.FONT_SMALL);
        chkShowPassword.addActionListener(e -> txtPassword.setEchoChar(chkShowPassword.isSelected() ? (char) 0 : '\u2022'));
        lblPasswordStrength = new JLabel(" ");
        lblPasswordStrength.setForeground(C_TEXT_HINT);
        lblPasswordStrength.setFont(ThemeConfig.FONT_SMALL);
        pwOptions.add(chkShowPassword);
        pwOptions.add(lblPasswordStrength);
        form.add(pwOptions, gbc);

        txtPassword.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePasswordStrength(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePasswordStrength(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePasswordStrength(); }
        });

        gbc.insets = new Insets(4, 0, 0, 0);
        gbc.gridy = 7;
        lblError = new JLabel(" ");
        lblError.setFont(ThemeConfig.FONT_SMALL);
        lblError.setForeground(C_DANGER);
        form.add(lblError, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnCancel = buildCancelButton("Hủy");
        btnSave = buildSaveButton("Lưu");
        btnCancel.addActionListener(e -> dispose());
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        glass.add(lblTitle, BorderLayout.NORTH);
        glass.add(form, BorderLayout.CENTER);
        glass.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(glass);
    }

    private void setupKeyBindings() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private JLabel buildLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ThemeConfig.FONT_BODY);
        lbl.setForeground(C_TEXT_HINT);
        return lbl;
    }

    private void styleTextField(JTextField field, String placeholder) {
        if (placeholder != null && !placeholder.isBlank()) {
            field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        }
        field.putClientProperty(FlatClientProperties.STYLE,
                "arc: 16; " +
                        "margin: 4, 12, 4, 12; " +
                        "focusWidth: 2; " +
                        "innerFocusWidth: 0;");
        field.setFont(ThemeConfig.FONT_BODY);
        field.setBackground(new Color(15, 23, 42, 180));
        field.setForeground(C_TEXT_PRIMARY);
        field.setPreferredSize(new Dimension(0, 44));
        field.setCaretColor(C_ACCENT);
    }

    private JButton buildSaveButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
        btn.setBackground(C_ACCENT);
        btn.setForeground(Color.WHITE);
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 16; borderWidth: 0; margin: 6, 20, 6, 20;");
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buildCancelButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(C_TEXT_HINT);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        btn.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
        btn.putClientProperty(FlatClientProperties.STYLE, "margin: 6, 20, 6, 20; hoverBackground: null;");
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updatePasswordStrength() {
        if (lblPasswordStrength == null) return;
        String pw = getPassword();
        if (pw.isBlank()) {
            lblPasswordStrength.setText(" ");
            return;
        }
        int score = 0;
        if (pw.length() >= 8) score++;
        if (pw.matches(".*[A-Z].*")) score++;
        if (pw.matches(".*\\d.*")) score++;
        if (pw.matches(".*[^A-Za-z0-9].*")) score++;
        if (score <= 1) {
            lblPasswordStrength.setText("Yeu");
            lblPasswordStrength.setForeground(C_DANGER);
        } else if (score <= 3) {
            lblPasswordStrength.setText("Trung binh");
            lblPasswordStrength.setForeground(Color.decode("#F59E0B"));
        } else {
            lblPasswordStrength.setText("Manh");
            lblPasswordStrength.setForeground(ThemeConfig.TEXT_SUCCESS);
        }
    }
}

