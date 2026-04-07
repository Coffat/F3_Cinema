package com.f3cinema.app.util.format;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BankTransferInvoiceCodeStrategy implements InvoiceCodeStrategy {
    private static final DateTimeFormatter DATE_PART = DateTimeFormatter.BASIC_ISO_DATE;

    @Override
    public String generateCode(LocalDate issueDate, int sequenceInDay) {
        int seq = Math.max(1, sequenceInDay);
        return String.format("F3-MM-%s-%03d", issueDate.format(DATE_PART), seq);
    }
}
