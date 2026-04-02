package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.controller.ShowtimeController;
import com.f3cinema.app.dto.MovieSummaryDTO;
import com.f3cinema.app.entity.Room;
import com.f3cinema.app.entity.Showtime;
import com.f3cinema.app.service.MovieService;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.dashboard.showtime.ShowtimeScheduleSupport;
import com.f3cinema.app.ui.dashboard.showtime.ShowtimeScheduleTableModel;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.RowSorter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ShowtimePanel — Admin "Quản lý Lịch chiếu" with schedule table (room + time columns).
 */
public class ShowtimePanel extends BaseDashboardModule {

    private static final Color BG_ELEVATED = ThemeConfig.BORDER_COLOR;
    private static final Color ACCENT = ThemeConfig.ACCENT_COLOR;

    private JComboBox<MovieSummaryDTO> cbMovies;
    private JComboBox<Room> cbRooms;

    private JButton btnDatePicker;
    private LocalDate selectedDate = LocalDate.now();
    private static final DateTimeFormatter PICKER_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JTable scheduleTable;
    private JPanel legendPanel;

    private final Set<Long> hiddenMovieIds = new HashSet<>();

    private final ShowtimeController controller;
    /** Staff: bảng lịch giống admin nhưng không thêm/sửa/xóa suất. */
    private final boolean readOnly;

    private JButton btnAddShowtime;

    public ShowtimePanel() {
        this(false);
    }

    public ShowtimePanel(boolean readOnly) {
        super(readOnly ? "Lịch chiếu" : "Quản lý Lịch chiếu",
                readOnly ? "Trang chủ / Suất chiếu" : "Home > Showtime Management");
        this.readOnly = readOnly;
        this.controller = new ShowtimeController(this);
        initUI();
        loadFilterData();
        controller.init();
    }

    private void initUI() {
        contentBody.add(createToolbar(), BorderLayout.NORTH);
        contentBody.add(createTableArea(), BorderLayout.CENTER);
        legendPanel = createLegendPanel();
        contentBody.add(legendPanel, BorderLayout.SOUTH);
    }

