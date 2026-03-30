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
    private JComboBox<LocalDate> cbDate;
    private JPanel listPanel;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("EEEE, dd/MM");

    // ── Design Tokens ───────────────────────────────────────────────
    private static final Color BG_MAIN        = new Color(0x0F172A);  // Slate 900
    private static final Color BG_SURFACE     = new Color(0x1E293B);  // Slate 800
    private static final Color TEXT_PRIMARY   = new Color(0xF8FAFC);  // Slate 50
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);  // Slate 400
    private static final Color ACCENT_PRIMARY = new Color(0x6366F1);  // Indigo 500

    public ShowtimeListView(TicketingPanel navigator) {
        this.navigator = navigator;
        initLayout();
        loadMoviesToComboBox(); // Bước 1: Load danh sách phim thật
        loadShowtimesData();    // Bước 2: Load suất chiếu mặc định (Async)
    }

    private void initLayout() {
        // 1. Áp dụng BorderLayout
        setLayout(new BorderLayout(0, 24));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 0, 10, 0)); // Padding nhẹ

        // ──────────────────────────────────────────────────────────────────
        // 2. Vùng NORTH: Bộ Toolbar (Lọc Phim & Ngày chiếu)
        // ──────────────────────────────────────────────────────────────────
        JPanel topFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        topFilterPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Lịch chiếu ngày:");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_PRIMARY);

        // Ô Chọn Ngày (Thay thế TextField nhập liệu)
        cbDate = new JComboBox<>();
        cbDate.setPreferredSize(new Dimension(180, 38));
        cbDate.setFont(new Font("Inter", Font.BOLD, 14));
        cbDate.putClientProperty(FlatClientProperties.STYLE, "arc: 12; focusWidth: 2;");
        
        // Nạp 14 ngày kể từ hôm nay
        for (int i = 0; i < 14; i++) {
            cbDate.addItem(LocalDate.now().plusDays(i));
        }
        
        // Renderer giúp hiển thị ngày "Thứ 2, 30/03" thay vì ISO date
        cbDate.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LocalDate date) {
                    setText(date.format(DISPLAY_FORMATTER));
                }
                return this;
            }
        });
        
        // Tự động tải lại khi chọn ngày khác
        cbDate.addActionListener(e -> loadShowtimesData());

        // Combo Box chọn phim (Sử dụng Record DTO)
        cbMovies = new JComboBox<>();
        cbMovies.setPreferredSize(new Dimension(240, 38));
        cbMovies.setFont(new Font("Inter", Font.PLAIN, 14));
        cbMovies.putClientProperty(FlatClientProperties.STYLE, "arc: 12; focusWidth: 2;");
        cbMovies.addActionListener(e -> loadShowtimesData()); // Auto-reload khi đổi phim

        // Nút Tìm Kiếm
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.addActionListener(e -> loadShowtimesData()); // Trigger tải lại dữ liệu khi bấm nút
        btnSearch.setFont(new Font("Inter", Font.BOLD, 14));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBackground(ACCENT_PRIMARY);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.setPreferredSize(new Dimension(120, 38));
        btnSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0; focusWidth: 0;");

        topFilterPanel.add(lblTitle);
        topFilterPanel.add(cbDate);
        topFilterPanel.add(cbMovies);
        topFilterPanel.add(btnSearch);

        add(topFilterPanel, BorderLayout.NORTH);

        // ──────────────────────────────────────────────────────────────────
        // 3. Vùng CENTER: Hiển thị Suất Chiếu bằng WrapLayout
        // ──────────────────────────────────────────────────────────────────
        listPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 24, 24));
        listPanel.setBackground(BG_MAIN);
        listPanel.setOpaque(true); 

        // Bọc vào Scroll Pane an toàn, ẩn viền
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(BG_MAIN);
        scrollPane.getViewport().setBackground(BG_MAIN);
        // Tắt scroll ngang, thanh scroll dọc tự biến mất nếu chưa cần cuộn
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Tải danh sách phim từ Service và đưa vào ComboBox.
     */
    private void loadMoviesToComboBox() {
        List<MovieSummaryDTO> movies = MovieService.getInstance().getMovieSummaries();
        cbMovies.addItem(new MovieSummaryDTO(null, "Tất cả các phim")); // Option mặc định
        for (MovieSummaryDTO m : movies) {
            cbMovies.addItem(m);
        }
    }

    /**
     * Tải danh sách suất chiếu bất đồng bộ (SwingWorker) để tránh block UI.
     * Tuân thủ Frontend Style Guide: Async Processing & Zero Latency.
     */
    private void loadShowtimesData() {
        // 1. Lấy điều kiện lọc (Không cần parse từ text nữa, lấy trực tiếp object)
        LocalDate selectedDate = (LocalDate) cbDate.getSelectedItem();
        if (selectedDate == null) return;

        MovieSummaryDTO selectedMovie = (MovieSummaryDTO) cbMovies.getSelectedItem();
        Long movieId = (selectedMovie != null) ? selectedMovie.id() : null;

        // 2. Xóa danh sách cũ
        listPanel.removeAll();
        listPanel.revalidate();
        listPanel.repaint();

        // 3. Thực thi truy vấn ngầm (Background Thread)
        new SwingWorker<List<ShowtimeSummaryDTO>, Void>() {
            @Override
            protected List<ShowtimeSummaryDTO> doInBackground() throws Exception {
                return ShowtimeService.getInstance().getShowtimesForUI(selectedDate, movieId);
            }

            @Override
            protected void done() {
                try {
                    List<ShowtimeSummaryDTO> results = get();
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
        }.execute();
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
        lblMovie.setFont(new Font("Inter", Font.BOLD, 15));
        lblMovie.setForeground(Color.WHITE);

        // Text Component 2: Khung giờ chiếu (Font to, màu nhấn)
        JLabel lblTime = new JLabel("⏰ " + timeRange);
        lblTime.setFont(new Font("Inter", Font.BOLD, 20));
        lblTime.setForeground(ACCENT_PRIMARY);

        // Text Component 3: Thông số Rạp
        JLabel lblRoom = new JLabel(roomName);
        lblRoom.setFont(new Font("Inter", Font.PLAIN, 14));
        lblRoom.setForeground(TEXT_SECONDARY);

        // Nút Kích hoạt chuyển trang
        JButton btnSelect = new JButton("CHỌN GHẾ");
        btnSelect.setFont(new Font("Inter", Font.BOLD, 14));
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
