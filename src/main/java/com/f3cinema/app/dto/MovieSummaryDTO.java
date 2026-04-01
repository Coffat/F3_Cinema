package com.f3cinema.app.dto;

/**
 * MovieSummaryDTO - Tinh gọn dữ liệu phim cho các thành phần UI như JComboBox.
 * Tuân thủ DTO Pattern: Không trả về thực thể JPA trực tiếp cho UI.
 */
public record MovieSummaryDTO(
    Long id,
    String title
) {
    @Override
    public String toString() {
        return title; // Để JComboBox hiển thị trực tiếp tên phim
    }
}
