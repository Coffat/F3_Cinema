package com.f3cinema.app.dto.customer;

/**
 * Thống kê giao dịch khách vãng lai: hóa đơn không gắn tài khoản thành viên (customer null).
 */
public record WalkInStats(long totalPaidInvoices, long monthPaidInvoices) {}
