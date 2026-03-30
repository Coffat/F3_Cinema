package com.f3cinema.app.dto;

/**
 * RoomSummaryDTO — Tinh gọn dữ liệu phòng chiếu cho JComboBox.
 * Tuân thủ DTO Pattern: Không trả về JPA Entity trực tiếp cho UI.
 *
 * @param id       ID phòng
 * @param name     Tên phòng chiếu
 * @param roomType Loại phòng (2D, 3D, IMAX…)
 */
public record RoomSummaryDTO(
    Long id,
    String name,
    String roomType
) {
    @Override
    public String toString() {
        return name + " (" + roomType + ")";
    }
}