    private JScrollPane createTableArea() {
        scheduleTable = new JTable();
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleTable.setRowHeight(28);
        scheduleTable.setShowGrid(true);
        scheduleTable.setGridColor(ThemeConfig.CHART_GRID);
        scheduleTable.setBackground(ThemeConfig.BG_CARD);
        scheduleTable.setForeground(ThemeConfig.TEXT_PRIMARY);
        scheduleTable.setFont(ThemeConfig.FONT_BODY);
        scheduleTable.setFillsViewportHeight(true);
        scheduleTable.getTableHeader().setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD));
        scheduleTable.getTableHeader().setBackground(ThemeConfig.BG_CARD);
        scheduleTable.getTableHeader().setForeground(ThemeConfig.TEXT_SECONDARY);

        scheduleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
        });

        JScrollPane scroll = new JScrollPane(scheduleTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 24, 12, 24));
        scroll.getViewport().setBackground(ThemeConfig.BG_MAIN);
        return scroll;
    }

    private void maybeShowPopup(MouseEvent e) {
        if (readOnly) return;
        if (!e.isPopupTrigger()) return;
        int row = scheduleTable.rowAtPoint(e.getPoint());
        if (row < 0) return;
        int modelRow = scheduleTable.convertRowIndexToModel(row);
        ShowtimeScheduleTableModel model = (ShowtimeScheduleTableModel) scheduleTable.getModel();
        Showtime st = model.getShowtimeAt(modelRow);
        showRowPopupMenu(e.getComponent(), e.getX(), e.getY(), st);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOOLBAR
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JPanel controlBar = new JPanel();
        controlBar.setLayout(new BoxLayout(controlBar, BoxLayout.X_AXIS));
        controlBar.setBackground(ThemeConfig.BG_CARD);
        controlBar.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        controlBar.putClientProperty(FlatClientProperties.STYLE, "arc: 20");

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        btnDatePicker = new JButton("\uD83D\uDCC5  " + selectedDate.format(PICKER_FORMATTER));
        btnDatePicker.setFont(ThemeConfig.FONT_BODY);
        btnDatePicker.setForeground(ThemeConfig.TEXT_PRIMARY);
        btnDatePicker.setBackground(ThemeConfig.BG_CARD);
        btnDatePicker.setPreferredSize(new Dimension(175, 36));
        btnDatePicker.setMaximumSize(new Dimension(175, 36));
        btnDatePicker.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDatePicker.setHorizontalAlignment(SwingConstants.LEFT);
        btnDatePicker.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0; background: #0F172A; foreground: #FFFFFF;");
        btnDatePicker.addActionListener(e -> {
            CalendarPopup popup = new CalendarPopup();
            popup.show(btnDatePicker, 0, btnDatePicker.getHeight() + 4);
        });
        JButton btnPrev = createToolbarIconButton("<");
        btnPrev.addActionListener(e -> {
            selectedDate = selectedDate.minusDays(1);
            btnDatePicker.setText("\uD83D\uDCC5  " + selectedDate.format(PICKER_FORMATTER));
            controller.loadShowtimes(getSelectedDate(), getSelectedMovieId(), getSelectedRoomId());
        });
        JButton btnToday = createToolbarIconButton("•");
        btnToday.addActionListener(e -> {
            selectedDate = LocalDate.now();
            btnDatePicker.setText("\uD83D\uDCC5  " + selectedDate.format(PICKER_FORMATTER));
            controller.loadShowtimes(getSelectedDate(), getSelectedMovieId(), getSelectedRoomId());
        });
        JButton btnNext = createToolbarIconButton(">");
        btnNext.addActionListener(e -> {
            selectedDate = selectedDate.plusDays(1);
            btnDatePicker.setText("\uD83D\uDCC5  " + selectedDate.format(PICKER_FORMATTER));
            controller.loadShowtimes(getSelectedDate(), getSelectedMovieId(), getSelectedRoomId());
        });

        cbMovies = new JComboBox<>();
        styleToolbarComponent(cbMovies, 200);
        cbMovies.addActionListener(e ->
                controller.loadShowtimes(getSelectedDate(), getSelectedMovieId(), getSelectedRoomId()));

        cbRooms = new JComboBox<>();
        styleToolbarComponent(cbRooms, 180);
        cbRooms.addActionListener(e ->
                controller.loadShowtimes(getSelectedDate(), getSelectedMovieId(), getSelectedRoomId()));

        leftPanel.add(btnDatePicker);
        leftPanel.add(btnPrev);
        leftPanel.add(btnToday);
        leftPanel.add(btnNext);
        leftPanel.add(cbMovies);
        leftPanel.add(cbRooms);

        btnAddShowtime = new JButton("+ Thêm suất chiếu");
        btnAddShowtime.setBackground(ThemeConfig.ACCENT_COLOR);
        btnAddShowtime.setForeground(Color.WHITE);
        btnAddShowtime.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
        btnAddShowtime.setPreferredSize(new Dimension(180, 36));
        btnAddShowtime.setMaximumSize(new Dimension(180, 36));
        btnAddShowtime.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0;");
        btnAddShowtime.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAddShowtime.addActionListener(e -> controller.handleAddAction());
        btnAddShowtime.setVisible(!readOnly);

        controlBar.add(leftPanel);
        controlBar.add(Box.createHorizontalGlue());
        controlBar.add(btnAddShowtime);
        toolbar.add(controlBar, BorderLayout.CENTER);
        return toolbar;
    }

    private JButton createToolbarIconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 16));
        btn.setForeground(ThemeConfig.TEXT_PRIMARY);
        btn.setBackground(ThemeConfig.BG_CARD);
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setMaximumSize(new Dimension(36, 36));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0;");
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LEGEND PANEL
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        return panel;
    }

    private void updateLegend(List<Showtime> rows, MovieColorPalette palette) {
        legendPanel.removeAll();
        Set<String> seen = new LinkedHashSet<>();

        for (Showtime st : rows) {
            if (st.getMovie() == null) continue;
            String title = st.getMovie().getTitle();
            if (!seen.add(title)) continue;
            Long movieId = st.getMovie().getId();
            Color color = palette.getColor(movieId);

            JPanel swatch = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    g2.dispose();
                }
            };
            swatch.setOpaque(false);
            swatch.setPreferredSize(new Dimension(12, 12));

            JLabel label = new JLabel(title);
            label.setFont(ThemeConfig.FONT_SMALL);
            label.setForeground(ThemeConfig.TEXT_SECONDARY);

            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            item.setOpaque(false);
            item.add(swatch);
            item.add(label);
            JToggleButton toggle = new JToggleButton("ON", !hiddenMovieIds.contains(movieId));
            toggle.setFont(ThemeConfig.FONT_SMALL);
            toggle.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #1E293B;");
            toggle.addActionListener(e -> {
                if (toggle.isSelected()) hiddenMovieIds.remove(movieId);
                else hiddenMovieIds.add(movieId);
                applyLegendRowFilter();
            });
            item.add(toggle);
            legendPanel.add(item);
        }
        legendPanel.revalidate();
        legendPanel.repaint();
    }

    @SuppressWarnings("unchecked")
    private void applyLegendRowFilter() {
        RowSorter<?> rs = scheduleTable.getRowSorter();
        if (!(rs instanceof TableRowSorter)) return;
        TableRowSorter<ShowtimeScheduleTableModel> sorter =
                (TableRowSorter<ShowtimeScheduleTableModel>) rs;
        if (hiddenMovieIds.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(new RowFilter<ShowtimeScheduleTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends ShowtimeScheduleTableModel, ? extends Integer> entry) {
                Showtime st = entry.getModel().getShowtimeAt(entry.getIdentifier());
                Long mid = st.getMovie() != null ? st.getMovie().getId() : null;
                return mid == null || !hiddenMovieIds.contains(mid);
            }
        });
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
            setBackground(ThemeConfig.BG_CARD);
            setLayout(new BorderLayout(0, 8));
            setPreferredSize(new Dimension(320, 310));

            add(buildHeader(), BorderLayout.NORTH);

            gridPanel = new JPanel(new GridLayout(0, 7, 4, 4));
            gridPanel.setBackground(ThemeConfig.BG_CARD);
            add(gridPanel, BorderLayout.CENTER);

            renderGrid();
        }

        private JPanel buildHeader() {
            JPanel header = new JPanel(new BorderLayout(4, 0));
            header.setOpaque(false);

            JButton btnPrevYear = navBtn("«");
            JButton btnNextYear = navBtn("»");
            JButton btnPrevMonth = navBtn("<");
            JButton btnNextMonth = navBtn(">");

            btnPrevYear.addActionListener(e -> {
                currentYearMonth = currentYearMonth.minusYears(1);
                renderGrid();
            });
            btnNextYear.addActionListener(e -> {
                currentYearMonth = currentYearMonth.plusYears(1);
                renderGrid();
            });
            btnPrevMonth.addActionListener(e -> {
                currentYearMonth = currentYearMonth.minusMonths(1);
                renderGrid();
            });
            btnNextMonth.addActionListener(e -> {
                currentYearMonth = currentYearMonth.plusMonths(1);
                renderGrid();
            });

            lblMonthYear = new JLabel("", SwingConstants.CENTER);
            lblMonthYear.setFont(new Font("Inter", Font.BOLD, 15));
            lblMonthYear.setForeground(ThemeConfig.TEXT_PRIMARY);

            JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            leftNav.setOpaque(false);
            leftNav.add(btnPrevYear);
            leftNav.add(btnPrevMonth);

            JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
            rightNav.setOpaque(false);
            rightNav.add(btnNextMonth);
            rightNav.add(btnNextYear);

            header.add(leftNav, BorderLayout.WEST);
            header.add(lblMonthYear, BorderLayout.CENTER);
            header.add(rightNav, BorderLayout.EAST);
            return header;
        }

        private void renderGrid() {
            lblMonthYear.setText("Tháng " + currentYearMonth.getMonthValue()
                    + "  Năm " + currentYearMonth.getYear());
            gridPanel.removeAll();

            for (String d : new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"}) {
                JLabel lbl = new JLabel(d, SwingConstants.CENTER);
                lbl.setFont(new Font("Inter", Font.BOLD, 12));
                lbl.setForeground(ThemeConfig.TEXT_SECONDARY);
                gridPanel.add(lbl);
            }

            LocalDate firstDay = currentYearMonth.atDay(1);
            int offset = firstDay.getDayOfWeek().getValue() - 1;
            for (int i = 0; i < offset; i++) gridPanel.add(new JLabel(""));

            for (int day = 1; day <= currentYearMonth.lengthOfMonth(); day++) {
                LocalDate date = currentYearMonth.atDay(day);
                gridPanel.add(dayBtn(day, date));
            }

            gridPanel.revalidate();
            gridPanel.repaint();
            pack();
        }

        private JButton dayBtn(int day, LocalDate date) {
            JButton btn = new JButton(String.valueOf(day));
            btn.setFont(new Font("Inter", Font.PLAIN, 13));
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setPreferredSize(new Dimension(36, 36));
            btn.setOpaque(true);

            boolean isSel = date.equals(selectedDate);
            btn.setBackground(isSel ? ACCENT : ThemeConfig.BG_CARD);
            btn.setForeground(isSel ? Color.WHITE : ThemeConfig.TEXT_PRIMARY);

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!date.equals(selectedDate)) btn.setBackground(ThemeConfig.BG_CARD_HOVER);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.setBackground(date.equals(selectedDate) ? ACCENT : ThemeConfig.BG_CARD);
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

        private JButton navBtn(String text) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Inter", Font.BOLD, 13));
            btn.setForeground(ThemeConfig.TEXT_PRIMARY);
            btn.setBackground(ThemeConfig.BG_CARD);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setOpaque(true);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(30, 28));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    btn.setBackground(ThemeConfig.BG_CARD_HOVER);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.setBackground(ThemeConfig.BG_CARD);
                }
            });
            return btn;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void styleToolbarComponent(JComponent c, int width) {
        c.setPreferredSize(new Dimension(width, 36));
        c.setMaximumSize(new Dimension(width, 36));
        c.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; background: #0F172A; foreground: #FFFFFF; borderWidth: 0;");
        c.setFont(ThemeConfig.FONT_BODY);
    }

    private void loadFilterData() {
        cbMovies.addItem(new MovieSummaryDTO(null, "Tất cả các phim"));
        for (MovieSummaryDTO m : MovieService.getInstance().getMovieSummaries()) cbMovies.addItem(m);

        cbRooms.addItem(null);
        cbRooms.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, idx, sel, focus);
                if (value == null) setText("Tất cả phòng");
                else if (value instanceof Room r) setText(r.getName());
                return this;
            }
        });
        for (Room r : com.f3cinema.app.service.RoomService.getInstance().getAllRooms()) {
            cbRooms.addItem(r);
        }
    }

    private void showRowPopupMenu(Component invoker, int x, int y, Showtime st) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem itemEdit = new JMenuItem("✏  Chỉnh sửa");
        JMenuItem itemDelete = new JMenuItem("\uD83D\uDDD1  Xóa suất chiếu");
        itemDelete.setForeground(new Color(0xEF4444));

        itemEdit.addActionListener(e -> {
            Showtime fresh = com.f3cinema.app.service.ShowtimeService.getInstance().getShowtimeById(st.getId());
            controller.handleEditAction(fresh);
        });
        itemDelete.addActionListener(e -> {
            Showtime fresh = com.f3cinema.app.service.ShowtimeService.getInstance().getShowtimeById(st.getId());
            controller.handleDeleteAction(fresh);
        });

        menu.add(itemEdit);
        menu.add(itemDelete);
        menu.show(invoker, x, y);
    }

    private void installTableRenderer(MovieColorPalette rowPalette) {
        DefaultTableCellRenderer base = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int modelRow = table.convertRowIndexToModel(row);
                ShowtimeScheduleTableModel m = (ShowtimeScheduleTableModel) table.getModel();
                Showtime st = m.getShowtimeAt(modelRow);
                Long movieId = st.getMovie() != null ? st.getMovie().getId() : null;
                Color tint = movieId != null ? rowPalette.getBlockBackground(movieId)
                        : ThemeConfig.BG_CARD;
                if (isSelected) {
                    ((JLabel) c).setBackground(tint.darker());
                } else {
                    ((JLabel) c).setBackground(tint);
                }
                ((JLabel) c).setOpaque(true);
                if (column == ShowtimeScheduleTableModel.COL_WARN && value != null && !value.toString().isEmpty()) {
                    ((JLabel) c).setForeground(ThemeConfig.TEXT_DANGER);
                } else {
                    ((JLabel) c).setForeground(ThemeConfig.TEXT_PRIMARY);
                }
                return c;
            }
        };
        scheduleTable.setDefaultRenderer(Object.class, base);
    }

    private void sizeColumns() {
        TableColumn col;
        int[] widths = {100, 88, 120, 220, 96, 200};
        for (int i = 0; i < widths.length && i < scheduleTable.getColumnCount(); i++) {
            col = scheduleTable.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API (Controller ↔ View)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Receives rooms and showtimes grouped by room ID; fills the schedule table (layout A: all rooms in one table).
     */
    public void updateScheduleTable(List<Room> rooms, Map<Long, List<Showtime>> grouped) {
        List<Showtime> flat = ShowtimeScheduleSupport.flattenInRoomOrder(rooms, grouped);
        Map<Long, String> conflicts = ShowtimeScheduleSupport.conflictMessagesByShowtimeId(rooms, grouped);

        MovieColorPalette palette = new MovieColorPalette();
        for (Showtime st : flat) {
            if (st.getMovie() != null) palette.getColor(st.getMovie().getId());
        }

        ShowtimeScheduleTableModel model = new ShowtimeScheduleTableModel(flat, conflicts, selectedDate);
        scheduleTable.setModel(model);

        TableRowSorter<ShowtimeScheduleTableModel> sorter = new TableRowSorter<>(model);
        scheduleTable.setRowSorter(sorter);
        applyLegendRowFilter();

        installTableRenderer(palette);
        sizeColumns();

        updateLegend(flat, palette);
        scheduleTable.revalidate();
        scheduleTable.repaint();
    }

    /** @deprecated Use {@link #updateScheduleTable(List, Map)}. */
    @Deprecated
    public void updateTimelineData(List<Room> rooms, Map<Long, List<Showtime>> grouped) {
        updateScheduleTable(rooms, grouped);
    }

    public void updateTableData(List<Showtime> data) {
        Map<Long, List<Showtime>> grouped = new LinkedHashMap<>();
        Set<Room> roomSet = new LinkedHashSet<>();
        for (Showtime s : data) {
            Room room = s.getRoom();
            roomSet.add(room);
            grouped.computeIfAbsent(room.getId(), k -> new ArrayList<>()).add(s);
        }
        List<Room> rooms = new ArrayList<>(roomSet);
        rooms.sort(Comparator.comparing(Room::getName));
        updateScheduleTable(rooms, grouped);
    }

    public void setLoadingState(boolean isLoading) {
        setCursor(isLoading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    public void showErrorMessage(String msg) {
        AppMessageDialogs.showError(this, "Lỗi Quản lý Suất chiếu", msg);
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public Long getSelectedMovieId() {
        MovieSummaryDTO sel = (MovieSummaryDTO) cbMovies.getSelectedItem();
        return (sel != null) ? sel.id() : null;
    }

    public Long getSelectedRoomId() {
        Room sel = (Room) cbRooms.getSelectedItem();
        return (sel != null) ? sel.getId() : null;
    }
}
