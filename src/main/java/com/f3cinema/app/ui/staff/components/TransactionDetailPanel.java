package com.f3cinema.app.ui.staff.components;

import com.f3cinema.app.dto.transaction.PaymentLineDTO;
import com.f3cinema.app.dto.transaction.SnackLineDTO;
import com.f3cinema.app.dto.transaction.TicketLineDTO;
import com.f3cinema.app.dto.transaction.TransactionDetailDTO;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class TransactionDetailPanel extends JPanel {
    private static final Color TEXT_PRIMARY = Color.decode("#F8FAFC");
    private static final Color TEXT_SECONDARY = Color.decode("#94A3B8");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");

    private final JLabel lblHeader = new JLabel("Chọn giao dịch để xem chi tiết");
    private final JTextPane txtContent = new JTextPane();
    private final JButton btnExport = new JButton("In/Xuất hóa đơn");
    private final JButton btnRefund = new JButton("Hoàn tiền");
    private final JButton btnCancel = new JButton("Hủy đơn");

    public TransactionDetailPanel() {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B; borderColor: #334155; borderWidth: 1");
        setBorder(new EmptyBorder(14, 14, 14, 14));

        lblHeader.setFont(new Font("Inter", Font.BOLD, 15));
        lblHeader.setForeground(TEXT_PRIMARY);

        txtContent.setEditable(false);
        txtContent.setOpaque(false);
        txtContent.setForeground(TEXT_SECONDARY);
        txtContent.setFont(new Font("Inter", Font.PLAIN, 13));
        txtContent.setBorder(new EmptyBorder(0, 0, 0, 0));
        txtContent.setContentType("text/html");
        txtContent.setText("<html>Khong co du lieu</html>");

        JPanel actions = new JPanel(new GridLayout(1, 3, 8, 0));
        actions.setOpaque(false);
        styleButton(btnExport, "#6366F1");
        styleButton(btnRefund, "#F59E0B");
        styleButton(btnCancel, "#EF4444");
        actions.add(btnExport);
        actions.add(btnRefund);
        actions.add(btnCancel);

        add(lblHeader, BorderLayout.NORTH);
        JScrollPane sc = new JScrollPane(txtContent);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.getViewport().setOpaque(false);
        sc.setOpaque(false);
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
            lblHeader.setText("Chọn giao dịch để xem chi tiết");
            txtContent.setText("<html>Khong co du lieu</html>");
            return;
        }
        lblHeader.setText("Hóa đơn #" + detail.invoiceId());
        StringBuilder sb = new StringBuilder("<html><div style='font-family:Inter; color:#F8FAFC'>");
        sb.append("<h2 style='margin:0'>F3 CINEMA</h2>");
        sb.append("<div>Hoa don #").append(detail.invoiceId()).append("</div><hr/>");
        sb.append("<div>Thoi gian: ").append(detail.createdAt() != null ? DATE_TIME_FORMATTER.format(detail.createdAt()) : "N/A").append("</div>");
        sb.append("<div>Trang thai: ").append(detail.invoiceStatus()).append("</div>");
        sb.append("<div>Khach hang: ").append(valueOr(detail.customerName(), "Khach le")).append("</div>");
        sb.append("<div>SDT: ").append(valueOr(detail.customerPhone(), "-")).append("</div>");
        sb.append("<div>Nhan vien: ").append(valueOr(detail.staffName(), "-")).append("</div>");
        sb.append("<div style='margin-top:8px'><b>Tong tien: ").append(formatMoney(detail.totalAmount())).append(" VNĐ</b></div>");
        sb.append("<div>Diem dung/tich: ").append(safeInt(detail.pointsUsed())).append(" / ").append(safeInt(detail.pointsEarned())).append("</div>");
        sb.append("<hr/><b>VE XEM PHIM</b><br/>");
        for (TicketLineDTO t : detail.tickets()) {
            sb.append("• ").append(valueOr(t.movieTitle(), "N/A")).append(" | ")
                    .append(valueOr(t.seatLabel(), "N/A")).append(" | ")
                    .append(formatMoney(t.finalPrice())).append(" VNĐ<br/>");
        }
        sb.append("<br/><b>BAP NUOC</b><br/>");
        for (SnackLineDTO s : detail.snacks()) {
            sb.append("• ").append(valueOr(s.productName(), "N/A")).append(" x")
                    .append(safeInt(s.quantity())).append(" | ")
                    .append(formatMoney(s.lineTotal())).append(" VNĐ<br/>");
        }
        sb.append("<br/><b>THANH TOAN</b><br/>");
        for (PaymentLineDTO p : detail.payments()) {
            sb.append("• ").append(p.method()).append(" | ")
                    .append(p.status()).append(" | ")
                    .append(formatMoney(p.amount())).append(" VNĐ<br/>");
        }
        sb.append("</div></html>");
        txtContent.setText(sb.toString());
        txtContent.setCaretPosition(0);
    }

    private String formatMoney(java.math.BigDecimal amount) {
        return amount == null ? "0" : MONEY.format(amount);
    }

    private String valueOr(String s, String fallback) {
        return s == null || s.isBlank() ? fallback : s;
    }

    private int safeInt(Integer v) {
        return v == null ? 0 : v;
    }
}
