package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.ui.staff.TicketingPanel;
import java.awt.BorderLayout;

public class ShowtimePanel extends BaseDashboardModule {
    public ShowtimePanel() {
        super("Lịch chiếu", "Home > Showtimes");
        
        // Cắm giao diện Ticket (CardLayout Navigatior) vào giữa nội dung
        TicketingPanel ticketingPanel = new TicketingPanel();
        contentBody.add(ticketingPanel, BorderLayout.CENTER);
        
        // TICKETING PANEL TỰ ĐỘNG LOAD SHOWTIME LIST LÀM MẶC ĐỊNH RỒI
    }
}

