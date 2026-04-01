package com.f3cinema.app.ui.admin.dialog;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.entity.Movie;
import com.f3cinema.app.entity.enums.MovieStatus;
import com.f3cinema.app.service.MovieService;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import com.f3cinema.app.entity.Genre;
import com.f3cinema.app.service.GenreService;

/**
 * MovieDialog — Add / Edit Movie form.
 * Updated to include Movie Poster URL field.
 */
public class MovieDialog extends JDialog {

    private static final Color C_BG_SURFACE  = new Color(30, 41, 59, 220);
    private static final Color C_ACCENT      = ThemeConfig.ACCENT_COLOR;
    private static final Color C_DANGER      = ThemeConfig.TEXT_DANGER;
    private static final Color C_TEXT_PRIMARY= ThemeConfig.TEXT_PRIMARY;
    private static final Color C_TEXT_HINT   = ThemeConfig.TEXT_SECONDARY;
    private static final Color C_BORDER      = new Color(255, 255, 255, 25);

    private JTextField txtTitle;
    private JTextField txtPosterUrl;
    private JTextField txtDuration;
    private JComboBox<MovieStatus> cmbStatus;
    private JLabel lblPosterPreview;
    private JLabel lblError;
    private List<JCheckBox> genreCheckboxes = new ArrayList<>();

    private final Movie editTarget;
    private final MovieService movieService;
    private boolean saved = false;

    public MovieDialog(Window owner, Movie movie, MovieService movieService) {
        super(owner, movie == null ? "Thêm Phim Mới" : "Chỉnh sửa Phim", ModalityType.APPLICATION_MODAL);
        this.editTarget = movie;
        this.movieService = movieService;
        initUI();
        if (movie != null) prefillData(movie);
        setupKeyBindings();
    }

    public boolean isSaved() { return saved; }

