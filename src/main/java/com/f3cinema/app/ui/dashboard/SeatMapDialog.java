package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.entity.Seat;
import com.f3cinema.app.entity.enums.SeatType;
import com.f3cinema.app.service.RoomService;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.common.dialog.BaseAppDialog;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatMapDialog extends BaseAppDialog {
    private final RoomService roomService = RoomService.getInstance();
    private final Long roomId;
    private JPanel mapPanel;
    private JPanel topPanel;
    private JLabel capacityLabel;
    private JComboBox<SeatType> bulkTypeCombo;
    private JButton applyBulkButton;
    private final Set<Seat> selectedSeats = new HashSet<>();
    private List<Seat> seats;
    
    public SeatMapDialog(JFrame owner, Long roomId) {
        super(owner, "Sơ đồ ghế (Nhấp để đổi Loại Ghế)");
        this.roomId = roomId;
        setupBaseDialog(900, 700);
        JPanel surface = createSurfacePanel();
        setContentPane(surface);
        
        mapPanel = new JPanel();
        mapPanel.setOpaque(false);
        mapPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        
        JScrollPane scrollPane = new JScrollPane(mapPanel);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        surface.add(scrollPane, BorderLayout.CENTER);

        topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(10, 16, 8, 16));
        topPanel.add(buildLegend(), BorderLayout.WEST);

        JPanel bulkPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bulkPanel.setOpaque(false);
        capacityLabel = new JLabel("Capacity: 0/0");
        capacityLabel.setForeground(ThemeConfig.TEXT_SECONDARY);
        capacityLabel.setFont(ThemeConfig.FONT_BODY);
        bulkPanel.add(capacityLabel);
        bulkTypeCombo = new JComboBox<>(SeatType.values());
        bulkTypeCombo.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #0F172A; foreground: #F8FAFC;");
        applyBulkButton = new JButton("Apply to selection");
        applyBulkButton.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #6366F1; foreground: #FFFFFF; borderWidth: 0;");
        applyBulkButton.addActionListener(e -> applyBulkUpdate());
        bulkPanel.add(bulkTypeCombo);
        bulkPanel.add(applyBulkButton);
        topPanel.add(bulkPanel, BorderLayout.EAST);
        surface.add(topPanel, BorderLayout.NORTH);
        
        loadSeats();
    }

    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        legend.setOpaque(false);
        legend.add(createLegendItem("Regular", ThemeConfig.TEXT_SECONDARY));
        legend.add(createLegendItem("VIP", Color.decode("#F59E0B")));
        legend.add(createLegendItem("Couple", Color.decode("#EC4899")));
        legend.add(createLegendItem("Selected", ThemeConfig.ACCENT_COLOR));
        return legend;
    }

    private JPanel createLegendItem(String label, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setOpaque(false);
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
        if(seats.isEmpty()) return;
        
        int maxRows = 0;
        int maxCols = 0;
        for(Seat s : seats) {
            maxRows = Math.max(maxRows, s.getRowChar().charAt(0) - 'A' + 1);
            maxCols = Math.max(maxCols, s.getNumber());
        }
        
        mapPanel.setLayout(new GridLayout(maxRows, maxCols, 5, 5));
        mapPanel.removeAll();
        
        Seat[][] matrix = new Seat[maxRows][maxCols];
        for(Seat s : seats) {
            matrix[s.getRowChar().charAt(0) - 'A'][s.getNumber()-1] = s;
        }
        
        for (int r = 0; r < maxRows; r++) {
            for (int c = 0; c < maxCols; c++) {
                Seat s = matrix[r][c];
                if (s == null) {
                    mapPanel.add(new JLabel());
                } else {
                    JButton btn = new JButton(s.getRowChar() + s.getNumber());
                    btn.setPreferredSize(new Dimension(44, 44));
                    btn.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD));
                    btn.setForeground(Color.WHITE);
                    btn.setFocusPainted(false);
                    btn.setBorderPainted(false);
                    btn.putClientProperty(FlatClientProperties.STYLE, "arc: 12; margin: 0,0,0,0; padding: 0,0,0,0");
                    btn.setBackground(colorForType(s.getSeatType()));
                    btn.setToolTipText(s.getRowChar() + s.getNumber() + " - " + s.getSeatType().name());

                    btn.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                        boolean multiSelect = e.isControlDown();
                        if (multiSelect) {
                            if (selectedSeats.contains(s)) selectedSeats.remove(s);
                            else selectedSeats.add(s);
                            refreshSeatSelectionStyles();
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
        mapPanel.revalidate();
        mapPanel.repaint();
    }

    private void applyBulkUpdate() {
        if (selectedSeats.isEmpty()) return;
        SeatType target = (SeatType) bulkTypeCombo.getSelectedItem();
        if (target == null) return;
        for (Component c : mapPanel.getComponents()) {
            if (c instanceof JButton btn) {
                Object seatObj = btn.getClientProperty("seat");
                if (seatObj instanceof Seat seat && selectedSeats.contains(seat)) {
                    seat.setSeatType(target);
                    btn.setBackground(colorForType(target));
                    persistAsync(seat);
                }
            }
        }
        selectedSeats.clear();
        refreshSeatSelectionStyles();
        updateCapacity();
    }

    private void refreshSeatSelectionStyles() {
        for (Component c : mapPanel.getComponents()) {
            if (c instanceof JButton btn) {
                Object seatObj = btn.getClientProperty("seat");
                if (seatObj instanceof Seat seat) {
                    boolean selected = selectedSeats.contains(seat);
                    btn.setBorderPainted(selected);
                    btn.setBorder(BorderFactory.createLineBorder(selected ? ThemeConfig.ACCENT_COLOR : new Color(0, 0, 0, 0), selected ? 2 : 0));
                }
            }
        }
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
                        AppMessageDialogs.showError(this, "Loi", "Loi khi luu trang thai ghe!"));
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
