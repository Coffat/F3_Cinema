package com.f3cinema.app.dto.customer;

/**
 * Search criteria for customer list screen.
 */
public record CustomerSearchRequest(
        String query,
        Integer minPoints,
        Integer maxPoints,
        int offset,
        int limit,
        CustomerSort sort
) {
    public CustomerSearchRequest {
        if (offset < 0) {
            offset = 0;
        }
        if (limit <= 0) {
            limit = 30;
        }
        if (sort == null) {
            sort = CustomerSort.NAME_ASC;
        }
    }
}
