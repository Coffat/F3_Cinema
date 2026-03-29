package com.f3cinema.app.service;

import com.f3cinema.app.dto.SeatDTO;
import com.f3cinema.app.dto.ShowtimeSummaryDTO;

import java.util.List;

public interface TicketingService {
    List<SeatDTO> getSeatsForShowtime(Long showtimeId);
    ShowtimeSummaryDTO getShowtimeSummary(Long showtimeId);
    
    // Giao diện mock API nhận request Đặt Ghế từ UI để lưu trạng thái
    void bookSeats(Long showtimeId, List<Long> seatIds);
}
