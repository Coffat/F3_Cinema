package com.f3cinema.app.util.format;

import java.time.LocalDate;

/**
 * Strategy interface for generating invoice codes based on payment method.
 */
public interface InvoiceCodeStrategy {
    String generateCode(LocalDate issueDate, int sequenceInDay);
}
