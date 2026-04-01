package com.f3cinema.app.service.impl;

import com.f3cinema.app.dto.transaction.PaymentLineDTO;
import com.f3cinema.app.dto.transaction.SnackLineDTO;
import com.f3cinema.app.dto.transaction.TicketLineDTO;
import com.f3cinema.app.dto.transaction.TransactionActionResultDTO;
import com.f3cinema.app.dto.transaction.TransactionDetailDTO;
import com.f3cinema.app.dto.transaction.TransactionSearchRequest;
import com.f3cinema.app.dto.transaction.TransactionSearchResult;
import com.f3cinema.app.entity.Invoice;
import com.f3cinema.app.entity.enums.InvoiceStatus;
import com.f3cinema.app.entity.enums.PaymentStatus;
import com.f3cinema.app.repository.InvoiceRepository;
import com.f3cinema.app.repository.InvoiceRepositoryImpl;
import com.f3cinema.app.service.TransactionHistoryService;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class TransactionHistoryServiceImpl implements TransactionHistoryService {
    private static final TransactionHistoryServiceImpl INSTANCE = new TransactionHistoryServiceImpl();
    private final InvoiceRepository invoiceRepository = new InvoiceRepositoryImpl();

    private TransactionHistoryServiceImpl() {
    }

    public static TransactionHistoryServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public TransactionSearchResult searchTransactions(TransactionSearchRequest request) {
        TransactionSearchRequest safe = request == null
                ? new TransactionSearchRequest(null, null, null, null, null, null, 0, 20)
                : request;

        List<com.f3cinema.app.dto.transaction.TransactionRowDTO> rows = invoiceRepository.searchRows(
                safe.keyword(),
                safe.fromDate(),
                safe.toDate(),
                safe.invoiceStatus(),
                safe.paymentStatus(),
                safe.staffId(),
                safe.offset(),
                safe.limit()
        );
        long total = invoiceRepository.countRows(
                safe.keyword(),
                safe.fromDate(),
                safe.toDate(),
                safe.invoiceStatus(),
                safe.paymentStatus(),
                safe.staffId()
        );
        return new TransactionSearchResult(rows, total, safe.offset(), safe.limit());
    }

    @Override
    public TransactionDetailDTO getTransactionDetail(Long invoiceId) {
        Invoice invoice = invoiceRepository.findDetailHeaderById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn #" + invoiceId));

        List<TicketLineDTO> tickets = invoiceRepository.findTicketLinesByInvoiceId(invoiceId);
        List<SnackLineDTO> snacks = invoiceRepository.findSnackLinesByInvoiceId(invoiceId);
        List<PaymentLineDTO> payments = invoiceRepository.findPaymentLinesByInvoiceId(invoiceId);

        String customerName = invoice.getCustomer() != null ? invoice.getCustomer().getFullName() : null;
        String customerPhone = invoice.getCustomer() != null ? invoice.getCustomer().getPhone() : null;
        String staffName = invoice.getUser() != null ? invoice.getUser().getFullName() : null;

        return new TransactionDetailDTO(
                invoice.getId(),
                invoice.getCreatedAt(),
                invoice.getStatus(),
                customerName,
                customerPhone,
                staffName,
                invoice.getTotalAmount(),
                invoice.getPointsUsed(),
                invoice.getPointsEarned(),
                tickets,
                snacks,
                payments
        );
    }

    @Override
    public TransactionActionResultDTO cancelInvoice(Long invoiceId, String reason, Long actorUserId) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy.");
        }

        TransactionDetailDTO detail = getTransactionDetail(invoiceId);
        if (detail.invoiceStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Hóa đơn đã hủy trước đó.");
        }
        boolean hasCompletedPayment = detail.payments().stream().anyMatch(p -> p.status() == PaymentStatus.COMPLETED);
        if (hasCompletedPayment) {
            throw new IllegalStateException("Đơn đã thanh toán, vui lòng dùng chức năng hoàn tiền.");
        }

        invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.CANCELLED);
        invoiceRepository.updatePaymentsStatusByInvoiceId(invoiceId, PaymentStatus.FAILED);
        log.warn("Invoice #{} cancelled by userId={} reason={}", invoiceId, actorUserId, reason);
        return new TransactionActionResultDTO(invoiceId, InvoiceStatus.CANCELLED, PaymentStatus.FAILED, "Đã hủy hóa đơn.");
    }

    @Override
    public TransactionActionResultDTO refundInvoice(Long invoiceId, String reason, Long actorUserId) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hoàn tiền.");
        }

        TransactionDetailDTO detail = getTransactionDetail(invoiceId);
        if (detail.invoiceStatus() != InvoiceStatus.PAID) {
            throw new IllegalStateException("Chỉ hoàn tiền cho hóa đơn ở trạng thái PAID.");
        }
        boolean hasCompletedPayment = detail.payments().stream().anyMatch(p -> p.status() == PaymentStatus.COMPLETED);
        if (!hasCompletedPayment) {
            throw new IllegalStateException("Không tìm thấy giao dịch thanh toán COMPLETED để hoàn tiền.");
        }

        invoiceRepository.updateInvoiceStatus(invoiceId, InvoiceStatus.CANCELLED);
        invoiceRepository.updatePaymentsStatusByInvoiceId(invoiceId, PaymentStatus.FAILED);
        log.warn("Invoice #{} refunded by userId={} reason={}", invoiceId, actorUserId, reason);
        return new TransactionActionResultDTO(invoiceId, InvoiceStatus.CANCELLED, PaymentStatus.FAILED, "Hoàn tiền thành công.");
    }
}
