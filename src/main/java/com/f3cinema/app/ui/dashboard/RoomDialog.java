package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.entity.Room;
import com.f3cinema.app.entity.enums.RoomType;
import com.f3cinema.app.service.RoomService;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RoomDialog extends JDialog {
    private final RoomPanel roomPanel;
    private final Room editTarget;
    private JTextField txtName;
    private JComboBox<RoomType> cbType;
    private JComboBox<RoomTemplate> cbTemplate;
    private JPanel previewPanel;

    // ── 2. Khuôn mẫu phòng chuẩn ──────────────────────────────────
    public record RoomTemplate(String name, int capacity, int rows, int cols) {
        @Override
        public String toString() {
            return String.format("%s (%d ghế — %dx%d)", name, capacity, rows, cols);
        }
    }

    private static final RoomTemplate[] TEMPLATES = {
        new RoomTemplate("Phòng Nhỏ", 60, 6, 10),
        new RoomTemplate("Phòng Tiêu Chuẩn", 100, 10, 10),
        new RoomTemplate("Phòng Lớn / IMAX", 160, 10, 16)
    };

    public RoomDialog(JFrame owner, Room room, RoomPanel parent) {
        super(owner, room == null ? "Thêm Phòng Chiếu Mới" : "Cấu hình Phòng Chiếu", true);
        this.roomPanel = parent;
        this.editTarget = room;
        setSize(840, 750);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Color.decode("#0F172A")); // Slate 900

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ------------- HEADER -------------
        JLabel lblHeader = new JLabel(room == null ? "CẤU HÌNH PHÒNG MỚI" : "CHỈNH SỬA PHÒNG");
        lblHeader.setFont(new Font("Inter", Font.BOLD, 22));
        lblHeader.setForeground(Color.decode("#F8FAFC"));
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(lblHeader);
        mainContent.add(Box.createRigidArea(new Dimension(0, 24)));

        // ------------- FORM -------------
        JPanel form = new JPanel(new GridLayout(2, 2, 24, 20));
        form.setOpaque(false);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setMaximumSize(new Dimension(1000, 160));

        form.add(createFieldGroup("Tên phòng chiếu *", txtName = new JTextField()));

        cbType = new JComboBox<>(RoomType.values());
        form.add(createFieldGroup("Loại phòng chiếu *", cbType));

        // Thay input tự do bằng ComboBox Template
        cbTemplate = new JComboBox<>(TEMPLATES);
        cbTemplate.addActionListener(e -> renderPreview());
        form.add(createFieldGroup("Cấu hình sơ đồ ghế *", cbTemplate));

        mainContent.add(form);
        mainContent.add(Box.createRigidArea(new Dimension(0, 36)));

        // ------------- PREVIEW SECTION -------------
        JLabel lblPreviewHeader = new JLabel("BẢN XEM TRƯỚC SƠ ĐỒ GHẾ");
        lblPreviewHeader.setFont(new Font("Inter", Font.BOLD, 14));
        lblPreviewHeader.setForeground(Color.decode("#94A3B8"));
        lblPreviewHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(lblPreviewHeader);
        mainContent.add(Box.createRigidArea(new Dimension(0, 16)));

        previewPanel = new JPanel();
        previewPanel.setOpaque(false);

        JPanel previewContainer = new JPanel(new BorderLayout());
        previewContainer.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #1E293B; border: 1,1,1,1, #334155");
        previewContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        previewContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(previewPanel);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        previewContainer.add(scrollPane, BorderLayout.CENTER);

        mainContent.add(previewContainer);
        mainContent.add(Box.createRigidArea(new Dimension(0, 24)));

        // ------------- BUTTONS -------------
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Inter", Font.BOLD, 14));
        btnCancel.setFocusPainted(false);
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 12; foreground: #94A3B8; background: #334155; borderWidth: 0; padding: 12,24,12,24");
        btnCancel.addActionListener(e -> dispose());
        btnPanel.add(btnCancel);

        JButton btnSave = new JButton(room == null ? "Xác Nhận & Tạo Sơ Đồ" : "Cập Nhật Phòng");
        btnSave.setFont(new Font("Inter", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; foreground: #FFFFFF; background: #6366F1; borderWidth: 0; padding: 12,24,12,24");
        btnSave.addActionListener(e -> saveRoom());
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPanel.add(btnSave);

        mainContent.add(btnPanel);
        add(mainContent);

        if (editTarget != null) {
            prefill();
        }
        renderPreview(); 
    }

    private void prefill() {
        txtName.setText(editTarget.getName());
        cbType.setSelectedItem(editTarget.getRoomType());
        
        // Dựa vào số lượng ghế hiện tại để chọn Template khớp nhất
        int seatCount = (editTarget.getSeats() != null) ? editTarget.getSeats().size() : 0;
        for (RoomTemplate t : TEMPLATES) {
            if (t.capacity() == seatCount) {
                cbTemplate.setSelectedItem(t);
                break;
            }
        }
    }

    private JPanel createFieldGroup(String labelText, JComponent inputComp) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Inter", Font.BOLD, 12));
        lbl.setForeground(Color.decode("#94A3B8"));

        if (inputComp instanceof JTextField || inputComp instanceof JComboBox || inputComp instanceof JSpinner) {
            inputComp.putClientProperty(FlatClientProperties.STYLE, "arc: 12; padding: 6,12,6,12; background: #1E293B; foreground: #F8FAFC; borderColor: #334155; focusWidth: 2");
            inputComp.setFont(new Font("Inter", Font.PLAIN, 15));
            if (inputComp instanceof JTextField) {
                ((JTextField) inputComp).setCaretColor(Color.WHITE);
            }
        }

        p.add(lbl, BorderLayout.NORTH);
        p.add(inputComp, BorderLayout.CENTER);
        return p;
    }

    private void renderPreview() {
        previewPanel.removeAll();
        RoomTemplate template = (RoomTemplate) cbTemplate.getSelectedItem();
        if (template == null) return;

        int rows = template.rows();
        int cols = template.cols();
        previewPanel.setLayout(new GridLayout(rows, cols, 6, 6));
        for (int r = 0; r < rows; r++) {
            for (int c = 1; c <= cols; c++) {
                JButton btn = new JButton(String.valueOf(c));
                btn.setPreferredSize(new Dimension(36, 36));
                btn.setFont(new Font("Inter", Font.BOLD, 10));
                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);
                btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #6366F1; borderWidth: 0");
                previewPanel.add(btn);
            }
        }
        previewPanel.revalidate();
        previewPanel.repaint();
    }

    private void saveRoom() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên phòng chiếu!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        RoomTemplate template = (RoomTemplate) cbTemplate.getSelectedItem();
        RoomService service = RoomService.getInstance();
        
        Room r = (editTarget != null) ? editTarget : new Room();
        r.setName(txtName.getText().trim());
        r.setRoomType((RoomType) cbType.getSelectedItem());

        if (editTarget == null) {
            service.saveRoomWithSeats(r, template.rows(), template.cols());
        } else {
            service.updateRoom(r);
            // Lưu ý: Thường không đổi sơ đồ ghế khi đang Edit phòng đã có lịch chiếu để tránh data integrity
        }
        
        roomPanel.loadData();
        dispose();
    }
}
