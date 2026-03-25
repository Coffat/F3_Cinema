package com.f3cinema.app.ui;

import com.f3cinema.app.entity.User;
import com.f3cinema.app.exception.AuthenticationException;
import com.f3cinema.app.service.UserService;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.text.JTextComponent;


/**
 * LoginFrame — "Modern Midnight" UI for F3 Cinema.
 */
public class LoginFrame extends JFrame {

    // ── Design Tokens ──────────────────────────────────────────────────
    private static final Color BG_DARK        = new Color(0x0F172A);  // Slate 900
    private static final Color CARD_BG        = new Color(0x1E293B);  // Slate 800 (used as card paint)
    private static final Color ACCENT         = new Color(0x6366F1);  // Indigo 500
    private static final Color ACCENT_HOVER   = new Color(0x818CF8);  // Indigo 400
    private static final Color TEXT_PRIMARY   = new Color(0xF8FAFC);  // Slate 50
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);  // Slate 400
    private static final Color ERROR_COLOR    = new Color(0xF43F5E);  // Rose 500
    private static final Color FIELD_BG       = new Color(0x0F172A);  // Slate 900
    private static final Color BORDER_COLOR   = new Color(0x334155);  // Slate 700
    private static final int   ARC            = 16;

    // ── Components ────────────────────────────────────────────────────
    private JTextField     tfUsername;
    private JPasswordField pfPassword;
    private JButton        btnLogin;
    private JLabel         lblError;
    private JCheckBox      chkShowPwd;
    private final UserService userService = new UserService();

    public LoginFrame() {
        initialize();
    }

    private void initialize() {
        setTitle("F3 Cinema — Đăng Nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));

        // Main split layout: left brand panel + right card
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(BG_DARK);

        root.add(buildBrandPanel());
        root.add(buildLoginCard());

        setContentPane(root);

        // Enter key on password triggers login
        pfPassword.addActionListener(e -> attemptLogin());

        // Drag to move window (since titlebar is hidden)
        MouseAdapter drag = new MouseAdapter() {
            private Point start;
            public void mousePressed(MouseEvent e)  { start = e.getPoint(); }
            public void mouseDragged(MouseEvent e)  {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - start.x, loc.y + e.getY() - start.y);
            }
        };
        root.addMouseListener(drag);
        root.addMouseMotionListener(drag);
    }

    // ── Left Brand Panel ───────────────────────────────────────────────
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Radial gradient: midnight center → darker edge
                int cx = getWidth() / 2, cy = getHeight() / 2;
                float[] fractions = {0f, 1f};
                Color[] colors = {new Color(0x1e1b4b), BG_DARK}; // indigo 950 → slate 900
                g2.setPaint(new RadialGradientPaint(cx, cy, Math.max(cx, cy), fractions, colors));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative rings
                g2.setColor(new Color(0x6366F1, true));
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        0, new float[]{6, 4}, 0));
                g2.drawOval(cx - 110, cy - 200, 220, 220);
                g2.drawOval(cx - 160, cy - 100, 320, 320);
                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        // Cinema icon (emoji fallback — replace with SVG in production)
        JLabel icon = new JLabel("🎬", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("F3 CINEMA");
        title.setFont(new Font("Inter", Font.BOLD, 36));
        title.setForeground(ACCENT);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Cinema Management System");
        sub.setFont(new Font("Inter", Font.PLAIN, 14));
        sub.setForeground(TEXT_SECONDARY);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        inner.add(icon);
        inner.add(Box.createVerticalStrut(12));
        inner.add(title);
        inner.add(Box.createVerticalStrut(6));
        inner.add(sub);

        panel.add(inner);
        return panel;
    }

    // ── Right Login Card ───────────────────────────────────────────────
    private JPanel buildLoginCard() {
        // Outer panel (right half bg)
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_DARK);

        // Glassmorphism card
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x1E293B));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC * 2, ARC * 2);
                // Subtle border
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC * 2, ARC * 2);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(40, 44, 40, 44));
        card.setPreferredSize(new Dimension(400, 480));

        // Close button (top-right)
        JButton btnClose = makeIconButton("✕");
        btnClose.addActionListener(e -> System.exit(0));
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topRow.setOpaque(false);
        topRow.add(btnClose);

        // Title
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 20));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Chào mừng trở lại ✦");
        lblSub.setFont(new Font("Inter", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_SECONDARY);
        lblSub.setAlignmentX(CENTER_ALIGNMENT);

        // Fields
        JPanel fieldsPanel = buildFieldsPanel();

        // Error label
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Inter", Font.PLAIN, 12));
        lblError.setForeground(ERROR_COLOR);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        // Login button
        btnLogin = buildLoginButton();

        card.add(topRow);
        card.add(Box.createVerticalStrut(16));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(28));
        card.add(fieldsPanel);
        card.add(Box.createVerticalStrut(8));
        card.add(lblError);
        card.add(Box.createVerticalStrut(20));
        card.add(btnLogin);

        outer.add(card);
        return outer;
    }

    private JPanel buildFieldsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(LEFT_ALIGNMENT);

        // Username
        panel.add(makeLabel("Tên đăng nhập"));
        panel.add(Box.createVerticalStrut(6));
        tfUsername = new JTextField();
        styleTextField(tfUsername, "Nhập tên đăng nhập...");
        panel.add(tfUsername);

        panel.add(Box.createVerticalStrut(16));

        // Password
        panel.add(makeLabel("Mật khẩu"));
        panel.add(Box.createVerticalStrut(6));
        pfPassword = new JPasswordField();
        styleTextField(pfPassword, "Nhập mật khẩu...");
        panel.add(pfPassword);

        panel.add(Box.createVerticalStrut(8));

        // Show/Hide password toggle
        chkShowPwd = new JCheckBox("Hiện mật khẩu");
        chkShowPwd.setFont(new Font("Inter", Font.PLAIN, 12));
        chkShowPwd.setForeground(TEXT_SECONDARY);
        chkShowPwd.setOpaque(false);
        chkShowPwd.addActionListener(e -> pfPassword.setEchoChar(
                chkShowPwd.isSelected() ? '\0' : '●'));
        panel.add(chkShowPwd);

        return panel;
    }

    private JButton buildLoginButton() {
        JButton btn = new JButton("ĐĂNG NHẬP") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isRollover() ? ACCENT_HOVER : ACCENT;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Short.MAX_VALUE, 46));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addActionListener(e -> attemptLogin());
        return btn;
    }

    // ── Login Logic (run off EDT using Virtual Threads) ────────────────
    private void attemptLogin() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());

        lblError.setText(" ");
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang xử lý...");

        // Java 21 Virtual Thread — keeps UI responsive
        Thread.ofVirtual().start(() -> {
            try {
                User user = userService.authenticate(username, password);
                SwingUtilities.invokeLater(() -> onLoginSuccess(user));
            } catch (AuthenticationException ex) {
                SwingUtilities.invokeLater(() -> onLoginFailure(ex.getMessage()));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> onLoginFailure("Lỗi hệ thống. Vui lòng thử lại."));
            }
        });
    }

    private void onLoginSuccess(User user) {
        JOptionPane.showMessageDialog(this,
                "Đăng nhập thành công!\nChào mừng " + user.getFullName(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        MainDashboardFrame dashboard = new MainDashboardFrame(user);
        dashboard.setVisible(true);
        this.dispose();
    }

    private void onLoginFailure(String message) {
        lblError.setText(message);
        btnLogin.setEnabled(true);
        btnLogin.setText("ĐĂNG NHẬP");
        // Shake animation
        animateShake(pfPassword);
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private void styleTextField(JTextComponent field, String placeholder) {
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(FIELD_BG);
        field.setCaretColor(ACCENT);
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Inter", Font.PLAIN, 13));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton makeIconButton(String icon) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Inter", Font.PLAIN, 14));
        btn.setForeground(TEXT_SECONDARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void animateShake(JComponent target) {
        int originalX = target.getX();
        Timer timer = new Timer(30, null);
        int[] offsets = {6, -6, 4, -4, 2, -2, 0};
        int[] step = {0};
        timer.addActionListener(e -> {
            if (step[0] < offsets.length) {
                target.setLocation(originalX + offsets[step[0]++], target.getY());
            } else {
                timer.stop();
                target.setLocation(originalX, target.getY());
            }
        });
        timer.start();
    }
}
