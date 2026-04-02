package com.f3cinema.app.ui.staff.components;

import com.f3cinema.app.dto.transaction.PaymentLineDTO;
import com.f3cinema.app.dto.transaction.SnackLineDTO;
import com.f3cinema.app.dto.transaction.TicketLineDTO;
import com.f3cinema.app.dto.transaction.TransactionDetailDTO;
import com.f3cinema.app.entity.enums.InvoiceStatus;
import com.f3cinema.app.entity.enums.PaymentMethod;
import com.f3cinema.app.entity.enums.PaymentStatus;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Chi tiết hóa đơn — bố cục kiểu biên lai / hóa đơn điện tử, tối ưu đọc trên nền tối.
 */
public class TransactionDetailPanel extends JPanel {
    private static final Color TEXT_PRIMARY = Color.decode("#F8FAFC");
    private static final Color TEXT_SECONDARY = Color.decode("#94A3B8");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");

    /** CSS tối giản — JTextPane/HTMLEditorKit hỗ trợ hạn chế, tránh flex/grid. */
    private static final String CSS = """
            <style>
            body { margin:0; padding:0; font-family: sans-serif; color: #e2e8f0; font-size: 13px; }
            .wrap { padding: 2px 0 8px 0; }
            .brand { font-size: 10px; letter-spacing: 4px; color: #818cf8; font-weight: bold; }
            .title { font-size: 18px; font-weight: bold; color: #f8fafc; }
            .sub { font-size: 12px; color: #94a3b8; }
            .inv-num { font-size: 14px; font-weight: bold; color: #a5b4fc; }
            .section { font-size: 11px; color: #64748b; font-weight: bold; margin: 14px 0 6px 0; }
            .meta-k { color: #94a3b8; font-size: 12px; }
            .meta-v { color: #f1f5f9; font-size: 13px; }
            table.meta { width: 100%; border-collapse: collapse; margin: 4px 0; }
            table.meta td { padding: 4px 6px 4px 0; vertical-align: top; }
            table.items { width: 100%; border-collapse: collapse; font-size: 12px; margin-top: 4px; }
            table.items th { text-align: left; color: #64748b; font-size: 11px; font-weight: bold; padding: 6px 4px; border-bottom: 1px solid #334155; }
            table.items td { padding: 7px 4px; border-bottom: 1px solid #1e293b; color: #e2e8f0; }
            td.num { text-align: right; color: #cbd5e1; }
            table.total { width: 100%; background: #0f172a; border: 1px solid #334155; margin-top: 14px; }
            table.total td { padding: 12px 14px; }
            .total-label { font-size: 12px; color: #94a3b8; }
            .total-amount { font-size: 20px; font-weight: bold; color: #34d399; }
            .points { font-size: 12px; color: #94a3b8; margin-top: 8px; }
            .badge-paid { color: #34d399; font-weight: bold; font-size: 12px; }
            .badge-pending { color: #fbbf24; font-weight: bold; font-size: 12px; }
            .badge-cancel { color: #f87171; font-weight: bold; font-size: 12px; }
            .muted { color: #64748b; font-size: 12px; }
            hr { border: 0; border-top: 1px solid #334155; margin: 12px 0; }
            </style>
            """;

    private final JLabel lblHeader = new JLabel("Chi tiết hóa đơn");
    private final JTextPane txtContent = new JTextPane();
    private final JButton btnExport = new JButton("Xuất PDF");
    private final JButton btnRefund = new JButton("Hoàn tiền");
    private final JButton btnCancel = new JButton("Hủy đơn");

