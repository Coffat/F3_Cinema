package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.entity.Seat;
import com.f3cinema.app.entity.enums.SeatType;
import com.f3cinema.app.service.RoomService;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class SeatMapDialog extends JDialog {
    private final RoomService roomService = RoomService.getInstance();
    private final Long roomId;
    private JPanel mapPanel;
    
    public SeatMapDialog(JFrame owner, Long roomId) {
        super(owner, "Sơ đồ ghế (Nhấp để đổi Loại Ghế)", true);
        this.roomId = roomId;
        setSize(900, 700);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Color.decode("#1E293B")); // Slate 800
        
        mapPanel = new JPanel();
        mapPanel.setOpaque(false);
        mapPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        
        JScrollPane scrollPane = new JScrollPane(mapPanel);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem("Thường (Indigo)", "#6366F1"));
        legendPanel.add(createLegendItem("VIP (Purple)", "#A855F7"));
        legendPanel.add(createLegendItem("Sweetbox (Pink)", "#EC4899"));
        add(legendPanel, BorderLayout.NORTH);
        
        loadSeats();
    }
    
    private JLabel createLegendItem(String label, String hex) {
        JLabel l = new JLabel(" " + label);
        l.setForeground(Color.decode("#F8FAFC"));
        l.setIcon(new com.formdev.flatlaf.icons.FlatTreeLeafIcon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(Color.decode(hex));
                g.fillOval(x, y, 12, 12);
            }
        });
        return l;
    }
    
    private void loadSeats() {
        List<Seat> seats = roomService.getSeatsByRoom(roomId);
        if(seats.isEmpty()) return;
        
        int maxRows = 0;
        int maxCols = 0;
        for(Seat s : seats) {
            maxRows = Math.max(maxRows, s.getRowChar().charAt(0) - 'A' + 1);
            maxCols = Math.max(maxCols, s.getNumber());
        }
        
        mapPanel.setLayout(new GridLayout(maxRows, maxCols, 8, 8));
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
                    btn.setPreferredSize(new Dimension(50, 50));
                    btn.setFont(new Font("Inter", Font.BOLD, 10));
                    btn.setForeground(Color.WHITE);
                    btn.setFocusPainted(false);
                    btn.setBorderPainted(false);
                    btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
                    
                    switch(s.getSeatType()) {
                        case NORMAL -> btn.setBackground(Color.decode("#6366F1")); // Indigo
                        case VIP -> btn.setBackground(Color.decode("#A855F7")); // Purple
                        case SWEETBOX -> btn.setBackground(Color.decode("#EC4899")); // Pink
                    }
                    
                    btn.addActionListener(e -> {
                        s.setSeatType(s.getSeatType() == SeatType.NORMAL ? SeatType.VIP :
                                    (s.getSeatType() == SeatType.VIP ? SeatType.SWEETBOX : SeatType.NORMAL));
                        roomService.updateSeat(s);
                        loadSeats(); // Refresh colors
                    });
                    mapPanel.add(btn);
                }
            }
        }
        mapPanel.revalidate();
        mapPanel.repaint();
    }
}
