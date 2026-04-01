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
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * LoginFrame — "Modern Midnight Pro Max" UI for F3 Cinema.
 */
public class LoginFrame extends JFrame {

    // ── Design Tokens ──────────────────────────────────────────────────
    private static final Color BG_DARK        = new Color(0x0F172A);  // Slate 900
    private static final Color CARD_BG        = new Color(15, 23, 42, 220); // Deep Slate 900
    private static final Color ACCENT         = new Color(0x6366F1);  // Indigo 500
    private static final Color ACCENT_HOVER   = new Color(0x818CF8);  // Indigo 400
    private static final Color TEXT_PRIMARY   = new Color(0xF8FAFC);  // Slate 50
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);  // Slate 400
    private static final Color ERROR_COLOR    = new Color(0xF43F5E);  // Rose 500
    private static final Color FIELD_BG       = new Color(30, 41, 59, 180);  // Slate 800 Translucent
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
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setUndecorated(true);

        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new BorderLayout());

        // ── Window Controls Bar (North) ───────────────────────────────
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        topBar.setOpaque(false);

        WindowControlButton btnMinimize = new WindowControlButton(new Color(0xFBBF24)); // Amber 400
        btnMinimize.addActionListener(e -> setState(JFrame.ICONIFIED));

        WindowControlButton btnMaximize = new WindowControlButton(new Color(0x34D399)); // Emerald 400
        btnMaximize.addActionListener(e -> {
            if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.NORMAL);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });

        WindowControlButton btnClose = new WindowControlButton(ERROR_COLOR); // Rose 500
        btnClose.addActionListener(e -> System.exit(0));

        topBar.add(btnMinimize);
        topBar.add(btnMaximize);
        topBar.add(btnClose);

        root.add(topBar, BorderLayout.NORTH);

        // ── Center Card Wrapper ───────────────────────────────────────
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(buildCenteredCard());

        root.add(centerWrapper, BorderLayout.CENTER);

        setContentPane(root);

        pfPassword.addActionListener(e -> attemptLogin());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                    setShape(null);
                } else {
                    setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
                }
            }
        });

        MouseAdapter drag = new MouseAdapter() {
            private Point start;
            public void mousePressed(MouseEvent e)  { start = e.getPoint(); }
            public void mouseDragged(MouseEvent e)  {
                if (getExtendedState() == JFrame.MAXIMIZED_BOTH) return;
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - start.x, loc.y + e.getY() - start.y);
            }
        };
        root.addMouseListener(drag);
        root.addMouseMotionListener(drag);
    }

    private static class WindowControlButton extends JButton {
        private final Color baseColor;
        private boolean hovered = false;

        public WindowControlButton(Color color) {
            this.baseColor = color;
            setPreferredSize(new Dimension(14, 14));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double center = getWidth() / 2.0;
            double radius = 6.0;
            g2.setColor(hovered ? baseColor : baseColor.darker());
            g2.fill(new Ellipse2D.Double(center - radius, center - radius, radius * 2, radius * 2));
            g2.setStroke(new BasicStroke(0.5f));
            g2.setColor(new Color(255, 255, 255, 40));
            g2.draw(new Ellipse2D.Double(center - radius, center - radius, radius * 2, radius * 2));
            g2.dispose();
        }
    }

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            if (backgroundImage != null) {
                double scaleX = (double) getWidth() / backgroundImage.getWidth();
                double scaleY = (double) getHeight() / backgroundImage.getHeight();
                double scale = Math.max(scaleX, scaleY);
                int w = (int) (backgroundImage.getWidth() * scale);
                int h = (int) (backgroundImage.getHeight() * scale);
                int x = (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;
                g2.drawImage(backgroundImage, x, y, w, h, null);
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42, 140), 0, getHeight(), new Color(15, 23, 42, 200));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g2.setColor(BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
        }
    }

    private JPanel buildCenteredCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC * 2, ARC * 2);
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
        card.setBorder(new EmptyBorder(30, 50, 40, 50));
        card.setPreferredSize(new Dimension(460, 580));

        JLabel brandLogo = new JLabel("F3 CINEMA") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = fm.getAscent() + (getHeight() - fm.getHeight()) / 2;
                g2.setColor(GLOW_COLOR);
                for(int i = 1; i <= 3; i++) {
                    g2.drawString(getText(), textX + i, textY + i);
                    g2.drawString(getText(), textX - i, textY - i);
                }
                g2.setColor(TEXT_PRIMARY); 
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }
        };
        brandLogo.setFont(new Font("Inter", Font.BOLD, 42));
        brandLogo.setAlignmentX(CENTER_ALIGNMENT);
        brandLogo.setPreferredSize(new Dimension(400, 60));

        JLabel lblTitle = new JLabel("HỆ THỐNG QUẢN LÝ");
        lblTitle.setFont(new Font("Inter", Font.PLAIN, 18));
        lblTitle.setForeground(ACCENT);
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Vui lòng đăng nhập để tiếp tục");
        lblSub.setFont(new Font("Inter", Font.PLAIN, 14));
        lblSub.setForeground(TEXT_SECONDARY);
        lblSub.setAlignmentX(CENTER_ALIGNMENT);

        JPanel fieldsPanel = buildFieldsPanel();
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Inter", Font.PLAIN, 13));
        lblError.setForeground(ERROR_COLOR);
        lblError.setAlignmentX(CENTER_ALIGNMENT);
        btnLogin = buildLoginButton();

        card.add(brandLogo);
        card.add(Box.createVerticalStrut(4));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(40));
        card.add(fieldsPanel);
        card.add(Box.createVerticalStrut(10));
        card.add(lblError);
        card.add(Box.createVerticalStrut(20));
        card.add(btnLogin);

        return card;
    }

    private JPanel buildFieldsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        // Important: Panel itself is center-aligned in card
        panel.setAlignmentX(CENTER_ALIGNMENT);

        // ── Username Group ──
        panel.add(makeLabel("Tên đăng nhập"));
        panel.add(Box.createVerticalStrut(8));
        tfUsername = new JTextField();
        styleTextField(tfUsername, "Nhập username của bạn...");
        panel.add(tfUsername);

        panel.add(Box.createVerticalStrut(22));

        // ── Password Group ──
        panel.add(makeLabel("Mật khẩu"));
        panel.add(Box.createVerticalStrut(8));
        pfPassword = new JPasswordField();
        styleTextField(pfPassword, "Nhập mật khẩu...");
        panel.add(pfPassword);

        panel.add(Box.createVerticalStrut(12));

        chkShowPwd = new JCheckBox("Hiện mật khẩu");
        chkShowPwd.setFont(new Font("Inter", Font.PLAIN, 13));
        chkShowPwd.setForeground(TEXT_SECONDARY);
        chkShowPwd.setOpaque(false);
        chkShowPwd.setFocusPainted(false);
        chkShowPwd.setIconTextGap(8);
        chkShowPwd.addActionListener(e -> pfPassword.setEchoChar(chkShowPwd.isSelected() ? '\0' : '●'));
        
        JPanel chkPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        chkPanel.setOpaque(false);
        chkPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        // Align checkbox to right of input field bound
        chkPanel.setAlignmentX(LEFT_ALIGNMENT);
        chkPanel.add(chkShowPwd);
        panel.add(chkPanel);

        return panel;
    }

    private JButton buildLoginButton() {
        JButton btn = new JButton("ĐĂNG NHẬP") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRoundRect(2, 6, getWidth() - 4, getHeight() - 4, ARC, ARC);
                Color fill = getModel().isRollover() ? ACCENT_HOVER : ACCENT;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() - 4, ARC, ARC);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
        btn.setAlignmentX(CENTER_ALIGNMENT);
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
        com.f3cinema.app.util.SessionManager.setCurrentUser(user);
        if (user.getRole() == com.f3cinema.app.entity.enums.UserRole.ADMIN) {
            com.f3cinema.app.ui.admin.AdminMainFrame adminFrame = new com.f3cinema.app.ui.admin.AdminMainFrame(user);
            adminFrame.setVisible(true);
        } else {
            com.f3cinema.app.ui.staff.StaffMainFrame staffFrame = new com.f3cinema.app.ui.staff.StaffMainFrame(user);
            staffFrame.setVisible(true);
        }
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
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, 52));
        // SET TO LEFT_ALIGNMENT: so it shares the same left edge as the Label
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 16; margin: 8,16,8,16; borderWidth: 0; focusWidth: 2;");
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.OUTLINE, new Color[]{ACCENT}); 
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Inter", Font.BOLD, 14));
        lbl.setForeground(new Color(226, 232, 240)); 
        // SET TO LEFT_ALIGNMENT: lines up perfectly with the Input field left edge
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Short.MAX_VALUE, 24)); 
        return lbl;
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
