package com.f3cinema.app.ui.staff.ticketing.step2;

import com.f3cinema.app.dto.SeatDTO;
import com.f3cinema.app.dto.ShowtimeSummaryDTO;
import com.f3cinema.app.service.TicketingService;
import com.f3cinema.app.service.impl.TicketingServiceImpl;
import com.f3cinema.app.ui.staff.ticketing.TicketOrderState;
import com.f3cinema.app.ui.staff.ticketing.TicketingFlowPanel;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.staff.ticketing.components.OrderSummaryCard;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

/**
 * Step 2: Seat selection with cinema-standard 2-column layout.
 * Left: Seat map with legend | Right: Order summary card.
 */
public class SeatSelectionPanel extends JPanel {

    private static final Color TEXT_PRIMARY = new Color(0xF8FAFC);
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);
    private static final Color ACCENT_PRIMARY = new Color(0x6366F1);

    private static final Color COLOR_NORMAL = new Color(0xE2E8F0);
    private static final Color COLOR_VIP = new Color(0xF59E0B);
    private static final Color COLOR_SELECTED = new Color(0x22C55E);
    private static final Color COLOR_SOLD = new Color(0xEF4444);

    private final TicketingFlowPanel navigator;
    private final TicketOrderState state;
    private final TicketingService ticketingService;

    private JPanel seatMapContainer;
    private JLabel lblMovieTitle;
    private JLabel lblRoomAndTime;
    private OrderSummaryCard summaryCard;
    private JButton btnNext;

    private Map<SeatDTO, JToggleButton> seatButtonsMap = new HashMap<>();

    public SeatSelectionPanel(TicketingFlowPanel navigator) {
        this.navigator = navigator;
        this.state = TicketOrderState.getInstance();
        this.ticketingService = TicketingServiceImpl.getInstance();

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        initUI();
        attachListeners();
    }

    private void initUI() {
        JPanel contentBody = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59, 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setStroke(new BasicStroke(1.5f));
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 30),
                        0, getHeight(), new Color(255, 255, 255, 5)));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 24, 24);
                g2.dispose();
            }
        };
        contentBody.setLayout(new BorderLayout(20, 16));
        contentBody.setOpaque(false);
        contentBody.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topSection = createTopSection();
        contentBody.add(topSection, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(780);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOpaque(false);

        splitPane.setLeftComponent(createSeatMapArea());
        splitPane.setRightComponent(createRightSidebar());

        contentBody.add(splitPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottomBar.setOpaque(false);

        btnNext = new JButton("Tiếp tục >");
        btnNext.setFont(new Font("Inter", Font.BOLD, 15));
        btnNext.setForeground(Color.WHITE);
        btnNext.setBackground(ACCENT_PRIMARY);
        btnNext.setPreferredSize(new Dimension(160, 44));
        btnNext.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0;");
        btnNext.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNext.setEnabled(false);
        btnNext.addActionListener(e -> navigator.nextStep());

        bottomBar.add(btnNext);
        contentBody.add(bottomBar, BorderLayout.SOUTH);

        add(contentBody, BorderLayout.CENTER);
    }

    private JPanel createTopSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel lblStep = new JLabel("Bước 2/4");
        lblStep.setFont(new Font("Inter", Font.PLAIN, 13));
        lblStep.setForeground(TEXT_SECONDARY);

        lblMovieTitle = new JLabel("---");
        lblMovieTitle.setFont(new Font("Inter", Font.BOLD, 22));
        lblMovieTitle.setForeground(TEXT_PRIMARY);

        lblRoomAndTime = new JLabel("---");
        lblRoomAndTime.setFont(new Font("Inter", Font.PLAIN, 14));
        lblRoomAndTime.setForeground(TEXT_SECONDARY);

        JPanel infoWrapper = new JPanel();
        infoWrapper.setLayout(new BoxLayout(infoWrapper, BoxLayout.Y_AXIS));
        infoWrapper.setOpaque(false);
        infoWrapper.add(lblStep);
        infoWrapper.add(Box.createVerticalStrut(4));
        infoWrapper.add(lblMovieTitle);
        infoWrapper.add(Box.createVerticalStrut(2));
        infoWrapper.add(lblRoomAndTime);

        JButton btnBack = new JButton("Quay lại");
        FlatSVGIcon backIcon = new FlatSVGIcon("icons/arrow-left.svg", 14, 14);
        backIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> TEXT_PRIMARY));
        btnBack.setIcon(backIcon);
        btnBack.setFont(new Font("Inter", Font.PLAIN, 13));
        btnBack.setForeground(TEXT_PRIMARY);
        btnBack.setPreferredSize(new Dimension(120, 36));
        btnBack.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; background: #1E293B; borderWidth: 0;");
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> navigator.previousStep());

        panel.add(infoWrapper, BorderLayout.WEST);
        panel.add(btnBack, BorderLayout.EAST);

        return panel;
    }

    private JPanel createSeatMapArea() {
        JPanel area = new JPanel(new BorderLayout(0, 16));
        area.setOpaque(false);

        JPanel screenWrapper = buildScreenIndicator();

        seatMapContainer = new JPanel();
        seatMapContainer.setOpaque(false);

        JPanel mapWrapper = new JPanel(new GridBagLayout());
        mapWrapper.setOpaque(false);
        mapWrapper.add(seatMapContainer);

        JScrollPane scrollPane = new JScrollPane(mapWrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        area.add(screenWrapper, BorderLayout.NORTH);
        area.add(scrollPane, BorderLayout.CENTER);
        area.add(createLegendPanel(), BorderLayout.SOUTH);

        return area;
    }

    private JPanel createRightSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(320, 0));

        summaryCard = new OrderSummaryCard();

        sidebar.add(summaryCard, BorderLayout.NORTH);

        return sidebar;
    }

    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12));
        panel.setOpaque(false);

        panel.add(createLegendItem("Còn trống", COLOR_NORMAL));
        panel.add(createLegendItem("Ghế VIP", COLOR_VIP));
        panel.add(createLegendItem("Đang chọn", COLOR_SELECTED));
        panel.add(createLegendItem("Đã bán", COLOR_SOLD));

        return panel;
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
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
        colorBox.setPreferredSize(new Dimension(18, 18));
        colorBox.setOpaque(false);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Inter", Font.PLAIN, 13));
        lbl.setForeground(TEXT_PRIMARY);

        item.add(colorBox);
        item.add(lbl);
        return item;
    }

    private void attachListeners() {
        state.addPropertyChangeListener("showtimeId", this::onShowtimeChanged);
        state.addPropertyChangeListener("selectedSeats", this::onSeatsChanged);
    }

    public void onStepActivated() {
        loadSeatMapIfNeeded();
    }

    private void onShowtimeChanged(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(this::loadSeatMapIfNeeded);
    }

    private void onSeatsChanged(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            btnNext.setEnabled(!state.getSelectedSeats().isEmpty());
        });
    }

    private void loadSeatMapIfNeeded() {
        Long showtimeId = state.getShowtimeId();
        if (showtimeId == null) return;

        seatMapContainer.removeAll();
        seatMapContainer.setLayout(new BorderLayout());

        JLabel lblLoading = new JLabel("Đang tải sơ đồ ghế...", SwingConstants.CENTER);
        lblLoading.setFont(new Font("Inter", Font.ITALIC, 16));
        lblLoading.setForeground(TEXT_SECONDARY);
        seatMapContainer.add(lblLoading, BorderLayout.CENTER);
        seatMapContainer.revalidate();
        seatMapContainer.repaint();

        new SwingWorker<LoadResult, Void>() {
            @Override
            protected LoadResult doInBackground() throws Exception {
                ShowtimeSummaryDTO summary = ticketingService.getShowtimeSummary(showtimeId);
                List<SeatDTO> seats = ticketingService.getSeatsForShowtime(showtimeId);
                return new LoadResult(summary, seats);
            }

            @Override
            protected void done() {
                try {
                    LoadResult result = get();
                    renderSummary(result.summary);
                    renderSeatMap(result.seats);
                } catch (Exception e) {
                    AppMessageDialogs.showError(SeatSelectionPanel.this, "Lỗi", "Lỗi khi tải sơ đồ ghế: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void renderSummary(ShowtimeSummaryDTO summary) {
        lblMovieTitle.setText(summary.movieTitle());
        FlatSVGIcon videoIcon = new FlatSVGIcon("icons/video.svg", 16, 16);
        videoIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> ACCENT_PRIMARY));
        lblMovieTitle.setIcon(videoIcon);
        
        String timeDisplay = String.format("%02d:%02d, %02d/%02d/%d", 
                summary.startTime().getHour(), 
                summary.startTime().getMinute(),
                summary.startTime().getDayOfMonth(),
                summary.startTime().getMonthValue(),
                summary.startTime().getYear());
        lblRoomAndTime.setText("📍 " + summary.roomName() + " • " + timeDisplay);
    }

    private void renderSeatMap(List<SeatDTO> seats) {
        seatMapContainer.removeAll();
        seatMapContainer.setLayout(new GridLayout(10, 16, 8, 8));
        seatButtonsMap.clear();

        state.clearSeats();

        for (SeatDTO seat : seats) {
            JToggleButton btnSeat = createSeatButton(seat);
            seatButtonsMap.put(seat, btnSeat);
            seatMapContainer.add(btnSeat);
        }
        
        seatMapContainer.revalidate();
        seatMapContainer.repaint();
    }

    private JToggleButton createSeatButton(SeatDTO seat) {
        String label = String.format("%02d", seat.number());
        JToggleButton btn = new JToggleButton(label);
        btn.setPreferredSize(new Dimension(48, 48));
        btn.setFont(new Font("Inter", Font.BOLD, 11));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8; borderWidth: 0; focusWidth: 0; margin: 0,0,0,0;");

        Color baseColor = seat.seatType() == SeatDTO.SeatType.VIP ? COLOR_VIP : COLOR_NORMAL;
        Color textColor = seat.seatType() == SeatDTO.SeatType.VIP ? Color.WHITE : new Color(0x0F172A);

        if (seat.isSold()) {
            btn.setBackground(COLOR_SOLD);
            btn.setForeground(Color.WHITE);
            btn.setEnabled(false);
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "disabledText: #ffffff; disabledBackground: #EF4444; arc: 8; borderWidth: 0;");
        } else {
            btn.setBackground(baseColor);
            btn.setForeground(textColor);
            btn.setToolTipText(seat.rowChar() + String.valueOf(seat.number()) + " - " + seat.seatType() + " - " + String.format("%,.0f", seat.price()) + " đ");

            final Color finalBase = baseColor;
            final Color finalText = textColor;

            btn.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    btn.setBackground(COLOR_SELECTED);
                    btn.setForeground(Color.WHITE);
                    FlatSVGIcon checkIcon = new FlatSVGIcon("icons/check.svg", 14, 14);
                    checkIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
                    btn.setIcon(checkIcon);
                    btn.setText("");
                    state.addSeat(seat.id(), String.format("G%02d", seat.number()),
                            BigDecimal.valueOf(seat.price()));
                } else {
                    btn.setBackground(finalBase);
                    btn.setForeground(finalText);
                    btn.setIcon(null);
                    btn.setText(label);
                    state.removeSeat(seat.id());
                }
            });
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!btn.isSelected()) {
                        btn.setBorder(BorderFactory.createLineBorder(ACCENT_PRIMARY, 2));
                        btn.setPreferredSize(new Dimension(50, 50));
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!btn.isSelected()) {
                        btn.setBorder(null);
                        btn.setPreferredSize(new Dimension(48, 48));
                    }
                }
            });
        }
        return btn;
    }

    private JPanel buildScreenIndicator() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int[] xPoints = {20, getWidth() - 20, getWidth() - 40, 40};
                int[] yPoints = {0, 0, getHeight(), getHeight()};
                g2.setPaint(new GradientPaint(0, 0, ACCENT_PRIMARY, getWidth(), 0, new Color(129, 140, 248)));
                g2.fillPolygon(xPoints, yPoints, 4);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Inter", Font.BOLD, 12));
                String text = "MAN HINH";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 + 4);
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(0, 40));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.setOpaque(false);
        return panel;
    }

    private record LoadResult(ShowtimeSummaryDTO summary, List<SeatDTO> seats) {}
}
