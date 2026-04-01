package com.f3cinema.app.service.payment;

import com.f3cinema.app.entity.Invoice;

/**
 * Strategy interface for different payment methods.
 */
public interface PaymentStrategy {
    void processPayment(Invoice invoice);
}
