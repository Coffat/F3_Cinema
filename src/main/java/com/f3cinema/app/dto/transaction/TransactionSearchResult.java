package com.f3cinema.app.dto.transaction;

import java.util.List;

public record TransactionSearchResult(
        List<TransactionRowDTO> rows,
        long total,
        int offset,
        int limit
) {
}
