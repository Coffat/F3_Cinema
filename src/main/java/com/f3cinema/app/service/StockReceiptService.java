package com.f3cinema.app.service;

import com.f3cinema.app.dto.StockReceiptDTO;

import com.f3cinema.app.dto.StockReceiptSummaryDTO;
import java.util.List;

public interface StockReceiptService {
    void createReceipt(StockReceiptDTO dto);
    List<StockReceiptSummaryDTO> getAllReceipts();
    StockReceiptDTO getReceiptDetails(Long receiptId);
}
