package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.entity.Room;
import com.f3cinema.app.service.RoomService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RoomPanel extends BaseDashboardModule {
    private final RoomService roomService = new RoomService();
    private JPanel gridPanel;

    public RoomPanel() {
        super("Phòng & Ghế", "Home > Rooms & Seats");
        setupUI();
        loadData();
    }

    private void setupUI() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        
        JButton btnAdd = new JButton("Thêm Phòng");
        btnAdd.setBackground(Color.decode("#6366F1"));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Inter", Font.BOLD, 14));
        btnAdd.setFocusPainted(false);
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        
        btnAdd.addActionListener(e -> {
            RoomDialog dialog = new RoomDialog((JFrame) SwingUtilities.getWindowAncestor(this), null, this);
            dialog.setVisible(true);
        });
        btnPanel.add(btnAdd);
        
        contentBody.add(btnPanel, BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        gridPanel.setLayout(new com.f3cinema.app.util.WrapLayout(FlowLayout.LEFT, 20, 20)); // WrapLayout fixes JScrollPane overflow
        
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.decode("#1E293B")); // match contentBody gradient visually or just transparent
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        contentBody.add(scrollPane, BorderLayout.CENTER);
    }
    
    public void loadData() {
        gridPanel.removeAll();
        gridPanel.revalidate();
        gridPanel.repaint();
        
        SwingWorker<List<Room>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Room> doInBackground() {
                return roomService.getAllRooms();
            }
            @Override
            protected void done() {
                try {
                    List<Room> items = get();
                    for(Room r : items) {
                        int count = roomService.getSeatsByRoom(r.getId()).size();
                        RoomCard card = new RoomCard(r, count, 
                            () -> editRoom(r.getId()),
                            () -> deleteRoom(r.getId()),
                            () -> viewSeatMap(r.getId())
                        );
                        gridPanel.add(card);
                    }
                    int prefHeight = (items.size() / 4 + 1) * 200;
                    gridPanel.setPreferredSize(new Dimension(gridPanel.getParent().getWidth() - 20, Math.max(prefHeight, 600)));
                    gridPanel.revalidate();
                    gridPanel.repaint();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void deleteRoom(Long id) {
        Room r = new Room(); r.setId(id);
        roomService.deleteRoom(r);
        loadData();
    }
    
    private void editRoom(Long id) {
        // Can be implemented similarly to RoomDialog if passing Room obj
    }

    private void viewSeatMap(Long id) {
        SeatMapDialog d = new SeatMapDialog((JFrame) SwingUtilities.getWindowAncestor(this), id);
        d.setVisible(true);
    }
}
