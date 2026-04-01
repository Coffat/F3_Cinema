package com.f3cinema.app.ui.staff;

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

import static com.f3cinema.app.ui.staff.StaffMainFrame.*;

/**
 * Modern Top Navbar for Staff Suite.
 */
public class StaffNavbarPanel extends JPanel {

    private final User staffUser;
    private final JPanel navListPanel;
    private final List<NavItem> navItems = new ArrayList<>();
    private NavItem activeItem;
    private Consumer<String> onMenuSelected;

    private static final Color BG_NAVBAR = Color.decode("#1E293B"); // Slate 800
    private static final Color INDIGO_500 = Color.decode("#6366F1");

    public StaffNavbarPanel(User user) {
        this.staffUser = user;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 80));
        setBackground(BG_NAVBAR);
        setBorder(new EmptyBorder(0, 32, 0, 32));

        // 1. Left: Logo
        JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        leftContainer.setOpaque(false);
        JLabel logo = new JLabel(new FlatSVGIcon("icons/f3_logo.svg", 56, 56));
        logo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        leftContainer.add(logo);

        // 2. Center: Staff Navigation
        navListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 16));
        navListPanel.setOpaque(false);
        initializeNavItems();

        // 3. Right: Profile & Actions
        JPanel rightContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 18));
        rightContainer.setOpaque(false);

        JButton btnBell = new JButton(new FlatSVGIcon("icons/bell.svg", 20, 20));
        btnBell.putClientProperty(FlatClientProperties.STYLE, "arc: 99; background: #2D3748; foreground: #94A3B8; borderWidth: 0;");
        btnBell.setPreferredSize(new Dimension(42, 42));
        btnBell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnAvatar = createAvatarButton();
        JPopupMenu userMenu = createUserMenu();
        btnAvatar.addActionListener(e -> userMenu.show(btnAvatar, btnAvatar.getWidth() - userMenu.getPreferredSize().width, btnAvatar.getHeight() + 8));

        rightContainer.add(btnBell);
        rightContainer.add(btnAvatar);

        add(leftContainer, BorderLayout.WEST);
        add(navListPanel, BorderLayout.CENTER);
        add(rightContainer, BorderLayout.EAST);
    }

    private void initializeNavItems() {
        addNavItem("Bán vé", "icons/ticket.svg", CARD_TICKETING);
        addNavItem("Bắp nước", "icons/snacks.svg", CARD_SNACKS);
        addNavItem("Suất chiếu", "icons/search.svg", CARD_SEARCH);
        addNavItem("Khách hàng", "icons/users.svg", CARD_CUSTOMERS);
        addNavItem("Lịch sử", "icons/history.svg", CARD_TRANSACTIONS);

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

    public void setOnMenuSelected(Consumer<String> callback) { this.onMenuSelected = callback; }

    private JButton createAvatarButton() {
        JButton btnAvatar = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, INDIGO_500, getWidth(), getHeight(), Color.WHITE));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.decode("#1E293B"));
                g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                String initial = staffUser.getFullName().substring(0, 1).toUpperCase();
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("-apple-system", Font.BOLD, 16));
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
        // Không set STYLE arc trên JPopupMenu — FlatLaf không áp dụng, có thể gây UnknownStyleException
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(Color.decode("#1E293B"));
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel lblName = new JLabel(staffUser.getFullName());
        lblName.setFont(new Font("-apple-system", Font.BOLD, 14));
        lblName.setForeground(Color.WHITE);
        JLabel lblRole = new JLabel(staffUser.getRole().getLabel());
        lblRole.setFont(new Font("-apple-system", Font.PLAIN, 12));
        lblRole.setForeground(Color.decode("#94A3B8"));
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        infoPanel.add(lblName);
        infoPanel.add(lblRole);
        header.add(infoPanel, BorderLayout.CENTER);
        menu.add(header);
        menu.addSeparator();

        JMenuItem itemLogout = new JMenuItem("Đăng xuất", new FlatSVGIcon("icons/log-out.svg", 16, 16));
        itemLogout.setForeground(Color.decode("#F43F5E"));
        itemLogout.addActionListener(e -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            if (parent != null) {
                parent.dispose();
                new com.f3cinema.app.ui.LoginFrame().setVisible(true);
            }
        });
        menu.add(itemLogout);
        return menu;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(30, 41, 59, 230)); 
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setPaint(new GradientPaint(0, getHeight()-1, new Color(99, 102, 241, 0), getWidth()/2, getHeight()-1, new Color(99, 102, 241, 150), true));
        g2.fillRect(0, getHeight()-1, getWidth(), 1);
        g2.dispose();
    }

    private static class NavItem extends JPanel {
        private boolean active = false;
        private boolean hovered = false;
        private final FlatSVGIcon icon;
        private final JLabel lblText;
        public NavItem(String label, String iconPath, String id) {
            this.icon = new FlatSVGIcon(iconPath, 18, 18);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.decode("#6366F1")));
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(120, 42));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lblText = new JLabel(label, SwingConstants.CENTER);
            lblText.setFont(new Font("-apple-system", Font.BOLD, 13));
            lblText.setForeground(Color.decode("#94A3B8"));
            lblText.setIcon(icon);
            lblText.setHorizontalTextPosition(SwingConstants.RIGHT);
            lblText.setIconTextGap(8);
            add(lblText, BorderLayout.CENTER);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }
        public void setActive(boolean active) {
            this.active = active;
            lblText.setForeground(active ? Color.WHITE : Color.decode("#94A3B8"));
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> active ? Color.WHITE : Color.decode("#6366F1")));
            repaint();
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setPaint(new GradientPaint(0, 0, new Color(99, 102, 241, 100), 0, getHeight(), new Color(99, 102, 241, 40)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(99, 102, 241, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 14, 14);
            } else if (hovered) {
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
