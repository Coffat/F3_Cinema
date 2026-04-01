package com.f3cinema.app.dto.customer;

/**
 * Lightweight customer row used by staff customer listing.
 */
public record CustomerListItemDTO(
        Long id,
        String fullName,
        String phone,
        Integer points
) {
}
