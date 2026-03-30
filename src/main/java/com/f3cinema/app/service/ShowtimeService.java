package com.f3cinema.app.service;

import com.f3cinema.app.dto.ShowtimeSummaryDTO;
import com.f3cinema.app.entity.Showtime;
import com.f3cinema.app.repository.ShowtimeRepository;
import com.f3cinema.app.repository.ShowtimeRepositoryImpl;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ShowtimeService - Xử lý nghiệp vụ liên quan đến suất chiếu.
 * Tuân thủ quy tắc Backend Standards: Sử dụng Singleton Pattern và chuyển đổi sang DTO trước khi trả về UI.
 */
@Log4j2
public class ShowtimeService {

    private static ShowtimeService instance;
    private final ShowtimeRepository showtimeRepository;
    private ShowtimeService() {
        this.showtimeRepository = new ShowtimeRepositoryImpl();
    }

    public static synchronized ShowtimeService getInstance() {
        if (instance == null) {
            instance = new ShowtimeService();
        }
        return instance;
    }

    /**
     * Lấy danh sách suất chiếu rút gọn cho UI theo ngày và phim.
     */
    public List<ShowtimeSummaryDTO> getShowtimesForUI(LocalDate date, Long movieId, Long roomId) {
        log.info("Fetching showtimes for date: {}, movieId: {}, roomId: {}", date, movieId, roomId);
        List<Showtime> showtimes = showtimeRepository.findByFilter(date, movieId, roomId);
        
        return showtimes.stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi từ JPA Entity sang UI DTO (Record).
     */
    private ShowtimeSummaryDTO mapToSummaryDTO(Showtime showtime) {
        return new ShowtimeSummaryDTO(
            showtime.getId(),
            showtime.getMovie().getTitle(),
            showtime.getRoom().getName() + " - " + showtime.getRoom().getRoomType(),
            showtime.getStartTime(),
            showtime.getEndTime(),
            showtime.getBasePrice().doubleValue()
        );
    }

    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }

    public Showtime getShowtimeById(Long id) {
        return showtimeRepository.findById(id).orElse(null);
    }

    public void addShowtime(Showtime showtime) {
        validateShowtime(showtime);
        showtimeRepository.save(showtime);
    }

    public void updateShowtime(Showtime showtime) {
        validateShowtime(showtime);
        showtimeRepository.update(showtime);
    }

    public void deleteShowtime(Long id) {
        Showtime s = getShowtimeById(id);
        if (s != null) {
            showtimeRepository.delete(s);
        }
    }

    private void validateShowtime(Showtime s) {
        // 1. Tự động tính toán endTime
        if (s.getMovie() != null && s.getStartTime() != null) {
            s.setEndTime(s.getStartTime().plusMinutes(s.getMovie().getDuration()));
        }

        // 2. Kiểm tra logic thời gian: Không cho phép đặt suất chiếu trong quá khứ
        if (s.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Thời gian bắt đầu (" + s.getStartTime() + ") không hợp lệ. Bạn không thể tạo suất chiếu trong quá khứ!");
        }

        // 3. Kiểm tra trùng lịch
        if (showtimeRepository.existsConflict(s.getRoom().getId(), s.getStartTime(), s.getEndTime(), s.getId())) {
            throw new RuntimeException("Suất chiếu bị trùng lịch với một suất chiếu khác tại rạp " + s.getRoom().getName());
        }
    }
}
