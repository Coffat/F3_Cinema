package com.f3cinema.app.ui.staff;

import com.f3cinema.app.dto.MovieSummaryDTO;
import com.f3cinema.app.dto.ShowtimeSummaryDTO;
import com.f3cinema.app.service.MovieService;
import com.f3cinema.app.service.ShowtimeService;
import com.f3cinema.app.util.WrapLayout;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Giai đoạn Thiết kế Midnight Dark Mode cho Màn hình Chọn Suất Chiếu.
 */
public class ShowtimeListView extends JPanel {

    private final TicketingPanel navigator;
    private JComboBox<MovieSummaryDTO> cbMovies;
    private JComboBox<com.f3cinema.app.entity.Room> cbRooms;
    private LocalDate selectedDate = LocalDate.now();
    private JPanel listPanel;
    private SwingWorker<List<ShowtimeSummaryDTO>, Void> currentWorker;

    // ── Design Tokens ───────────────────────────────────────────────
    private static final Color BG_MAIN        = new Color(0x0F172A);  // Slate 900
    private static final Color BG_SURFACE     = new Color(0x1E293B);  // Slate 800
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);  // Slate 400
    private static final Color ACCENT_PRIMARY = new Color(0x6366F1);  // Indigo 500

    public ShowtimeListView(TicketingPanel navigator) {
        this.navigator = navigator;
        initLayout();
        loadMoviesToComboBox();
        loadRoomsToComboBox();
        loadShowtimesData();
    }

    private void initLayout() {
        setLayout(new BorderLayout(0, 24));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 0, 10, 0));

        // ──────────────────────────────────────────────────────────────────
        // 2. Vùng NORTH: Bộ Toolbar (Date Strip & Lọc Phim)
        // ──────────────────────────────────────────────────────────────────
        JPanel northPanel = new JPanel(new BorderLayout(0, 16));
        northPanel.setOpaque(false);

        // Hàng 1: Thanh chọn ngày ngang (Date Strip)
        northPanel.add(createDateStripPanel(), BorderLayout.NORTH);

        // Hàng 2: Lọc Phim & Phòng & Tìm kiếm
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        filterRow.setOpaque(false);

        cbMovies = new JComboBox<>();
        cbMovies.setPreferredSize(new Dimension(220, 42));
        cbMovies.setFont(new Font("Inter", Font.PLAIN, 15));
        cbMovies.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #1E293B; foreground: #F8FAFC; borderWidth: 0;");
        cbMovies.addActionListener(e -> loadShowtimesData());

        cbRooms = new JComboBox<>();
        cbRooms.setPreferredSize(new Dimension(180, 42));
        cbRooms.setFont(new Font("Inter", Font.PLAIN, 15));
        cbRooms.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #1E293B; foreground: #F8FAFC; borderWidth: 0;");
        cbRooms.addActionListener(e -> loadShowtimesData());

        JButton btnSearch = new JButton("Làm mới");
        btnSearch.addActionListener(e -> loadShowtimesData());
        btnSearch.setFont(new Font("Inter", Font.BOLD, 14));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBackground(BG_SURFACE);
        btnSearch.setPreferredSize(new Dimension(120, 42));
        btnSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0;");

        filterRow.add(new JLabel("<html><font color='#94A3B8'>Phim:</font></html>"));
        filterRow.add(cbMovies);
        filterRow.add(new JLabel("<html><font color='#94A3B8'>Phòng:</font></html>"));
        filterRow.add(cbRooms);
        filterRow.add(btnSearch);

        northPanel.add(filterRow, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        // ──────────────────────────────────────────────────────────────────
        // 3. Vùng CENTER: Hiển thị Suất Chiếu bằng WrapLayout
        // ──────────────────────────────────────────────────────────────────
        listPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 24, 24));
        listPanel.setBackground(BG_MAIN);
        listPanel.setOpaque(true); 

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(BG_MAIN);
        scrollPane.getViewport().setBackground(BG_MAIN);
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createDateStripPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 4, 10, 0));

        ButtonGroup group = new ButtonGroup();
        // Sử dụng Java 19+ Locale.of hoặc Locale.forLanguageTag để tránh Deprecated
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE", java.util.Locale.forLanguageTag("vi-VN"));
        DateTimeFormatter dateStrFormatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            String dayName = (i == 0) ? "Hôm nay" : date.format(dayFormatter);
            String label = "<html><center>" + dayName + "<br><font size='4'><b>" + date.format(dateStrFormatter) + "</b></font></center></html>";

            JToggleButton btn = new JToggleButton(label);
            btn.setPreferredSize(new Dimension(110, 65));
            btn.setFont(new Font("Inter", Font.PLAIN, 13));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Indigo style cho nút được chọn
            btn.putClientProperty(FlatClientProperties.STYLE, 
                "arc: 16; " +
                "background: #1E293B; " +
                "foreground: #94A3B8; " +
                "selectedBackground: #6366F1; " +
                "selectedForeground: #FFFFFF; " +
                "borderWidth: 0; " +
                "innerFocusWidth: 0;"
            );

            if (i == 0) btn.setSelected(true);

            btn.addActionListener(e -> {
                this.selectedDate = date;
                loadShowtimesData();
            });

            group.add(btn);
            panel.add(btn);
        }
        return panel;
    }

    private void loadMoviesToComboBox() {
        List<MovieSummaryDTO> movies = MovieService.getInstance().getMovieSummaries();
        cbMovies.addItem(new MovieSummaryDTO(null, "Tất cả các phim"));
        for (MovieSummaryDTO m : movies) {
            cbMovies.addItem(m);
        }
    }

    private void loadRoomsToComboBox() {
        cbRooms.addItem(null); // Option "Tất cả phòng"
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

    /**
     * Tải danh sách suất chiếu bất đồng bộ (SwingWorker) sử dụng các bộ lọc.
     */
    private void loadShowtimesData() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }

        // Sử dụng biến đã cập nhật từ thanh chọn ngày, phim và phòng
        final LocalDate finalFilterDate = this.selectedDate;
        
        MovieSummaryDTO selectedMovie = (MovieSummaryDTO) cbMovies.getSelectedItem();
        final Long finalMovieId = (selectedMovie != null) ? selectedMovie.id() : null;

        com.f3cinema.app.entity.Room selectedRoom = (com.f3cinema.app.entity.Room) cbRooms.getSelectedItem();
        final Long finalRoomId = (selectedRoom != null) ? selectedRoom.getId() : null;

        currentWorker = new SwingWorker<List<ShowtimeSummaryDTO>, Void>() {
            @Override
            protected List<ShowtimeSummaryDTO> doInBackground() throws Exception {
                return ShowtimeService.getInstance().getShowtimesForUI(finalFilterDate, finalMovieId, finalRoomId);
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    List<ShowtimeSummaryDTO> results = get();
                    
                    listPanel.removeAll();
                    if (results.isEmpty()) {
                        JLabel lblEmpty = new JLabel("Không có suất chiếu nào cho ngày này.");
                        lblEmpty.setForeground(TEXT_SECONDARY);
                        lblEmpty.setFont(new Font("Inter", Font.ITALIC, 16));
                        listPanel.add(lblEmpty);
                    } else {
                        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                        for (ShowtimeSummaryDTO s : results) {
                            String timeRange = s.startTime().format(timeFormatter) + " - " + s.endTime().format(timeFormatter);
                            listPanel.add(createShowtimeCard(s.showtimeId(), s.movieTitle(), timeRange, s.roomName()));
                        }
                    }
                    listPanel.revalidate();
                    listPanel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ShowtimeListView.this, "Lỗi khi tải dữ liệu suất chiếu từ máy chủ.", "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        currentWorker.execute();
    }

    /**
     * Factory Pattern để đúc ra Card Suất Chiếu đồng bộ UI
     */
    private JPanel createShowtimeCard(Long id, String movieTitle, String timeRange, String roomName) {
        // Container JPanel chỉnh màu và bo viền arc 16px thuần thủ công
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(250, 160));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Text Component 1: Tên phim
        JLabel lblMovie = new JLabel(movieTitle);
        lblMovie.setFont(new Font("-apple-system", Font.BOLD, 15));
        lblMovie.setForeground(Color.WHITE);

        // Text Component 2: Khung giờ chiếu (Font to, màu nhấn)
        JLabel lblTime = new JLabel("⏰ " + timeRange);
        lblTime.setFont(new Font("-apple-system", Font.BOLD, 20));
        lblTime.setForeground(ACCENT_PRIMARY);

        // Text Component 3: Thông số Rạp
        JLabel lblRoom = new JLabel(roomName);
        lblRoom.setFont(new Font("-apple-system", Font.PLAIN, 14));
        lblRoom.setForeground(TEXT_SECONDARY);

        // Nút Kích hoạt chuyển trang
        JButton btnSelect = new JButton("CHỌN GHẾ");
        btnSelect.setFont(new Font("-apple-system", Font.BOLD, 14));
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setBackground(ACCENT_PRIMARY);
        btnSelect.setFocusPainted(false);
        btnSelect.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSelect.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0;");
        btnSelect.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        // 5. Thao tác CardLayout Navigation bằng Click Handling
        java.awt.event.MouseAdapter clickHandler = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                navigator.navigateToSeatMap(id); // Lật qua GĐ 3
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                // Hiệu ứng Hover: Sáng rực cái Card lên khi trỏ qua
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_PRIMARY, 2, true),
                    new EmptyBorder(14, 14, 14, 14) // Ép padding lại tránh xê dịch layout
                ));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(new EmptyBorder(16, 16, 16, 16));
            }
        };

        // Ràng buộc vào cả khối Card và Khối Button
        card.addMouseListener(clickHandler);
        btnSelect.addMouseListener(clickHandler);

        card.add(lblMovie);
        card.add(Box.createVerticalStrut(10));
        card.add(lblTime);
        card.add(Box.createVerticalStrut(6));
        card.add(lblRoom);
        card.add(Box.createVerticalGlue()); // Tự động đẩy nút bấm xuống sát đáy box
        card.add(btnSelect);

        return card;
    }
}
