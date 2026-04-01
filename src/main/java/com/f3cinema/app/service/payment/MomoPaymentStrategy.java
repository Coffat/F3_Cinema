package com.f3cinema.app.service.payment;

import com.f3cinema.app.entity.Invoice;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;

public class MomoPaymentStrategy implements PaymentStrategy {
    @Override
    public void processPayment(Invoice invoice) {
        // Implement Momo logic (e.g., calling Momo API or generating QR code)
        System.out.println("Processing Momo Payment for Invoice ID: " + invoice.getId());
        AppMessageDialogs.showInfo(null, "Thành công", "Thanh toán Momo thành công!\nHoá đơn " + invoice.getId());
    }
}
