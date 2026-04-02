package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.entity.Room;
import com.f3cinema.app.entity.enums.RoomType;
import com.f3cinema.app.service.RoomService;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Collectors;

public class RoomPanel extends BaseDashboardModule {
    private final RoomService roomService = RoomService.getInstance();
    private JPanel gridPanel;
    private JTextField txtSearch;
    private JComboBox<Object> cmbType;
    private List<Room> allRooms = List.of();

    public RoomPanel() {
        super("Phòng & Ghế", "Home > Rooms & Seats");
        setupUI();
        loadData();
    }

    private void setupUI() {
        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 24, 16, 24));

        JPanel controlBar = new JPanel(new BorderLayout(16, 0));
        controlBar.setBackground(ThemeConfig.BG_CARD);
        controlBar.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        controlBar.putClientProperty(FlatClientProperties.STYLE, "arc: 20");

        txtSearch = new JTextField(20);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tim phong...");
        txtSearch.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; background: #0F172A; foreground: #F8FAFC; margin: 6,10,6,10;");
        txtSearch.setFont(ThemeConfig.FONT_BODY);
        txtSearch.setPreferredSize(new Dimension(260, 40));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });

        cmbType = new JComboBox<>();
        cmbType.addItem("All Types");
        for (RoomType type : RoomType.values()) cmbType.addItem(type);
        cmbType.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof RoomType roomType) setText(roomType.getLabel());
                return this;
            }
        });
        cmbType.setPreferredSize(new Dimension(180, 40));
        cmbType.setFont(ThemeConfig.FONT_BODY);
        cmbType.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #0F172A; foreground: #F8FAFC;");
        cmbType.addActionListener(e -> applyFilters());

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftGroup.setOpaque(false);
        leftGroup.add(txtSearch);
        leftGroup.add(cmbType);

        JButton btnAdd = new JButton("Them phong");
        btnAdd.setBackground(ThemeConfig.ACCENT_COLOR);
        btnAdd.setForeground(ThemeConfig.TEXT_PRIMARY);
        btnAdd.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
        btnAdd.putClientProperty(FlatClientProperties.STYLE, "arc: 15; borderWidth: 0; margin: 4,18,4,18;");
        btnAdd.addActionListener(e -> {
            RoomDialog dialog = new RoomDialog((JFrame) SwingUtilities.getWindowAncestor(this), null, this);
            dialog.setVisible(true);
        });

        controlBar.add(leftGroup, BorderLayout.WEST);
        controlBar.add(btnAdd, BorderLayout.EAST);
        toolbar.add(controlBar, BorderLayout.CENTER);
        contentBody.add(toolbar, BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        gridPanel.setLayout(new com.f3cinema.app.util.WrapLayout(FlowLayout.LEFT, 24, 24));

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(true);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(ThemeConfig.BG_MAIN);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "trackArc: 999; thumbArc: 999;");

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
                    allRooms = get();
                    applyFilters();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void applyFilters() {
        gridPanel.removeAll();
        String keyword = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        Object typeFilter = cmbType != null ? cmbType.getSelectedItem() : "All Types";
        List<Room> filtered = allRooms.stream()
                .filter(r -> keyword.isEmpty() || (r.getName() != null && r.getName().toLowerCase().contains(keyword)))
                .filter(r -> !(typeFilter instanceof RoomType t) || r.getRoomType() == t)
                .collect(Collectors.toList());

        for (Room r : filtered) {
            int count = roomService.getSeatsByRoom(r.getId()).size();
            RoomCard card = new RoomCard(r, count,
                    () -> editRoom(r.getId()),
                    () -> deleteRoom(r.getId()),
                    () -> viewSeatMap(r.getId()));
            gridPanel.add(card);
        }
        int prefHeight = (Math.max(filtered.size(), 1) / 3 + 1) * 230;
        gridPanel.setPreferredSize(new Dimension(gridPanel.getParent() != null ? gridPanel.getParent().getWidth() - 20 : 1000, Math.max(prefHeight, 600)));
        gridPanel.revalidate();
        gridPanel.repaint();
    }
    
    private void deleteRoom(Long id) {
        Room r = new Room(); r.setId(id);
        roomService.deleteRoom(r);
        loadData();
    }
    
    private void editRoom(Long id) {
        Room room = roomService.getRoomById(id);
        if (room == null) {
            return;
        }
        if (room.getSeats() == null || room.getSeats().isEmpty()) {
            room.setSeats(roomService.getSeatsByRoom(id));
        }
        RoomDialog dialog = new RoomDialog((JFrame) SwingUtilities.getWindowAncestor(this), room, this);
        dialog.setVisible(true);
    }

    private void viewSeatMap(Long id) {
        SeatMapDialog d = new SeatMapDialog((JFrame) SwingUtilities.getWindowAncestor(this), id);
        d.setVisible(true);
    }
}
