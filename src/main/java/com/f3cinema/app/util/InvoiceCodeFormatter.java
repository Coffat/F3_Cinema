package com.f3cinema.app.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Mã hóa đơn hiển thị: {@code F3-yyyyMMdd-XXX} — XXX là thứ tự hóa đơn trong ngày (theo {@code id} tăng dần).
 */
public final class InvoiceCodeFormatter {

    private static final DateTimeFormatter DATE_PART = DateTimeFormatter.BASIC_ISO_DATE;

    private InvoiceCodeFormatter() {}

    public static String format(LocalDate issueDate, int sequenceInDay) {
        int seq = Math.max(1, sequenceInDay);
        return String.format("F3-%s-%03d", issueDate.format(DATE_PART), seq);
    }
}
