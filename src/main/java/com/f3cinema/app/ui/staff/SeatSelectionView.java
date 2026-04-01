package com.f3cinema.app.ui.staff;

import com.f3cinema.app.dto.SeatDTO;
import com.f3cinema.app.dto.ShowtimeSummaryDTO;
import com.f3cinema.app.service.TicketingService;
import com.f3cinema.app.service.impl.TicketingServiceImpl;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SeatSelectionView extends JPanel {

    // ── Nền tối ───────────────────────────────────────────────
    private static final Color BG_MAIN = new Color(0x0F172A);
    private static final Color BG_SURFACE = new Color(0x1E293B);
    private static final Color TEXT_PRIMARY = new Color(0xF8FAFC);
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);
    private static final Color ACCENT_PRIMARY = new Color(0x6366F1);
    private static final Color ACCENT_DANGER = new Color(0xEF4444);

    // ── Custom Design Tokens theo chuẩn Hình Mẫu User đưa ───────
    private static final Color COLOR_NORMAL = new Color(0xE2E8F0); // Xám nhạt (Mới tinh)
    private static final Color COLOR_VIP = new Color(0xF59E0B); // Vàng (Hạng VIP)
    private static final Color COLOR_SELECTED = new Color(0x22C55E); // Xanh lá (Đang nhắm)
    private static final Color COLOR_SOLD = new Color(0xEF4444); // Đỏ (Ai đó đã lấy)

    private final TicketingPanel navigator;
    private JPanel seatMapContainer;
    private JPanel invoicePanel;

    private JLabel lblMovieTitle;
    private JLabel lblRoomAndTime;
    private JTextArea txtSelectedSeatsList;
    private JLabel lblTotalPrice;
    private JButton btnPay;

    private TicketingService ticketingService;
    private Long currentShowtimeId; // Theo dõi cờ phiên Giao dịch hiện tại

    // State Management
    private List<SeatDTO> selectedSeats = new ArrayList<>();
    private Map<SeatDTO, JToggleButton> seatButtonsMap = new HashMap<>();

    public SeatSelectionView(TicketingPanel navigator) {
        this.navigator = navigator;
        this.ticketingService = TicketingServiceImpl.getInstance();
        initLayout();
        setupKeyboardBindings();
    }

    private void initLayout() {
        setLayout(new BorderLayout(20, 0));
        setOpaque(false);

        // ---- NORTH BAR (Nút Back) ----
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 16, 0));

        JButton btnBack = new JButton("⬅ Trở về Lịch Chiếu");
        btnBack.setFont(new Font("-apple-system", Font.BOLD, 14));
        btnBack.setForeground(Color.WHITE);
        btnBack.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; margin: 8,16,8,16; background: #1E293B; borderWidth: 0; focusWidth: 0;");
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> navigator.navigateToShowtimes());
        topBar.add(btnBack);

        add(topBar, BorderLayout.NORTH);

        // ---- CENTER: Khu vực Biểu Đồ Ghế & Legend ----
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);

        seatMapContainer = new JPanel();
        seatMapContainer.setBackground(BG_MAIN);

        JScrollPane scrollPane = new JScrollPane(seatMapContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(BG_MAIN);
        scrollPane.getViewport().setBackground(BG_MAIN);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));

        centerWrapper.add(scrollPane, BorderLayout.CENTER);

        // Thêm Chú Thích (Legend) Màu sắc theo chuẩn hình vô đáy Cụm Giữa
        centerWrapper.add(createLegendPanel(), BorderLayout.SOUTH);

        add(centerWrapper, BorderLayout.CENTER);

        // ---- EAST: Hóa đơn Panel ----
        invoicePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        invoicePanel.setOpaque(false);
        invoicePanel.setPreferredSize(new Dimension(350, 0));
        invoicePanel.setLayout(new BorderLayout());
        invoicePanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel topInvoice = new JPanel();
        topInvoice.setLayout(new BoxLayout(topInvoice, BoxLayout.Y_AXIS));
        topInvoice.setOpaque(false);
        lblMovieTitle = new JLabel("Chưa tải...");
        lblMovieTitle.setFont(new Font("-apple-system", Font.BOLD, 20));
        lblMovieTitle.setForeground(TEXT_PRIMARY);
        lblRoomAndTime = new JLabel("-- | --");
        lblRoomAndTime.setFont(new Font("-apple-system", Font.PLAIN, 14));
        lblRoomAndTime.setForeground(TEXT_SECONDARY);
        topInvoice.add(lblMovieTitle);
        topInvoice.add(Box.createVerticalStrut(8));
        topInvoice.add(lblRoomAndTime);
        topInvoice.add(Box.createVerticalStrut(20));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 20));
        sep.setBackground(new Color(0, 0, 0, 0));
        topInvoice.add(sep);
        invoicePanel.add(topInvoice, BorderLayout.NORTH);

        JPanel selectedSeatsPanel = new JPanel(new BorderLayout(0, 8));
        selectedSeatsPanel.setOpaque(false);
        selectedSeatsPanel.setBorder(new EmptyBorder(16, 0, 16, 0));

        JLabel titleSeats = new JLabel("Ghế đã chọn:");
        titleSeats.setFont(new Font("-apple-system", Font.BOLD, 14));
        titleSeats.setForeground(TEXT_PRIMARY);

        txtSelectedSeatsList = new JTextArea("Chưa có ghế nào");
        txtSelectedSeatsList.setFont(new Font("-apple-system", Font.PLAIN, 15));
        txtSelectedSeatsList.setForeground(ACCENT_PRIMARY);
        txtSelectedSeatsList.setOpaque(false);
        txtSelectedSeatsList.setEditable(false);
        txtSelectedSeatsList.setLineWrap(true);
        txtSelectedSeatsList.setWrapStyleWord(true);
        txtSelectedSeatsList.setHighlighter(null);

        selectedSeatsPanel.add(titleSeats, BorderLayout.NORTH);
        selectedSeatsPanel.add(txtSelectedSeatsList, BorderLayout.CENTER);
        invoicePanel.add(selectedSeatsPanel, BorderLayout.CENTER);

        JPanel bottomInvoice = new JPanel(new BorderLayout(0, 16));
        bottomInvoice.setOpaque(false);

        JSeparator sepBottom = new JSeparator();
        sepBottom.setForeground(new Color(255, 255, 255, 20));
        sepBottom.setBackground(new Color(0, 0, 0, 0));

        JPanel priceRow = new JPanel(new BorderLayout());
        priceRow.setOpaque(false);
        priceRow.setBorder(new EmptyBorder(16, 0, 0, 0));

        JLabel lblTotalText = new JLabel("Tổng tiền:");
        lblTotalText.setFont(new Font("-apple-system", Font.PLAIN, 16));
        lblTotalText.setForeground(TEXT_SECONDARY);

        lblTotalPrice = new JLabel("0 VNĐ");
        lblTotalPrice.setFont(new Font("-apple-system", Font.BOLD, 26));
        lblTotalPrice.setForeground(new Color(0xEF4444)); // Rose

        priceRow.add(lblTotalText, BorderLayout.WEST);
        priceRow.add(lblTotalPrice, BorderLayout.EAST);

        btnPay = new JButton("THANH TOÁN (Enter)");
        btnPay.setFont(new Font("-apple-system", Font.BOLD, 16));
        btnPay.setForeground(Color.WHITE);
        btnPay.setBackground(ACCENT_PRIMARY);
        btnPay.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPay.setPreferredSize(new Dimension(0, 50));
        btnPay.putClientProperty(FlatClientProperties.STYLE, "arc: 16; borderWidth: 0; focusWidth: 0;");
        btnPay.addActionListener(e -> checkoutAction());

        JPanel bottomWrapper = new JPanel();
        bottomWrapper.setLayout(new BoxLayout(bottomWrapper, BoxLayout.Y_AXIS));
        bottomWrapper.setOpaque(false);
        bottomWrapper.add(sepBottom);
        bottomWrapper.add(priceRow);
        bottomWrapper.add(Box.createVerticalStrut(20));
        bottomWrapper.add(btnPay);

        bottomInvoice.add(bottomWrapper, BorderLayout.SOUTH);
        invoicePanel.add(bottomInvoice, BorderLayout.SOUTH);
        add(invoicePanel, BorderLayout.EAST);
    }

    /**
     * Dựng hàng Chú Thích Mẫu (Legend)
     */
    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 16));
        legendPanel.setOpaque(false);

        legendPanel.add(createLegendItem("Còn trống", COLOR_NORMAL));
        legendPanel.add(createLegendItem("Ghế VIP", COLOR_VIP));
        legendPanel.add(createLegendItem("Đang chọn", COLOR_SELECTED));
        legendPanel.add(createLegendItem("Đã bán", COLOR_SOLD));

        return legendPanel;
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        item.setOpaque(false);

        JPanel colorBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
        };
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setOpaque(false);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("-apple-system", Font.PLAIN, 14));
        lbl.setForeground(TEXT_PRIMARY);

        item.add(colorBox);
        item.add(lbl);
        return item;
    }

    private void setupKeyboardBindings() {
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "clearSelection");
        actionMap.put("clearSelection", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearSelection();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "checkout");
        actionMap.put("checkout", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                checkoutAction();
            }
        });
    }

    public void loadSeatMap(Long showtimeId) {
        this.currentShowtimeId = showtimeId;
        seatMapContainer.removeAll();
        seatMapContainer.setLayout(new BorderLayout());

        JLabel lblLoading = new JLabel("⏳ Đang tải thông tin suất chiếu và ghế...", SwingConstants.CENTER);
        lblLoading.setFont(new Font("-apple-system", Font.ITALIC, 18));
        lblLoading.setForeground(TEXT_SECONDARY);
        seatMapContainer.add(lblLoading, BorderLayout.CENTER);

        seatMapContainer.revalidate();
        seatMapContainer.repaint();

        SwingWorker<List<SeatDTO>, Void> worker = new SwingWorker<>() {
            private ShowtimeSummaryDTO summary;

            @Override
            protected List<SeatDTO> doInBackground() throws Exception {
                Thread.sleep(300); // Rút ngắn sleep cho mượt
                summary = ticketingService.getShowtimeSummary(showtimeId);
                return ticketingService.getSeatsForShowtime(showtimeId);
            }

            @Override
            protected void done() {
                try {
                    List<SeatDTO> seats = get();
                    renderSummary(summary);
                    renderSeatMap(seats);
                    selectedSeats.clear();
                    updateInvoiceUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void renderSummary(ShowtimeSummaryDTO summary) {
        lblMovieTitle.setText(summary.movieTitle());
        String timeDisplay = String.format("%02d:%02d", summary.startTime().getHour(), summary.startTime().getMinute());
        lblRoomAndTime.setText(summary.roomName() + " | " + timeDisplay);
    }

    private void renderSeatMap(List<SeatDTO> seats) {
        seatMapContainer.removeAll();
        // Áp dụng lưới 10x16 chuẩn Form ảnh User cấp
        seatMapContainer.setLayout(new GridLayout(10, 16, 6, 6));
        seatButtonsMap.clear();

        for (SeatDTO seat : seats) {
            JToggleButton btnSeat = createSeatButton(seat);
            seatButtonsMap.put(seat, btnSeat);
            seatMapContainer.add(btnSeat);
        }
        seatMapContainer.revalidate();
        seatMapContainer.repaint();
    }

    private JToggleButton createSeatButton(SeatDTO seat) {
        // Build số theo cấu trúc Form. "02", "14", "160"
        String defaultLabel = String.format("%02d", seat.number());
        JToggleButton btn = new JToggleButton(defaultLabel);
        btn.setFont(new Font("-apple-system", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8; borderWidth: 0; focusWidth: 0;");

        // Set Màu Nền & Màu Chữ
        Color baseColor = COLOR_NORMAL;
        Color textColor = new Color(0x0F172A); // Chữ đen cho nền xám (giữ độ tương phản)

        if (seat.seatType() == SeatDTO.SeatType.VIP) {
            baseColor = COLOR_VIP;
            textColor = Color.WHITE; // Nền vàng/chữ trắng cho nổi bật
        }

        if (seat.isSold()) {
            btn.setBackground(COLOR_SOLD);
            btn.setForeground(Color.WHITE);
            btn.setEnabled(false);
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "disabledText: #ffffff; disabledBackground: #EF4444; arc: 8; borderWidth: 0;");
        } else {
            btn.setBackground(baseColor);
            btn.setForeground(textColor);

            final Color finalBaseColor = baseColor;
            final Color finalTextColor = textColor;

            // Xử lý Sự kiện Hover/Đánh đấu -> Hiện "X"
            btn.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    btn.setBackground(COLOR_SELECTED); // Xanh lá
                    btn.setForeground(Color.WHITE);
                    btn.setText("X"); // Hiện "X" theo lệnh User
                    selectedSeats.add(seat);
                } else {
                    btn.setBackground(finalBaseColor); // Quay lại màu vốn có
                    btn.setForeground(finalTextColor);
                    btn.setText(defaultLabel); // Quay lại Số gốc
                    selectedSeats.remove(seat);
                }
                updateInvoiceUI();
            });
        }
        return btn;
    }

    private void updateInvoiceUI() {
        if (selectedSeats.isEmpty()) {
            txtSelectedSeatsList.setText("Chưa có ghế nào");
        } else {
            String seatListStr = selectedSeats.stream()
                    .map(s -> String.format("G%02d", s.number()))
                    .collect(Collectors.joining(", "));
            txtSelectedSeatsList.setText(seatListStr);
        }

        double total = selectedSeats.stream().mapToDouble(SeatDTO::price).sum();
        lblTotalPrice.setText(String.format("%,.0f VNĐ", total));
    }

    private void clearSelection() {
        List<SeatDTO> copyToClear = new ArrayList<>(selectedSeats);
        for (SeatDTO seat : copyToClear) {
            JToggleButton btn = seatButtonsMap.get(seat);
            if (btn != null)
                btn.setSelected(false);
        }
    }

    private void checkoutAction() {
        if (selectedSeats.isEmpty()) {
            UIManager.put("OptionPane.messageFont", new Font("-apple-system", Font.PLAIN, 14));
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 ghế trước khi thanh toán!",
                    "Chưa chọn ghế", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total = selectedSeats.stream().mapToDouble(SeatDTO::price).sum();
        UIManager.put("OptionPane.messageFont", new Font("-apple-system", Font.BOLD, 14));
        String msg = String.format("Thanh toán thành công suất chiếu!\nTổng thu: %,.0f VNĐ", total);
        JOptionPane.showMessageDialog(this, msg, "Xác nhận", JOptionPane.INFORMATION_MESSAGE);

        // LƯU CƠ SỞ DỮ LIỆU GIAO DỊCH VÀ ĐỔI MÀU REAL-TIME
        // ------------------------------
        // Trích list Ids của những ghế đang chọn -> Gửi về API Save Backend Simulator
        List<Long> soldIds = selectedSeats.stream().map(SeatDTO::id).collect(Collectors.toList());
        ticketingService.bookSeats(currentShowtimeId, soldIds);

        // Ngay sau khi gọi API, ra lệnh Tải Lại UI toàn cục của phim đó -> Tự quét đỏ
        // ghế
        loadSeatMap(currentShowtimeId);
        // --------------------------------------------------------------------------------
    }
}
