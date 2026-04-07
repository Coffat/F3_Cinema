package com.f3cinema.app.util;

import com.f3cinema.app.entity.enums.PaymentMethod;
import com.f3cinema.app.util.format.*;

import java.time.LocalDate;

/**
 * Mã hóa đơn hiển thị (Sử dụng Strategy Pattern): {@code F3-[Phương thức]-[yyyyMMdd]-XXX}.
 */
public final class InvoiceCodeFormatter {

    private InvoiceCodeFormatter() {}

    private static InvoiceCodeStrategy getStrategy(PaymentMethod method) {
        if (method == null) {
            return new UnknownInvoiceCodeStrategy();
        }
        return switch (method) {
            case CASH -> new CashInvoiceCodeStrategy();
            case BANK_TRANSFER -> new BankTransferInvoiceCodeStrategy();
            case CARD -> new CardInvoiceCodeStrategy();
        };
    }

    public static String format(LocalDate issueDate, int sequenceInDay, PaymentMethod method) {
        return getStrategy(method).generateCode(issueDate, sequenceInDay);
    }
}
