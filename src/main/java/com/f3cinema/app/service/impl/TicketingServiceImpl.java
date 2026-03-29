package com.f3cinema.app.service.impl;

import com.f3cinema.app.dto.SeatDTO;
import com.f3cinema.app.dto.ShowtimeSummaryDTO;
import com.f3cinema.app.service.TicketingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TicketingServiceImpl implements TicketingService {

    private static final TicketingServiceImpl INSTANCE = new TicketingServiceImpl();
    
    // Lưu tạm các ID ghế đã bị người khác mua để test Mock Real-time Update
    private Set<Long> mockSoldSeats = new HashSet<>();

    private TicketingServiceImpl() {
        // Tái tạo bản đồ giả lập giống hệt hình ảnh mẫu: Có một vài ghế bị đỏ đầu tiên
        mockSoldSeats.add(1L); mockSoldSeats.add(2L); mockSoldSeats.add(3L); mockSoldSeats.add(4L);
        mockSoldSeats.add(17L); mockSoldSeats.add(18L); mockSoldSeats.add(19L); mockSoldSeats.add(20L);
        mockSoldSeats.add(129L); mockSoldSeats.add(130L); mockSoldSeats.add(134L);
        mockSoldSeats.add(149L); mockSoldSeats.add(150L); mockSoldSeats.add(160L);
    }

    public static TicketingServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<SeatDTO> getSeatsForShowtime(Long showtimeId) {
        List<SeatDTO> seats = new ArrayList<>();
        int rows = 10;
        int cols = 16;
        long seatIdCounter = 1;

        for (int r = 0; r < rows; r++) {
            for (int c = 1; c <= cols; c++) {
                SeatDTO.SeatType type = SeatDTO.SeatType.NORMAL;
                double price = 50000;
                
                // Giả lập vùng VIP ở giữa giống hình của bạn cung cấp (Màu vàng rực)
                if (r >= 2 && r <= 7 && c >= 4 && c <= 13) {
                    type = SeatDTO.SeatType.VIP;
                    price = 80000;
                }
                
                // Kiểm tra ghế có đang nằm trong danh sách đã chốt đơn chưa
                boolean isSold = mockSoldSeats.contains(seatIdCounter);
                
                seats.add(new SeatDTO(seatIdCounter, "", (int)seatIdCounter, type, price, isSold));
                seatIdCounter++;
            }
        }
        return seats;
    }

    @Override
    public ShowtimeSummaryDTO getShowtimeSummary(Long showtimeId) {
        return new ShowtimeSummaryDTO(
            showtimeId != null ? showtimeId : 101L,
            "Lật Mặt 7: Một Điều Ước",
            "Phòng 1 (IMAX)",
            LocalDateTime.now().plusHours(2), 
            50000.0
        );
    }

    @Override
    public void bookSeats(Long showtimeId, List<Long> seatIds) {
        // Thao tác chốt đơn: Thêm ghế vào HashSet để không ai mua được nữa
        mockSoldSeats.addAll(seatIds);
    }
}
