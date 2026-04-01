package com.f3cinema.app.ui.staff;

import com.f3cinema.app.ui.dashboard.BaseDashboardModule;
import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.dto.transaction.TransactionDetailDTO;
import com.f3cinema.app.dto.transaction.TransactionRowDTO;
import com.f3cinema.app.dto.transaction.TransactionSearchRequest;
import com.f3cinema.app.entity.enums.InvoiceStatus;
import com.f3cinema.app.entity.enums.PaymentStatus;
import com.f3cinema.app.service.TransactionHistoryService;
import com.f3cinema.app.service.impl.TransactionHistoryServiceImpl;
import com.f3cinema.app.ui.staff.components.TransactionCard;
import com.f3cinema.app.ui.staff.components.TransactionDetailPanel;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.util.SessionManager;
import com.f3cinema.app.util.pdf.InvoiceExportService;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionHistoryPanel extends BaseDashboardModule {
    private static final int PAGE_SIZE = 20;

    private final TransactionHistoryService historyService = TransactionHistoryServiceImpl.getInstance();
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cbInvoiceStatus = new JComboBox<>(new String[]{"Tất cả hóa đơn", "PAID", "PENDING", "CANCELLED"});
    private final JComboBox<String> cbPaymentStatus = new JComboBox<>(new String[]{"Tất cả thanh toán", "COMPLETED", "PENDING", "FAILED"});
    private final JComboBox<String> cbStaff = new JComboBox<>(new String[]{"Tất cả nhân viên"});
    private final JSpinner spFrom = new JSpinner(new SpinnerDateModel());
    private final JSpinner spTo = new JSpinner(new SpinnerDateModel());
    private final JButton btnRefresh = new JButton("Làm mới");

    private JPanel timelineContainer;
    private Long selectedInvoiceId;
    private TransactionDetailDTO selectedDetail;

    public TransactionHistoryPanel() {
        super("Lịch sử giao dịch", "Trang chủ / Giao dịch");
        initUI();
        bindEvents();
        loadTimeline();
    }

    private void initUI() {
        contentBody.setLayout(new BorderLayout(0, 12));

        JPanel toolbar = buildToolbar();
        contentBody.add(toolbar, BorderLayout.NORTH);

        timelineContainer = new JPanel();
        timelineContainer.setLayout(new BoxLayout(timelineContainer, BoxLayout.Y_AXIS));
        timelineContainer.setOpaque(false);
        timelineContainer.setBorder(new EmptyBorder(0, 24, 16, 24));
        JScrollPane scroll = new JScrollPane(timelineContainer);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.decode("#0F172A"));
        contentBody.add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildToolbar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 24, 4, 24));

        JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        control.setOpaque(true);
        control.setBackground(ThemeConfig.BG_CARD);
        control.setBorder(new EmptyBorder(12, 12, 12, 12));
        control.putClientProperty(FlatClientProperties.STYLE, "arc: 20");

        txtSearch.setPreferredSize(new Dimension(220, 36));
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mã đơn / KH / SĐT");
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #0F172A; borderColor: #334155;");
        styleCombo(cbInvoiceStatus, 145);
        styleCombo(cbPaymentStatus, 155);
        styleCombo(cbStaff, 145);
        styleDateSpinner(spFrom);
        styleDateSpinner(spTo);

        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #6366F1; borderWidth: 0; foreground: #FFFFFF");

        control.add(txtSearch);
        control.add(cbInvoiceStatus);
        control.add(cbPaymentStatus);
        control.add(cbStaff);
        control.add(spFrom);
        control.add(spTo);
        control.add(btnRefresh);
        panel.add(control, BorderLayout.CENTER);
        return panel;
    }

    private void styleCombo(JComboBox<String> combo, int width) {
        combo.setPreferredSize(new Dimension(width, 36));
        combo.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #0F172A; borderColor: #334155;");
    }

    private void styleDateSpinner(JSpinner spinner) {
        spinner.setPreferredSize(new Dimension(130, 36));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
    }

    private void bindEvents() {
        btnRefresh.addActionListener(e -> loadTimeline());
        txtSearch.addActionListener(e -> loadTimeline());
        cbInvoiceStatus.addActionListener(e -> loadTimeline());
        cbPaymentStatus.addActionListener(e -> loadTimeline());
        cbStaff.addActionListener(e -> loadTimeline());
    }

    private void loadTimeline() {
        TransactionSearchRequest request = buildRequest();
        new SwingWorker<List<TransactionRowDTO>, Void>() {
            @Override
            protected List<TransactionRowDTO> doInBackground() {
                return historyService.searchTransactions(request).rows();
            }

            @Override
            protected void done() {
                try {
                    List<TransactionRowDTO> rows = get();
                    refreshTimeline(rows);
                } catch (Exception ex) {
                    AppMessageDialogs.showError(TransactionHistoryPanel.this, "Lỗi", "Không tải được lịch sử: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void refreshTimeline(List<TransactionRowDTO> rows) {
        timelineContainer.removeAll();
        Map<LocalDate, List<TransactionRowDTO>> grouped = rows.stream()
                .collect(Collectors.groupingBy(r -> r.createdAt() != null ? r.createdAt().toLocalDate() : LocalDate.now()));
        grouped.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, List<TransactionRowDTO>>comparingByKey(Comparator.reverseOrder()))
                .forEach(entry -> {
                    timelineContainer.add(createDateHeader(entry.getKey()));
                    timelineContainer.add(Box.createVerticalStrut(6));
                    for (TransactionRowDTO row : entry.getValue()) {
                        timelineContainer.add(new TransactionCard(
                                row,
                                () -> openDetailDialog(row.invoiceId()),
                                () -> {
                                    selectedInvoiceId = row.invoiceId();
                                    try {
                                        selectedDetail = historyService.getTransactionDetail(selectedInvoiceId);
                                        performExport();
                                    } catch (Exception ex) {
                                        AppMessageDialogs.showError(this, "Loi", "Khong tai duoc chi tiet de xuat hoa don.");
                                    }
                                },
                                () -> {
                                    selectedInvoiceId = row.invoiceId();
                                    performRefund();
                                }));
                        timelineContainer.add(Box.createVerticalStrut(6));
                    }
                    timelineContainer.add(Box.createVerticalStrut(4));
                });
        timelineContainer.revalidate();
        timelineContainer.repaint();
    }

    private JLabel createDateHeader(LocalDate date) {
        String dateText = date.equals(LocalDate.now()) ? "Hom nay" : date.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy"));
        JLabel header = new JLabel(dateText);
        header.setFont(new Font("Inter", Font.BOLD, 16));
        header.setForeground(Color.decode("#F8FAFC"));
        header.setAlignmentX(LEFT_ALIGNMENT);
        return header;
    }

    private TransactionSearchRequest buildRequest() {
        InvoiceStatus invoiceStatus = null;
        PaymentStatus paymentStatus = null;
        if (cbInvoiceStatus.getSelectedIndex() > 0) {
            invoiceStatus = InvoiceStatus.valueOf((String) cbInvoiceStatus.getSelectedItem());
        }
        if (cbPaymentStatus.getSelectedIndex() > 0) {
            paymentStatus = PaymentStatus.valueOf((String) cbPaymentStatus.getSelectedItem());
        }

        java.util.Date fromDate = (java.util.Date) spFrom.getValue();
        java.util.Date toDate = (java.util.Date) spTo.getValue();
        LocalDate from = new java.sql.Date(fromDate.getTime()).toLocalDate();
        LocalDate to = new java.sql.Date(toDate.getTime()).toLocalDate();

        String keyword = txtSearch.getText() == null || txtSearch.getText().isBlank()
                ? null
                : txtSearch.getText().trim();

        return new TransactionSearchRequest(
                keyword,
                from,
                to,
                invoiceStatus,
                paymentStatus,
                null,
                0,
                PAGE_SIZE
        );
    }

    private void openDetailDialog(Long invoiceId) {
        TransactionDetailDTO detail;
        try {
            detail = historyService.getTransactionDetail(invoiceId);
        } catch (Exception ex) {
            AppMessageDialogs.showError(this, "Lỗi", "Không tải được chi tiết: " + ex.getMessage());
            return;
        }

        selectedInvoiceId = invoiceId;
        selectedDetail = detail;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết hóa đơn #" + invoiceId, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(760, 560);
        dialog.setLocationRelativeTo(this);

        TransactionDetailPanel panel = new TransactionDetailPanel();
        panel.render(detail);
        panel.getBtnExport().setEnabled(true);
        panel.getBtnRefund().setEnabled(detail.invoiceStatus() == InvoiceStatus.PAID);
        panel.getBtnCancel().setEnabled(detail.invoiceStatus() != InvoiceStatus.CANCELLED);

        panel.getBtnCancel().addActionListener(e -> {
            performCancel();
            dialog.dispose();
        });
        panel.getBtnRefund().addActionListener(e -> {
            performRefund();
            dialog.dispose();
        });
        panel.getBtnExport().addActionListener(e -> performExport());

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void performCancel() {
        if (selectedInvoiceId == null) {
            return;
        }
        String reason = AppMessageDialogs.promptInput(this, "Hủy đơn", "Nhập lý do hủy đơn:");
        if (reason == null) {
            return;
        }
        try {
            Long actor = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : null;
            historyService.cancelInvoice(selectedInvoiceId, reason, actor);
            AppMessageDialogs.showInfo(this, "Thành công", "Đã hủy hóa đơn #" + selectedInvoiceId);
            loadTimeline();
        } catch (Exception ex) {
            AppMessageDialogs.showError(this, "Không thể hủy", ex.getMessage());
        }
    }

    private void performRefund() {
        if (selectedInvoiceId == null) {
            return;
        }
        String reason = AppMessageDialogs.promptInput(this, "Hoàn tiền", "Nhập lý do hoàn tiền:");
        if (reason == null) {
            return;
        }
        try {
            Long actor = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : null;
            historyService.refundInvoice(selectedInvoiceId, reason, actor);
            AppMessageDialogs.showInfo(this, "Thành công", "Hoàn tiền thành công cho hóa đơn #" + selectedInvoiceId);
            loadTimeline();
        } catch (Exception ex) {
            AppMessageDialogs.showError(this, "Không thể hoàn tiền", ex.getMessage());
        }
    }

    private void performExport() {
        if (selectedDetail == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("invoice-" + selectedDetail.invoiceId() + ".pdf"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Path output = chooser.getSelectedFile().toPath();
            InvoiceExportService.getInstance().exportInvoice(selectedDetail, output);
            AppMessageDialogs.showInfo(this, "Thành công", "Đã xuất: " + output);
        } catch (Exception ex) {
            AppMessageDialogs.showError(this, "Lỗi xuất PDF", ex.getMessage());
        }
    }
}
