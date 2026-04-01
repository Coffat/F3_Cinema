package com.f3cinema.app.service;

import com.f3cinema.app.dto.transaction.TransactionActionResultDTO;
import com.f3cinema.app.dto.transaction.TransactionDetailDTO;
import com.f3cinema.app.dto.transaction.TransactionSearchRequest;
import com.f3cinema.app.dto.transaction.TransactionSearchResult;

public interface TransactionHistoryService {
    TransactionSearchResult searchTransactions(TransactionSearchRequest request);

    TransactionDetailDTO getTransactionDetail(Long invoiceId);

    TransactionActionResultDTO cancelInvoice(Long invoiceId, String reason, Long actorUserId);

    TransactionActionResultDTO refundInvoice(Long invoiceId, String reason, Long actorUserId);
}
