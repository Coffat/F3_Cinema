package com.f3cinema.app.repository;

import com.f3cinema.app.entity.Invoice;

public class InvoiceRepositoryImpl extends BaseRepositoryImpl<Invoice, Long> implements InvoiceRepository {
    public InvoiceRepositoryImpl() {
        super(Invoice.class);
    }
}
