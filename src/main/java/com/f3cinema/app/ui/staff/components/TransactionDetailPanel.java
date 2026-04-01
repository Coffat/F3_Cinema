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
    private final JTextArea txtContent = new JTextArea();
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
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setBorder(new EmptyBorder(0, 0, 0, 0));
        txtContent.setText("Không có dữ liệu");

        JPanel actions = new JPanel(new GridLayout(1, 3, 8, 0));
        actions.setOpaque(false);
        styleButton(btnExport, "#6366F1");
        styleButton(btnRefund, "#F59E0B");
        styleButton(btnCancel, "#EF4444");
        actions.add(btnExport);
        actions.add(btnRefund);
        actions.add(btnCancel);

        add(lblHeader, BorderLayout.NORTH);
        add(new JScrollPane(txtContent), BorderLayout.CENTER);
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
            txtContent.setText("Không có dữ liệu");
            return;
        }
        lblHeader.setText("Hóa đơn #" + detail.invoiceId());
        StringBuilder sb = new StringBuilder();
        sb.append("Thời gian: ").append(detail.createdAt() != null ? DATE_TIME_FORMATTER.format(detail.createdAt()) : "N/A").append("\n");
        sb.append("Trạng thái: ").append(detail.invoiceStatus()).append("\n");
        sb.append("Khách hàng: ").append(valueOr(detail.customerName(), "Khách lẻ")).append("\n");
        sb.append("SĐT: ").append(valueOr(detail.customerPhone(), "-")).append("\n");
        sb.append("Nhân viên: ").append(valueOr(detail.staffName(), "-")).append("\n");
        sb.append("Tổng tiền: ").append(formatMoney(detail.totalAmount())).append(" VNĐ\n");
        sb.append("Điểm dùng/tích: ").append(safeInt(detail.pointsUsed())).append(" / ").append(safeInt(detail.pointsEarned())).append("\n\n");

        sb.append("Vé:\n");
        for (TicketLineDTO t : detail.tickets()) {
            sb.append("- ").append(valueOr(t.movieTitle(), "N/A")).append(" | ")
                    .append(valueOr(t.seatLabel(), "N/A")).append(" | ")
                    .append(formatMoney(t.finalPrice())).append(" VNĐ\n");
        }
        sb.append("\nBắp nước:\n");
        for (SnackLineDTO s : detail.snacks()) {
            sb.append("- ").append(valueOr(s.productName(), "N/A")).append(" x")
                    .append(safeInt(s.quantity())).append(" | ")
                    .append(formatMoney(s.lineTotal())).append(" VNĐ\n");
        }
        sb.append("\nThanh toán:\n");
        for (PaymentLineDTO p : detail.payments()) {
            sb.append("- ").append(p.method()).append(" | ")
                    .append(p.status()).append(" | ")
                    .append(formatMoney(p.amount())).append(" VNĐ\n");
        }
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
