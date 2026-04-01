package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.controller.ShowtimeController;
import com.f3cinema.app.dto.MovieSummaryDTO;
import com.f3cinema.app.entity.Room;
import com.f3cinema.app.entity.Showtime;
import com.f3cinema.app.service.MovieService;
import com.f3cinema.app.ui.dashboard.timeline.*;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static com.f3cinema.app.ui.dashboard.timeline.TimelineConstants.*;

/**
 * ShowtimePanel — Admin "Quản lý Lịch chiếu" with Timeline View.
 * Displays showtimes as color-coded blocks on a room x time grid.
 */
public class ShowtimePanel extends BaseDashboardModule {

    private JComboBox<MovieSummaryDTO> cbMovies;
    private JComboBox<Room> cbRooms;

    private JButton btnDatePicker;
    private LocalDate selectedDate = LocalDate.now();
    private static final DateTimeFormatter PICKER_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private TimelinePanel timelinePanel;
    private TimeRulerHeader timeRulerHeader;
    private RoomSidebar roomSidebar;
    private JScrollPane timelineScroll;
    private JPanel legendPanel;

    private JButton btnZoomIn;
    private JButton btnZoomOut;

    private final ShowtimeController controller;

    public ShowtimePanel() {
        super("Quản lý Lịch chiếu", "Home > Showtime Management");
        this.controller = new ShowtimeController(this);
        initUI();
        loadFilterData();
        controller.init();
    }

    private void initUI() {
        contentBody.add(createToolbar(), BorderLayout.NORTH);
        contentBody.add(createTimelineArea(), BorderLayout.CENTER);
        legendPanel = createLegendPanel();
        contentBody.add(legendPanel, BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TIMELINE AREA
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createTimelineArea() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        timelinePanel = new TimelinePanel();
        timeRulerHeader = new TimeRulerHeader();
        roomSidebar = new RoomSidebar();

        timelineScroll = new JScrollPane(timelinePanel);
        timelineScroll.setBorder(BorderFactory.createEmptyBorder());
        timelineScroll.getViewport().setBackground(BG_MAIN);
        timelineScroll.setRowHeaderView(roomSidebar);
        timelineScroll.setColumnHeaderView(timeRulerHeader);
        timelineScroll.getHorizontalScrollBar().setUnitIncrement(20);
        timelineScroll.getVerticalScrollBar().setUnitIncrement(20);

        // Corner panel (top-left intersection of ruler and sidebar)
        JPanel corner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(ROW_SEPARATOR);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        corner.setBackground(BG_MAIN);
        corner.setPreferredSize(new Dimension(SIDEBAR_WIDTH, RULER_HEIGHT));
        timelineScroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, corner);

        wrapper.add(timelineScroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOOLBAR
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        // Calendar button
        btnDatePicker = new JButton("\uD83D\uDCC5  " + selectedDate.format(PICKER_FORMATTER));
        btnDatePicker.setFont(new Font("Inter", Font.PLAIN, 14));
        btnDatePicker.setForeground(TEXT_PRIMARY);
        btnDatePicker.setBackground(BG_SURFACE);
        btnDatePicker.setPreferredSize(new Dimension(175, 36));
        btnDatePicker.setMaximumSize(new Dimension(175, 36));
        btnDatePicker.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDatePicker.setHorizontalAlignment(SwingConstants.LEFT);
        btnDatePicker.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; borderWidth: 0; background: #1E293B; foreground: #FFFFFF;");
        btnDatePicker.addActionListener(e -> {
            CalendarPopup popup = new CalendarPopup();
            popup.show(btnDatePicker, 0, btnDatePicker.getHeight() + 4);
        });

        // Movie filter
        cbMovies = new JComboBox<>();
        styleToolbarComponent(cbMovies, 200);
        cbMovies.addActionListener(e ->
                controller.loadShowtimes(getSelectedDate(), getSelectedMovieId(), getSelectedRoomId()));

        // Room filter
        cbRooms = new JComboBox<>();
        styleToolbarComponent(cbRooms, 180);
        cbRooms.addActionListener(e ->
                controller.loadShowtimes(getSelectedDate(), getSelectedMovieId(), getSelectedRoomId()));

        leftPanel.add(btnDatePicker);
        leftPanel.add(cbMovies);
        leftPanel.add(cbRooms);

        // Zoom controls
        JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        zoomPanel.setOpaque(false);

        btnZoomOut = createToolbarIconButton("−");
        btnZoomOut.addActionListener(e -> zoom(-ZOOM_STEP));
        btnZoomIn = createToolbarIconButton("+");
        btnZoomIn.addActionListener(e -> zoom(ZOOM_STEP));

        zoomPanel.add(btnZoomOut);
        zoomPanel.add(btnZoomIn);

        // Add button
        JButton btnAdd = new JButton("+ Thêm suất chiếu");
        btnAdd.setBackground(ACCENT);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Inter", Font.BOLD, 14));
        btnAdd.setPreferredSize(new Dimension(180, 36));
        btnAdd.setMaximumSize(new Dimension(180, 36));
        btnAdd.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0;");
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> controller.handleAddAction());

        toolbar.add(leftPanel);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(zoomPanel);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(btnAdd);
        return toolbar;
    }

