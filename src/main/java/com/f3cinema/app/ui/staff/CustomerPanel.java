package com.f3cinema.app.ui.staff;

import com.f3cinema.app.ui.dashboard.BaseDashboardModule;
import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.dto.customer.CustomerListItemDTO;
import com.f3cinema.app.dto.customer.CustomerSearchRequest;
import com.f3cinema.app.dto.customer.CustomerSearchResult;
import com.f3cinema.app.dto.customer.CustomerSort;
import com.f3cinema.app.dto.customer.WalkInStats;
import com.f3cinema.app.service.CustomerService;
import com.f3cinema.app.service.impl.CustomerServiceImpl;
import com.f3cinema.app.ui.components.WrapLayout;
import com.f3cinema.app.ui.staff.components.CustomerCardItem;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class CustomerPanel extends BaseDashboardModule {

    private static final int PAGE_SIZE = 30;
    private static final Color TEXT_PRIMARY = Color.decode("#F8FAFC");
    private static final Color TEXT_SECONDARY = Color.decode("#94A3B8");

    private final CustomerService customerService = CustomerServiceImpl.getInstance();

    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cbTierFilter = new JComboBox<>(new String[]{"Tất cả hạng", "Mới", "Đồng", "Bạc", "Vàng"});
    private final JComboBox<String> cbPointFilter = new JComboBox<>(new String[]{"Tất cả điểm", "0+", "500+", "1000+", "2000+"});
    private final JLabel lblResultCount = new JLabel("0 kết quả");
    private final JLabel lblStatTotal = new JLabel("0");
    private final JLabel lblStatNew = new JLabel("0");
    private final JLabel lblStatActive = new JLabel("0");
    private final JLabel lblStatWalkInMonth = new JLabel("0");
    private final JLabel lblStatWalkInTotalLine = new JLabel("Tổng: 0");

    private JPanel cardsPanel;
    private JScrollPane scrollPane;
    private JPanel centerPanel;

    private final Timer searchDebounce;
    private int currentOffset = 0;
    private long totalItems = 0;
    private boolean isLoading = false;
    private boolean appendMode = false;

    public CustomerPanel() {
        super("Quản lý khách hàng", "Trang chủ / Khách hàng");
        searchDebounce = new Timer(280, e -> refreshFromStart());
        searchDebounce.setRepeats(false);
        initUI();
        refreshFromStart();
    }

    private void initUI() {
        contentBody.setLayout(new BorderLayout(0, 14));

        JPanel top = buildToolbar();
        contentBody.add(top, BorderLayout.NORTH);

        cardsPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 12, 12));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(2, 2, 2, 2));

        scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "arc: 16; borderColor: #334155; borderWidth: 1");
        attachInfiniteScroll();

        centerPanel = new JPanel(new CardLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(scrollPane, "LIST");
        centerPanel.add(buildLoadingPanel(), "LOADING");
        centerPanel.add(buildEmptyPanel(), "EMPTY");

        contentBody.add(centerPanel, BorderLayout.CENTER);
        showCenterCard(centerPanel, "LOADING");
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 8));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(12, 24, 4, 24));

        JPanel controlBar = new JPanel(new BorderLayout(12, 0));
        controlBar.setBackground(ThemeConfig.BG_CARD);
        controlBar.setBorder(new EmptyBorder(12, 12, 12, 12));
        controlBar.putClientProperty(FlatClientProperties.STYLE, "arc: 20");

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo tên hoặc SĐT...");
        txtSearch.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; background: #0F172A; borderColor: #334155; foreground: #F8FAFC");
        txtSearch.setPreferredSize(new Dimension(280, 38));
        txtSearch.addActionListener(e -> refreshFromStart());
        txtSearch.getDocument().addDocumentListener(new SimpleDocumentListener(() -> searchDebounce.restart()));

        styleCombo(cbTierFilter, 130);
        styleCombo(cbPointFilter, 130);

        cbTierFilter.addActionListener(e -> refreshFromStart());
        cbPointFilter.addActionListener(e -> refreshFromStart());

        left.add(txtSearch);
        left.add(cbTierFilter);
        left.add(cbPointFilter);

        lblResultCount.setFont(new Font("Inter", Font.PLAIN, 13));
        lblResultCount.setForeground(TEXT_SECONDARY);
        lblResultCount.putClientProperty(FlatClientProperties.STYLE,
                "arc: 999; background: #1E293B; borderColor: #334155; borderWidth: 1");
        lblResultCount.setBorder(new EmptyBorder(8, 12, 8, 12));

        controlBar.add(left, BorderLayout.WEST);
        controlBar.add(lblResultCount, BorderLayout.EAST);
        toolbar.add(controlBar, BorderLayout.CENTER);
        toolbar.add(buildStatsRow(), BorderLayout.SOUTH);
        return toolbar;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(10, 0, 0, 0));
        row.add(statCard("Tổng KH", lblStatTotal, "#6366F1"));
        row.add(statCard("Mới tháng nay", lblStatNew, "#10B981"));
        row.add(statCard("Hoạt động", lblStatActive, "#F59E0B"));
        row.add(walkInStatCard());
        return row;
    }

    /**
     * Giao dịch khách vãng lai: hóa đơn đã thanh toán không gắn tài khoản thành viên.
     */
    private JPanel walkInStatCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(Color.decode("#1E293B"));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 14; borderWidth: 1; borderColor: #334155");
        card.setBorder(new EmptyBorder(10, 12, 10, 12));

        lblStatWalkInMonth.setFont(new Font("Inter", Font.BOLD, 18));
        lblStatWalkInMonth.setForeground(Color.decode("#A78BFA"));
        lblStatWalkInTotalLine.setFont(new Font("Inter", Font.PLAIN, 11));
        lblStatWalkInTotalLine.setForeground(TEXT_SECONDARY);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        lblStatWalkInMonth.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblStatWalkInTotalLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(lblStatWalkInMonth);
        center.add(Box.createVerticalStrut(4));
        center.add(lblStatWalkInTotalLine);

        JLabel cap = new JLabel("Khách vãng lai (tháng này)");
        cap.setFont(new Font("Inter", Font.PLAIN, 12));
        cap.setForeground(TEXT_SECONDARY);

        card.add(center, BorderLayout.CENTER);
        card.add(cap, BorderLayout.SOUTH);
        return card;
    }

    private JPanel statCard(String label, JLabel value, String color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(Color.decode("#1E293B"));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 14; borderWidth: 1; borderColor: #334155");
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        value.setFont(new Font("Inter", Font.BOLD, 18));
        value.setForeground(Color.decode(color));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Inter", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SECONDARY);
        card.add(value, BorderLayout.CENTER);
        card.add(lbl, BorderLayout.SOUTH);
        return card;
    }

    private void styleCombo(JComboBox<String> combo, int width) {
        combo.setPreferredSize(new Dimension(width, 38));
        combo.setFont(new Font("Inter", Font.PLAIN, 13));
        combo.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; background: #0F172A; borderColor: #334155; foreground: #F8FAFC");
    }

    private JPanel buildLoadingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel("Đang tải danh sách khách hàng...");
        label.setFont(new Font("Inter", Font.PLAIN, 14));
        label.setForeground(TEXT_SECONDARY);
        panel.add(label);
        return panel;
    }

    private JPanel buildEmptyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel("Không tìm thấy khách hàng phù hợp");
        label.setFont(new Font("Inter", Font.BOLD, 15));
        label.setForeground(TEXT_PRIMARY);
        panel.add(label);
        return panel;
    }

    private void attachInfiniteScroll() {
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (e.getValueIsAdjusting() || isLoading) {
                return;
            }
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            int threshold = 180;
            boolean nearBottom = bar.getValue() + bar.getVisibleAmount() >= bar.getMaximum() - threshold;
            if (nearBottom && currentOffset < totalItems) {
                appendMode = true;
                loadCustomers();
            }
        });
    }

    private void refreshFromStart() {
        if (isLoading) {
            return;
        }
        currentOffset = 0;
        totalItems = 0;
        appendMode = false;
        cardsPanel.removeAll();
        cardsPanel.revalidate();
        cardsPanel.repaint();
        showCenterCard(centerPanel, "LOADING");
        loadCustomers();
    }

    private void loadCustomers() {
        isLoading = true;
        final boolean fetchWalkIn = !appendMode;
        CustomerSearchRequest request = buildRequest();
        new SwingWorker<CustomerSearchResult, Void>() {
            private WalkInStats walkInStats;

            @Override
            protected CustomerSearchResult doInBackground() {
                if (fetchWalkIn) {
                    walkInStats = customerService.walkInInvoiceStats();
                }
                return customerService.searchCustomers(request);
            }

            @Override
            protected void done() {
                try {
                    CustomerSearchResult result = get();
                    if (fetchWalkIn && walkInStats != null) {
                        lblStatWalkInMonth.setText(String.valueOf(walkInStats.monthPaidInvoices()));
                        lblStatWalkInTotalLine.setText("Tổng: " + walkInStats.totalPaidInvoices());
                    }
                    totalItems = result.total();
                    lblResultCount.setText(totalItems + " kết quả");
                    updateStats(result.items(), result.total());
                    if (!appendMode) {
                        cardsPanel.removeAll();
                    }
                    renderCards(result.items());
                    currentOffset = result.offset() + result.items().size();
                    if (cardsPanel.getComponentCount() == 0) {
                        showCenterCard(centerPanel, "EMPTY");
                    } else {
                        showCenterCard(centerPanel, "LIST");
                    }
                } catch (Exception ex) {
                    showCenterCard(centerPanel, "EMPTY");
                    lblResultCount.setText("0 kết quả");
                } finally {
                    isLoading = false;
                    appendMode = false;
                }
            }
        }.execute();
    }

    private void renderCards(List<CustomerListItemDTO> items) {
        for (CustomerListItemDTO item : items) {
            int points = item.points() == null ? 0 : item.points();
            String tier = resolveTier(points);
            cardsPanel.add(new CustomerCardItem(item.fullName(), item.phone(), points, tier));
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void updateStats(List<CustomerListItemDTO> items, long total) {
        lblStatTotal.setText(String.valueOf(total));
        long active = items.stream().filter(i -> (i.points() != null ? i.points() : 0) > 0).count();
        long recent = items.stream().filter(i -> (i.points() != null ? i.points() : 0) >= 500).count();
        lblStatActive.setText(String.valueOf(active));
        lblStatNew.setText(String.valueOf(recent));
    }

    private CustomerSearchRequest buildRequest() {
        String query = txtSearch.getText() != null ? txtSearch.getText().trim() : "";
        Integer minPoints = null;
        Integer maxPoints = null;

        switch (cbPointFilter.getSelectedIndex()) {
            case 1 -> minPoints = 0;
            case 2 -> minPoints = 500;
            case 3 -> minPoints = 1000;
            case 4 -> minPoints = 2000;
            default -> {
            }
        }

        switch (cbTierFilter.getSelectedIndex()) {
            case 1 -> {
                minPoints = max(minPoints, 0);
                maxPoints = 499;
            }
            case 2 -> {
                minPoints = max(minPoints, 500);
                maxPoints = 999;
            }
            case 3 -> {
                minPoints = max(minPoints, 1000);
                maxPoints = 1999;
            }
            case 4 -> minPoints = max(minPoints, 2000);
            default -> {
            }
        }

        return new CustomerSearchRequest(
                query.isEmpty() ? null : query,
                minPoints,
                maxPoints,
                currentOffset,
                PAGE_SIZE,
                CustomerSort.NAME_ASC
        );
    }

    private int max(Integer a, int b) {
        return a == null ? b : Math.max(a, b);
    }

    private String resolveTier(int points) {
        if (points >= 2000) {
            return "Thành viên Vàng";
        }
        if (points >= 1000) {
            return "Thành viên Bạc";
        }
        if (points >= 500) {
            return "Thành viên Đồng";
        }
        return "Thành viên Mới";
    }

    private void showCenterCard(JPanel center, String name) {
        CardLayout cardLayout = (CardLayout) center.getLayout();
        cardLayout.show(center, name);
    }

    private interface DocumentCallback {
        void onChange();
    }

    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final DocumentCallback callback;

        private SimpleDocumentListener(DocumentCallback callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            callback.onChange();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            callback.onChange();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            callback.onChange();
        }
    }
}
