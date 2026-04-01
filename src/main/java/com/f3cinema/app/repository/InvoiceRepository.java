package com.f3cinema.app.repository;

import com.f3cinema.app.dto.transaction.PaymentLineDTO;
import com.f3cinema.app.dto.transaction.SnackLineDTO;
import com.f3cinema.app.dto.transaction.TicketLineDTO;
import com.f3cinema.app.dto.transaction.TransactionRowDTO;
import com.f3cinema.app.entity.Invoice;
import com.f3cinema.app.entity.enums.InvoiceStatus;
import com.f3cinema.app.entity.enums.PaymentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends BaseRepository<Invoice, Long> {

    /**
     * Đếm hóa đơn đã thanh toán không gắn khách hàng thành viên (khách vãng lai).
     * {@code fromInclusive} / {@code toExclusive} null = không lọc theo cạnh đó (khoảng mở).
     */
    long countWalkInPaidInvoices(LocalDate fromInclusive, LocalDate toExclusive);
    List<TransactionRowDTO> searchRows(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            InvoiceStatus invoiceStatus,
            PaymentStatus paymentStatus,
            Long staffId,
            int offset,
            int limit
    );

    long countRows(
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            InvoiceStatus invoiceStatus,
            PaymentStatus paymentStatus,
            Long staffId
    );

    Optional<Invoice> findDetailHeaderById(Long invoiceId);

    List<TicketLineDTO> findTicketLinesByInvoiceId(Long invoiceId);

    List<SnackLineDTO> findSnackLinesByInvoiceId(Long invoiceId);

    List<PaymentLineDTO> findPaymentLinesByInvoiceId(Long invoiceId);

    void updateInvoiceStatus(Long invoiceId, InvoiceStatus status);

    void updatePaymentsStatusByInvoiceId(Long invoiceId, PaymentStatus status);
}
