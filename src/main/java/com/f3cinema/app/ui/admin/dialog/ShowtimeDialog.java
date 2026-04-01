package com.f3cinema.app.ui.admin.dialog;

import com.f3cinema.app.dto.MovieSummaryDTO;
import com.f3cinema.app.dto.RoomSummaryDTO;
import com.f3cinema.app.dto.ShowtimeCreateDTO;
import com.f3cinema.app.entity.Showtime;
import com.f3cinema.app.service.MovieService;
import com.f3cinema.app.service.ShowtimeService;
import com.f3cinema.app.ui.common.dialog.BaseAppDialog;
import com.f3cinema.app.ui.common.dialog.DialogStyle;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * ShowtimeDialog — Thêm / Sửa suất chiếu.
 * Dùng CalendarPopup nội bộ để chọn ngày + Spinner giờ:phút riêng biệt.
 */
public class ShowtimeDialog extends BaseAppDialog {

    // ── Design Tokens ─────────────────────────────────────────────
    private static final Color BG_SURFACE = new Color(0x1E293B);
    private static final Color BG_ELEVATED = new Color(0x334155);
    private static final Color TEXT_WHITE = new Color(0xF8FAFC);
    private static final Color TEXT_MUTED = new Color(0x94A3B8);
    private static final Color ACCENT = new Color(0x6366F1);
    private static final Color DANGER = new Color(0xF43F5E);

    private JComboBox<MovieSummaryDTO> cbMovie; // DTO — không expose Entity
    private JComboBox<RoomSummaryDTO> cbRoom; // DTO — không expose Entity

    // Date picker
    private JButton btnDatePicker;
    private LocalDate selectedDate = LocalDate.now();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Time spinner (giờ và phút tách riêng)
    private JSpinner spHour;
    private JSpinner spMinute;

    private JTextField txtPrice;
    private JLabel lblPreview;
    private JLabel lblError;

    private final Showtime editTarget;
    private final ShowtimeService service = ShowtimeService.getInstance();
    private boolean saved = false;

