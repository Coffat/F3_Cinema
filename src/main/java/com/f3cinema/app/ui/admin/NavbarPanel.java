package com.f3cinema.app.ui.admin;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.entity.User;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern Horizontal Top Navigation Bar for F3 Cinema Admin Suite.
 */
public class NavbarPanel extends JPanel {

    private final User adminUser;
    private final JPanel navListPanel;
    private final List<NavItem> navItems = new ArrayList<>();
    private NavItem activeItem;
    private Consumer<String> onMenuSelected;

    // Slate Colors
    private static final Color BG_NAVBAR = ThemeConfig.BG_CARD;
    private static final Color INDIGO_500 = ThemeConfig.ACCENT_COLOR;

    public NavbarPanel(User user) {
        this.adminUser = user;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 80));
        setBackground(BG_NAVBAR);
        setBorder(new EmptyBorder(0, 32, 0, 32));

        // 1. Left: New Premium SVG Logo
        JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        leftContainer.setOpaque(false);
        
        JLabel logo = new JLabel(new FlatSVGIcon("icons/f3_logo.svg", 56, 56));
        logo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        leftContainer.add(logo);

        // 2. Center: Navigation Items (Modern Pills)
        navListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 16));
        navListPanel.setOpaque(false);
        initializeNavItems();

        // 3. Right: Notifications & User Menu (Pro Max)
        JPanel rightContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 18));
        rightContainer.setOpaque(false);

        // Notification Bell
        JButton btnBell = new JButton(new FlatSVGIcon("icons/bell.svg", 20, 20));
        btnBell.putClientProperty(FlatClientProperties.STYLE, "arc: 99; background: #2D3748; foreground: #94A3B8; borderWidth: 0;");
        btnBell.setPreferredSize(new Dimension(42, 42));
        btnBell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // User Avatar with Popup Menu
        JButton btnAvatar = createAvatarButton();
        JPopupMenu userMenu = createUserMenu();

        btnAvatar.addActionListener(e -> userMenu.show(btnAvatar, btnAvatar.getWidth() - userMenu.getPreferredSize().width, btnAvatar.getHeight() + 8));

        rightContainer.add(btnBell);
        rightContainer.add(btnAvatar);

        add(leftContainer, BorderLayout.WEST);
        add(navListPanel, BorderLayout.CENTER);
        add(rightContainer, BorderLayout.EAST);
    }

    private JButton createAvatarButton() {
        JButton btnAvatar = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Outer Glow Circle
                g2.setPaint(new GradientPaint(0, 0, INDIGO_500, getWidth(), getHeight(), Color.WHITE));
                g2.fillOval(0, 0, getWidth(), getHeight());
                
                // Inner Image/Initial Area
                g2.setColor(Color.decode("#1E293B"));
                g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                
                // Draw Initials
                String initial = adminUser.getFullName().substring(0, 1).toUpperCase();
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Inter", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (getWidth()-fm.stringWidth(initial))/2, fm.getAscent() + (getHeight()-fm.getHeight())/2 + 2);
                
                g2.dispose();
            }
        };
        btnAvatar.setPreferredSize(new Dimension(44, 44));
        btnAvatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAvatar.setBorder(null);
        btnAvatar.setOpaque(false);
        return btnAvatar;
    }

    private JPopupMenu createUserMenu() {
        JPopupMenu menu = new JPopupMenu();
        
        // --- Header Section ---
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(Color.decode("#1E293B"));
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        
        JLabel lblName = new JLabel(adminUser.getFullName());
        lblName.setFont(new Font("Inter", Font.BOLD, 14));
        lblName.setForeground(Color.WHITE);
        
        JLabel lblRole = new JLabel(adminUser.getRole().getLabel());
        lblRole.setFont(new Font("Inter", Font.PLAIN, 12));
        lblRole.setForeground(Color.decode("#94A3B8"));
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        infoPanel.add(lblName);
        infoPanel.add(lblRole);
        
        header.add(infoPanel, BorderLayout.CENTER);
        
        menu.add(header);
        menu.addSeparator();

        // --- Items ---
        JMenuItem itemProfile = new JMenuItem("Thông tin tài khoản", new FlatSVGIcon("icons/user.svg", 16, 16));
        JMenuItem itemPassword = new JMenuItem("Đổi mật khẩu", new FlatSVGIcon("icons/grid.svg", 16, 16));
        JMenuItem itemLogout = new JMenuItem("Đăng xuất", new FlatSVGIcon("icons/log-out.svg", 16, 16));
        
        itemLogout.setForeground(Color.decode("#F43F5E"));
        itemLogout.addActionListener(e -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            if (parent != null) {
                parent.dispose();
                new com.f3cinema.app.ui.LoginFrame().setVisible(true);
            }
        });

        menu.add(itemProfile);
        menu.add(itemPassword);
        menu.addSeparator();
        menu.add(itemLogout);

        return menu;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Glassmorphism background with subtle top line
        g2.setColor(new Color(30, 41, 59, 230)); // Slate 800 Translucent
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Bottom Accent Border Glow
        g2.setPaint(new GradientPaint(0, getHeight()-1, new Color(99, 102, 241, 0), 
                    getWidth()/2, getHeight()-1, new Color(99, 102, 241, 150), true));
        g2.fillRect(0, getHeight()-1, getWidth(), 1);
        
        g2.dispose();
    }

    private void initializeNavItems() {
        addNavItem("Dashboard", "icons/home.svg", "DASHBOARD");
        addNavItem("Phim", "icons/video.svg", "MOVIES");
        addNavItem("Phòng", "icons/grid.svg", "ROOMS");
        addNavItem("Lịch chiếu", "icons/calendar.svg", "SHOWTIMES");
        addNavItem("Nhân viên", "icons/users.svg", "STAFF");
        addNavItem("Kho", "icons/box.svg", "WAREHOUSE");
        addNavItem("Khuyến mãi", "icons/gift.svg", "PROMOTION");
        addNavItem("Thống kê", "icons/pie-chart.svg", "STATISTICS");

        if (!navItems.isEmpty()) setActiveItem(navItems.get(0));
    }

    private void addNavItem(String label, String iconPath, String id) {
        NavItem item = new NavItem(label, iconPath, id);
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActiveItem(item);
                if (onMenuSelected != null) onMenuSelected.accept(id);
            }
        });
        navItems.add(item);
        navListPanel.add(item);
    }

    private void setActiveItem(NavItem item) {
        if (activeItem != null) activeItem.setActive(false);
        activeItem = item;
        activeItem.setActive(true);
    }

    public void setOnMenuSelected(Consumer<String> callback) {
        this.onMenuSelected = callback;
    }

    private static class NavItem extends JPanel {
        private boolean active = false;
        private boolean hovered = false;
        private final FlatSVGIcon icon;
        private final JLabel lblText;
        private static final Color TEXT_ACTIVE = ThemeConfig.TEXT_PRIMARY;
        private static final Color TEXT_DEFAULT = ThemeConfig.TEXT_SECONDARY;
        private static final Color ICON_ACTIVE = ThemeConfig.ACCENT_COLOR;
        private static final Color ICON_DEFAULT = ThemeConfig.TEXT_SECONDARY;

        public NavItem(String label, String iconPath, String id) {
            this.icon = new FlatSVGIcon(iconPath, 18, 18);
            updateIconColor(false);

            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(120, 42));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            lblText = new JLabel(label, SwingConstants.CENTER);
            lblText.setFont(new Font("Inter", Font.BOLD, 13));
            lblText.setForeground(TEXT_DEFAULT);
            lblText.setIcon(icon);
            lblText.setHorizontalTextPosition(SwingConstants.RIGHT);
            lblText.setIconTextGap(8);

            add(lblText, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        private void updateIconColor(boolean isActive) {
            Color iconColor = isActive ? ICON_ACTIVE : ICON_DEFAULT;
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> iconColor));
        }

        public void setActive(boolean active) {
            this.active = active;
            lblText.setForeground(active ? TEXT_ACTIVE : TEXT_DEFAULT);
            updateIconColor(active);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                // Pill Background
                g2.setPaint(new GradientPaint(0, 0, new Color(99, 102, 241, 100), 
                             0, getHeight(), new Color(99, 102, 241, 40)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Border Glow
                g2.setColor(new Color(99, 102, 241, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 15, 15);
            } else if (hovered) {
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
