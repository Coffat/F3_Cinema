package com.f3cinema.app.ui.staff;

import javax.swing.*;
import java.awt.*;

/**
 * Giai đoạn Giao Tiếp: TicketingPanel Role đổi thành Container (CardLayout) Manager 
 */
public class TicketingPanel extends JPanel {

    public static final String CARD_SHOWTIME_LIST = "SHOWTIME_LIST";
    public static final String CARD_SEAT_MAP = "SEAT_MAP";

    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    private ShowtimeListView showtimeListView;
    private SeatSelectionView seatSelectionView;
    
    private Runnable onNavigateToSnacks;

    public TicketingPanel() {
        initLayout();
    }
    
    public void setOnNavigateToSnacks(Runnable callback) {
        this.onNavigateToSnacks = callback;
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Core Container để flip qua lại
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setOpaque(false);

        // Khởi tạo các Views
        showtimeListView = new ShowtimeListView(this);
        seatSelectionView = new SeatSelectionView(this);

        // Đổ Views vào CardLayout
        mainContainer.add(showtimeListView, CARD_SHOWTIME_LIST);
        mainContainer.add(seatSelectionView, CARD_SEAT_MAP);

        add(mainContainer, BorderLayout.CENTER);

        // Đặt thẻ mặc định khi màn hình bật lên
        cardLayout.show(mainContainer, CARD_SHOWTIME_LIST);
    }

    /**
     * Navigator trigger: Điều hướng tới Chọn Ghế
     */
    public void navigateToSeatMap(Long showtimeId) {
        seatSelectionView.loadSeatMap(showtimeId);
        cardLayout.show(mainContainer, CARD_SEAT_MAP);
    }

    /**
     * Navigator trigger: Quay lại danh sách suất chiếu
     */
    public void navigateToShowtimes() {
        cardLayout.show(mainContainer, CARD_SHOWTIME_LIST);
    }
    
    public void navigateToSnacks() {
        if (onNavigateToSnacks != null) {
            onNavigateToSnacks.run();
        }
    }
}
