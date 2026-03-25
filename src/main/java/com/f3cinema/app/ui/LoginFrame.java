package com.f3cinema.app.ui;

import com.f3cinema.app.entity.User;
import com.f3cinema.app.exception.AuthenticationException;
import com.f3cinema.app.service.UserService;
import com.formdev.flatlaf.FlatClientProperties;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * LoginFrame — "Modern Midnight Pro Max" UI for F3 Cinema.
 */
public class LoginFrame extends JFrame {

    // ── Design Tokens ──────────────────────────────────────────────────
    private static final Color BG_DARK        = new Color(0x0F172A);  // Slate 900
    private static final Color CARD_BG        = new Color(30, 41, 59, 180); // Translucent Slate 800
    private static final Color ACCENT         = new Color(0x6366F1);  // Indigo 500
    private static final Color ACCENT_HOVER   = new Color(0x818CF8);  // Indigo 400
    private static final Color TEXT_PRIMARY   = new Color(0xF8FAFC);  // Slate 50
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);  // Slate 400
    private static final Color ERROR_COLOR    = new Color(0xF43F5E);  // Rose 500
    private static final Color FIELD_BG       = new Color(15, 23, 42, 200);  // Slate 900 Translucent
    private static final Color GLOW_COLOR     = new Color(99, 102, 241, 100); // Glow Indigo
    private static final int   ARC            = 16;

    // ── Components & Assets ───────────────────────────────────────────
    private JTextField     tfUsername;
    private JPasswordField pfPassword;
    private JButton        btnLogin;
    private JLabel         lblError;
    private JCheckBox      chkShowPwd;
    private final UserService userService = new UserService();
    private BufferedImage backgroundImage;

    public LoginFrame() {
        loadBackground();
        initialize();
    }

    private void loadBackground() {
        try {
            InputStream is = getClass().getResourceAsStream("/assets/bg_login.png");
            if (is != null) {
                backgroundImage = ImageIO.read(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initialize() {
        setTitle("F3 Cinema — Đăng Nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new GridLayout(1, 2));

        root.add(buildBrandPanel());
        root.add(buildLoginCard());

        setContentPane(root);

        pfPassword.addActionListener(e -> attemptLogin());

        // Drag to move
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

    // Custom Background Panel to draw the cinematic image
    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (backgroundImage != null) {
                // Scale to fit or cover
                double scaleX = (double) getWidth() / backgroundImage.getWidth();
                double scaleY = (double) getHeight() / backgroundImage.getHeight();
                double scale = Math.max(scaleX, scaleY);
                int w = (int) (backgroundImage.getWidth() * scale);
                int h = (int) (backgroundImage.getHeight() * scale);
                int x = (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;
                g2.drawImage(backgroundImage, x, y, w, h, null);
                
                // Add a very subtle dark overlay to ensure readability
                g2.setColor(new Color(15, 23, 42, 100));
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g2.setColor(BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
        }
    }

    // ── Left Brand Panel ───────────────────────────────────────────────
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        // Highly detailed glowing icon text
        JLabel icon = new JLabel("🎬", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = fm.getAscent() + (getHeight() - fm.getHeight()) / 2;
                
                // Soft glowing shadow
                g2.setColor(GLOW_COLOR);
                g2.drawString(getText(), textX, textY + 2);
                g2.drawString(getText(), textX, textY - 2);
                g2.drawString(getText(), textX + 2, textY);
                g2.drawString(getText(), textX - 2, textY);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("F3 CINEMA") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = fm.getAscent() + (getHeight() - fm.getHeight()) / 2;
                
                // Glow effect
                g2.setColor(GLOW_COLOR);
                for(int i = 1; i <= 3; i++) {
                    g2.drawString(getText(), textX + i, textY + i);
                    g2.drawString(getText(), textX - i, textY - i);
                }
                g2.setColor(TEXT_PRIMARY); // Slate 50 white
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }
        };
        title.setFont(new Font("Inter", Font.BOLD, 48));
        title.setAlignmentX(CENTER_ALIGNMENT);
        title.setPreferredSize(new Dimension(300, 70));

        JLabel sub = new JLabel("Cinema Management System");
        sub.setFont(new Font("Inter", Font.PLAIN, 16));
        sub.setForeground(TEXT_SECONDARY);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        inner.add(icon);
        inner.add(Box.createVerticalStrut(16));
        inner.add(title);
        inner.add(Box.createVerticalStrut(4));
        inner.add(sub);

        panel.add(inner);
        return panel;
    }

    // ── Right Login Card ───────────────────────────────────────────────
    private JPanel buildLoginCard() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        // Advanced Glassmorphism Card
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Base Translucent Background
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC * 2, ARC * 2);
                
                // Outer glowing Indigo edge stroke
                g2.setStroke(new BasicStroke(1.5f));
                GradientPaint edgeGlow = new GradientPaint(0, 0, new Color(99, 102, 241, 150),
                                                           getWidth(), getHeight(), new Color(99, 102, 241, 30));
                g2.setPaint(edgeGlow);
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, ARC * 2, ARC * 2);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(40, 44, 40, 44));
        card.setPreferredSize(new Dimension(420, 500));

        // Top Row (Film frame outline logo & Close button)
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        
        JLabel logoSmall = new JLabel("🎞") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(getFont());
                g2.setColor(GLOW_COLOR);
                g2.drawString(getText(), 2, 18);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        logoSmall.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        logoSmall.setForeground(ACCENT);
        
        JButton btnClose = makeIconButton("✕");
        btnClose.addActionListener(e -> System.exit(0));
        
        topRow.add(logoSmall, BorderLayout.WEST);
        topRow.add(btnClose, BorderLayout.EAST);
        topRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));

        // Texts
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Chào mừng trở lại ✦");
        lblSub.setFont(new Font("Inter", Font.PLAIN, 14));
        lblSub.setForeground(TEXT_SECONDARY);
        lblSub.setAlignmentX(CENTER_ALIGNMENT);

        JPanel fieldsPanel = buildFieldsPanel();

        lblError = new JLabel(" ");
        lblError.setFont(new Font("Inter", Font.PLAIN, 13));
        lblError.setForeground(ERROR_COLOR);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        btnLogin = buildLoginButton();

        card.add(topRow);
        card.add(Box.createVerticalStrut(20));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(8));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(32));
        card.add(fieldsPanel);
        card.add(Box.createVerticalStrut(10));
        card.add(lblError);
        card.add(Box.createVerticalStrut(24));
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
        panel.add(makeLabel(" Tên đăng nhập"));
        panel.add(Box.createVerticalStrut(8));
        tfUsername = new JTextField();
        styleTextField(tfUsername, "Nhập tên đăng nhập...");
        panel.add(tfUsername);

        panel.add(Box.createVerticalStrut(20));

        // Password
        panel.add(makeLabel(" Mật khẩu"));
        panel.add(Box.createVerticalStrut(8));
        pfPassword = new JPasswordField();
        styleTextField(pfPassword, "Nhập mật khẩu...");
        panel.add(pfPassword);

        panel.add(Box.createVerticalStrut(10));

        // Show/Hide password toggle
        chkShowPwd = new JCheckBox("Hiện mật khẩu");
        chkShowPwd.setFont(new Font("Inter", Font.PLAIN, 13));
        chkShowPwd.setForeground(TEXT_SECONDARY);
        chkShowPwd.setOpaque(false);
        chkShowPwd.setFocusPainted(false);
        chkShowPwd.setIconTextGap(8);
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
                
                // Soft dimensional drop shadow
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, ARC, ARC);
                
                Color fill = getModel().isRollover() ? ACCENT_HOVER : ACCENT;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 2, ARC, ARC);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Short.MAX_VALUE, 48));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addActionListener(e -> attemptLogin());
        return btn;
    }

    private void attemptLogin() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());

        lblError.setText(" ");
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang xử lý...");

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
        MainDashboardFrame dashboard = new MainDashboardFrame(user);
        dashboard.setVisible(true);
        this.dispose();
    }

    private void onLoginFailure(String message) {
        lblError.setText(message);
        btnLogin.setEnabled(true);
        btnLogin.setText("ĐĂNG NHẬP");
        animateShake(pfPassword);
    }

    private void styleTextField(JTextComponent field, String placeholder) {
        field.setFont(new Font("Inter", Font.PLAIN, 15));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(FIELD_BG);
        field.setCaretColor(ACCENT);
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 46));
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        
        // Minimalist borderless dark pane with refined glowing indigo outline on focus
        field.putClientProperty(FlatClientProperties.OUTLINE, ACCENT);
        
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(1, 1, 1, 1),
                new EmptyBorder(8, 14, 8, 14)));
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Inter", Font.PLAIN, 14));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton makeIconButton(String icon) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Inter", Font.BOLD, 16));
        btn.setForeground(new Color(148, 163, 184)); // Slate 400
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Add hover glow effect for the X button
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(ACCENT_HOVER); }
            public void mouseExited(MouseEvent e)  { btn.setForeground(new Color(148, 163, 184)); }
        });
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
