package com.f3cinema.app.ui.staff;

import com.f3cinema.app.util.WrapLayout;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Giai đoạn Thiết kế Midnight Dark Mode cho Màn hình Chọn Suất Chiếu.
 */
public class ShowtimeListView extends JPanel {

    private final TicketingPanel navigator;

    // ── Design Tokens ───────────────────────────────────────────────
    private static final Color BG_MAIN        = new Color(0x0F172A);  // Slate 900
    private static final Color BG_SURFACE     = new Color(0x1E293B);  // Slate 800
    private static final Color TEXT_PRIMARY   = new Color(0xF8FAFC);  // Slate 50
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);  // Slate 400
    private static final Color ACCENT_PRIMARY = new Color(0x6366F1);  // Indigo 500

    public ShowtimeListView(TicketingPanel navigator) {
        this.navigator = navigator;
        initLayout();
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
        lblTitle.setFont(new Font("-apple-system", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_PRIMARY);

        // Ô Text Input chọn Ngày (thay cho JDatePicker tạm)
        JTextField txtDate = new JTextField("28/03/2026");
        txtDate.setPreferredSize(new Dimension(140, 38));
        txtDate.setFont(new Font("-apple-system", Font.BOLD, 14));
        txtDate.setHorizontalAlignment(JTextField.CENTER);
        txtDate.putClientProperty(FlatClientProperties.STYLE, "arc: 12; focusWidth: 2; margin: 4,8,4,8");

        // Combo Box chọn phim
        String[] mockMovies = {"Tất cả các phim", "Lật Mặt 7: Một Điều Ước", "Mai", "Dune 2"};
        JComboBox<String> cbMovies = new JComboBox<>(mockMovies);
        cbMovies.setPreferredSize(new Dimension(240, 38));
        cbMovies.setFont(new Font("-apple-system", Font.PLAIN, 14));
        cbMovies.putClientProperty(FlatClientProperties.STYLE, "arc: 12; focusWidth: 2;");

        // Nút Tìm Kiếm
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setFont(new Font("-apple-system", Font.BOLD, 14));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBackground(ACCENT_PRIMARY);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.setPreferredSize(new Dimension(120, 38));
        btnSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0; focusWidth: 0;");

        topFilterPanel.add(lblTitle);
        topFilterPanel.add(txtDate);
        topFilterPanel.add(cbMovies);
        topFilterPanel.add(btnSearch);

        add(topFilterPanel, BorderLayout.NORTH);

        // ──────────────────────────────────────────────────────────────────
        // 3. Vùng CENTER: Hiển thị Suất Chiếu bằng WrapLayout
        // ──────────────────────────────────────────────────────────────────
        // Lấy WrapLayout đã có sẵn trong dự án thay vì FlowLayout nằm thẳng hàng
        JPanel listPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 24, 24));
        listPanel.setBackground(BG_MAIN);
        listPanel.setOpaque(true); 

        // 4. Render thử Dữ liệu Mock 5 suất chiếu đẹp theo chuẩn Midnight
        listPanel.add(createShowtimeCard(101L, "Lật Mặt 7: Một Điều Ước", "14:00 - 16:30", "Phòng 1 - IMAX"));
        listPanel.add(createShowtimeCard(102L, "Lật Mặt 7: Một Điều Ước", "17:00 - 19:30", "Phòng 1 - IMAX"));
        listPanel.add(createShowtimeCard(103L, "Mai", "18:00 - 20:00", "Phòng 3 - Standard"));
        listPanel.add(createShowtimeCard(104L, "Dune: Hành Tinh Cát 2", "19:00 - 22:15", "Phòng 2 - 4DX"));
        listPanel.add(createShowtimeCard(105L, "Dune: Hành Tinh Cát 2", "19:30 - 22:45", "Phòng 4 - Sweetbox"));

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
