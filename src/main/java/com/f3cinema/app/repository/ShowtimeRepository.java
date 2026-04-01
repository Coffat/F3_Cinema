package com.f3cinema.app.repository;

import com.f3cinema.app.entity.Showtime;
import java.time.LocalDate;
import java.util.List;

/**
 * ShowtimeRepository - Quản lý truy vấn dữ liệu suất chiếu.
 */
public interface ShowtimeRepository extends BaseRepository<Showtime, Long> {
    /**
     * Tìm kiếm suất chiếu dựa trên ngày và phim.
     * @param date Ngày cần lọc
     * @param movieId ID của phim (null nếu muốn lấy tất cả phim)
     * @param roomId ID của phòng (null nếu lấy tất cả)
     * @return Danh sách suất chiếu thỏa mãn điều kiện
     */
    List<Showtime> findByFilter(LocalDate date, Long movieId, Long roomId);

    /**
     * Kiểm tra xem suất chiếu mới có bị trùng lịch với bất kỳ suất nào khác trong cùng phòng không.
     * Logic: start < existing.end AND end > existing.start
     */
    boolean existsConflict(Long roomId, java.time.LocalDateTime start, java.time.LocalDateTime end, Long excludeId);
}
