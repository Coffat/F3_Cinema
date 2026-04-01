package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.controller.DashboardController;
import com.f3cinema.app.dto.dashboard.DashboardFinance;
import com.f3cinema.app.dto.dashboard.DashboardSnapshot;
import com.f3cinema.app.dto.dashboard.InventoryAlertRow;
import com.f3cinema.app.dto.dashboard.NowShowingRow;
import com.f3cinema.app.dto.dashboard.RevenueSeriesPoint;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Finance & operations dashboard — Modern Midnight, glass cards, JFreeChart analytics.
 */
public class DashboardPanel extends BaseDashboardModule {

    private static final Color BG_SURFACE = Color.decode("#1E293B");
    private static final Color BG_MAIN = Color.decode("#0F172A");
    private static final Color ACCENT = Color.decode("#6366F1");
    private static final Color TEXT_PRIMARY = Color.decode("#F8FAFC");
    private static final Color TEXT_SECONDARY = Color.decode("#94A3B8");
    private static final Color CHART_GRID = Color.decode("#334155");
    private static final Color DANGER = Color.decode("#F43F5E");

    private final DashboardController controller = new DashboardController();

    private final JLabel lblRevenue = statValueLabel();
    private final JLabel lblTickets = statValueLabel();
    private final JLabel lblOccupancy = statValueLabel();
    private final JLabel lblCustomers = statValueLabel();

    private final ChartPanel lineChartPanel;
    private final ChartPanel pieChartPanel;

    private final DefaultTableModel alertTableModel;
    private final DefaultListModel<String> nowShowingModel;

    public DashboardPanel() {
        super("Dashboard", "Home > Dashboard");

        lineChartPanel = buildLineChartPlaceholder();
        pieChartPanel = buildPieChartPlaceholder();

        alertTableModel = new DefaultTableModel(new Object[]{"Sản phẩm", "Tồn", "Ngưỡng tối thiểu"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        nowShowingModel = new DefaultListModel<>();

        initLayout();
        scheduleRefresh();
        loadDashboardAsync();
    }

    private static JLabel statValueLabel() {
        JLabel l = new JLabel("—");
        l.setFont(new Font("Inter", Font.BOLD, 22));
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    private void initLayout() {
        contentBody.setLayout(new BorderLayout(0, 20));
        contentBody.setBackground(BG_MAIN);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setOpaque(false);

        main.add(buildStatRow());
        main.add(Box.createVerticalStrut(8));
        main.add(buildChartsRow());
        main.add(Box.createVerticalStrut(8));
        main.add(buildOperationalRow());

        contentBody.add(main, BorderLayout.CENTER);
    }

    private JPanel buildStatRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        row.add(createStatCard("Doanh thu hôm nay", lblRevenue, "icons/pie-chart.svg", "Tổng thanh toán PAID trong ngày"));
        row.add(createStatCard("Vé đã bán", lblTickets, "icons/ticket.svg", "Số vé trên hóa đơn PAID hôm nay"));
        row.add(createStatCard("Tỷ lệ lấp đầy", lblOccupancy, "icons/video.svg", "Vé / sức chỗ suất chiếu hôm nay"));
        row.add(createStatCard("Khách hàng mới", lblCustomers, "icons/users.svg", "Khách mua vé lần đầu trong ngày"));
        return row;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String iconPath, String tooltip) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setOpaque(true);
        card.setBackground(BG_SURFACE);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B; borderColor: #334155; borderWidth: 1;");
        card.setToolTipText(tooltip);

        JLabel icon = new JLabel(new FlatSVGIcon(iconPath, 28, 28));
        icon.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Inter", Font.PLAIN, 13));
        lblTitle.setForeground(TEXT_SECONDARY);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(icon, BorderLayout.WEST);
        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        textCol.add(lblTitle);
        textCol.add(Box.createVerticalStrut(4));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textCol.add(valueLabel);
        north.add(textCol, BorderLayout.CENTER);

        card.add(north, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildChartsRow() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 0.70;
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(wrapChart(lineChartPanel, "Xu hướng doanh thu (7 ngày)", "icons/history.svg"), BorderLayout.CENTER);

        c.gridx = 1;
        c.weightx = 0.30;
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(wrapChart(pieChartPanel, "Cơ cấu doanh thu (Vé vs Bắp nước)", "icons/pie-chart.svg"), BorderLayout.CENTER);

        c.gridx = 0;
        c.weightx = 0.70;
        wrap.add(left, c);
        c.gridx = 1;
        c.weightx = 0.30;
        wrap.add(right, c);

        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        wrap.setPreferredSize(new Dimension(0, 320));
        return wrap;
    }

    private JPanel wrapChart(JComponent chart, String heading, String iconPath) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JLabel h = new JLabel(heading, new FlatSVGIcon(iconPath, 18, 18), SwingConstants.LEFT);
        h.setFont(new Font("Inter", Font.BOLD, 15));
        h.setForeground(TEXT_PRIMARY);
        h.setIconTextGap(10);
        chart.setOpaque(false);
        p.add(h, BorderLayout.NORTH);
        p.add(chart, BorderLayout.CENTER);
        return p;
    }

    private ChartPanel buildLineChartPlaceholder() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createLineChart(
                null,
                null,
                null,
                ds,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );
        styleCategoryChart(chart);
        ChartPanel cp = new ChartPanel(chart);
        cp.setOpaque(false);
        cp.setBackground(BG_MAIN);
        cp.setMouseZoomable(false, false);
        return cp;
    }

