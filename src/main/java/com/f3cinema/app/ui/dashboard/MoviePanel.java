package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.entity.Movie;
import com.f3cinema.app.controller.MovieController;
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

        // Overall Main Background for depth
        contentBody.setBackground(new Color(15, 23, 42)); 
        contentBody.add(toolbar, BorderLayout.NORTH);
        contentBody.add(scrollPane, BorderLayout.CENTER);

        // Ctrl+F focus
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"), "focusSearch");
        getActionMap().put("focusSearch", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { txtSearch.requestFocusInWindow(); }
        });
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 20, 30, 20));

        // ── Modern Search ──
        txtSearch = new JTextField(25);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "🔍  Tìm kiếm tên phim (phím tắt Ctrl+F)...");
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B; foreground: #F8FAFC; caretColor: #6366F1;");
        txtSearch.setFont(new Font("Inter", Font.PLAIN, 15));
        txtSearch.setPreferredSize(new Dimension(300, 40));
        
        // ── Genre Combo Box ──
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
        cmbGenreFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B; foreground: #F8FAFC; focusWidth: 0;");
        cmbGenreFilter.setFont(new Font("Inter", Font.BOLD, 14));
        cmbGenreFilter.setPreferredSize(new Dimension(180, 40));
        cmbGenreFilter.addActionListener(e -> controller.loadMovies(txtSearch.getText(), getSelectedGenreId()));

        Timer searchTimer = new Timer(300, e -> controller.loadMovies(txtSearch.getText(), getSelectedGenreId()));
        searchTimer.setRepeats(false);
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { searchTimer.restart(); }
        });

        btnAdd = new JButton("+ Tạo phim mới");
        btnAdd.setFont(new Font("Inter", Font.BOLD, 14));
        btnAdd.setBackground(Color.decode("#6366F1"));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.putClientProperty(FlatClientProperties.STYLE, "arc: 16; margin: 0, 20, 0, 20; borderWidth: 0;");
        btnAdd.setPreferredSize(new Dimension(160, 40));
        btnAdd.addActionListener(e -> controller.handleAddAction());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(txtSearch);
        leftPanel.add(cmbGenreFilter);

        toolbar.add(leftPanel, BorderLayout.WEST);
        toolbar.add(btnAdd, BorderLayout.EAST);
        return toolbar;
    }

    private JScrollPane buildCardView() {
        // WrapLayout to properly wrap multiple elements without overflowing MainFrame
        cardContainer = new JPanel(new com.f3cinema.app.util.WrapLayout(FlowLayout.LEFT, 32, 32));
        cardContainer.setOpaque(false);
        
        // Inner wrapper to force cards to stay at top if list is short
        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.add(cardContainer, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        // FORCE repaint on scroll, completely eliminating tearing/ghosting artifacts for scaled components
        scroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        
        return scroll;
    }

    public void updateTableData(List<Movie> movies) {
        cardContainer.removeAll();
        if (movies.isEmpty()) {
            showNoDataFound();
        } else {
            for (Movie m : movies) {
                MovieCard card = new MovieCard(m, 
                    () -> controller.handleEditAction(m),
                    () -> controller.handleDeleteAction(m)
                );
                cardContainer.add(card);
            }
        }
        cardContainer.revalidate();
        cardContainer.repaint();
    }

    private void showNoDataFound() {
        JLabel lbl = new JLabel("Không tìm thấy kết quả nào. Hãy thử từ khóa khác! 🍿");
        lbl.setForeground(Color.decode("#6366F1"));
        lbl.setFont(new Font("Inter", Font.BOLD, 18));
        lbl.setBorder(BorderFactory.createEmptyBorder(60, 40, 0, 0));
        cardContainer.add(lbl);
    }

    public void setLoadingState(boolean isLoading) {
        setCursor(isLoading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    public void showErrorMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
    }

    public String getSearchText() { return txtSearch.getText(); }

    public Long getSelectedGenreId() {
        Genre selected = (Genre) cmbGenreFilter.getSelectedItem();
        return selected != null ? selected.getId() : -1L;
    }
}