    private void initUI() {
        setUndecorated(true);
        setSize(500, 640);
        setLocationRelativeTo(getOwner());
        setBackground(new Color(0, 0, 0, 0));

        JPanel glass = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 24, 24);
                g2.setStroke(new BasicStroke(1.2f));
                g2.setColor(C_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 7, getHeight() - 7, 24, 24);
                g2.setColor(C_ACCENT);
                g2.fillRoundRect(0, 0, getWidth() - 6, 4, 4, 4);
                g2.dispose();
            }
        };
        glass.setOpaque(false);
        glass.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        JLabel lblTitle = new JLabel(editTarget == null ? "Thêm Phim Mới" : "Chỉnh sửa Phim");
        lblTitle.setFont(ThemeConfig.FONT_H1);
        lblTitle.setForeground(C_TEXT_PRIMARY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;

        // Title
        gbc.insets = new Insets(10, 0, 4, 0);
        gbc.gridy = 0; form.add(buildLabel("Tên phim *"), gbc);
        gbc.insets = new Insets(0, 0, 14, 0);
        gbc.gridy = 1; txtTitle = new JTextField(); styleTextField(txtTitle, ""); form.add(txtTitle, gbc);

        // Poster URL
        gbc.insets = new Insets(6, 0, 4, 0);
        gbc.gridy = 2; form.add(buildLabel("Đường dẫn ảnh (URL)"), gbc);
        gbc.insets = new Insets(0, 0, 8, 0);
        gbc.gridy = 3; txtPosterUrl = new JTextField(); styleTextField(txtPosterUrl, ""); form.add(txtPosterUrl, gbc);
        txtPosterUrl.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updatePosterPreview();
            }
        });

        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.gridy = 4;
        lblPosterPreview = new JLabel("Preview: chua co poster");
        lblPosterPreview.setOpaque(true);
        lblPosterPreview.setBackground(new Color(15, 23, 42, 170));
        lblPosterPreview.setForeground(C_TEXT_HINT);
        lblPosterPreview.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        lblPosterPreview.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        lblPosterPreview.setFont(ThemeConfig.FONT_SMALL);
        form.add(lblPosterPreview, gbc);

        // Duration
        gbc.insets = new Insets(6, 0, 4, 0);
        gbc.gridy = 5; form.add(buildLabel("Thời lượng (phút) *"), gbc);
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.gridy = 6; txtDuration = new JTextField(); styleTextField(txtDuration, "Vi du: 120"); form.add(txtDuration, gbc);

        // Status
        gbc.insets = new Insets(6, 0, 4, 0);
        gbc.gridy = 7; form.add(buildLabel("Trạng thái *"), gbc);
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.gridy = 8; cmbStatus = new JComboBox<>(MovieStatus.values()); styleComboBox(cmbStatus); form.add(cmbStatus, gbc);

        // Genres
        gbc.insets = new Insets(6, 0, 4, 0);
        gbc.gridy = 9; form.add(buildLabel("Thể loại phim"), gbc);
        
        JPanel pnlGenres = new JPanel(new GridLayout(0, 3, 5, 5));
        pnlGenres.setOpaque(false);
        List<Genre> allGenres = GenreService.getInstance().getAllGenres();
        for (Genre g : allGenres) {
            JCheckBox chk = new JCheckBox(g.getName());
            chk.putClientProperty("GenreData", g);
            chk.setOpaque(false);
            chk.setForeground(C_TEXT_PRIMARY);
            chk.setFont(new Font("Inter", Font.PLAIN, 13));
            genreCheckboxes.add(chk);
            pnlGenres.add(chk);
        }
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.gridy = 10; form.add(pnlGenres, gbc);

        // Error
        gbc.insets = new Insets(4, 0, 0, 0);
        gbc.gridy = 11; lblError = new JLabel(" "); lblError.setFont(ThemeConfig.FONT_SMALL); lblError.setForeground(C_DANGER); form.add(lblError, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnCancel = buildCancelButton("Hủy");
        JButton btnSave   = buildSaveButton("Lưu");
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleSave());
        btnPanel.add(btnCancel); btnPanel.add(btnSave);

        glass.add(lblTitle, BorderLayout.NORTH);
        glass.add(form, BorderLayout.CENTER);
        glass.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(glass);
    }

    private void handleSave() {
        try {
            Movie movie = (editTarget != null) ? editTarget : Movie.builder().build();
            movie.setTitle(txtTitle.getText().trim());
            movie.setPosterUrl(txtPosterUrl.getText().trim());
            
            try {
                movie.setDuration(Integer.parseInt(txtDuration.getText().trim()));
            } catch (NumberFormatException nfe) {
                throw new Exception("Thời lượng phải là một số (phút)");
            }
            
            movie.setStatus((MovieStatus) cmbStatus.getSelectedItem());

            List<Genre> selectedGenres = new ArrayList<>();
            for (JCheckBox chk : genreCheckboxes) {
                if (chk.isSelected()) {
                    selectedGenres.add((Genre) chk.getClientProperty("GenreData"));
                }
            }
            movie.setGenres(selectedGenres);

            if (editTarget == null) movieService.addMovie(movie);
            else movieService.updateMovie(movie);
            
            saved = true;
            dispose();
        } catch (Exception ex) {
            lblError.setText("⚠ " + ex.getMessage());
        }
    }

    private void prefillData(Movie movie) {
        txtTitle.setText(movie.getTitle());
        txtPosterUrl.setText(movie.getPosterUrl());
        updatePosterPreview();
        txtDuration.setText(String.valueOf(movie.getDuration()));
        cmbStatus.setSelectedItem(movie.getStatus());

        if (movie.getGenres() != null) {
            for (JCheckBox chk : genreCheckboxes) {
                Genre g = (Genre) chk.getClientProperty("GenreData");
                if (movie.getGenres().stream().anyMatch(mg -> mg.getId().equals(g.getId()))) {
                    chk.setSelected(true);
                }
            }
        }
    }

    private void setupKeyBindings() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { dispose(); }
        });
    }

    private JLabel buildLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ThemeConfig.FONT_BODY);
        lbl.setForeground(C_TEXT_HINT);
        return lbl;
    }

    private void styleTextField(JTextField field, String placeholder) {
        if (!placeholder.isEmpty()) {
            field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        }
        field.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 16; " +
            "margin: 4, 12, 4, 12; " +
            "focusWidth: 2; " +
            "innerFocusWidth: 0;");
        field.setFont(ThemeConfig.FONT_BODY);
        field.setBackground(new Color(15, 23, 42, 180));
        field.setForeground(C_TEXT_PRIMARY);
        field.setPreferredSize(new Dimension(0, 44));
        field.setCaretColor(C_ACCENT);
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 16; " +
            "focusWidth: 2; " +
            "innerFocusWidth: 0;");
        combo.setFont(ThemeConfig.FONT_BODY);
        combo.setBackground(new Color(15, 23, 42, 180));
        combo.setForeground(C_TEXT_PRIMARY);
        combo.setPreferredSize(new Dimension(0, 44));
    }

    private JButton buildSaveButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
        btn.setBackground(C_ACCENT);
        btn.setForeground(Color.WHITE);
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 16; borderWidth: 0; margin: 10, 22, 10, 22;");
        btn.setPreferredSize(new Dimension(120, 42));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buildCancelButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(C_TEXT_HINT);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        btn.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
        btn.putClientProperty(FlatClientProperties.STYLE, "margin: 6, 20, 6, 20; hoverBackground: null;");
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updatePosterPreview() {
        if (lblPosterPreview == null) return;
        String url = txtPosterUrl != null ? txtPosterUrl.getText().trim() : "";
        if (url.isEmpty()) {
            lblPosterPreview.setText("Preview: chua co poster");
            return;
        }
        lblPosterPreview.setText("Preview: " + (url.length() > 54 ? url.substring(0, 54) + "..." : url));
    }
}