    private ChartPanel buildPieChartPlaceholder() {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        JFreeChart chart = ChartFactory.createPieChart(null, ds, false, false, false);
        stylePieChart(chart);
        ChartPanel cp = new ChartPanel(chart);
        cp.setOpaque(false);
        cp.setBackground(BG_MAIN);
        return cp;
    }

    private void styleCategoryChart(JFreeChart chart) {
        chart.setBackgroundPaint(BG_MAIN);
        chart.getPlot().setBackgroundPaint(BG_MAIN);
        if (chart.getCategoryPlot() != null) {
            var plot = chart.getCategoryPlot();
            plot.setDomainGridlinePaint(CHART_GRID);
            plot.setRangeGridlinePaint(CHART_GRID);
            plot.setOutlineVisible(false);
            plot.getDomainAxis().setLabelPaint(TEXT_SECONDARY);
            plot.getDomainAxis().setTickLabelPaint(TEXT_SECONDARY);
            plot.getRangeAxis().setLabelPaint(TEXT_SECONDARY);
            plot.getRangeAxis().setTickLabelPaint(TEXT_SECONDARY);
            if (plot.getRenderer() instanceof LineAndShapeRenderer renderer) {
                renderer.setSeriesPaint(0, ACCENT);
                renderer.setSeriesStroke(0, new BasicStroke(2.2f));
            }
        }
        if (chart.getTitle() != null) {
            chart.getTitle().setPaint(TEXT_PRIMARY);
        }
    }

    private void stylePieChart(JFreeChart chart) {
        chart.setBackgroundPaint(BG_MAIN);
        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(BG_MAIN);
        plot.setOutlineVisible(false);
        plot.setLabelPaint(TEXT_PRIMARY);
        plot.setSectionPaint("Vé xem phim", ACCENT);
        plot.setSectionPaint("Bắp nước", DANGER);
        plot.setSectionPaint("Chưa có dữ liệu", TEXT_SECONDARY);
        plot.setLabelBackgroundPaint(new Color(30, 41, 59, 200));
    }

