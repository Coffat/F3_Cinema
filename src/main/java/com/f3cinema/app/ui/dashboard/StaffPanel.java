package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.entity.User;
import com.f3cinema.app.service.UserService;
import com.f3cinema.app.ui.admin.dialog.StaffDialog;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class StaffPanel extends BaseDashboardModule {
    private static final Color BG_MAIN = Color.decode("#0F172A");
    private static final Color BG_SURFACE = Color.decode("#1E293B");
    private static final Color ACCENT = Color.decode("#6366F1");
    private static final Color TEXT_PRIMARY = Color.decode("#F8FAFC");
    private static final Color TEXT_SECONDARY = Color.decode("#94A3B8");

    private final UserService userService = new UserService();

    private JTextField txtSearch;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JTable table;
    private DefaultTableModel tableModel;

    public StaffPanel() {
        super("Nhân viên", "Home > Staffs");
        initUI();
        loadDataAsync("");
    }

    private void initUI() {
        contentBody.setBackground(BG_MAIN);

        JPanel toolbar = buildToolbar();
        JScrollPane scrollPane = buildTableView();

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
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 20, 22, 20));

        txtSearch = new JTextField(28);
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("icons/search.svg", 16, 16));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo username / họ tên (Ctrl+F)...");
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B; foreground: #F8FAFC; caretColor: #6366F1;");
        txtSearch.setFont(new Font("Inter", Font.PLAIN, 15));
        txtSearch.setPreferredSize(new Dimension(340, 40));

        Timer searchTimer = new Timer(300, e -> loadDataAsync(txtSearch.getText()));
        searchTimer.setRepeats(false);
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchTimer.restart();
            }
        });

        btnAdd = new JButton("+ Thêm nhân viên");
        btnAdd.setFont(new Font("Inter", Font.BOLD, 14));
        btnAdd.setBackground(ACCENT);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.putClientProperty(FlatClientProperties.STYLE, "arc: 16; margin: 0, 20, 0, 20; borderWidth: 0;");
        btnAdd.setPreferredSize(new Dimension(170, 40));
        btnAdd.addActionListener(e -> openAddDialog());

        btnEdit = new JButton("Sửa");
        btnEdit.setFont(new Font("Inter", Font.BOLD, 14));
        btnEdit.setForeground(TEXT_PRIMARY);
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEdit.putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #0F172A; foreground: #F8FAFC; borderWidth: 1; borderColor: #334155; margin: 0, 16, 0, 16;");
        btnEdit.setPreferredSize(new Dimension(90, 40));
        btnEdit.addActionListener(e -> openEditDialog());

        btnDelete = new JButton("Xóa");
        btnDelete.setFont(new Font("Inter", Font.BOLD, 14));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDelete.putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #F43F5E; borderWidth: 0; margin: 0, 16, 0, 16;");
        btnDelete.setPreferredSize(new Dimension(90, 40));
        btnDelete.addActionListener(e -> handleDelete());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(txtSearch);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(btnEdit);
        right.add(btnDelete);
        right.add(btnAdd);

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);
        return toolbar;
    }

    private JScrollPane buildTableView() {
        tableModel = new DefaultTableModel(new Object[]{"ID", "Username", "Họ tên", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(44);
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_SURFACE);
        table.setSelectionBackground(new Color(99, 102, 241, 80));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(255, 255, 255, 18));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.getTableHeader().setBackground(new Color(30, 41, 59, 220));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));

        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBorder(new EmptyBorder(0, 12, 0, 12)); // padding 12px
                c.setOpaque(true);

                if (isSelected) {
                    c.setBackground(new Color(99, 102, 241, 80));
                    c.setForeground(TEXT_PRIMARY);
                } else {
                    // Hover-like zebra subtle depth (Flat Design 2.0)
                    c.setBackground(row % 2 == 0 ? BG_SURFACE : new Color(30, 41, 59, 210));
                    c.setForeground(TEXT_PRIMARY);
                }
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cell);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(BG_MAIN);
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        return scroll;
    }

    private void loadDataAsync(String keyword) {
        setBusy(true);
        Thread.ofVirtual().start(() -> {
            try {
                List<User> staff = userService.getAllStaff(keyword);
                SwingUtilities.invokeLater(() -> {
                    setTableData(staff);
                    setBusy(false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    setBusy(false);
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void setTableData(List<User> staffList) {
        tableModel.setRowCount(0);
        for (User u : staffList) {
            tableModel.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getRole() != null ? u.getRole().getLabel() : ""
            });
        }
    }

    private Long getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object val = tableModel.getValueAt(row, 0);
        if (val instanceof Long) return (Long) val;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.parseLong(String.valueOf(val));
        } catch (Exception ignore) {
            return null;
        }
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

    private void openEditDialog() {
        int row = table.getSelectedRow();
        Long id = getSelectedId();
        if (row < 0 || id == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 nhân viên để sửa.", "Thiếu lựa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String username = String.valueOf(tableModel.getValueAt(row, 1));
        String fullName = String.valueOf(tableModel.getValueAt(row, 2));

        Window owner = SwingUtilities.getWindowAncestor(this);
        StaffDialog dlg = new StaffDialog(owner, true);
        dlg.setInitialValues(username, fullName);
        dlg.setOnSave(() -> {
            dlg.setError(null);
            setBusy(true);
            Thread.ofVirtual().start(() -> {
                try {
                    userService.updateStaff(id, dlg.getUsername(), dlg.getFullName(), dlg.getPassword());
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

    private void handleDelete() {
        int row = table.getSelectedRow();
        Long id = getSelectedId();
        if (row < 0 || id == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 nhân viên để xóa.", "Thiếu lựa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String username = String.valueOf(tableModel.getValueAt(row, 1));
        int ok = JOptionPane.showConfirmDialog(
                this,
                "Xóa nhân viên `" + username + "`?\nHành động này không thể hoàn tác.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (ok != JOptionPane.YES_OPTION) return;

        setBusy(true);
        Thread.ofVirtual().start(() -> {
            try {
                userService.deleteStaff(id);
                SwingUtilities.invokeLater(() -> loadDataAsync(txtSearch.getText()));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    setBusy(false);
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void setBusy(boolean busy) {
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        btnAdd.setEnabled(!busy);
        btnEdit.setEnabled(!busy);
        btnDelete.setEnabled(!busy);
    }
}
