package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.entity.Seat;
import com.f3cinema.app.entity.enums.SeatType;
import com.f3cinema.app.service.RoomService;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.common.dialog.BaseAppDialog;
import com.f3cinema.app.ui.common.dialog.DialogStyle;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Sơ đồ ghế: mỗi lần click một ghế là đổi loại (Regular → VIP → Couple) và lưu.
 */
public class SeatMapDialog extends BaseAppDialog {
    private final RoomService roomService = RoomService.getInstance();
    private final Long roomId;
    private JPanel mapPanel;
    /** Chỉ khác null khi lưới lớn hơn màn hình và cần cuộn */
    private JScrollPane scrollPane;
    private JPanel topPanel;
    private JLabel capacityLabel;
    private List<Seat> seats;

    public SeatMapDialog(JFrame owner, Long roomId) {
        super(owner, "Sơ đồ ghế — click từng ghế để đổi loại");
        this.roomId = roomId;
        setupUndecoratedNoFixedSize();
        JPanel surface = createSurfacePanel();
        setContentPane(surface);

        mapPanel = new JPanel();
        mapPanel.setOpaque(true);
        mapPanel.setBackground(ThemeConfig.BG_CARD);
        mapPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        topPanel = new JPanel();
        topPanel.setOpaque(true);
        topPanel.setBackground(ThemeConfig.BG_CARD);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(new EmptyBorder(10, 16, 8, 16));

        JPanel legendRow = buildLegend();
        legendRow.setBackground(ThemeConfig.BG_CARD);
        legendRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(legendRow);
        topPanel.add(Box.createVerticalStrut(8));

        JPanel capRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        capRow.setOpaque(true);
        capRow.setBackground(ThemeConfig.BG_CARD);
        capRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        capacityLabel = new JLabel("Capacity: 0/0");
        capacityLabel.setForeground(ThemeConfig.TEXT_SECONDARY);
        capacityLabel.setFont(ThemeConfig.FONT_BODY);
        capRow.add(capacityLabel);
        topPanel.add(capRow);
        surface.add(topPanel, BorderLayout.NORTH);

        JPanel south = new JPanel(new BorderLayout(0, 10));
        south.setOpaque(true);
        south.setBackground(ThemeConfig.BG_CARD);
        south.setBorder(new EmptyBorder(8, 20, 16, 20));
        JLabel hint = new JLabel("Mỗi lần click ghế đã lưu ngay — bấm nút bên dưới khi xong.");
        hint.setFont(ThemeConfig.FONT_SMALL);
        hint.setForeground(ThemeConfig.TEXT_SECONDARY);
        south.add(hint, BorderLayout.NORTH);
        JButton btnDone = DialogStyle.primaryButton("Lưu và đóng");
        btnDone.addActionListener(e -> dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnDone);
        south.add(btnRow, BorderLayout.SOUTH);
        surface.add(south, BorderLayout.SOUTH);

        loadSeats();

        Dimension mapPref = mapPanel.getPreferredSize();
        Rectangle win = computeAvailableWindowBounds(owner);
        // Trừ chỗ cho legend + capacity + footer — tránh pack() vượt màn hình rồi cắt không cuộn
        int reserveTopBottom = 200;
        boolean needScroll = mapPref.width > win.width - 32
                || mapPref.height > win.height - reserveTopBottom;

        if (needScroll) {
            scrollPane = new JScrollPane(mapPanel);
            scrollPane.getViewport().setOpaque(true);
            scrollPane.getViewport().setBackground(ThemeConfig.BG_CARD);
            scrollPane.setOpaque(true);
            scrollPane.setBackground(ThemeConfig.BG_CARD);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getVerticalScrollBar().setUnitIncrement(20);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
            surface.add(scrollPane, BorderLayout.CENTER);
        } else {
            scrollPane = null;
            surface.add(mapPanel, BorderLayout.CENTER);
        }

        pack();
        capDialogToScreen(win);
        setLocationRelativeTo(owner);
    }

