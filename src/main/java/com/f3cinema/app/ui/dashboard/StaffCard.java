package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.entity.User;
import com.f3cinema.app.entity.enums.UserRole;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class StaffCard extends JPanel {
    private final User staff;
    private final Consumer<User> onEdit;
    private final Consumer<User> onDelete;
    private boolean hovered = false;

    public StaffCard(User staff, Consumer<User> onEdit, Consumer<User> onDelete) {
        this.staff = staff;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        setLayout(new BorderLayout(12, 12));
        setPreferredSize(new Dimension(300, 180));
        setOpaque(false);
        initComponents();
        addHoverListeners();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, hovered ? 56 : 30));
        g2.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 8, ThemeConfig.RADIUS_CARD, ThemeConfig.RADIUS_CARD);

        g2.setColor(ThemeConfig.BG_CARD);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ThemeConfig.RADIUS_CARD, ThemeConfig.RADIUS_CARD);

        g2.setColor(ThemeConfig.BORDER_COLOR);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ThemeConfig.RADIUS_CARD, ThemeConfig.RADIUS_CARD);

        g2.dispose();
        super.paintComponent(g);
    }

    private void initComponents() {
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContactInfo(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);

        JLabel avatar = createAvatarLabel(staff.getFullName());
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel name = new JLabel(staff.getFullName());
        name.setFont(ThemeConfig.FONT_H3);
        name.setForeground(ThemeConfig.TEXT_PRIMARY);

        JLabel role = createRoleBadge(staff.getRole());
        textPanel.add(name);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(role);

        panel.add(avatar, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createAvatarLabel(String fullName) {
        String initial = (fullName == null || fullName.isBlank()) ? "U" : fullName.substring(0, 1).toUpperCase();
        JLabel avatar = new JLabel(initial) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, ThemeConfig.ACCENT_COLOR, getWidth(), getHeight(), ThemeConfig.ACCENT_HOVER));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(ThemeConfig.FONT_H2);
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(48, 48));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        return avatar;
    }

    private JLabel createRoleBadge(UserRole role) {
        Color badgeColor = role == UserRole.ADMIN ? ThemeConfig.ACCENT_COLOR : ThemeConfig.TEXT_SECONDARY;
        JLabel badge = new JLabel(role != null ? role.getLabel() : "");
        badge.setFont(ThemeConfig.FONT_SMALL);
        badge.setForeground(badgeColor);
        badge.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #233049;");
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        return badge;
    }

    private JPanel buildContactInfo() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.add(createInfoRow("Username", staff.getUsername()));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createInfoRow("ID", String.valueOf(staff.getId())));
        return panel;
    }

    private JPanel createInfoRow(String label, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label + ": " + (text == null ? "" : text));
        lbl.setFont(ThemeConfig.FONT_SMALL);
        lbl.setForeground(ThemeConfig.TEXT_SECONDARY);
        row.add(lbl);
        return row;
    }

    private JPanel buildActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);

        JButton btnEdit = new JButton("Edit");
        btnEdit.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #334155");
        btnEdit.setPreferredSize(new Dimension(64, 32));
        btnEdit.addActionListener(e -> onEdit.accept(staff));

        JButton btnDelete = new JButton("Delete");
        btnDelete.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #334155");
        btnDelete.setForeground(ThemeConfig.TEXT_DANGER);
        btnDelete.setPreferredSize(new Dimension(72, 32));
        btnDelete.addActionListener(e -> onDelete.accept(staff));

        panel.add(btnEdit);
        panel.add(btnDelete);
        return panel;
    }

    private void addHoverListeners() {
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
        });
    }
}
