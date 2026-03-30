package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.entity.Room;
import com.f3cinema.app.entity.enums.RoomType;
import com.f3cinema.app.service.RoomService;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RoomDialog extends JDialog {
    private final RoomPanel parent;
    private JTextField txtName;
    private JComboBox<RoomType> cbType;
    private JSpinner spinRows, spinCols;
    private JPanel previewPanel;
    
    public RoomDialog(JFrame owner, Room room, RoomPanel parent) {
        super(owner, "Thiết lập cấu hình phòng", true);
        this.parent = parent;
        setSize(800, 720);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Color.decode("#0F172A")); // Slate 900
        
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        // ------------- HEADER -------------
        JLabel lblHeader = new JLabel("CẤU HÌNH PHÒNG MỚI");
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
        
        form.add(createFieldGroup("Tên phòng chiếu", txtName = new JTextField()));
        
        cbType = new JComboBox<>(RoomType.values());
        form.add(createFieldGroup("Loại phòng chiếu", cbType));
        
        spinRows = new JSpinner(new SpinnerNumberModel(10, 1, 30, 1));
        form.add(createFieldGroup("Số hàng (Từ dòng A)", spinRows));
        
        spinCols = new JSpinner(new SpinnerNumberModel(12, 1, 50, 1));
        form.add(createFieldGroup("Số ghế mỗi hàng", spinCols));
        
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
        
        JButton btnPreview = new JButton("Tải Lại Xem Trước");
        btnPreview.setFont(new Font("Inter", Font.BOLD, 14));
        btnPreview.setFocusPainted(false);
        btnPreview.putClientProperty(FlatClientProperties.STYLE, "arc: 12; foreground: #F8FAFC; background: #334155; borderWidth: 0; padding: 12,24,12,24");
        btnPreview.addActionListener(e -> renderPreview());
        btnPreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPanel.add(btnPreview);
        
        JButton btnSave = new JButton("Xác Nhận & Tạo Sơ Đồ");
        btnSave.setFont(new Font("Inter", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 12; foreground: #FFFFFF; background: #6366F1; borderWidth: 0; padding: 12,24,12,24");
        btnSave.addActionListener(e -> saveRoom());
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPanel.add(btnSave);
        
        mainContent.add(btnPanel);
        add(mainContent);
        
        renderPreview(); // render instantly
    }

    private JPanel createFieldGroup(String labelText, JComponent inputComp) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Inter", Font.PLAIN, 13));
        lbl.setForeground(Color.decode("#94A3B8"));
        
        if (inputComp instanceof JTextField || inputComp instanceof JComboBox || inputComp instanceof JSpinner) {
            inputComp.putClientProperty(FlatClientProperties.STYLE, "arc: 12; padding: 6,12,6,12; background: #1E293B; foreground: #F8FAFC; borderColor: #334155");
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
        int rows = (int)spinRows.getValue();
        int cols = (int)spinCols.getValue();
        previewPanel.setLayout(new GridLayout(rows, cols, 6, 6)); 
        for (int r = 0; r < rows; r++) {
            for (int c = 1; c <= cols; c++) {
                JButton btn = new JButton(String.valueOf(c));
                btn.setPreferredSize(new Dimension(40, 40));
                btn.setFont(new Font("Inter", Font.BOLD, 11));
                btn.setForeground(Color.decode("#FFFFFF"));
                btn.setFocusPainted(false);
                btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8; background: #6366F1; borderWidth: 0");
                btn.setToolTipText("Hàng " + (char)('A'+r) + " - Ghế " + c);
                previewPanel.add(btn);
            }
        }
        previewPanel.revalidate();
        previewPanel.repaint();
    }
    
    private void saveRoom() {
        if(txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên phòng chiếu!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Room r = new Room();
        RoomService service = RoomService.getInstance();
        r.setName(txtName.getText());
        r.setRoomType((RoomType) cbType.getSelectedItem());
        
        service.saveRoomWithSeats(r, (int)spinRows.getValue(), (int)spinCols.getValue());
        parent.loadData();
        dispose();
    }
}
