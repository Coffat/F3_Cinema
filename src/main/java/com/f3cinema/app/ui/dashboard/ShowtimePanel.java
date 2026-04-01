package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.controller.ShowtimeController;
import com.f3cinema.app.dto.MovieSummaryDTO;
import com.f3cinema.app.entity.Showtime;
import com.f3cinema.app.service.MovieService;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ShowtimePanel - Giao diện Quản lý Suất chiếu dành cho Admin.
 * Tuân thủ Frontend Style Guide: Midnight Dark Mode & Table Management.
 */
public class ShowtimePanel extends BaseDashboardModule {

    // ── Design Tokens ────────────────────────────────────────────
    private static final Color BG_MAIN      = new Color(0x0F172A);
    private static final Color BG_SURFACE   = new Color(0x1E293B);
    private static final Color BG_ELEVATED  = new Color(0x334155);
    private static final Color TEXT_PRIMARY = new Color(0xF8FAFC);
    private static final Color TEXT_MUTED   = new Color(0x94A3B8);
    private static final Color ACCENT       = new Color(0x6366F1);

    private JTable showtimeTable;
    private DefaultTableModel tableModel;
    private JComboBox<MovieSummaryDTO> cbMovies;
    private JComboBox<com.f3cinema.app.entity.Room> cbRooms;

    // Custom Date Picker
    private JButton btnDatePicker;
    private LocalDate selectedDate = LocalDate.now();
    private static final DateTimeFormatter PICKER_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER   = DateTimeFormatter.ofPattern("HH:mm");

    private final ShowtimeController controller;

    public ShowtimePanel() {
        super("Quản lý Lịch chiếu", "Home > Showtime Management");
        this.controller = new ShowtimeController(this);
        initUI();
        loadMoviesToComboBox();
        controller.init();
    }

    private void initUI() {
        contentBody.add(createToolbar(), BorderLayout.NORTH);

        String[] cols = {"ID", "Phim", "Phòng chiếu", "Bắt đầu", "Kết thúc", "Giá vé (VNĐ)", "Hành động"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        showtimeTable = createStyledTable(tableModel);
        setupTableActions();

        JScrollPane sp = new JScrollPane(showtimeTable);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG_SURFACE);
        contentBody.add(sp, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOOLBAR
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        // ── Custom Calendar Button ─────────────────────────────────────────
        btnDatePicker = new JButton("\uD83D\uDCC5  " + selectedDate.format(PICKER_FORMATTER));
        btnDatePicker.setFont(new Font("Inter", Font.PLAIN, 14));
        btnDatePicker.setForeground(TEXT_PRIMARY);
        btnDatePicker.setBackground(BG_SURFACE);
        btnDatePicker.setPreferredSize(new Dimension(175, 40));
        btnDatePicker.setMaximumSize(new Dimension(175, 40));
        btnDatePicker.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDatePicker.setHorizontalAlignment(SwingConstants.LEFT);
        btnDatePicker.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; borderWidth: 0; background: #1E293B; foreground: #FFFFFF;");
        btnDatePicker.addActionListener(e -> {
            CalendarPopup popup = new CalendarPopup();
            popup.show(btnDatePicker, 0, btnDatePicker.getHeight() + 4);
        });

        // ── Combo Phim ────────────────────────────────────────────────────
        cbMovies = new JComboBox<>();
        styleToolbarComponent(cbMovies, 200);
        cbMovies.addActionListener(e ->
                controller.loadShowtimes(getSelectedDate(), getSelectedMovieId(), getSelectedRoomId()));

        // ── Combo Phòng ───────────────────────────────────────────────────
        cbRooms = new JComboBox<>();
        styleToolbarComponent(cbRooms, 180);
        cbRooms.addActionListener(e ->
                controller.loadShowtimes(getSelectedDate(), getSelectedMovieId(), getSelectedRoomId()));

        leftPanel.add(btnDatePicker);
        leftPanel.add(new JLabel("\uD83C\uDFAC"));
        leftPanel.add(cbMovies);
        leftPanel.add(new JLabel("\uD83C\uDFE0"));
        leftPanel.add(cbRooms);

        // ── Nút Thêm ─────────────────────────────────────────────────────
        JButton btnAdd = new JButton("+ Thêm suất chiếu");
        btnAdd.setBackground(ACCENT);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Inter", Font.BOLD, 14));
        btnAdd.setPreferredSize(new Dimension(180, 40));
        btnAdd.setMaximumSize(new Dimension(180, 40));
        btnAdd.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0;");
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> controller.handleAddAction());

        toolbar.add(leftPanel);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(btnAdd);
        return toolbar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INNER CLASS: CalendarPopup
    // ─────────────────────────────────────────────────────────────────────────
    private class CalendarPopup extends JPopupMenu {

        private YearMonth currentYearMonth;
        private JLabel lblMonthYear;
        private JPanel gridPanel;

        CalendarPopup() {
            currentYearMonth = YearMonth.from(selectedDate);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BG_ELEVATED, 1, true),
                    new EmptyBorder(8, 8, 8, 8)
            ));
            setBackground(BG_SURFACE);
            setLayout(new BorderLayout(0, 8));
            setPreferredSize(new Dimension(320, 310));

