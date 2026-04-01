package com.f3cinema.app.service.payment;

import com.f3cinema.app.entity.Invoice;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;

public class CashPaymentStrategy implements PaymentStrategy {
    @Override
    public void processPayment(Invoice invoice) {
        // Implement cash payment logic (e.g., verifying amounts received vs total, updating invoice status)
        System.out.println("Processing Cash Payment for Invoice ID: " + invoice.getId());
        AppMessageDialogs.showInfo(null, "Thành công", "Thanh toán Tiền mặt thành công!\nHoá đơn " + invoice.getId());
    }
}
