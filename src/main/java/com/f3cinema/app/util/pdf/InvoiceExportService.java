package com.f3cinema.app.util.pdf;

import com.f3cinema.app.dto.transaction.PaymentLineDTO;
import com.f3cinema.app.dto.transaction.SnackLineDTO;
import com.f3cinema.app.dto.transaction.TicketLineDTO;
import com.f3cinema.app.dto.transaction.TransactionDetailDTO;
import com.f3cinema.app.entity.enums.PaymentMethod;
import com.f3cinema.app.entity.enums.PaymentStatus;
import com.f3cinema.app.repository.InvoiceRepositoryImpl;
import com.f3cinema.app.util.InvoiceCodeFormatter;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Xuất PDF hóa đơn — bố cục bảng, căn lề, phông Unicode khi có {@code /fonts/NotoSans-Regular.ttf}.
 */
public class InvoiceExportService {
    private static final InvoiceExportService INSTANCE = new InvoiceExportService();
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
            float margin = 40f;
            Document document = new Document(PageSize.A4, margin, margin, margin, margin);
            PdfWriter.getInstance(document, new FileOutputStream(targetFile.toFile()));
            document.open();

            Font titleFont = font(16, Font.BOLD);
            Font headFont = font(10, Font.BOLD);
            Font normal = font(10, Font.NORMAL);
            Font small = font(9, Font.NORMAL);

            String code = resolveInvoiceCode(detail);
            document.add(new Paragraph("HÓA ĐƠN BÁN HÀNG", titleFont));
            document.add(new Paragraph(" ", small));

            PdfPTable top = new PdfPTable(2);
            top.setWidthPercentage(100);
            top.setWidths(new float[]{1.2f, 1f});

            PdfPCell left = new PdfPCell();
            left.setBorder(PdfPCell.NO_BORDER);
            left.addElement(new Paragraph("F3 CINEMA", headFont));
            left.addElement(new Paragraph("Địa chỉ: Trụ sở rạp chiếu F3", small));
            left.addElement(new Paragraph("Hotline: 1900 xxxx · MST: xxxxxxxxxx", small));

            PdfPCell right = new PdfPCell();
            right.setBorder(PdfPCell.NO_BORDER);
            // Không dùng setHorizontalAlignment(ALIGN_RIGHT) trên PdfPCell khi add Paragraph — OpenPDF có thể không vẽ text.
            Paragraph pLbl = new Paragraph("Mã hóa đơn", small);
            pLbl.setAlignment(Element.ALIGN_RIGHT);
            right.addElement(pLbl);
            Paragraph pCode = new Paragraph(code, font(14, Font.BOLD));
            pCode.setAlignment(Element.ALIGN_RIGHT);
            right.addElement(pCode);
            Paragraph pDate = new Paragraph(
                    "Ngày lập: " + (detail.createdAt() != null ? DT.format(detail.createdAt()) : "—"), normal);
            pDate.setAlignment(Element.ALIGN_RIGHT);
            right.addElement(pDate);
            Paragraph pSt = new Paragraph("Trạng thái: " + labelInvoice(detail.invoiceStatus()), normal);
            pSt.setAlignment(Element.ALIGN_RIGHT);
            right.addElement(pSt);

            top.addCell(left);
            top.addCell(right);
            document.add(top);

            document.add(new Paragraph(" ", normal));
            PdfPTable buyer = new PdfPTable(2);
            buyer.setWidthPercentage(100);
            buyer.setWidths(new float[]{0.35f, 0.65f});
            addMetaRow(buyer, "Khách hàng", val(detail.customerName(), "Khách lẻ"), normal);
            addMetaRow(buyer, "Số điện thoại", val(detail.customerPhone(), "—"), normal);
            addMetaRow(buyer, "Nhân viên thu ngân", val(detail.staffName(), "—"), normal);
            addMetaRow(buyer, "ID tham chiếu", "#" + detail.invoiceId(), small);
            document.add(buyer);

            document.add(new Paragraph(" ", normal));

