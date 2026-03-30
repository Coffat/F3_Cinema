package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.controller.ShowtimeController;
import com.f3cinema.app.dto.MovieSummaryDTO;
import com.f3cinema.app.entity.Showtime;
import com.f3cinema.app.service.MovieService;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ShowtimePanel - Giao diện Quản lý Suất chiếu dành cho Admin.
 * Được viết lại từ đầu để thay thế giao diện bán vé cũ.
 * Tuân thủ Frontend Style Guide: Midnight Dark Mode & Table Management.
 */
public class ShowtimePanel extends BaseDashboardModule {

    private JTable showtimeTable;
    private DefaultTableModel tableModel;
    private JComboBox<MovieSummaryDTO> cbMovies;
    private JComboBox<com.f3cinema.app.entity.Room> cbRooms;
    private JSpinner spinnerDate;
    private final ShowtimeController controller;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public ShowtimePanel() {
        super("Quản lý Lịch chiếu", "Home > Showtime Management");
        this.controller = new ShowtimeController(this);
        initUI();
        loadMoviesToComboBox();
        controller.init();
    }

    private void initUI() {
        // 1. Toolbar
        JPanel toolbar = createToolbar();
        contentBody.add(toolbar, BorderLayout.NORTH);

        // 2. Table
        String[] columnNames = {"ID", "Phim", "Phòng chiếu", "Bắt đầu", "Kết thúc", "Giá vé (VNĐ)", "Hành động"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Chỉ cho phép edit cột hành động (nếu dùng button renderer)
            }
        };

        showtimeTable = createStyledTable(tableModel);
        setupTableActions();
        
        JScrollPane scrollPane = new JScrollPane(showtimeTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.decode("#1E293B"));
        contentBody.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // 1. Cụm bên trái (Bộ lọc)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        // Date Picker (JSpinner + SpinnerDateModel)
        spinnerDate = new JSpinner(new SpinnerDateModel());
        spinnerDate.setValue(new java.util.Date()); // Đảm bảo lấy ngày giờ hiện tại của hệ thống
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinnerDate, "dd/MM/yyyy");
        spinnerDate.setEditor(dateEditor);
        styleToolbarComponent(spinnerDate, 140);
        spinnerDate.addChangeListener(e -> controller.loadShowtimes(getSelectedDate(), getSelectedMovieId(), getSelectedRoomId()));

        // Phim filter
        cbMovies = new JComboBox<>();
        styleToolbarComponent(cbMovies, 200);
        cbMovies.addActionListener(e -> controller.loadShowtimes(getSelectedDate(), getSelectedMovieId(), getSelectedRoomId()));

        // Phòng filter
        cbRooms = new JComboBox<>();
        styleToolbarComponent(cbRooms, 180);
        cbRooms.addActionListener(e -> controller.loadShowtimes(getSelectedDate(), getSelectedMovieId(), getSelectedRoomId()));

        leftPanel.add(new JLabel("📅"));
        leftPanel.add(spinnerDate);
        leftPanel.add(new JLabel("🎬"));
        leftPanel.add(cbMovies);
        leftPanel.add(new JLabel("🏠"));
        leftPanel.add(cbRooms);

        // 2. Cụm bên phải (Hành động)
        JButton btnAdd = new JButton("+ Thêm suất chiếu");
        btnAdd.setBackground(Color.decode("#6366F1")); // Indigo 500
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Inter", Font.BOLD, 14));
        btnAdd.setPreferredSize(new Dimension(180, 40));
        btnAdd.setMaximumSize(new Dimension(180, 40));
        btnAdd.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0;");
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> controller.handleAddAction());

        toolbar.add(leftPanel);
        toolbar.add(Box.createHorizontalGlue()); // Đẩy nút thêm về bên phải
        toolbar.add(btnAdd);

        return toolbar;
    }

    private void styleToolbarComponent(JComponent c, int width) {
        c.setPreferredSize(new Dimension(width, 40));
        c.setMaximumSize(new Dimension(width, 40));
        c.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #1E293B; foreground: #FFFFFF; borderWidth: 0;");
        c.setFont(new Font("Inter", Font.PLAIN, 14));
    }

    private void loadMoviesToComboBox() {
        // Load Phim
        cbMovies.addItem(new MovieSummaryDTO(null, "Tất cả các phim"));
        for (MovieSummaryDTO m : MovieService.getInstance().getMovieSummaries()) {
            cbMovies.addItem(m);
        }
        
        // Load Phòng
        cbRooms.addItem(null); // Placeholder cho "Tất cả phòng"
        cbRooms.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
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
        table.setBackground(Color.decode("#1E293B"));
        table.setForeground(Color.decode("#F8FAFC"));
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setGridColor(Color.decode("#0F172A"));
        table.getTableHeader().setBackground(Color.decode("#0F172A"));
        table.getTableHeader().setForeground(Color.decode("#94A3B8"));
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                c.setBackground(isSelected ? Color.decode("#334155") : Color.decode("#1E293B"));
                return c;
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
        JMenuItem itemEdit = new JMenuItem("Chỉnh sửa");
        JMenuItem itemDelete = new JMenuItem("Xóa suất chiếu");
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

    public LocalDate getSelectedDate() { 
        java.util.Date date = (java.util.Date) spinnerDate.getValue();
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }
    
    public Long getSelectedMovieId() {
        MovieSummaryDTO selected = (MovieSummaryDTO) cbMovies.getSelectedItem();
        return (selected != null) ? selected.id() : null;
    }

    public Long getSelectedRoomId() {
        com.f3cinema.app.entity.Room selected = (com.f3cinema.app.entity.Room) cbRooms.getSelectedItem();
        return (selected != null) ? selected.getId() : null;
    }
}