    private JPanel buildOperationalRow() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.5);
        split.setOpaque(false);
        split.setBorder(null);
        split.setDividerSize(12);
        split.setContinuousLayout(true);

        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        JLabel h1 = sectionTitle("Inventory Alerts", "icons/box.svg");
        JTable table = new JTable(alertTableModel);
        table.setFont(new Font("Inter", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(99, 102, 241, 60));
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_SURFACE);
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 12));
        table.getTableHeader().setBackground(BG_SURFACE);
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.putClientProperty(FlatClientProperties.STYLE, "arc: 16; showHorizontalLines: true; showVerticalLines: false;");
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG_SURFACE);
        sp.putClientProperty(FlatClientProperties.STYLE, "arc: 16; borderColor: #334155; borderWidth: 1;");
        left.add(h1, BorderLayout.NORTH);
        left.add(sp, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setOpaque(false);
        JLabel h2 = sectionTitle("Now Showing", "icons/video.svg");
        JList<String> list = new JList<>(nowShowingModel);
        list.setFont(new Font("Inter", Font.PLAIN, 13));
        list.setForeground(TEXT_PRIMARY);
        list.setBackground(BG_SURFACE);
        list.setFixedCellHeight(52);
        list.putClientProperty(FlatClientProperties.STYLE, "arc: 16;");
        JScrollPane sp2 = new JScrollPane(list);
        sp2.setBorder(BorderFactory.createEmptyBorder());
        sp2.getViewport().setBackground(BG_SURFACE);
        sp2.putClientProperty(FlatClientProperties.STYLE, "arc: 16; borderColor: #334155; borderWidth: 1;");
        right.add(h2, BorderLayout.NORTH);
        right.add(sp2, BorderLayout.CENTER);

        split.setLeftComponent(left);
        split.setRightComponent(right);
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(split, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        row.setPreferredSize(new Dimension(0, 260));
        return row;
    }

    private JLabel sectionTitle(String text, String iconPath) {
        JLabel l = new JLabel(text, new FlatSVGIcon(iconPath, 20, 20), SwingConstants.LEFT);
        l.setFont(new Font("Inter", Font.BOLD, 16));
        l.setForeground(TEXT_PRIMARY);
        l.setIconTextGap(10);
        return l;
    }

    private void loadDashboardAsync() {
        Thread.ofVirtual().start(() -> {
            try {
                DashboardSnapshot snap = controller.loadSnapshot();
                SwingUtilities.invokeLater(() -> applySnapshot(snap));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                "Không tải được dashboard: " + ex.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE));
            }
        });
    }

    private void scheduleRefresh() {
        Timer timer = new Timer(60_000, e -> loadDashboardAsync());
        timer.start();
    }

    private void applySnapshot(DashboardSnapshot snap) {
        DashboardFinance f = snap.finance();
        DecimalFormat money = new DecimalFormat("#,##0");
        lblRevenue.setText(money.format(f.revenueToday()) + " ₫");
        lblTickets.setText(String.valueOf(f.ticketsSoldToday()));
        lblOccupancy.setText(String.format("%.1f%%", f.occupancyPercent()));
        lblCustomers.setText(String.valueOf(f.newCustomersToday()));

        updateLineChart(f.revenueLast7Days());
        updatePieChart(f.ticketRevenue7d(), f.fnbRevenue7d());

        alertTableModel.setRowCount(0);
        for (InventoryAlertRow row : snap.inventoryAlerts()) {
            alertTableModel.addRow(new Object[]{row.productName(), row.currentQuantity(), row.minThreshold()});
        }

        nowShowingModel.clear();
        for (NowShowingRow row : snap.nowShowing()) {
            String line = "<html><b>" + escapeHtml(row.movieTitle()) + "</b><br/>"
                    + "<span style='color:#94A3B8'>" + escapeHtml(row.statusLabel()) + "</span></html>";
            nowShowingModel.addElement(line);
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void updateLineChart(List<RevenueSeriesPoint> points) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM");
        for (RevenueSeriesPoint p : points) {
            ds.addValue(p.amount(), "Doanh thu", df.format(p.day()));
        }
        JFreeChart chart = ChartFactory.createLineChart(
                null,
                null,
                null,
                ds,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );
        styleCategoryChart(chart);
        lineChartPanel.setChart(chart);
    }

    private void updatePieChart(BigDecimal ticket, BigDecimal fnb) {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        double t = ticket != null ? ticket.doubleValue() : 0;
        double f = fnb != null ? fnb.doubleValue() : 0;
        if (t <= 0 && f <= 0) {
            ds.setValue("Chưa có dữ liệu", 1);
        } else {
            ds.setValue("Vé xem phim", Math.max(t, 0));
            ds.setValue("Bắp nước", Math.max(f, 0));
        }
        JFreeChart chart = ChartFactory.createPieChart(null, ds, false, false, false);
        stylePieChart(chart);
        pieChartPanel.setChart(chart);
    }
}