            List<TicketLineDTO> tickets = detail.tickets() != null ? detail.tickets() : List.of();
            if (!tickets.isEmpty()) {
                document.add(new Paragraph("Chi tiết vé xem phim", headFont));
                PdfPTable tTable = new PdfPTable(new float[]{3.2f, 1.2f, 1.8f, 0.8f, 1.5f});
                tTable.setWidthPercentage(100);
                tTable.setSpacingBefore(6);
                tTable.setSpacingAfter(10);
                addHeaderCell(tTable, "Tên phim", headFont);
                addHeaderCell(tTable, "Phòng", headFont);
                addHeaderCell(tTable, "Giờ chiếu", headFont);
                addHeaderCell(tTable, "Ghế", headFont);
                addHeaderCell(tTable, "Thành tiền (đ)", headFont);
                for (TicketLineDTO t : tickets) {
                    tTable.addCell(cell(val(t.movieTitle(), "—"), normal));
                    tTable.addCell(cell(val(t.roomName(), "—"), normal));
                    tTable.addCell(cell(t.startTime() != null ? DT.format(t.startTime()) : "—", normal));
                    tTable.addCell(cell(val(t.seatLabel(), "—"), normal));
                    tTable.addCell(cellMoney(t.finalPrice(), normal));
                }
                document.add(tTable);
            }

            List<SnackLineDTO> snacks = detail.snacks() != null ? detail.snacks() : List.of();
            if (!snacks.isEmpty()) {
                document.add(new Paragraph("Chi tiết đồ ăn / đồ uống", headFont));
                PdfPTable sTable = new PdfPTable(new float[]{3.5f, 0.6f, 1.4f, 1.5f});
                sTable.setWidthPercentage(100);
                sTable.setSpacingBefore(6);
                sTable.setSpacingAfter(10);
                addHeaderCell(sTable, "Sản phẩm", headFont);
                addHeaderCell(sTable, "SL", headFont);
                addHeaderCell(sTable, "Đơn giá (đ)", headFont);
                addHeaderCell(sTable, "Thành tiền (đ)", headFont);
                for (SnackLineDTO s : snacks) {
                    sTable.addCell(cell(val(s.productName(), "—"), normal));
                    sTable.addCell(cell(String.valueOf(s.quantity() != null ? s.quantity() : 0), normal));
                    sTable.addCell(cellMoney(s.unitPrice(), normal));
                    sTable.addCell(cellMoney(s.lineTotal(), normal));
                }
                document.add(sTable);
            }

            List<PaymentLineDTO> pays = detail.payments() != null ? detail.payments() : List.of();
            if (!pays.isEmpty()) {
                document.add(new Paragraph("Thanh toán", headFont));
                PdfPTable pTable = new PdfPTable(new float[]{1.4f, 1.2f, 2f, 1.4f});
                pTable.setWidthPercentage(100);
                pTable.setSpacingBefore(6);
                pTable.setSpacingAfter(10);
                addHeaderCell(pTable, "Phương thức", headFont);
                addHeaderCell(pTable, "Trạng thái", headFont);
                addHeaderCell(pTable, "Mã giao dịch", headFont);
                addHeaderCell(pTable, "Số tiền (đ)", headFont);
                for (PaymentLineDTO p : pays) {
                    pTable.addCell(cell(paymentMethodVi(p.method()), normal));
                    pTable.addCell(cell(paymentStatusVi(p.status()), normal));
                    pTable.addCell(cell(val(p.transactionId(), "—"), normal));
                    pTable.addCell(cellMoney(p.amount(), normal));
                }
                document.add(pTable);
            }

            PdfPTable total = new PdfPTable(1);
            total.setWidthPercentage(100);
            PdfPCell tot = new PdfPCell();
            tot.setBorder(PdfPCell.BOX);
            tot.setPadding(12);
            tot.addElement(new Paragraph("Tổng thanh toán", headFont));
            tot.addElement(new Paragraph(MONEY.format(detail.totalAmount() != null ? detail.totalAmount() : BigDecimal.ZERO) + " VNĐ",
                    font(18, Font.BOLD)));
            tot.addElement(new Paragraph(
                    "Điểm sử dụng / Tích lũy: " + nz(detail.pointsUsed()) + " / " + nz(detail.pointsEarned()) + " điểm",
                    small));
            total.addCell(tot);
            document.add(total);

