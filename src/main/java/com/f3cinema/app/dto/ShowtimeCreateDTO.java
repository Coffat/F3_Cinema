package com.f3cinema.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ShowtimeCreateDTO — Gói dữ liệu từ UI gửi xuống Service để tạo/sửa suất chiếu.
 * Tuân thủ DTO Pattern (Backend Standards §3.2): Không truyền JPA Entity lên UI.
 *
 * @param movieId   ID của phim được chọn
 * @param roomId    ID của phòng chiếu được chọn
 * @param startTime Thời điểm bắt đầu suất chiếu
 * @param basePrice Giá vé gốc (VNĐ)
 */
public record ShowtimeCreateDTO(
    Long movieId,
    Long roomId,
    LocalDateTime startTime,
    BigDecimal basePrice
) {}
