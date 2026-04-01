package com.f3cinema.app.dto.customer;

import java.util.List;

/**
 * Paged customer search result for staff list UI.
 */
public record CustomerSearchResult(
        List<CustomerListItemDTO> items,
        long total,
        int offset,
        int limit
) {
}