    /** Vùng an toàn trên màn hình (trừ taskbar / menu). */
    private static Rectangle computeAvailableWindowBounds(Window owner) {
        GraphicsConfiguration gc = owner != null ? owner.getGraphicsConfiguration() : null;
        if (gc == null) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        }
        Rectangle bounds = gc.getBounds();
        Insets inset = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        int w = bounds.width - inset.left - inset.right - 48;
        int h = bounds.height - inset.top - inset.bottom - 48;
        return new Rectangle(0, 0, Math.max(400, w), Math.max(400, h));
    }

    private void capDialogToScreen(Rectangle avail) {
        int w = getWidth();
        int h = getHeight();
        if (w > avail.width) {
            w = avail.width;
        }
        if (h > avail.height) {
            h = avail.height;
        }
        setSize(w, h);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(this::refreshSeatMapLayout);
    }

    private void refreshSeatMapLayout() {
        if (mapPanel == null) return;
        mapPanel.revalidate();
        mapPanel.repaint();
        if (scrollPane != null) {
            scrollPane.revalidate();
            scrollPane.repaint();
        }
        Container root = getContentPane();
        if (root != null) {
            root.revalidate();
            root.repaint();
        }
    }

    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        legend.setOpaque(true);
        legend.setBackground(ThemeConfig.BG_CARD);
        legend.add(createLegendItem("Regular", ThemeConfig.TEXT_SECONDARY));
        legend.add(createLegendItem("VIP", Color.decode("#F59E0B")));
        legend.add(createLegendItem("Couple", Color.decode("#EC4899")));
        return legend;
    }

    private JPanel createLegendItem(String label, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setOpaque(true);
        item.setBackground(ThemeConfig.BG_CARD);
        JPanel swatch = new JPanel();
        swatch.setPreferredSize(new Dimension(16, 16));
        swatch.setBackground(color);
        swatch.putClientProperty(FlatClientProperties.STYLE, "arc: 4");
        JLabel lbl = new JLabel(label);
        lbl.setForeground(ThemeConfig.TEXT_PRIMARY);
        lbl.setFont(ThemeConfig.FONT_SMALL);
        item.add(swatch);
        item.add(lbl);
        return item;
    }

    private void loadSeats() {
        seats = roomService.getSeatsByRoom(roomId);
        if (seats.isEmpty()) return;

        int maxRows = 0;
        int maxCols = 0;
        for (Seat s : seats) {
            maxRows = Math.max(maxRows, s.getRowChar().charAt(0) - 'A' + 1);
            maxCols = Math.max(maxCols, s.getNumber());
        }

        mapPanel.setLayout(new GridLayout(maxRows, maxCols, 4, 4));
        mapPanel.removeAll();

        Seat[][] matrix = new Seat[maxRows][maxCols];
        for (Seat s : seats) {
            matrix[s.getRowChar().charAt(0) - 'A'][s.getNumber() - 1] = s;
        }

        for (int r = 0; r < maxRows; r++) {
            for (int c = 0; c < maxCols; c++) {
                Seat s = matrix[r][c];
                if (s == null) {
                    JLabel empty = new JLabel();
                    empty.setOpaque(true);
                    empty.setBackground(ThemeConfig.BG_CARD);
                    mapPanel.add(empty);
                } else {
                    String row = s.getRowChar();
                    int num = s.getNumber();
                    String label = row + num;
                    JButton btn = new JButton(label);
                    Font seatFont = new Font(Font.MONOSPACED, Font.BOLD, 11);
                    btn.setFont(seatFont);
                    btn.setPreferredSize(new Dimension(56, 44));
                    btn.setForeground(Color.WHITE);
                    btn.setFocusPainted(false);
                    btn.setBorderPainted(false);
                    btn.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
                    btn.setBackground(colorForType(s.getSeatType()));
                    btn.setToolTipText(label + " — click để đổi loại ghế");

                    btn.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            if (!SwingUtilities.isLeftMouseButton(e)) {
                                return;
                            }
                            SeatType nextType = nextType(s.getSeatType());
                            s.setSeatType(nextType);
                            btn.setBackground(colorForType(nextType));
                            persistAsync(s);
                            updateCapacity();
                        }
                    });
                    btn.putClientProperty("seat", s);
                    mapPanel.add(btn);
                }
            }
        }
        updateCapacity();
        refreshSeatMapLayout();
    }

    private SeatType nextType(SeatType current) {
        return switch (current) {
            case NORMAL -> SeatType.VIP;
            case VIP -> SeatType.SWEETBOX;
            case SWEETBOX -> SeatType.NORMAL;
        };
    }

    private Color colorForType(SeatType seatType) {
        return switch (seatType) {
            case NORMAL -> Color.decode("#64748B");
            case VIP -> Color.decode("#F59E0B");
            case SWEETBOX -> Color.decode("#EC4899");
        };
    }

    private void persistAsync(Seat seat) {
        new Thread(() -> {
            try {
                roomService.updateSeat(seat);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        AppMessageDialogs.showError(this, "Lỗi", "Không lưu được trạng thái ghế."));
            }
        }).start();
    }

    private void updateCapacity() {
        if (seats == null || seats.isEmpty()) return;
        long normal = seats.stream().filter(s -> s.getSeatType() == SeatType.NORMAL).count();
        long vip = seats.stream().filter(s -> s.getSeatType() == SeatType.VIP).count();
        long couple = seats.stream().filter(s -> s.getSeatType() == SeatType.SWEETBOX).count();
        capacityLabel.setText("Capacity: " + seats.size() + " | N:" + normal + " VIP:" + vip + " C:" + couple);
    }
}
