package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.controller.DashboardController;
import com.f3cinema.app.dto.dashboard.DashboardFinance;
import com.f3cinema.app.dto.dashboard.DashboardSnapshot;
import com.f3cinema.app.dto.dashboard.InventoryAlertRow;
import com.f3cinema.app.dto.dashboard.NowShowingRow;
import com.f3cinema.app.dto.dashboard.RevenueSeriesPoint;
import com.f3cinema.app.dto.dashboard.TopMovieRow;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Finance & operations dashboard — Modern Midnight, glass cards, XChart analytics.
 */
public class DashboardPanel extends BaseDashboardModule {

    private static final Color BG_SURFACE = ThemeConfig.BG_CARD;
    private static final Color BG_MAIN = ThemeConfig.BG_MAIN;
    private static final Color ACCENT = ThemeConfig.ACCENT_COLOR;
    private static final Color TEXT_PRIMARY = ThemeConfig.TEXT_PRIMARY;
    private static final Color TEXT_SECONDARY = ThemeConfig.TEXT_SECONDARY;
    private static final Color DANGER = ThemeConfig.TEXT_DANGER;
    private static final Color SUCCESS = ThemeConfig.TEXT_SUCCESS;

    private final DashboardController controller = new DashboardController();

    private final JLabel lblRevenue = statValueLabel();
    private final JLabel lblTickets = statValueLabel();
    private final JLabel lblOccupancy = statValueLabel();
    private final JLabel lblCustomers = statValueLabel();
    
    private final JLabel lblRevenueTrend = trendLabel();
    private final JLabel lblTicketsTrend = trendLabel();
    private final JLabel lblOccupancyTrend = trendLabel();
    private final JLabel lblCustomersTrend = trendLabel();

    private XChartPanel<CategoryChart> lineChartPanel;
    private XChartPanel<PieChart> pieChartPanel;
    private JPanel topMoviesPanel;

    private final DefaultTableModel alertTableModel;
    private final DefaultListModel<String> nowShowingModel;

    public DashboardPanel() {
        super("Dashboard", "Home > Dashboard");

        lineChartPanel = buildXChartLinePanel();
        pieChartPanel = buildXChartPiePanel();
        topMoviesPanel = buildTopMoviesCard();

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
        l.setFont(ThemeConfig.FONT_STAT);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }
    
    private static JLabel trendLabel() {
        JLabel l = new JLabel("");
        l.setFont(ThemeConfig.FONT_SMALL);
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    private void initLayout() {
        contentBody.setLayout(new BorderLayout(0, ThemeConfig.GAP_SECTION));
        contentBody.setBackground(BG_MAIN);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(ThemeConfig.MARGIN_PAGE, ThemeConfig.MARGIN_PAGE, 
                                       ThemeConfig.MARGIN_PAGE, ThemeConfig.MARGIN_PAGE));

        main.add(buildStatRow());
        main.add(Box.createVerticalStrut(ThemeConfig.GAP_SECTION));
        main.add(buildChartsRow());
        main.add(Box.createVerticalStrut(ThemeConfig.GAP_SECTION));
        main.add(buildOperationalRow());

        contentBody.add(main, BorderLayout.CENTER);
    }