    // ─────────────────────────────────────────────────────────────────────────
    public ShowtimeDialog(Window owner, Showtime showtime) {
        super(owner, showtime == null ? "Thêm Suất Chiếu Mới" : "Chỉnh sửa Suất Chiếu");
        this.editTarget = showtime;
        initUI();
        loadData();
        if (showtime != null)
            prefill();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────────────────────────────────────
    private void initUI() {
        setupBaseDialog(480, 580);
        setLayout(new BorderLayout());

        JPanel glass = createSurfacePanel();
        glass.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        // ── Tiêu đề ──────────────────────────────────────────────
        JLabel title = DialogStyle.titleLabel(editTarget == null ? "Tạo Suất Chiếu" : "Sửa Suất Chiếu");
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        glass.add(title, gbc);

        // ── Phim ─────────────────────────────────────────────────
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        glass.add(formLabel("Phim chiếu *"), gbc);
        cbMovie = new JComboBox<>();
        styleField(cbMovie);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 14, 0);
        glass.add(cbMovie, gbc);

        // ── Phòng ────────────────────────────────────────────────
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 4, 0);
        glass.add(formLabel("Phòng chiếu *"), gbc);
        cbRoom = new JComboBox<>();
        styleField(cbRoom);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 14, 0);
        glass.add(cbRoom, gbc);

        // ── Ngày chiếu ───────────────────────────────────────────
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 4, 0);
        glass.add(formLabel("Ngày chiếu *"), gbc);

        btnDatePicker = new JButton("\uD83D\uDCC5  " + selectedDate.format(DATE_FMT));
        btnDatePicker.setFont(new Font("Inter", Font.PLAIN, 15));
        btnDatePicker.setForeground(TEXT_WHITE);
        btnDatePicker.setBackground(BG_SURFACE);
        btnDatePicker.setHorizontalAlignment(SwingConstants.LEFT);
        btnDatePicker.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDatePicker.setPreferredSize(new Dimension(0, 42));
        btnDatePicker.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; borderWidth: 0; background: #1E293B; foreground: #F8FAFC;");
        btnDatePicker.addActionListener(e -> {
            CalendarPopup popup = new CalendarPopup();
            popup.show(btnDatePicker, 0, btnDatePicker.getHeight() + 4);
        });
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 14, 0);
        glass.add(btnDatePicker, gbc);

        // ── Giờ : Phút ───────────────────────────────────────────
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 4, 0);
        glass.add(formLabel("Thời gian bắt đầu (Giờ : Phút) *"), gbc);

        JPanel timeRow = new JPanel(new GridLayout(1, 3, 10, 0));
        timeRow.setOpaque(false);
        timeRow.setPreferredSize(new Dimension(0, 42));

        spHour = new JSpinner(new SpinnerNumberModel(LocalTime.now().getHour(), 0, 23, 1));
        spMinute = new JSpinner(new SpinnerNumberModel(LocalTime.now().getMinute(), 0, 59, 1));
        styleField(spHour);
        styleField(spMinute);

        // Gắn DocumentFilter: chỉ cho nhập số, tối đa 2 ký tự
        applyTwoDigitFilter(spHour, 0, 23);
        applyTwoDigitFilter(spMinute, 0, 59);

        ((JSpinner.DefaultEditor) spHour.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        ((JSpinner.DefaultEditor) spMinute.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.CENTER);

        JLabel colon = new JLabel(":", SwingConstants.CENTER);
        colon.setFont(new Font("Inter", Font.BOLD, 22));
        colon.setForeground(TEXT_MUTED);

        timeRow.add(spHour);
        timeRow.add(colon);
        timeRow.add(spMinute);

        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 14, 0);
        glass.add(timeRow, gbc);

        // ── Giá vé ───────────────────────────────────────────────
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 4, 0);
        glass.add(formLabel("Giá vé gốc (VNĐ) *"), gbc);
        txtPrice = new JTextField("75000");
        styleField(txtPrice);
        gbc.gridy = 10;
        gbc.insets = new Insets(0, 0, 4, 0);
        glass.add(txtPrice, gbc);

        JPanel templateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        templateRow.setOpaque(false);
        JButton btnMorning = new JButton("Morning");
        JButton btnAfternoon = new JButton("Afternoon");
        JButton btnEvening = new JButton("Evening");
        styleSecondaryChip(btnMorning);
        styleSecondaryChip(btnAfternoon);
        styleSecondaryChip(btnEvening);
        btnMorning.addActionListener(e -> setTemplateTime(9, 0));
        btnAfternoon.addActionListener(e -> setTemplateTime(14, 0));
        btnEvening.addActionListener(e -> setTemplateTime(19, 0));
        templateRow.add(btnMorning);
        templateRow.add(btnAfternoon);
        templateRow.add(btnEvening);
        gbc.gridy = 11;
        gbc.insets = new Insets(0, 0, 8, 0);
        glass.add(templateRow, gbc);

        lblPreview = new JLabel("Preview: --");
        lblPreview.setForeground(TEXT_MUTED);
        lblPreview.setFont(new Font("Inter", Font.PLAIN, 12));
        gbc.gridy = 12;
        gbc.insets = new Insets(0, 0, 6, 0);
        glass.add(lblPreview, gbc);

        // ── Error label ──────────────────────────────────────────
        lblError = new JLabel(" ");
        lblError.setForeground(DANGER);
        lblError.setFont(new Font("Inter", Font.PLAIN, 12));
        gbc.gridy = 13;
        gbc.insets = new Insets(0, 0, 0, 0);
        glass.add(lblError, gbc);

        // ── Footer ───────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);

        JButton btnCancel = DialogStyle.secondaryButton("Hủy");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = DialogStyle.primaryButton("Lưu lại");
        btnSave.addActionListener(e -> handleSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        gbc.gridy = 14;
        gbc.insets = new Insets(16, 0, 0, 0);
        glass.add(footer, gbc);

        add(glass);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INNER CLASS: CalendarPopup (chuyên nghiệp, Midnight Dark Mode)
    // ─────────────────────────────────────────────────────────────────────────
    private class CalendarPopup extends JPopupMenu {

        private YearMonth currentYearMonth;
        private JLabel lblMonthYear;
        private JPanel gridPanel;

        CalendarPopup() {
            currentYearMonth = YearMonth.from(selectedDate);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BG_ELEVATED, 1, true),
                    new EmptyBorder(8, 8, 8, 8)));
            setBackground(BG_SURFACE);
            setLayout(new BorderLayout(0, 8));
            setPreferredSize(new Dimension(320, 310));

            add(buildHeader(), BorderLayout.NORTH);

            gridPanel = new JPanel(new GridLayout(0, 7, 4, 4));
            gridPanel.setBackground(BG_SURFACE);
            add(gridPanel, BorderLayout.CENTER);

            renderGrid();
        }

        private JPanel buildHeader() {
            JPanel header = new JPanel(new BorderLayout(4, 0));
            header.setOpaque(false);

            JButton btnPrevYear = navBtn("«");
            JButton btnNextYear = navBtn("»");
            JButton btnPrevMonth = navBtn("<");
            JButton btnNextMonth = navBtn(">");

            btnPrevYear.addActionListener(e -> {
                currentYearMonth = currentYearMonth.minusYears(1);
                renderGrid();
            });
            btnNextYear.addActionListener(e -> {
                currentYearMonth = currentYearMonth.plusYears(1);
                renderGrid();
            });
            btnPrevMonth.addActionListener(e -> {
                currentYearMonth = currentYearMonth.minusMonths(1);
                renderGrid();
            });
            btnNextMonth.addActionListener(e -> {
                currentYearMonth = currentYearMonth.plusMonths(1);
                renderGrid();
            });

            lblMonthYear = new JLabel("", SwingConstants.CENTER);
            lblMonthYear.setFont(new Font("Inter", Font.BOLD, 14));
            lblMonthYear.setForeground(TEXT_WHITE);

            JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            leftNav.setOpaque(false);
            leftNav.add(btnPrevYear);
            leftNav.add(btnPrevMonth);

            JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
            rightNav.setOpaque(false);
            rightNav.add(btnNextMonth);
            rightNav.add(btnNextYear);

            header.add(leftNav, BorderLayout.WEST);
            header.add(lblMonthYear, BorderLayout.CENTER);
            header.add(rightNav, BorderLayout.EAST);
            return header;
        }

        private void renderGrid() {
            lblMonthYear.setText("Tháng " + currentYearMonth.getMonthValue()
                    + "   Năm " + currentYearMonth.getYear());
            gridPanel.removeAll();

            // Tiêu đề thứ
            for (String d : new String[] { "T2", "T3", "T4", "T5", "T6", "T7", "CN" }) {
                JLabel lbl = new JLabel(d, SwingConstants.CENTER);
                lbl.setFont(new Font("Inter", Font.BOLD, 11));
                lbl.setForeground(TEXT_MUTED);
                gridPanel.add(lbl);
            }

            // Offset Mon=0…Sun=6
            LocalDate firstDay = currentYearMonth.atDay(1);
            int offset = firstDay.getDayOfWeek().getValue() - 1;
            for (int i = 0; i < offset; i++)
                gridPanel.add(new JLabel(""));

            for (int day = 1; day <= currentYearMonth.lengthOfMonth(); day++) {
                LocalDate date = currentYearMonth.atDay(day);
                gridPanel.add(dayBtn(day, date));
            }
            gridPanel.revalidate();
            gridPanel.repaint();
            pack();
        }

        private JButton dayBtn(int day, LocalDate date) {
            JButton btn = new JButton(String.valueOf(day));
            btn.setFont(new Font("Inter", Font.PLAIN, 13));
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setPreferredSize(new Dimension(36, 36));
            btn.setOpaque(true);

            boolean isSel = date.equals(selectedDate);
            btn.setBackground(isSel ? ACCENT : BG_SURFACE);
            btn.setForeground(isSel ? Color.WHITE : TEXT_WHITE);

            // Chặn chọn ngày trong quá khứ
            boolean isPast = date.isBefore(LocalDate.now());
            if (isPast) {
                btn.setForeground(new Color(0x475569)); // Slate 600 — màu mờ
                btn.setCursor(Cursor.getDefaultCursor());
            }

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!isPast && !date.equals(selectedDate))
                        btn.setBackground(BG_ELEVATED);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.setBackground(date.equals(selectedDate) ? ACCENT : BG_SURFACE);
                }
            });

            btn.addActionListener(e -> {
                if (isPast) {
                    lblError.setText("⚠ Không thể chọn ngày đã qua.");
                    return;
                }
                selectedDate = date;
                btnDatePicker.setText("\uD83D\uDCC5  " + selectedDate.format(DATE_FMT));
                lblError.setText(" ");
                setVisible(false);
            });
            return btn;
        }

        private JButton navBtn(String text) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Inter", Font.BOLD, 13));
            btn.setForeground(TEXT_WHITE);
            btn.setBackground(BG_SURFACE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setOpaque(true);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(30, 28));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    btn.setBackground(BG_ELEVATED);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.setBackground(BG_SURFACE);
                }
            });
            return btn;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DATA — dùng DTO, không để Entity lộ ra UI (Backend Standards §3.2)
    // ─────────────────────────────────────────────────────────────────────────
    private void loadData() {
        // ComboBox Phim — dùng MovieSummaryDTO
        var movies = MovieService.getInstance().getMovieSummaries();
        cbMovie.setModel(new DefaultComboBoxModel<>(movies.toArray(new MovieSummaryDTO[0])));
        // MovieSummaryDTO.toString() trả về title nên không cần renderer tùy chỉnh

        // ComboBox Phòng — dùng RoomSummaryDTO
        var rooms = ShowtimeService.getInstance().getRoomSummaries();
        cbRoom.setModel(new DefaultComboBoxModel<>(rooms.toArray(new RoomSummaryDTO[0])));
        // RoomSummaryDTO.toString() trả về "TênPhòng (TYPE)" nên không cần renderer
    }

    private void prefill() {
        // Map Entity → DTO để select đúng mục trong ComboBox
        var movie = editTarget.getMovie();
        for (int i = 0; i < cbMovie.getModel().getSize(); i++) {
            if (cbMovie.getModel().getElementAt(i).id().equals(movie.getId())) {
                cbMovie.setSelectedIndex(i);
                break;
            }
        }
        var room = editTarget.getRoom();
        for (int i = 0; i < cbRoom.getModel().getSize(); i++) {
            if (cbRoom.getModel().getElementAt(i).id().equals(room.getId())) {
                cbRoom.setSelectedIndex(i);
                break;
            }
        }
        selectedDate = editTarget.getStartTime().toLocalDate();
        btnDatePicker.setText("\uD83D\uDCC5  " + selectedDate.format(DATE_FMT));
        spHour.setValue(editTarget.getStartTime().getHour());
        spMinute.setValue(editTarget.getStartTime().getMinute());
        txtPrice.setText(editTarget.getBasePrice().toString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VALIDATION & SAVE
    // ─────────────────────────────────────────────────────────────────────────
    private void handleSave() {
        lblError.setText(" ");
        updatePreview();

        // 1. Kiểm tra Phim
        MovieSummaryDTO movie = (MovieSummaryDTO) cbMovie.getSelectedItem();
        if (movie == null || movie.id() == null) {
            lblError.setText("⚠ Vui lòng chọn Phim chiếu.");
            return;
        }

        // 2. Kiểm tra Phòng
        RoomSummaryDTO room = (RoomSummaryDTO) cbRoom.getSelectedItem();
        if (room == null) {
            lblError.setText("⚠ Vui lòng chọn Phòng chiếu.");
            return;
        }

        // 3. Kiểm tra Ngày
        if (selectedDate == null) {
            lblError.setText("⚠ Vui lòng chọn Ngày chiếu.");
            return;
        }

        // 4. Kiểm tra Giá vé
        BigDecimal price;
        try {
            price = new BigDecimal(txtPrice.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            lblError.setText("⚠ Giá vé phải là số dương hợp lệ.");
            return;
        }

        // 5. Kiểm tra & lấy Giờ / Phút
        int hour, minute;
        try {
            spHour.commitEdit();
            spMinute.commitEdit();
            Object hVal = spHour.getValue();
            Object mVal = spMinute.getValue();
            if (hVal == null || mVal == null) throw new IllegalStateException();
            hour   = (int) hVal;
            minute = (int) mVal;
        } catch (Exception e) {
            lblError.setText("⚠ Giờ hoặc Phút không hợp lệ.");
            return;
        }
        if (hour < 0 || hour > 23) {
            lblError.setText("⚠ Giờ phải từ 00 đến 23.");
            return;
        }
        if (minute < 0 || minute > 59) {
            lblError.setText("⚠ Phút phải từ 00 đến 59.");
            return;
        }
        LocalDateTime startTime = LocalDateTime.of(selectedDate, LocalTime.of(hour, minute));

        // 6. Chặn quá khứ CHỈ khi TẠO MỚI (buffer 5 phút)
        // Khi EDIT: bỏ qua — cho phép sửa giá/phòng dù suất đã/đang chiếu
        if (editTarget == null && startTime.isBefore(LocalDateTime.now().minusMinutes(5))) {
            lblError.setText("⚠ Không thể tạo suất chiếu trong quá khứ.");
            return;
        }

        // 7. Gom vào DTO và gửi xuống Service (Service tự tính endTime & check
        // conflict)
        ShowtimeCreateDTO dto = new ShowtimeCreateDTO(movie.id(), room.id(), startTime, price);
        try {
            if (editTarget == null)
                service.createFromDTO(dto);
            else
                service.updateFromDTO(editTarget.getId(), dto);

            saved = true;
            dispose();
        } catch (Exception e) {
            lblError.setText("⚠ " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STYLE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gắn DocumentFilter lên TextField bên trong JSpinner để:
     * - Chỉ cho nhập ký tự số (0-9).
     * - Tối đa 2 ký tự.
     * - Giá trị nhập phải nằm trong [min, max]; nếu không báo lỗi ngay.
     */
    private void applyTwoDigitFilter(JSpinner spinner, int min, int max) {
        JTextField tf = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {

            private void handle(FilterBypass fb, int offset, int removeLen,
                    String text, AttributeSet attr,
                    boolean insert) throws javax.swing.text.BadLocationException {
                // Lấy nội dung hiện tại sau khi áp dụng thay đổi
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String result;
                if (insert) {
                    result = current.substring(0, offset) + text + current.substring(offset);
                } else {
                    String after = current.substring(0, offset)
                            + current.substring(offset + removeLen);
                    result = text == null ? after : after.substring(0, offset) + text + after.substring(offset);
                }

                // Chặn ký tự không phải số
                if (!result.matches("\\d*")) {
                    lblError.setText("⚠ Chỉ được nhập chữ số (0–9).");
                    return;
                }
                // Chặn quá 2 ký tự
                if (result.length() > 2) {
                    lblError.setText("⚠ Tối đa 2 chữ số.");
                    return;
                }
                // Kiểm tra khoảng hợp lệ (nếu đã nhập đủ số)
                if (!result.isEmpty()) {
                    int val = Integer.parseInt(result);
                    if (val < min || val > max) {
                        lblError.setText("⚠ Giá trị phải từ "
                                + String.format("%02d", min) + " đến "
                                + String.format("%02d", max) + ".");
                        return;
                    }
                }

                lblError.setText(" "); // Xóa lỗi nếu hợp lệ
                if (insert) {
                    fb.insertString(offset, text, attr);
                } else {
                    fb.replace(offset, removeLen, text, attr);
                }
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String text,
                    AttributeSet attr)
                    throws javax.swing.text.BadLocationException {
                handle(fb, offset, 0, text, attr, true);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text,
                    AttributeSet attrs)
                    throws javax.swing.text.BadLocationException {
                handle(fb, offset, length, text, attrs, false);
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length)
                    throws javax.swing.text.BadLocationException {
                lblError.setText(" ");
                fb.remove(offset, length);
            }
        });
    }

    private JLabel formLabel(String text) {
        return DialogStyle.formLabel(text);
    }

    private void styleField(JComponent c) {
        c.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; background: #1E293B; foreground: #F8FAFC; focusWidth: 2;");
        c.setFont(new Font("Inter", Font.PLAIN, 15));
        c.setPreferredSize(new Dimension(0, 42));
    }

    private void styleSecondaryChip(JButton b) {
        b.setFont(new Font("Inter", Font.PLAIN, 12));
        b.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #334155; borderWidth: 0;");
        b.setForeground(TEXT_WHITE);
    }

    private void setTemplateTime(int hour, int minute) {
        spHour.setValue(hour);
        spMinute.setValue(minute);
        updatePreview();
    }

    private void updatePreview() {
        int hour = (int) spHour.getValue();
        int minute = (int) spMinute.getValue();
        lblPreview.setText("Preview: " + selectedDate.format(DATE_FMT) + " " + String.format("%02d:%02d", hour, minute));
    }

    public boolean isSaved() {
        return saved;
    }
}
