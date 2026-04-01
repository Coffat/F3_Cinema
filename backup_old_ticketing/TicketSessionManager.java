package com.f3cinema.app.service.cart;

import com.f3cinema.app.dto.SeatDTO;

import java.util.ArrayList;
import java.util.List;

public class TicketSessionManager {

    private static final TicketSessionManager INSTANCE = new TicketSessionManager();

    private Long currentShowtimeId;
    private String movieTitle;
    private String roomName;
    private String showtimeTime;
    private List<SeatDTO> selectedSeats = new ArrayList<>();
    private double ticketPrice = 0;

    private TicketSessionManager() {}

    public static TicketSessionManager getInstance() {
        return INSTANCE;
    }

    public void startTicketSession(Long showtimeId, String movieTitle, String roomName, String showtimeTime) {
        this.currentShowtimeId = showtimeId;
        this.movieTitle = movieTitle;
        this.roomName = roomName;
        this.showtimeTime = showtimeTime;
        this.selectedSeats.clear();
        this.ticketPrice = 0;
    }

    public void setSelectedSeats(List<SeatDTO> seats) {
        this.selectedSeats = new ArrayList<>(seats);
        this.ticketPrice = seats.stream().mapToDouble(SeatDTO::price).sum();
    }

    public List<SeatDTO> getSelectedSeats() {
        return new ArrayList<>(selectedSeats);
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public Long getCurrentShowtimeId() {
        return currentShowtimeId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getShowtimeTime() {
        return showtimeTime;
    }

    public boolean hasActiveTicketSession() {
        return currentShowtimeId != null && !selectedSeats.isEmpty();
    }

    public void clearSession() {
        this.currentShowtimeId = null;
        this.movieTitle = null;
        this.roomName = null;
        this.showtimeTime = null;
        this.selectedSeats.clear();
        this.ticketPrice = 0;
    }

    public String getSelectedSeatsDisplay() {
        if (selectedSeats.isEmpty()) {
            return "Chưa có ghế nào";
        }
        return selectedSeats.stream()
                .map(s -> String.format("G%02d", s.number()))
                .collect(java.util.stream.Collectors.joining(", "));
    }
}