    private JButton createToolbarIconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 16));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_SURFACE);
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setMaximumSize(new Dimension(36, 36));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0;");
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void zoom(double delta) {
        double current = timelinePanel.getPixelsPerMinute();
        double next = Math.max(MIN_PIXELS_PER_MINUTE, Math.min(MAX_PIXELS_PER_MINUTE, current + delta));
        timelinePanel.setPixelsPerMinute(next);
        timeRulerHeader.setPixelsPerMinute(next);
        timelineScroll.revalidate();
        timelineScroll.repaint();
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

    private void updateLegend() {
        legendPanel.removeAll();
        MovieColorPalette palette = timelinePanel.getPalette();
        Set<String> seen = new LinkedHashSet<>();

        for (ShowtimeBlock block : timelinePanel.getBlocks()) {
            String title = block.getShowtime().getMovie().getTitle();
            if (seen.add(title)) {
                Long movieId = block.getShowtime().getMovie().getId();
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
                label.setFont(new Font("Inter", Font.PLAIN, 12));
                label.setForeground(TEXT_MUTED);

                JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                item.setOpaque(false);
                item.add(swatch);
                item.add(label);
                legendPanel.add(item);
            }
        }
        legendPanel.revalidate();
        legendPanel.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INNER CLASS: CalendarPopup (preserved from original)
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

        private void renderGrid() {
            lblMonthYear.setText("Tháng " + currentYearMonth.getMonthValue()
                    + "  Năm " + currentYearMonth.getYear());
            gridPanel.removeAll();

            for (String d : new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"}) {
                JLabel lbl = new JLabel(d, SwingConstants.CENTER);
                lbl.setFont(new Font("Inter", Font.BOLD, 12));
                lbl.setForeground(TEXT_MUTED);
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

        private JButton navBtn(String text) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Inter", Font.BOLD, 13));
            btn.setForeground(TEXT_PRIMARY);
            btn.setBackground(BG_SURFACE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setOpaque(true);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
        c.setPreferredSize(new Dimension(width, 36));
        c.setMaximumSize(new Dimension(width, 36));
        c.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; background: #1E293B; foreground: #FFFFFF; borderWidth: 0;");
        c.setFont(new Font("Inter", Font.PLAIN, 14));
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

    private void showBlockPopupMenu(ShowtimeBlock block, int x, int y) {
        Showtime st = block.getShowtime();
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
        menu.show(block, x, y);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API (Controller ↔ View)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Replaces the old updateTableData. Receives rooms and showtimes grouped by room ID.
     */
    public void updateTimelineData(List<Room> rooms, Map<Long, List<Showtime>> grouped) {
        roomSidebar.setRooms(rooms);
        timelinePanel.setData(rooms, grouped);

        // Wire click listeners on all blocks
        for (ShowtimeBlock block : timelinePanel.getBlocks()) {
            block.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e) || e.getClickCount() == 1) {
                        showBlockPopupMenu(block, e.getX(), e.getY());
                    }
                }
            });
        }

        updateLegend();
        timelineScroll.revalidate();
        timelineScroll.repaint();
    }

    /** @deprecated Use updateTimelineData instead. Kept for backward compatibility during migration. */
    @Deprecated
    public void updateTableData(List<Showtime> data) {
        // Group by room for timeline display
        Map<Long, List<Showtime>> grouped = new LinkedHashMap<>();
        Set<Room> roomSet = new LinkedHashSet<>();
        for (Showtime s : data) {
            Room room = s.getRoom();
            roomSet.add(room);
            grouped.computeIfAbsent(room.getId(), k -> new ArrayList<>()).add(s);
        }
        List<Room> rooms = new ArrayList<>(roomSet);
        rooms.sort(Comparator.comparing(Room::getName));
        updateTimelineData(rooms, grouped);
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
        Room sel = (Room) cbRooms.getSelectedItem();
        return (sel != null) ? sel.getId() : null;
    }
}
