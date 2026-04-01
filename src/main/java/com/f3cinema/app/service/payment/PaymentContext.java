package com.f3cinema.app.service.payment;

import com.f3cinema.app.entity.Invoice;

public class PaymentContext {
    private PaymentStrategy strategy;

    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void processPayment(Invoice invoice) {
        if (strategy == null) {
            throw new IllegalStateException("PaymentStrategy is not set.");
        }
        strategy.processPayment(invoice);
    }
}
