package com.f3cinema.app.ui.staff;

import com.f3cinema.app.ui.dashboard.BaseDashboardModule;
import com.f3cinema.app.dto.transaction.TransactionDetailDTO;
import com.f3cinema.app.dto.transaction.TransactionRowDTO;
import com.f3cinema.app.dto.transaction.TransactionSearchRequest;
import com.f3cinema.app.entity.enums.InvoiceStatus;
import com.f3cinema.app.entity.enums.PaymentStatus;
import com.f3cinema.app.service.TransactionHistoryService;
import com.f3cinema.app.service.impl.TransactionHistoryServiceImpl;
import com.f3cinema.app.ui.staff.components.TransactionDetailPanel;
import com.f3cinema.app.util.SessionManager;
import com.f3cinema.app.util.pdf.InvoiceExportService;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransactionHistoryPanel extends BaseDashboardModule {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int PAGE_SIZE = 20;

    private final TransactionHistoryService historyService = TransactionHistoryServiceImpl.getInstance();
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cbInvoiceStatus = new JComboBox<>(new String[]{"Tất cả hóa đơn", "PAID", "PENDING", "CANCELLED"});
    private final JComboBox<String> cbPaymentStatus = new JComboBox<>(new String[]{"Tất cả thanh toán", "COMPLETED", "PENDING", "FAILED"});
    private final JComboBox<String> cbStaff = new JComboBox<>(new String[]{"Tất cả nhân viên"});
    private final JSpinner spFrom = new JSpinner(new SpinnerDateModel());
    private final JSpinner spTo = new JSpinner(new SpinnerDateModel());
    private final JButton btnRefresh = new JButton("Làm mới");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Mã đơn", "Thời gian", "Khách hàng", "Nhân viên", "Tổng tiền", "Hóa đơn", "Thanh toán"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private JTable table;
    private Long selectedInvoiceId;
    private TransactionDetailDTO selectedDetail;

    public TransactionHistoryPanel() {
        super("Lịch sử giao dịch", "Trang chủ / Giao dịch");
        initUI();
        bindEvents();
        loadTable();
    }

    private void initUI() {
        contentBody.setLayout(new BorderLayout(0, 12));

        JPanel toolbar = buildToolbar();
        contentBody.add(toolbar, BorderLayout.NORTH);

        table = new JTable(tableModel);
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setForeground(Color.decode("#F8FAFC"));
        table.setBackground(Color.decode("#0F172A"));
        table.getTableHeader().setReorderingAllowed(false);
        table.putClientProperty(FlatClientProperties.STYLE, "showHorizontalLines: true; showVerticalLines: false");

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(Color.decode("#0F172A"));
        tableScroll.putClientProperty(FlatClientProperties.STYLE, "arc: 16");
        contentBody.add(tableScroll, BorderLayout.CENTER);
    }

    private JPanel buildToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 4, 0));

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

        panel.add(txtSearch);
        panel.add(cbInvoiceStatus);
        panel.add(cbPaymentStatus);
        panel.add(cbStaff);
        panel.add(spFrom);
        panel.add(spTo);
        panel.add(btnRefresh);
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
        btnRefresh.addActionListener(e -> loadTable());
        txtSearch.addActionListener(e -> loadTable());
        cbInvoiceStatus.addActionListener(e -> loadTable());
        cbPaymentStatus.addActionListener(e -> loadTable());
        cbStaff.addActionListener(e -> loadTable());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) {
                    return;
                }
                selectedInvoiceId = (Long) tableModel.getValueAt(row, 0);
                openDetailDialog(selectedInvoiceId);
            }
        });
    }

    private void loadTable() {
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
                    tableModel.setRowCount(0);
                    for (TransactionRowDTO r : rows) {
                        tableModel.addRow(new Object[]{
                                r.invoiceId(),
                                r.createdAt() == null ? "-" : DATE_TIME_FORMATTER.format(r.createdAt()),
                                r.customerName(),
                                r.staffName(),
                                r.totalAmount(),
                                r.invoiceStatus(),
                                r.paymentStatus()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TransactionHistoryPanel.this,
                            "Không tải được lịch sử: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
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
            JOptionPane.showMessageDialog(this, "Không tải được chi tiết: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        String reason = JOptionPane.showInputDialog(this, "Nhập lý do hủy đơn:");
        if (reason == null) {
            return;
        }
        try {
            Long actor = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : null;
            historyService.cancelInvoice(selectedInvoiceId, reason, actor);
            JOptionPane.showMessageDialog(this, "Đã hủy hóa đơn #" + selectedInvoiceId, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể hủy", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performRefund() {
        if (selectedInvoiceId == null) {
            return;
        }
        String reason = JOptionPane.showInputDialog(this, "Nhập lý do hoàn tiền:");
        if (reason == null) {
            return;
        }
        try {
            Long actor = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : null;
            historyService.refundInvoice(selectedInvoiceId, reason, actor);
            JOptionPane.showMessageDialog(this, "Hoàn tiền thành công cho hóa đơn #" + selectedInvoiceId, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể hoàn tiền", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Đã xuất: " + output, "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi xuất PDF", JOptionPane.ERROR_MESSAGE);
        }
    }
}
