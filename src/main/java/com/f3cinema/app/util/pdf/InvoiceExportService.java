package com.f3cinema.app.util.pdf;

import com.f3cinema.app.dto.transaction.TransactionDetailDTO;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.nio.file.Path;

public class InvoiceExportService {
    private static final InvoiceExportService INSTANCE = new InvoiceExportService();

    private InvoiceExportService() {
    }

    public static InvoiceExportService getInstance() {
        return INSTANCE;
    }

    public Path exportInvoice(TransactionDetailDTO detail, Path targetFile) {
        if (detail == null) {
            throw new IllegalArgumentException("Không có dữ liệu hóa đơn để xuất.");
        }
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(targetFile.toFile()));
            document.open();
            document.add(new Paragraph("F3 Cinema - Hoa don #" + detail.invoiceId()));
            document.add(new Paragraph("Thoi gian: " + detail.createdAt()));
            document.add(new Paragraph("Khach hang: " + (detail.customerName() == null ? "Khach le" : detail.customerName())));
            document.add(new Paragraph("Nhan vien: " + detail.staffName()));
            document.add(new Paragraph("Tong tien: " + detail.totalAmount()));
            document.add(new Paragraph("Trang thai: " + detail.invoiceStatus()));
            document.close();
            return targetFile;
        } catch (Exception e) {
            throw new RuntimeException("Xuất PDF thất bại: " + e.getMessage(), e);
        }
    }
}