    public TransactionDetailPanel() {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B; borderColor: #334155; borderWidth: 1");
        setBorder(new EmptyBorder(16, 18, 16, 18));

        lblHeader.setFont(new Font("Inter", Font.BOLD, 16));
        lblHeader.setForeground(TEXT_PRIMARY);

        txtContent.setEditable(false);
        txtContent.setOpaque(false);
        txtContent.setForeground(TEXT_SECONDARY);
        txtContent.setBorder(new EmptyBorder(0, 0, 0, 0));
        txtContent.setContentType("text/html");
        HTMLEditorKit kit = new HTMLEditorKit();
        txtContent.setEditorKit(kit);
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        txtContent.setDocument(doc);
        txtContent.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        txtContent.setText(wrapBody("<p class='muted'>Chọn một giao dịch để xem chi tiết.</p>"));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        styleButton(btnExport, "#6366F1");
        styleButton(btnRefund, "#F59E0B");
        styleButton(btnCancel, "#EF4444");
        actions.add(btnCancel);
        actions.add(btnRefund);
        actions.add(btnExport);

        add(lblHeader, BorderLayout.NORTH);
        JScrollPane sc = new JScrollPane(txtContent);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.getViewport().setOpaque(false);
        sc.setOpaque(false);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        add(sc, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button, String color) {
        button.setFont(new Font("Inter", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; borderWidth: 0; background: " + color);
    }

    public JButton getBtnExport() {
        return btnExport;
    }

    public JButton getBtnRefund() {
        return btnRefund;
    }

    public JButton getBtnCancel() {
        return btnCancel;
    }

    public void setActionEnabled(boolean enabled) {
        btnExport.setEnabled(enabled);
        btnRefund.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
    }

    public void render(TransactionDetailDTO detail) {
        if (detail == null) {
            lblHeader.setText("Chi tiết hóa đơn");
            txtContent.setText(wrapBody("<p class='muted'>Không có dữ liệu.</p>"));
            return;
        }
        lblHeader.setText(detail.invoiceCode() != null ? detail.invoiceCode() : ("Hóa đơn #" + detail.invoiceId()));
        txtContent.setText(wrapBody(buildDetailHtml(detail)));
        txtContent.setCaretPosition(0);
    }

    private String wrapBody(String innerHtml) {
        return "<html><head>" + CSS + "</head><body><div class='wrap'>" + innerHtml + "</div></body></html>";
    }

    private String buildDetailHtml(TransactionDetailDTO d) {
        StringBuilder sb = new StringBuilder(512);

        sb.append("<div class='brand'>F3 CINEMA</div>");
        sb.append("<div class='title'>Hóa đơn bán hàng</div>");
        sb.append("<div class='sub'>Số hóa đơn <span class='inv-num'>")
                .append(esc(d.invoiceCode() != null ? d.invoiceCode() : ("#" + d.invoiceId())))
                .append("</span></div>");
        sb.append("<div class='muted' style='margin-bottom:8px'>ID tham chiếu hệ thống: #").append(d.invoiceId()).append("</div>");

        sb.append("<table class='meta' border='0'>");
        sb.append("<tr><td width='36%' class='meta-k'>Thời gian lập</td><td class='meta-v'>")
                .append(d.createdAt() != null ? esc(DATE_TIME.format(d.createdAt())) : "—")
                .append("</td></tr>");
        sb.append("<tr><td class='meta-k'>Trạng thái</td><td class='meta-v'>")
                .append(statusBadge(d.invoiceStatus()))
                .append("</td></tr>");
        sb.append("<tr><td class='meta-k'>Khách hàng</td><td class='meta-v'>")
                .append(esc(valueOr(d.customerName(), "Khách lẻ")))
                .append("</td></tr>");
        sb.append("<tr><td class='meta-k'>Số điện thoại</td><td class='meta-v'>")
                .append(esc(valueOr(d.customerPhone(), "—")))
                .append("</td></tr>");
        sb.append("<tr><td class='meta-k'>Nhân viên thu ngân</td><td class='meta-v'>")
                .append(esc(valueOr(d.staffName(), "—")))
                .append("</td></tr>");
        sb.append("</table>");

        sb.append("<hr/>");

        List<TicketLineDTO> tickets = d.tickets() != null ? d.tickets() : List.of();
        if (!tickets.isEmpty()) {
            sb.append("<div class='section'>Vé xem phim</div>");
            sb.append("<table class='items' border='0' cellspacing='0' cellpadding='0'>");
            sb.append("<tr><th>Phim</th><th>Phòng</th><th>Giờ chiếu</th><th>Ghế</th><th align='right'>Thành tiền</th></tr>");
            for (TicketLineDTO t : tickets) {
                String time = t.startTime() != null ? DATE_TIME.format(t.startTime()) : "—";
                sb.append("<tr><td>").append(esc(valueOr(t.movieTitle(), "—")))
                        .append("</td><td>").append(esc(valueOr(t.roomName(), "—")))
                        .append("</td><td>").append(esc(time))
                        .append("</td><td>").append(esc(valueOr(t.seatLabel(), "—")))
                        .append("</td><td class='num'>").append(formatMoney(t.finalPrice())).append(" đ</td></tr>");
            }
            sb.append("</table>");
        }

        List<SnackLineDTO> snacks = d.snacks() != null ? d.snacks() : List.of();
        if (!snacks.isEmpty()) {
            sb.append("<div class='section'>Đồ ăn &amp; đồ uống</div>");
            sb.append("<table class='items' border='0' cellspacing='0' cellpadding='0'>");
            sb.append("<tr><th>Sản phẩm</th><th align='center'>SL</th><th>Đơn giá</th><th align='right'>Thành tiền</th></tr>");
            for (SnackLineDTO s : snacks) {
                sb.append("<tr><td>").append(esc(valueOr(s.productName(), "—")))
                        .append("</td><td>").append(safeInt(s.quantity()))
                        .append("</td><td>").append(formatMoney(s.unitPrice())).append(" đ")
                        .append("</td><td class='num'>").append(formatMoney(s.lineTotal())).append(" đ</td></tr>");
            }
            sb.append("</table>");
        }

        List<PaymentLineDTO> payments = d.payments() != null ? d.payments() : List.of();
        if (!payments.isEmpty()) {
            sb.append("<div class='section'>Thanh toán</div>");
            sb.append("<table class='items' border='0' cellspacing='0' cellpadding='0'>");
            sb.append("<tr><th>Phương thức</th><th>Trạng thái</th><th>Mã giao dịch</th><th align='right'>Số tiền</th></tr>");
            for (PaymentLineDTO p : payments) {
                sb.append("<tr><td>").append(esc(formatPaymentMethod(p.method())))
                        .append("</td><td>").append(esc(formatPaymentStatus(p.status())))
                        .append("</td><td class='muted'>").append(esc(valueOr(p.transactionId(), "—")))
                        .append("</td><td class='num'>").append(formatMoney(p.amount())).append(" đ</td></tr>");
            }
            sb.append("</table>");
        }

        if (tickets.isEmpty() && snacks.isEmpty() && payments.isEmpty()) {
            sb.append("<p class='muted' style='margin-top:8px'>Không có dòng chi tiết.</p>");
        }

        sb.append("<table class='total' border='0' cellspacing='0' cellpadding='0'><tr><td>");
        sb.append("<div class='total-label'>Tổng thanh toán</div>");
        sb.append("<div class='total-amount'>").append(formatMoney(d.totalAmount())).append(" VNĐ</div>");
        sb.append("<div class='points'>Điểm sử dụng / Tích lũy: ")
                .append(safeInt(d.pointsUsed())).append(" / ").append(safeInt(d.pointsEarned())).append(" điểm</div>");
        sb.append("</td></tr></table>");

        return sb.toString();
    }

    private String statusBadge(InvoiceStatus status) {
        if (status == null) {
            return "<span class='badge-pending'>—</span>";
        }
        return switch (status) {
            case PAID -> "<span class='badge-paid'>Đã thanh toán</span>";
            case PENDING -> "<span class='badge-pending'>Chờ thanh toán</span>";
            case CANCELLED -> "<span class='badge-cancel'>Đã hủy</span>";
        };
    }

    private String formatPaymentMethod(PaymentMethod m) {
        if (m == null) return "—";
        return switch (m) {
            case CASH -> "Tiền mặt";
            case BANK_TRANSFER -> "Chuyển khoản / Thẻ";
            case MOMO -> "Ví MoMo";
        };
    }

    private String formatPaymentStatus(PaymentStatus s) {
        if (s == null) return "—";
        return switch (s) {
            case COMPLETED -> "Hoàn tất";
            case PENDING -> "Chờ xử lý";
            case FAILED -> "Thất bại";
        };
    }

    private String formatMoney(BigDecimal amount) {
        return amount == null ? "0" : MONEY.format(amount);
    }

    private String valueOr(String s, String fallback) {
        return s == null || s.isBlank() ? fallback : s;
    }

    private int safeInt(Integer v) {
        return v == null ? 0 : v;
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
