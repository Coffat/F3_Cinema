package com.f3cinema.app.service;

import com.f3cinema.app.dto.SeatDTO;
import com.f3cinema.app.dto.ShowtimeSummaryDTO;
import com.f3cinema.app.entity.enums.PointRedemptionTier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TicketingService {
    List<SeatDTO> getSeatsForShowtime(Long showtimeId);
    ShowtimeSummaryDTO getShowtimeSummary(Long showtimeId);
    
    void bookSeats(Long showtimeId, List<Long> seatIds);
    
    /**
     * Book seats with loyalty program integration.
     * @param showtimeId Showtime ID
     * @param seatIds Selected seat IDs
     * @param snacks Snacks cart items
     * @param customerId Customer ID (nullable for walk-in customers)
     * @param redemptionTier Point redemption tier (nullable if not using points)
     * @return Invoice ID
     */
    Long bookSeatsWithLoyalty(
        Long showtimeId, 
        List<Long> seatIds,
        Map<Long, Integer> snacks,
        Long customerId,
        PointRedemptionTier redemptionTier
    );
}
