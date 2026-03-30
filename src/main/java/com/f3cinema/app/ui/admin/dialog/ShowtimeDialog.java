package com.f3cinema.app.ui.admin.dialog;

import com.f3cinema.app.entity.Movie;
import com.f3cinema.app.entity.Room;
import com.f3cinema.app.entity.Showtime;
import com.f3cinema.app.service.MovieService;
import com.f3cinema.app.service.RoomService;
import com.f3cinema.app.service.ShowtimeService;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;

/**
 * ShowtimeDialog - Cửa sổ thêm/sửa suất chiếu.
 * Sử dụng JSpinner cho thời gian và JComboBox cho Phim/Phòng.
 */
public class ShowtimeDialog extends JDialog {

    private JComboBox<Movie> cbMovie;
    private JComboBox<Room> cbRoom;
    private JSpinner spinnerStart;
    private JTextField txtPrice;
    private JLabel lblError;
    
    private final Showtime editTarget;
    private final ShowtimeService service = ShowtimeService.getInstance();
    private boolean saved = false;

    public ShowtimeDialog(Window owner, Showtime showtime) {
        super(owner, showtime == null ? "Thêm Suất Chiếu Mới" : "Chỉnh sửa Suất Chiếu", ModalityType.APPLICATION_MODAL);
        this.editTarget = showtime;
        initUI();
        loadData();
        if (showtime != null) prefill();
    }

    private void initUI() {
        setUndecorated(true);
        setSize(450, 520);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel glass = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59, 240)); // Slate 800 blur
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(255, 255, 255, 30));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
            }
        };
        glass.setOpaque(false);
        glass.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 0, 4, 0);

        JLabel title = new JLabel(editTarget == null ? "Tạo Suất Chiếu" : "Sửa Suất Chiếu");
        title.setFont(new Font("Inter", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        gbc.gridy = 0; glass.add(title, gbc);

        // Phim
        gbc.gridy = 1; glass.add(createLabel("Phim chiếu *"), gbc);
        cbMovie = new JComboBox<>();
        gbc.gridy = 2; glass.add(cbMovie, gbc);

        // Phòng
        gbc.gridy = 3; glass.add(createLabel("Phòng chiếu *"), gbc);
        cbRoom = new JComboBox<>();
        gbc.gridy = 4; glass.add(cbRoom, gbc);

        // Bắt đầu
        gbc.gridy = 5; glass.add(createLabel("Thời điểm bắt đầu *"), gbc);
        spinnerStart = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerStart, "dd/MM/yyyy HH:mm");
        spinnerStart.setEditor(editor);
        gbc.gridy = 6; glass.add(spinnerStart, gbc);

        // Giá vé
        gbc.gridy = 7; glass.add(createLabel("Giá vé gốc (VNĐ) *"), gbc);
        txtPrice = new JTextField("75000");
        gbc.gridy = 8; glass.add(txtPrice, gbc);

        // Error
        lblError = new JLabel(" "); // Placeholder
        lblError.setForeground(new Color(244, 63, 94));
        lblError.setFont(new Font("Inter", Font.PLAIN, 12));
        gbc.gridy = 9; glass.add(lblError, gbc);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        JButton btnCancel = new JButton("Hủy");
        styleSecondary(btnCancel);
        btnCancel.addActionListener(e -> dispose());
        
        JButton btnSave = new JButton("Lưu lại");
        stylePrimary(btnSave);
        btnSave.addActionListener(e -> handleSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        gbc.gridy = 10; gbc.insets = new Insets(20, 0, 0, 0);
        glass.add(footer, gbc);

        add(glass);
        
        // Styling ComboBox/Spinner
        styleComponent(cbMovie);
        styleComponent(cbRoom);
        styleComponent(spinnerStart);
        styleComponent(txtPrice);
    }

    private void loadData() {
        cbMovie.setModel(new DefaultComboBoxModel<>(MovieService.getInstance().getAllMovies().toArray(new Movie[0])));
        cbMovie.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Movie m) setText(m.getTitle());
                return this;
            }
        });

        cbRoom.setModel(new DefaultComboBoxModel<>(RoomService.getInstance().getAllRooms().toArray(new Room[0])));
        cbRoom.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Room r) setText(r.getName() + " (" + r.getRoomType() + ")");
                return this;
            }
        });
    }

    private void prefill() {
        cbMovie.setSelectedItem(editTarget.getMovie());
        cbRoom.setSelectedItem(editTarget.getRoom());
        spinnerStart.setValue(Date.from(editTarget.getStartTime().atZone(ZoneId.systemDefault()).toInstant()));
        txtPrice.setText(editTarget.getBasePrice().toString());
    }

    private void handleSave() {
        try {
            Showtime s = (editTarget != null) ? editTarget : new Showtime();
            s.setMovie((Movie) cbMovie.getSelectedItem());
            s.setRoom((Room) cbRoom.getSelectedItem());
            
            Date d = (Date) spinnerStart.getValue();
            s.setStartTime(d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            
            try {
                s.setBasePrice(new BigDecimal(txtPrice.getText().trim()));
            } catch (Exception e) {
                throw new Exception("Giá vé không hợp lệ");
            }

            if (editTarget == null) service.addShowtime(s);
            else service.updateShowtime(s);

            saved = true;
            dispose();
        } catch (Exception e) {
            lblError.setText("⚠ " + e.getMessage());
        }
    }

    private JLabel createLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(new Color(148, 163, 184));
        l.setFont(new Font("Inter", Font.BOLD, 12));
        return l;
    }

    private void styleComponent(JComponent c) {
         c.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #1E293B; foreground: #F8FAFC; focusWidth: 2;");
         c.setFont(new Font("Inter", Font.PLAIN, 15));
         c.setPreferredSize(new Dimension(0, 42));
    }

    private void stylePrimary(JButton b) {
        b.setBackground(Color.decode("#6366F1"));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Inter", Font.BOLD, 14));
        b.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0; margin: 8,24,8,24");
    }

    private void styleSecondary(JButton b) {
        b.setForeground(new Color(148, 163, 184));
        b.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        b.setFont(new Font("Inter", Font.BOLD, 14));
    }

    public boolean isSaved() { return saved; }
}