            add(buildHeader(), BorderLayout.NORTH);

            gridPanel = new JPanel(new GridLayout(0, 7, 4, 4));
            gridPanel.setBackground(BG_SURFACE);
            add(gridPanel, BorderLayout.CENTER);

            renderGrid();
        }

        // ── Header: điều hướng tháng/năm ─────────────────────────────────
        private JPanel buildHeader() {
            JPanel header = new JPanel(new BorderLayout(4, 0));
            header.setOpaque(false);

            JButton btnPrevYear  = navBtn("«");
            JButton btnNextYear  = navBtn("»");
            JButton btnPrevMonth = navBtn("<");
            JButton btnNextMonth = navBtn(">");

            btnPrevYear .addActionListener(e -> { currentYearMonth = currentYearMonth.minusYears(1);  renderGrid(); });
            btnNextYear .addActionListener(e -> { currentYearMonth = currentYearMonth.plusYears(1);   renderGrid(); });
            btnPrevMonth.addActionListener(e -> { currentYearMonth = currentYearMonth.minusMonths(1); renderGrid(); });
            btnNextMonth.addActionListener(e -> { currentYearMonth = currentYearMonth.plusMonths(1);  renderGrid(); });

            lblMonthYear = new JLabel("", SwingConstants.CENTER);
            lblMonthYear.setFont(new Font("Inter", Font.BOLD, 15));
            lblMonthYear.setForeground(TEXT_PRIMARY);

            JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            leftNav.setOpaque(false);
            leftNav.add(btnPrevYear);
            leftNav.add(btnPrevMonth);

            JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
            rightNav.setOpaque(false);
            rightNav.add(btnNextMonth);
            rightNav.add(btnNextYear);

            header.add(leftNav,      BorderLayout.WEST);
            header.add(lblMonthYear, BorderLayout.CENTER);
            header.add(rightNav,     BorderLayout.EAST);
            return header;
        }

        // ── Render lưới ngày ─────────────────────────────────────────────
        private void renderGrid() {
            lblMonthYear.setText("Tháng " + currentYearMonth.getMonthValue()
                    + "  Năm " + currentYearMonth.getYear());
            gridPanel.removeAll();

            // Tiêu đề thứ (Mon → Sun theo chuẩn VN)
            for (String d : new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"}) {
                JLabel lbl = new JLabel(d, SwingConstants.CENTER);
                lbl.setFont(new Font("Inter", Font.BOLD, 12));
                lbl.setForeground(TEXT_MUTED);
                gridPanel.add(lbl);
            }

            // Offset: Mon=0 … Sun=6
            LocalDate firstDay = currentYearMonth.atDay(1);
            int offset = firstDay.getDayOfWeek().getValue() - 1;
            for (int i = 0; i < offset; i++) gridPanel.add(new JLabel(""));

            // Nút từng ngày
            for (int day = 1; day <= currentYearMonth.lengthOfMonth(); day++) {
                LocalDate date = currentYearMonth.atDay(day);
                gridPanel.add(dayBtn(day, date));
            }

            gridPanel.revalidate();
            gridPanel.repaint();
            pack();
        }

        // ── Factory: nút ngày ─────────────────────────────────────────────
        private JButton dayBtn(int day, LocalDate date) {
            JButton btn = new JButton(String.valueOf(day));
            btn.setFont(new Font("Inter", Font.PLAIN, 13));
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setMargin(new java.awt.Insets(0, 0, 0, 0)); // Xóa margin để chữ số 2 ký tự không bị '...'
            btn.setPreferredSize(new Dimension(36, 36));
            btn.setOpaque(true);

            boolean isSel = date.equals(selectedDate);
            btn.setBackground(isSel ? ACCENT : BG_SURFACE);
            btn.setForeground(isSel ? Color.WHITE : TEXT_PRIMARY);

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!date.equals(selectedDate)) btn.setBackground(BG_ELEVATED);
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.setBackground(date.equals(selectedDate) ? ACCENT : BG_SURFACE);
                }
            });

            btn.addActionListener(e -> {
                selectedDate = date;
                btnDatePicker.setText("\uD83D\uDCC5  " + selectedDate.format(PICKER_FORMATTER));
                setVisible(false);
                controller.loadShowtimes(getSelectedDate(), getSelectedMovieId(), getSelectedRoomId());
            });
            return btn;
        }

        // ── Factory: nút điều hướng ───────────────────────────────────────
        private JButton navBtn(String text) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Inter", Font.BOLD, 13));
            btn.setForeground(TEXT_PRIMARY);
            btn.setBackground(BG_SURFACE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setOpaque(true);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(30, 28));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(BG_ELEVATED); }
                @Override public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(BG_SURFACE);  }
            });
            return btn;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private void styleToolbarComponent(JComponent c, int width) {
        c.setPreferredSize(new Dimension(width, 40));
        c.setMaximumSize(new Dimension(width, 40));
        c.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; background: #1E293B; foreground: #FFFFFF; borderWidth: 0;");
        c.setFont(new Font("Inter", Font.PLAIN, 14));
    }

    private void loadMoviesToComboBox() {
        cbMovies.addItem(new MovieSummaryDTO(null, "Tất cả các phim"));
        for (MovieSummaryDTO m : MovieService.getInstance().getMovieSummaries()) cbMovies.addItem(m);

        cbRooms.addItem(null);
        cbRooms.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, idx, sel, focus);
                if (value == null) setText("Tất cả phòng");
                else if (value instanceof com.f3cinema.app.entity.Room r) setText(r.getName());
                return this;
            }
        });
        for (com.f3cinema.app.entity.Room r : com.f3cinema.app.service.RoomService.getInstance().getAllRooms()) {
            cbRooms.addItem(r);
        }
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setBackground(BG_SURFACE);
        table.setForeground(TEXT_PRIMARY);
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setGridColor(BG_MAIN);
        table.getTableHeader().setBackground(BG_MAIN);
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                setBackground(sel ? BG_ELEVATED : BG_SURFACE);
                return this;
            }
        });
        return table;
    }

    private void setupTableActions() {
        showtimeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = showtimeTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    Long id = (Long) tableModel.getValueAt(row, 0);
                    showPopupMenu(e.getComponent(), e.getX(), e.getY(), id);
                }
            }
        });
    }

    private void showPopupMenu(Component invoker, int x, int y, Long id) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemEdit   = new JMenuItem("✏  Chỉnh sửa");
        JMenuItem itemDelete = new JMenuItem("\uD83D\uDDD1  Xóa suất chiếu");
        itemDelete.setForeground(Color.RED);

        itemEdit.addActionListener(e -> {
            Showtime s = com.f3cinema.app.service.ShowtimeService.getInstance().getShowtimeById(id);
            controller.handleEditAction(s);
        });
        itemDelete.addActionListener(e -> {
            Showtime s = com.f3cinema.app.service.ShowtimeService.getInstance().getShowtimeById(id);
            controller.handleDeleteAction(s);
        });
        menu.add(itemEdit);
        menu.add(itemDelete);
        menu.show(invoker, x, y);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API (Controller ↔ View)
    // ─────────────────────────────────────────────────────────────────────────
    public void updateTableData(List<Showtime> data) {
        tableModel.setRowCount(0);
        for (Showtime s : data) {
            tableModel.addRow(new Object[]{
                s.getId(),
                s.getMovie().getTitle(),
                s.getRoom().getName(),
                s.getStartTime().format(TIME_FORMATTER),
                s.getEndTime().format(TIME_FORMATTER),
                String.format("%,.0f", s.getBasePrice()),
                "⚙ Thao tác"
            });
        }
    }

    public void setLoadingState(boolean isLoading) {
        setCursor(isLoading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    public void showErrorMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi Quản lý Suất chiếu", JOptionPane.ERROR_MESSAGE);
    }

    public LocalDate getSelectedDate() { return selectedDate; }

    public Long getSelectedMovieId() {
        MovieSummaryDTO sel = (MovieSummaryDTO) cbMovies.getSelectedItem();
        return (sel != null) ? sel.id() : null;
    }

    public Long getSelectedRoomId() {
        com.f3cinema.app.entity.Room sel = (com.f3cinema.app.entity.Room) cbRooms.getSelectedItem();
        return (sel != null) ? sel.getId() : null;
    }
}