    private JPanel buildStatRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 20, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        row.add(createStatCard("Doanh thu hôm nay", lblRevenue, lblRevenueTrend, "icons/pie-chart.svg", "Tổng thanh toán PAID trong ngày"));
        row.add(createStatCard("Vé đã bán", lblTickets, lblTicketsTrend, "icons/ticket.svg", "Số vé trên hóa đơn PAID hôm nay"));
        row.add(createStatCard("Tỷ lệ lấp đầy", lblOccupancy, lblOccupancyTrend, "icons/video.svg", "Vé / sức chỗ suất chiếu hôm nay"));
        row.add(createStatCard("Khách hàng mới", lblCustomers, lblCustomersTrend, "icons/users.svg", "Khách mua vé lần đầu trong ngày"));
        return row;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, JLabel trendLabel, String iconPath, String tooltip) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Subtle drop shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, ThemeConfig.RADIUS_CARD, ThemeConfig.RADIUS_CARD);
                
                // Card background
                g2.setColor(BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ThemeConfig.RADIUS_CARD, ThemeConfig.RADIUS_CARD);
                
                // Subtle border
                g2.setColor(new Color(51, 65, 85, 80));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, ThemeConfig.RADIUS_CARD, ThemeConfig.RADIUS_CARD);
                
                g2.dispose();
            }
        };
        
        card.setLayout(new BorderLayout(12, 10));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(ThemeConfig.PADDING_CARD, ThemeConfig.PADDING_CARD, 
                                       ThemeConfig.PADDING_CARD, ThemeConfig.PADDING_CARD));
        card.setToolTipText(tooltip);

        // Icon at top-left
        JLabel icon = new JLabel(new FlatSVGIcon(iconPath, 32, 32));
        icon.setHorizontalAlignment(SwingConstants.LEFT);
        
        // Title below icon
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(ThemeConfig.FONT_BODY);
        lblTitle.setForeground(TEXT_SECONDARY);

        // Value panel: large stat + trend indicator
        JPanel valuePanel = new JPanel();
        valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.Y_AXIS));
        valuePanel.setOpaque(false);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        trendLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valuePanel.add(valueLabel);
        valuePanel.add(Box.createVerticalStrut(4));
        valuePanel.add(trendLabel);

        // Assemble: icon top, title, then value+trend
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        valuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        content.add(icon);
        content.add(Box.createVerticalStrut(12));
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(8));
        content.add(valuePanel);

        card.add(content, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel buildTopMoviesCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setOpaque(false);
        
        // Header
        JLabel header = new JLabel("Top phim hot", new FlatSVGIcon("icons/star.svg", 20, 20), SwingConstants.LEFT);
        header.setFont(ThemeConfig.FONT_H2);
        header.setForeground(TEXT_PRIMARY);
        header.setIconTextGap(10);
        
        // Content panel with custom paint for background
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ThemeConfig.RADIUS_CARD, ThemeConfig.RADIUS_CARD);
                
                g2.setColor(new Color(51, 65, 85, 80));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, ThemeConfig.RADIUS_CARD, ThemeConfig.RADIUS_CARD);
                
                g2.dispose();
            }
        };
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(ThemeConfig.PADDING_CARD, ThemeConfig.PADDING_CARD,
                                          ThemeConfig.PADDING_CARD, ThemeConfig.PADDING_CARD));
        
        // Placeholder for top 5 movies
        for (int i = 1; i <= 5; i++) {
            content.add(createTopMovieRow(i, "—", 0));
            if (i < 5) {
                content.add(Box.createVerticalStrut(10));
            }
        }
        
        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createTopMovieRow(int rank, String title, long ticketsSold) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        
        // Rank badge
        JLabel lblRank = new JLabel(String.valueOf(rank));
        lblRank.setFont(ThemeConfig.FONT_BODY);
        lblRank.setForeground(rank == 1 ? ACCENT : TEXT_SECONDARY);
        lblRank.setPreferredSize(new Dimension(24, 24));
        lblRank.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Movie title
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(ThemeConfig.FONT_BODY);
        lblTitle.setForeground(TEXT_PRIMARY);
        
        // Tickets count
        JLabel lblCount = new JLabel(ticketsSold > 0 ? String.valueOf(ticketsSold) : "—");
        lblCount.setFont(ThemeConfig.FONT_SMALL);
        lblCount.setForeground(TEXT_SECONDARY);
        lblCount.setHorizontalAlignment(SwingConstants.RIGHT);
        
        row.add(lblRank, BorderLayout.WEST);
        row.add(lblTitle, BorderLayout.CENTER);
        row.add(lblCount, BorderLayout.EAST);
        
        return row;
    }

    private JPanel buildChartsRow() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.gridy = 0;
        
        // Line chart: 60% width
        c.gridx = 0;
        c.weightx = 0.60;
        c.insets = new Insets(0, 0, 0, 8);
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(wrapChart(lineChartPanel, "Xu hướng doanh thu (7 ngày)", "icons/history.svg"), BorderLayout.CENTER);
        wrap.add(left, c);

        // Pie chart: 20% width
        c.gridx = 1;
        c.weightx = 0.20;
        c.insets = new Insets(0, 4, 0, 4);
        JPanel middle = new JPanel(new BorderLayout());
        middle.setOpaque(false);
        middle.add(wrapChart(pieChartPanel, "Cơ cấu doanh thu", "icons/pie-chart.svg"), BorderLayout.CENTER);
        wrap.add(middle, c);
        
        // Top movies: 20% width
        c.gridx = 2;
        c.weightx = 0.20;
        c.insets = new Insets(0, 8, 0, 0);
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(topMoviesPanel, BorderLayout.CENTER);
        wrap.add(right, c);

        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        wrap.setPreferredSize(new Dimension(0, 340));
        return wrap;
    }

    private JPanel wrapChart(JComponent chart, String heading, String iconPath) {
        JPanel p = new JPanel(new BorderLayout(0, ThemeConfig.GAP_SMALL));
        p.setOpaque(false);
        JLabel h = new JLabel(heading, new FlatSVGIcon(iconPath, 20, 20), SwingConstants.LEFT);
        h.setFont(ThemeConfig.FONT_H2);
        h.setForeground(TEXT_PRIMARY);
        h.setIconTextGap(10);
        chart.setOpaque(false);
        p.add(h, BorderLayout.NORTH);
        p.add(chart, BorderLayout.CENTER);
        return p;
    }

    private XChartPanel<CategoryChart> buildXChartLinePanel() {
        CategoryChart chart = new CategoryChartBuilder()
                .width(800)
                .height(300)
                .title("")
                .xAxisTitle("")
                .yAxisTitle("")
                .theme(Styler.ChartTheme.Matlab)
                .build();
        
        styleXChartCategory(chart);
        
        // Placeholder data
        List<String> xData = List.of("", "", "", "", "", "", "");
        List<Number> yData = List.of(0, 0, 0, 0, 0, 0, 0);
        chart.addSeries("Doanh thu", xData, yData);
        
        XChartPanel<CategoryChart> panel = new XChartPanel<>(chart);
        panel.setOpaque(false);
        panel.setBackground(BG_MAIN);
        return panel;
    }
    
    private XChartPanel<PieChart> buildXChartPiePanel() {
        PieChart chart = new PieChartBuilder()
                .width(400)
                .height(300)
                .title("")
                .theme(Styler.ChartTheme.Matlab)
                .build();
        
        styleXChartPie(chart);
        
        // Placeholder data
        chart.addSeries("Chưa có dữ liệu", 1);
        
        XChartPanel<PieChart> panel = new XChartPanel<>(chart);
        panel.setOpaque(false);
        panel.setBackground(BG_MAIN);
        return panel;
    }
    
    private void styleXChartCategory(CategoryChart chart) {
        Styler styler = chart.getStyler();
        styler.setChartBackgroundColor(BG_MAIN);
        styler.setPlotBackgroundColor(BG_MAIN);
        styler.setPlotBorderVisible(false);
        styler.setChartTitleVisible(false);
        styler.setLegendVisible(false);
        styler.setSeriesColors(new Color[]{ACCENT});
        styler.setBaseFont(ThemeConfig.FONT_SMALL);
    }
    
    private void styleXChartPie(PieChart chart) {
        Styler styler = chart.getStyler();
        styler.setChartBackgroundColor(BG_MAIN);
        styler.setPlotBackgroundColor(BG_MAIN);
        styler.setPlotBorderVisible(false);
        styler.setChartTitleVisible(false);
        styler.setLegendVisible(true);
        styler.setLegendBackgroundColor(BG_MAIN);
        styler.setBaseFont(ThemeConfig.FONT_SMALL);
        styler.setSeriesColors(new Color[]{ACCENT, DANGER, TEXT_SECONDARY});
    }

    private JPanel buildOperationalRow() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.5);
        split.setOpaque(false);
        split.setBorder(null);
        split.setDividerSize(ThemeConfig.GAP_SECTION);
        split.setContinuousLayout(true);

        JPanel left = new JPanel(new BorderLayout(0, ThemeConfig.GAP_SMALL));
        left.setOpaque(false);
        JLabel h1 = sectionTitle("Inventory Alerts", "icons/box.svg");
        JTable table = new JTable(alertTableModel);
        table.setFont(ThemeConfig.FONT_BODY);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(99, 102, 241, 60));
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_SURFACE);
        table.getTableHeader().setFont(ThemeConfig.FONT_SMALL);
        table.getTableHeader().setBackground(BG_SURFACE);
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.putClientProperty(FlatClientProperties.STYLE, "arc: 20; showHorizontalLines: true; showVerticalLines: false;");
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG_SURFACE);
        sp.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderColor: #334155; borderWidth: 1;");
        left.add(h1, BorderLayout.NORTH);
        left.add(sp, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, ThemeConfig.GAP_SMALL));
        right.setOpaque(false);
        JLabel h2 = sectionTitle("Now Showing", "icons/video.svg");
        JList<String> list = new JList<>(nowShowingModel);
        list.setFont(ThemeConfig.FONT_BODY);
        list.setForeground(TEXT_PRIMARY);
        list.setBackground(BG_SURFACE);
        list.setFixedCellHeight(70);
        list.putClientProperty(FlatClientProperties.STYLE, "arc: 20;");
        JScrollPane sp2 = new JScrollPane(list);
        sp2.setBorder(BorderFactory.createEmptyBorder());
        sp2.getViewport().setBackground(BG_SURFACE);
        sp2.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderColor: #334155; borderWidth: 1;");
        right.add(h2, BorderLayout.NORTH);
        right.add(sp2, BorderLayout.CENTER);

        split.setLeftComponent(left);
        split.setRightComponent(right);
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(split, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        row.setPreferredSize(new Dimension(0, 280));
        return row;
    }

    private JLabel sectionTitle(String text, String iconPath) {
        JLabel l = new JLabel(text, new FlatSVGIcon(iconPath, 20, 20), SwingConstants.LEFT);
        l.setFont(ThemeConfig.FONT_H2);
        l.setForeground(TEXT_PRIMARY);
        l.setIconTextGap(10);
        return l;
    }

    private void loadDashboardAsync() {
        Thread.ofVirtual().start(() -> {
            try {
                DashboardSnapshot snap = controller.loadSnapshot();
                List<TopMovieRow> topMovies = controller.getTopMovies(7, 5);
                SwingUtilities.invokeLater(() -> {
                    applySnapshot(snap);
                    updateTopMovies(topMovies);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        AppMessageDialogs.showError(this, "Lỗi", "Không tải được dashboard: " + ex.getMessage()));
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
        
        // Update values
        lblRevenue.setText(money.format(f.revenueToday()) + " ₫");
        lblTickets.setText(String.valueOf(f.ticketsSoldToday()));
        lblOccupancy.setText(String.format("%.1f%%", f.occupancyPercent()));
        lblCustomers.setText(String.valueOf(f.newCustomersToday()));

        // Update trend indicators
        updateTrendIndicator(lblRevenueTrend, f.revenueToday().doubleValue(), 
                            f.revenueYesterday().doubleValue(), true);
        updateTrendIndicator(lblTicketsTrend, f.ticketsSoldToday(), 
                            f.ticketsYesterday(), false);
        updateTrendIndicator(lblOccupancyTrend, f.occupancyPercent(), 
                            f.occupancyYesterday(), false);
        updateTrendIndicator(lblCustomersTrend, f.newCustomersToday(), 
                            f.newCustomersYesterday(), false);

        updateLineChart(f.revenueLast7Days());
        updatePieChart(f.ticketRevenue7d(), f.fnbRevenue7d());

        alertTableModel.setRowCount(0);
        for (InventoryAlertRow row : snap.inventoryAlerts()) {
            alertTableModel.addRow(new Object[]{row.productName(), row.currentQuantity(), row.minThreshold()});
        }

        nowShowingModel.clear();
        for (NowShowingRow row : snap.nowShowing()) {
            String line = "<html><div style='padding:4px 0'><b style='font-size:13px'>" 
                    + escapeHtml(row.movieTitle()) + "</b><br/>"
                    + "<span style='color:#94A3B8; font-size:12px; margin-top:4px'>" 
                    + escapeHtml(row.statusLabel()) + "</span></div></html>";
            nowShowingModel.addElement(line);
        }
    }
    
    private void updateTrendIndicator(JLabel trendLabel, double today, double yesterday, boolean isCurrency) {
        if (yesterday == 0) {
            trendLabel.setText("");
            return;
        }
        
        double change = ((today - yesterday) / yesterday) * 100;
        String arrow = change > 0 ? "↑" : (change < 0 ? "↓" : "→");
        String sign = change > 0 ? "+" : "";
        Color color = change > 0 ? SUCCESS : (change < 0 ? DANGER : TEXT_SECONDARY);
        
        trendLabel.setText(String.format("%s %s%.1f%% vs hôm qua", arrow, sign, change));
        trendLabel.setForeground(color);
    }
    
    private void updateTopMovies(List<TopMovieRow> topMovies) {
        // Rebuild top movies panel with real data
        JPanel content = (JPanel) topMoviesPanel.getComponent(1);
        content.removeAll();
        
        if (topMovies.isEmpty()) {
            JLabel empty = new JLabel("Chưa có dữ liệu");
            empty.setFont(ThemeConfig.FONT_BODY);
            empty.setForeground(TEXT_SECONDARY);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            content.add(empty);
        } else {
            for (int i = 0; i < topMovies.size(); i++) {
                TopMovieRow movie = topMovies.get(i);
                content.add(createTopMovieRow(movie.rank(), movie.movieTitle(), movie.ticketsSold()));
                if (i < topMovies.size() - 1) {
                    content.add(Box.createVerticalStrut(10));
                }
            }
        }
        
        content.revalidate();
        content.repaint();
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void updateLineChart(List<RevenueSeriesPoint> points) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM");
        List<String> xData = new ArrayList<>();
        List<Number> yData = new ArrayList<>();
        
        for (RevenueSeriesPoint p : points) {
            xData.add(df.format(p.day()));
            yData.add(p.amount());
        }
        
        CategoryChart chart = lineChartPanel.getChart();
        chart.updateCategorySeries("Doanh thu", xData, yData, null);
        
        lineChartPanel.revalidate();
        lineChartPanel.repaint();
    }

    private void updatePieChart(BigDecimal ticket, BigDecimal fnb) {
        double t = ticket != null ? ticket.doubleValue() : 0;
        double f = fnb != null ? fnb.doubleValue() : 0;
        
        PieChart chart = pieChartPanel.getChart();
        
        // Safely clear and re-add series to avoid 'series not found' exceptions
        for (String key : new java.util.ArrayList<>(chart.getSeriesMap().keySet())) {
            chart.removeSeries(key);
        }
        
        if (t <= 0 && f <= 0) {
            chart.addSeries("Chưa có dữ liệu", 1);
        } else {
            chart.addSeries("Vé xem phim", Math.max(t, 0.01)); // XChart pie needs > 0
            chart.addSeries("Bắp nước", Math.max(f, 0.001));
        }
        
        pieChartPanel.revalidate();
        pieChartPanel.repaint();
    }
}