            document.add(new Paragraph(" ", small));
            document.add(new Paragraph("Cảm ơn quý khách. Vui lòng kiểm tra thông tin trước khi rời quầy.", small));

            document.close();
            return targetFile;
        } catch (Exception e) {
            throw new RuntimeException("Xuất PDF thất bại: " + e.getMessage(), e);
        }
    }

    private static void addMetaRow(PdfPTable table, String k, String v, Font f) {
        PdfPCell c1 = new PdfPCell(new Phrase(k + ":", f));
        c1.setBorder(PdfPCell.NO_BORDER);
        c1.setPadding(2);
        PdfPCell c2 = new PdfPCell(new Phrase(v, f));
        c2.setBorder(PdfPCell.NO_BORDER);
        c2.setPadding(2);
        table.addCell(c1);
        table.addCell(c2);
    }

    private static void addHeaderCell(PdfPTable table, String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(new Color(241, 245, 249));
        c.setPadding(6);
        table.addCell(c);
    }

    private static PdfPCell cell(String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setPadding(6);
        return c;
    }

    private static PdfPCell cellMoney(BigDecimal amount, Font f) {
        String s = MONEY.format(amount != null ? amount : BigDecimal.ZERO);
        PdfPCell c = new PdfPCell(new Phrase(s, f));
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c.setPadding(6);
        return c;
    }

    /**
     * Đảm bảo luôn có mã hiển thị: ưu tiên DTO, nếu trống thì tính lại F3-yyyyMMdd-XXX từ DB (cùng logic màn chi tiết).
     */
    private static String resolveInvoiceCode(TransactionDetailDTO detail) {
        String raw = detail.invoiceCode();
        if (raw != null && !raw.isBlank()) {
            return raw.trim();
        }
        if (detail.invoiceId() != null && detail.createdAt() != null) {
            try {
                InvoiceRepositoryImpl repo = new InvoiceRepositoryImpl();
                int seq = repo.countInvoicesSameCalendarDayWithIdUpTo(detail.invoiceId(), detail.createdAt());
                PaymentMethod pm = null;
                if (detail.payments() != null && !detail.payments().isEmpty()) {
                    pm = detail.payments().get(0).method();
                }
                return InvoiceCodeFormatter.format(detail.createdAt().toLocalDate(), seq, pm);
            } catch (Exception ignored) {
            }
        }
        return detail.invoiceId() != null ? ("#" + detail.invoiceId()) : "—";
    }

    private static String val(String s, String fb) {
        return s == null || s.isBlank() ? fb : s;
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }

    private static String labelInvoice(com.f3cinema.app.entity.enums.InvoiceStatus s) {
        if (s == null) return "—";
        return switch (s) {
            case PAID -> "Đã thanh toán";
            case PENDING -> "Chờ thanh toán";
            case CANCELLED -> "Đã hủy";
        };
    }

    private static String paymentMethodVi(PaymentMethod m) {
        if (m == null) return "—";
        return switch (m) {
            case CASH -> "Tiền mặt";
            case BANK_TRANSFER -> "Chuyển khoản";
            default -> "Khác";
        };
    }

    private static String paymentStatusVi(PaymentStatus s) {
        if (s == null) return "—";
        return switch (s) {
            case COMPLETED -> "Hoàn tất";
            case PENDING -> "Chờ xử lý";
            case FAILED -> "Thất bại";
            default -> "Khác";
        };
    }

    private static Font font(float size, int style) {
        try (InputStream is = InvoiceExportService.class.getResourceAsStream("/fonts/NotoSans-Regular.ttf")) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                BaseFont bf = BaseFont.createFont("NotoSans-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
                return new Font(bf, size, style);
            }
        } catch (Exception ignored) {
        }
        try {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            return new Font(bf, size, style);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
