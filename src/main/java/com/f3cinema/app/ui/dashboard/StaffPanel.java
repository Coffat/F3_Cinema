package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.entity.User;
import com.f3cinema.app.entity.enums.UserRole;
import com.f3cinema.app.service.UserService;
import com.f3cinema.app.ui.admin.dialog.StaffDialog;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StaffPanel extends BaseDashboardModule {
    private final UserService userService = new UserService();

    private JTextField txtSearch;
    private JButton btnAdd;
    private JComboBox<Object> cmbRole;
    private JComboBox<String> cmbSort;
    private JPanel cardContainer;
    private List<User> currentStaff = new ArrayList<>();

    public StaffPanel() {
        super("Nhân viên", "Home > Staffs");
        initUI();
        loadDataAsync("");
    }

    private void initUI() {
        contentBody.setBackground(ThemeConfig.BG_MAIN);

        JPanel toolbar = buildToolbar();
        JScrollPane scrollPane = buildCardView();

        contentBody.add(toolbar, BorderLayout.NORTH);
        contentBody.add(scrollPane, BorderLayout.CENTER);

        // Ctrl+F focus search
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"), "focusSearch");
        getActionMap().put("focusSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtSearch.requestFocusInWindow();
            }
        });
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 24, 16, 24));

        JPanel controlBar = new JPanel(new BorderLayout(16, 0));
        controlBar.setBackground(ThemeConfig.BG_CARD);
        controlBar.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        controlBar.putClientProperty(FlatClientProperties.STYLE, "arc: 20");

        txtSearch = new JTextField(28);
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("icons/search.svg", 16, 16));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tim theo username / ho ten (Ctrl+F)...");
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #0F172A; foreground: #F8FAFC; caretColor: #6366F1;");
        txtSearch.setFont(ThemeConfig.FONT_BODY);
        txtSearch.setPreferredSize(new Dimension(300, 40));

        Timer searchTimer = new Timer(300, e -> applyClientFilters());
        searchTimer.setRepeats(false);
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchTimer.restart();
            }
        });

        cmbRole = new JComboBox<>();
        cmbRole.addItem("All Roles");
        cmbRole.addItem(UserRole.ADMIN);
        cmbRole.addItem(UserRole.STAFF);
        cmbRole.setPreferredSize(new Dimension(150, 40));
        cmbRole.setFont(ThemeConfig.FONT_BODY);
        cmbRole.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #0F172A; foreground: #F8FAFC;");
        cmbRole.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof UserRole role) setText(role.getLabel());
                return this;
            }
        });
        cmbRole.addActionListener(e -> applyClientFilters());

        cmbSort = new JComboBox<>(new String[]{"Sort: Name", "Sort: Role", "Sort: ID"});
        cmbSort.setPreferredSize(new Dimension(140, 40));
        cmbSort.setFont(ThemeConfig.FONT_BODY);
        cmbSort.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #0F172A; foreground: #F8FAFC;");
        cmbSort.addActionListener(e -> applyClientFilters());

        btnAdd = new JButton("Them nhan vien");
        btnAdd.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
        btnAdd.setBackground(ThemeConfig.ACCENT_COLOR);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.putClientProperty(FlatClientProperties.STYLE, "arc: 15; margin: 0, 20, 0, 20; borderWidth: 0;");
        btnAdd.setPreferredSize(new Dimension(170, 40));
        btnAdd.addActionListener(e -> openAddDialog());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(txtSearch);
        left.add(cmbRole);
        left.add(cmbSort);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(btnAdd);

        controlBar.add(left, BorderLayout.WEST);
        controlBar.add(right, BorderLayout.EAST);
        toolbar.add(controlBar, BorderLayout.CENTER);
        return toolbar;
    }

    private JScrollPane buildCardView() {
        cardContainer = new JPanel(new com.f3cinema.app.util.WrapLayout(FlowLayout.LEFT, 24, 24));
        cardContainer.setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(true);
        inner.setBackground(ThemeConfig.BG_MAIN);
        inner.setBorder(BorderFactory.createEmptyBorder(8, 24, 24, 24));
        inner.add(cardContainer, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(ThemeConfig.BG_MAIN);
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "trackArc: 999; thumbArc: 999;");

        return scroll;
    }

    private void loadDataAsync(String keyword) {
        setBusy(true);
        Thread.ofVirtual().start(() -> {
            try {
                List<User> staff = userService.getAllStaff(keyword);
                SwingUtilities.invokeLater(() -> {
                    currentStaff = staff;
                    applyClientFilters();
                    setBusy(false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    setBusy(false);
                    AppMessageDialogs.showError(this, "Lỗi", ex.getMessage());
                });
            }
        });
    }

    private void applyClientFilters() {
        if (cardContainer == null) return;
        String keyword = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        Object selectedRole = cmbRole != null ? cmbRole.getSelectedItem() : "All Roles";
        String sort = cmbSort != null ? (String) cmbSort.getSelectedItem() : "Sort: Name";

        List<User> filtered = currentStaff.stream()
                .filter(u -> keyword.isEmpty()
                        || (u.getUsername() != null && u.getUsername().toLowerCase().contains(keyword))
                        || (u.getFullName() != null && u.getFullName().toLowerCase().contains(keyword)))
                .filter(u -> !(selectedRole instanceof UserRole role) || u.getRole() == role)
                .sorted(getComparator(sort))
                .toList();

        cardContainer.removeAll();
        for (User staff : filtered) {
            StaffCard card = new StaffCard(staff, this::openEditDialog, this::handleDelete);
            cardContainer.add(card);
        }
        cardContainer.revalidate();
        cardContainer.repaint();
    }

    private void openAddDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        StaffDialog dlg = new StaffDialog(owner, false);
        dlg.setOnSave(() -> {
            dlg.setError(null);
            setBusy(true);
            Thread.ofVirtual().start(() -> {
                try {
                    userService.createStaff(dlg.getUsername(), dlg.getFullName(), dlg.getPassword());
                    SwingUtilities.invokeLater(() -> {
                        dlg.markSavedAndClose();
                        loadDataAsync(txtSearch.getText());
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        setBusy(false);
                        dlg.setError(ex.getMessage());
                    });
                }
            });
        });
        dlg.setVisible(true);
    }

    private Comparator<User> getComparator(String sort) {
        if ("Sort: Role".equals(sort)) {
            return Comparator.comparing(u -> u.getRole() != null ? u.getRole().name() : "");
        }
        if ("Sort: ID".equals(sort)) {
            return Comparator.comparing(u -> u.getId() != null ? u.getId() : Long.MAX_VALUE);
        }
        return Comparator.comparing(u -> u.getFullName() != null ? u.getFullName().toLowerCase() : "");
    }

    private void openEditDialog(User selected) {
        if (selected == null || selected.getId() == null) return;
        Window owner = SwingUtilities.getWindowAncestor(this);
        StaffDialog dlg = new StaffDialog(owner, true);
        dlg.setInitialValues(selected.getUsername(), selected.getFullName());
        dlg.setOnSave(() -> {
            dlg.setError(null);
            setBusy(true);
            Thread.ofVirtual().start(() -> {
                try {
                    userService.updateStaff(selected.getId(), dlg.getUsername(), dlg.getFullName(), dlg.getPassword());
                    SwingUtilities.invokeLater(() -> {
                        dlg.markSavedAndClose();
                        loadDataAsync(txtSearch.getText());
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        setBusy(false);
                        dlg.setError(ex.getMessage());
                    });
                }
            });
        });
        dlg.setVisible(true);
    }

    private void handleDelete(User selected) {
        if (selected == null || selected.getId() == null) return;
        String username = String.valueOf(selected.getUsername());
        if (!AppMessageDialogs.confirmYesNo(
                this,
                "Xác nhận xóa",
                "Xóa nhân viên `" + username + "`?\nHành động này không thể hoàn tác."
        )) return;

        setBusy(true);
        Thread.ofVirtual().start(() -> {
            try {
                userService.deleteStaff(selected.getId());
                SwingUtilities.invokeLater(() -> loadDataAsync(txtSearch.getText()));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    setBusy(false);
                    AppMessageDialogs.showError(this, "Lỗi", ex.getMessage());
                });
            }
        });
    }

    private void setBusy(boolean busy) {
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        btnAdd.setEnabled(!busy);
        if (cmbRole != null) cmbRole.setEnabled(!busy);
        if (cmbSort != null) cmbSort.setEnabled(!busy);
    }
}
