package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.config.ThemeConfig;
import com.f3cinema.app.entity.Movie;
import com.f3cinema.app.controller.MovieController;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import com.f3cinema.app.entity.Genre;
import com.f3cinema.app.service.GenreService;

/**
 * MoviePanel — Premium Grid of MovieCards.
 * Features: Responsive-like wrapping, Elegant Search Bar, and Zero-Latency Filtering.
 */
public class MoviePanel extends BaseDashboardModule {

    private JTextField txtSearch;
    private JComboBox<Genre> cmbGenreFilter;
    private JButton btnAdd;
    private JPanel cardContainer;
    private JPanel statePanel;
    private CardLayout stateLayout;
    private JLabel loadingLabel;
    private JLabel emptyLabel;
    private final MovieController controller;

    public MoviePanel() {
        super("Quản lý Phim", "Home > Movie Management");
        this.controller = new MovieController(this);
        initUI();
        controller.init();
    }

    private void initUI() {
        JPanel toolbar = buildToolbar();
        JScrollPane scrollPane = buildCardView();

        contentBody.setBackground(ThemeConfig.BG_MAIN);
        contentBody.add(toolbar, BorderLayout.NORTH);
        contentBody.add(scrollPane, BorderLayout.CENTER);

        // Ctrl+F focus
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"), "focusSearch");
        getActionMap().put("focusSearch", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { txtSearch.requestFocusInWindow(); }
        });
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 24, 16, 24));

        JPanel controlBar = new JPanel(new BorderLayout(16, 0));
        controlBar.setOpaque(true);
        controlBar.setBackground(ThemeConfig.BG_CARD);
        controlBar.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        controlBar.putClientProperty(FlatClientProperties.STYLE, "arc: 20");

        txtSearch = new JTextField(25);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm kiếm tên phim (Ctrl+F)...");
        txtSearch.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; background: #0F172A; foreground: #F8FAFC; caretColor: #6366F1; margin: 6,10,6,10;");
        txtSearch.setFont(ThemeConfig.FONT_BODY);
        txtSearch.setPreferredSize(new Dimension(300, 40));

        cmbGenreFilter = new JComboBox<>();
        cmbGenreFilter.addItem(Genre.builder().id(-1L).name("All Genres").build());
        for (Genre g : GenreService.getInstance().getAllGenres()) {
            cmbGenreFilter.addItem(g);
        }
        cmbGenreFilter.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Genre) {
                    setText(((Genre) value).getName());
                }
                return this;
            }
        });
        cmbGenreFilter.putClientProperty(FlatClientProperties.STYLE,
                "arc: 12; background: #0F172A; foreground: #F8FAFC; focusWidth: 1; innerFocusWidth: 0;");
        cmbGenreFilter.setFont(ThemeConfig.FONT_BODY);
        cmbGenreFilter.setPreferredSize(new Dimension(180, 40));
        cmbGenreFilter.addActionListener(e -> controller.loadMovies(txtSearch.getText(), getSelectedGenreId()));

        Timer searchTimer = new Timer(300, e -> controller.loadMovies(txtSearch.getText(), getSelectedGenreId()));
        searchTimer.setRepeats(false);
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { searchTimer.restart(); }
        });

        btnAdd = new JButton("Thêm phim mới");
        btnAdd.setFont(ThemeConfig.FONT_BODY.deriveFont(Font.BOLD));
        btnAdd.setBackground(ThemeConfig.ACCENT_COLOR);
        btnAdd.setForeground(ThemeConfig.TEXT_PRIMARY);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.putClientProperty(FlatClientProperties.STYLE,
                "arc: 15; margin: 4,18,4,18; borderWidth: 0; focusWidth: 0;");
        btnAdd.setPreferredSize(new Dimension(170, 40));
        btnAdd.addActionListener(e -> controller.handleAddAction());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(txtSearch);
        leftPanel.add(cmbGenreFilter);

        controlBar.add(leftPanel, BorderLayout.WEST);
        controlBar.add(btnAdd, BorderLayout.EAST);
        toolbar.add(controlBar, BorderLayout.CENTER);
        return toolbar;
    }

    private JScrollPane buildCardView() {
        cardContainer = new JPanel(new com.f3cinema.app.util.WrapLayout(FlowLayout.LEFT, 22, 22));
        cardContainer.setOpaque(false);
        cardContainer.setBackground(ThemeConfig.BG_MAIN);

        stateLayout = new CardLayout();
        statePanel = new JPanel(stateLayout);
        statePanel.setOpaque(false);
        statePanel.add(buildLoadingState(), "loading");
        statePanel.add(buildEmptyState(), "empty");
        statePanel.add(cardContainer, "cards");

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(true);
        inner.setBackground(ThemeConfig.BG_MAIN);
        inner.setBorder(BorderFactory.createEmptyBorder(8, 24, 24, 24));
        inner.add(statePanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(ThemeConfig.BG_MAIN);
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "trackArc: 999; thumbArc: 999;");

        return scroll;
    }

    public void updateTableData(List<Movie> movies) {
        cardContainer.removeAll();
        if (movies.isEmpty()) {
            stateLayout.show(statePanel, "empty");
        } else {
            for (Movie m : movies) {
                MovieCard card = new MovieCard(m, 
                    () -> controller.handleEditAction(m),
                    () -> controller.handleDeleteAction(m)
                );
                cardContainer.add(card);
            }
            stateLayout.show(statePanel, "cards");
        }
        statePanel.revalidate();
        statePanel.repaint();
    }

    private JPanel buildLoadingState() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(80, 0, 0, 0));
        loadingLabel = new JLabel("Dang tai du lieu phim...");
        loadingLabel.setFont(ThemeConfig.FONT_BODY);
        loadingLabel.setForeground(ThemeConfig.TEXT_SECONDARY);
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(loadingLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildEmptyState() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(80, 0, 0, 0));

        JLabel icon = new JLabel(new com.formdev.flatlaf.extras.FlatSVGIcon("icons/video.svg", 56, 56));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyLabel = new JLabel("Chua co phim nao");
        emptyLabel.setFont(ThemeConfig.FONT_H2);
        emptyLabel.setForeground(ThemeConfig.TEXT_SECONDARY);
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(icon);
        panel.add(Box.createVerticalStrut(12));
        panel.add(emptyLabel);
        return panel;
    }

    public void setLoadingState(boolean isLoading) {
        setCursor(isLoading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        stateLayout.show(statePanel, isLoading ? "loading" : "cards");
    }

    public void showErrorMessage(String msg) {
        AppMessageDialogs.showError(this, "Lỗi hệ thống", msg);
    }

    public String getSearchText() { return txtSearch.getText(); }

    public Long getSelectedGenreId() {
        Genre selected = (Genre) cmbGenreFilter.getSelectedItem();
        return selected != null ? selected.getId() : -1L;
    }
}
