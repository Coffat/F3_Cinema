package com.f3cinema.app.ui.staff.ticketing.step1;

import com.f3cinema.app.dto.ShowtimeSummaryDTO;
import com.f3cinema.app.entity.Movie;
import com.f3cinema.app.service.MovieService;
import com.f3cinema.app.service.ShowtimeService;
import com.f3cinema.app.ui.staff.ticketing.TicketOrderState;
import com.f3cinema.app.ui.staff.ticketing.TicketingFlowPanel;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Step 1: Professional movie ticketing UI inspired by CGV/Galaxy Cinema.
 * Layout: Movie sidebar (left) + Showtime schedule grid (center/right).
 */
public class ShowtimeSelectionPanel extends JPanel {

    private final TicketingFlowPanel navigator;
    private final TicketOrderState state;

    private LocalDate selectedDate = LocalDate.now();
    private Long selectedMovieId = null;
    
    private JPanel movieListPanel;
    private JPanel showtimesContentPanel;
    private JLabel lblSelectedMovieTitle;
    private SwingWorker<?, ?> currentWorker;
    private Map<Long, SelectableMovieCard> movieCardMap = new HashMap<>();

    private static final Color BG_ELEVATED = new Color(0x334155);
    private static final Color TEXT_PRIMARY = new Color(0xF8FAFC);
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);
    private static final Color ACCENT_PRIMARY = new Color(0x6366F1);

    public ShowtimeSelectionPanel(TicketingFlowPanel navigator) {
        this.navigator = navigator;
        this.state = TicketOrderState.getInstance();

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        initUI();
        loadMovies();
    }

    private void initUI() {
        JPanel contentBody = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59, 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setStroke(new BasicStroke(1.5f));
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 30),
                        0, getHeight(), new Color(255, 255, 255, 5)));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 24, 24);
                g2.dispose();
            }
        };
        contentBody.setLayout(new BorderLayout(16, 0));
        contentBody.setOpaque(false);
        contentBody.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        contentBody.add(createDateStripPanel(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(280);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOpaque(false);

        splitPane.setLeftComponent(createMovieSidebar());
        splitPane.setRightComponent(createShowtimesArea());

        contentBody.add(splitPane, BorderLayout.CENTER);
        add(contentBody, BorderLayout.CENTER);
    }

    private JPanel createDateStripPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel lblTitle = new JLabel("Chọn ngày xem phim");
        FlatSVGIcon calendarIcon = new FlatSVGIcon("icons/calendar.svg", 18, 18);
        calendarIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> TEXT_PRIMARY));
        lblTitle.setIcon(calendarIcon);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_PRIMARY);

        JPanel dateStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dateStrip.setOpaque(false);
        dateStrip.setBorder(new EmptyBorder(12, 0, 0, 0));

        ButtonGroup group = new ButtonGroup();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("vi-VN"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            String dayName = (i == 0) ? "Hôm nay" : capitalize(date.format(dayFormatter));
            
            JToggleButton btn = new JToggleButton();
            btn.setLayout(new BoxLayout(btn, BoxLayout.Y_AXIS));
            btn.setPreferredSize(new Dimension(95, 70));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setOpaque(false);
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "arc: 16; background: #1E293B; borderWidth: 0; " +
                    "selectedBackground: #6366F1; selectedForeground: #FFFFFF;");

            JLabel lblDay = new JLabel(dayName);
            lblDay.setFont(new Font("Inter", Font.PLAIN, 11));
            lblDay.setAlignmentX(CENTER_ALIGNMENT);
            
            JLabel lblDate = new JLabel(date.format(dateFormatter));
            lblDate.setFont(new Font("Inter", Font.BOLD, 16));
            lblDate.setAlignmentX(CENTER_ALIGNMENT);

            btn.add(Box.createVerticalStrut(8));
            btn.add(lblDay);
            btn.add(Box.createVerticalStrut(2));
            btn.add(lblDate);
            btn.add(Box.createVerticalStrut(8));

            if (i == 0) btn.setSelected(true);

            btn.addActionListener(e -> {
                this.selectedDate = date;
                loadShowtimesForSelectedMovie();
            });

            group.add(btn);
            dateStrip.add(btn);
        }

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(lblTitle, BorderLayout.WEST);

        JButton btnSnackOnly = new JButton("Bán bắp nước");
        FlatSVGIcon snackIcon = new FlatSVGIcon("icons/popcorn.svg", 14, 14);
        snackIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Color.WHITE));
        btnSnackOnly.setIcon(snackIcon);
        btnSnackOnly.setFont(new Font("Inter", Font.BOLD, 13));
        btnSnackOnly.setForeground(Color.WHITE);
        btnSnackOnly.setBackground(ACCENT_PRIMARY);
        btnSnackOnly.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSnackOnly.setPreferredSize(new Dimension(150, 36));
        btnSnackOnly.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; borderWidth: 0; hoverBackground: #4F46E5;");
        btnSnackOnly.addActionListener(e -> {
            state.setShowtime(null, null, null, null, BigDecimal.ZERO);
            state.clearSeats();
            navigator.nextStep();
            navigator.nextStep();
        });

        headerRow.add(btnSnackOnly, BorderLayout.EAST);

        wrapper.add(headerRow, BorderLayout.NORTH);
        wrapper.add(dateStrip, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createMovieSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(280, 0));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel lblTitle = new JLabel("Phim đang chiếu");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_PRIMARY);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        movieListPanel = new JPanel();
        movieListPanel.setLayout(new BoxLayout(movieListPanel, BoxLayout.Y_AXIS));
        movieListPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(movieListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "width: 6; thumbInsets: 0,0,0,0;");

        sidebar.add(headerPanel, BorderLayout.NORTH);
        sidebar.add(scrollPane, BorderLayout.CENTER);

        return sidebar;
    }

    private JPanel createShowtimesArea() {
        JPanel area = new JPanel(new BorderLayout());
        area.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        lblSelectedMovieTitle = new JLabel("Vui lòng chọn phim");
        lblSelectedMovieTitle.setFont(new Font("Inter", Font.BOLD, 20));
        lblSelectedMovieTitle.setForeground(TEXT_PRIMARY);
        header.add(lblSelectedMovieTitle, BorderLayout.WEST);

        showtimesContentPanel = new JPanel();
        showtimesContentPanel.setLayout(new BoxLayout(showtimesContentPanel, BoxLayout.Y_AXIS));
        showtimesContentPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(showtimesContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        area.add(header, BorderLayout.NORTH);
        area.add(scrollPane, BorderLayout.CENTER);

        return area;
    }

    private void loadMovies() {
        movieListPanel.removeAll();
        
        new SwingWorker<List<Movie>, Void>() {
            @Override
            protected List<Movie> doInBackground() {
                return MovieService.getInstance().getAllMovies();
            }

            @Override
            protected void done() {
                try {
                    List<Movie> movies = get();
                    movieCardMap.clear();
                    for (Movie movie : movies) {
                        SelectableMovieCard card = new SelectableMovieCard(movie);
                        card.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseClicked(java.awt.event.MouseEvent e) {
                                selectMovie(movie);
                            }
                        });
                        movieCardMap.put(movie.getId(), card);
                        movieListPanel.add(card);
                        movieListPanel.add(Box.createVerticalStrut(10));
                    }
                    movieListPanel.revalidate();
                    movieListPanel.repaint();

                    if (!movies.isEmpty()) {
                        selectMovie(movies.get(0));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void selectMovie(Movie movie) {
        this.selectedMovieId = movie.getId();
        lblSelectedMovieTitle.setText(movie.getTitle());
        FlatSVGIcon videoIcon = new FlatSVGIcon("icons/video.svg", 16, 16);
        videoIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> ACCENT_PRIMARY));
        lblSelectedMovieTitle.setIcon(videoIcon);

        for (Map.Entry<Long, SelectableMovieCard> entry : movieCardMap.entrySet()) {
            entry.getValue().setSelected(entry.getKey().equals(movie.getId()));
        }

        loadShowtimesForSelectedMovie();
    }

    private void loadShowtimesForSelectedMovie() {
        if (selectedMovieId == null) return;

        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }

        showtimesContentPanel.removeAll();
        JLabel lblLoading = new JLabel("Đang tải lịch chiếu...");
        lblLoading.setForeground(TEXT_SECONDARY);
        lblLoading.setFont(new Font("Inter", Font.ITALIC, 14));
        showtimesContentPanel.add(lblLoading);
        showtimesContentPanel.revalidate();
        showtimesContentPanel.repaint();

        final Long movieId = this.selectedMovieId;
        final LocalDate date = this.selectedDate;

        currentWorker = new SwingWorker<List<ShowtimeSummaryDTO>, Void>() {
            @Override
            protected List<ShowtimeSummaryDTO> doInBackground() {
                return ShowtimeService.getInstance().getShowtimesForUI(date, movieId, null);
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    List<ShowtimeSummaryDTO> showtimes = get();
                    renderShowtimes(showtimes);
                } catch (Exception e) {
                    showtimesContentPanel.removeAll();
                    JLabel lblError = new JLabel("❌ Lỗi tải dữ liệu");
                    lblError.setForeground(new Color(0xEF4444));
                    showtimesContentPanel.add(lblError);
                    showtimesContentPanel.revalidate();
                    showtimesContentPanel.repaint();
                }
            }
        };
        currentWorker.execute();
    }

    private void renderShowtimes(List<ShowtimeSummaryDTO> showtimes) {
        showtimesContentPanel.removeAll();

        if (showtimes.isEmpty()) {
            JLabel lblEmpty = new JLabel("Không có suất chiếu nào cho ngày này");
            lblEmpty.setForeground(TEXT_SECONDARY);
            lblEmpty.setFont(new Font("Inter", Font.ITALIC, 14));
            showtimesContentPanel.add(lblEmpty);
        } else {
            Map<String, List<ShowtimeSummaryDTO>> groupedByRoom = showtimes.stream()
                    .collect(Collectors.groupingBy(
                            ShowtimeSummaryDTO::roomName,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            for (Map.Entry<String, List<ShowtimeSummaryDTO>> entry : groupedByRoom.entrySet()) {
                showtimesContentPanel.add(createRoomSection(entry.getKey(), entry.getValue()));
                showtimesContentPanel.add(Box.createVerticalStrut(16));
            }
        }

        showtimesContentPanel.revalidate();
        showtimesContentPanel.repaint();
    }

    private JPanel createRoomSection(String roomName, List<ShowtimeSummaryDTO> showtimes) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        section.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel lblRoom = new JLabel("📍 " + roomName);
        lblRoom.setFont(new Font("Inter", Font.BOLD, 14));
        lblRoom.setForeground(TEXT_PRIMARY);
        lblRoom.setAlignmentX(LEFT_ALIGNMENT);
        lblRoom.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel timesGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        timesGrid.setOpaque(false);
        timesGrid.setAlignmentX(LEFT_ALIGNMENT);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (ShowtimeSummaryDTO dto : showtimes) {
            JButton btnTime = new JButton(dto.startTime().format(timeFormatter));
            btnTime.setFont(new Font("Inter", Font.BOLD, 15));
            btnTime.setForeground(TEXT_PRIMARY);
            btnTime.setPreferredSize(new Dimension(90, 45));
            btnTime.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnTime.setFocusPainted(false);
            btnTime.putClientProperty(FlatClientProperties.STYLE,
                    "arc: 10; background: #1E293B; borderWidth: 1; borderColor: #334155; " +
                    "hoverBackground: #6366F1; hoverBorderColor: #6366F1; hoverForeground: #FFFFFF;");

            btnTime.addActionListener(e -> onShowtimeSelected(dto));
            timesGrid.add(btnTime);
        }

        section.add(lblRoom);
        section.add(timesGrid);

        return section;
    }

    private void onShowtimeSelected(ShowtimeSummaryDTO dto) {
        state.setShowtime(
                dto.showtimeId(),
                dto.movieTitle(),
                dto.roomName(),
                dto.startTime().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")),
                BigDecimal.valueOf(dto.basePrice())
        );
        navigator.nextStep();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private class SelectableMovieCard extends JPanel {
        private boolean selected = false;
        private final Movie movie;

        public SelectableMovieCard(Movie movie) {
            this.movie = movie;
            setLayout(new BorderLayout(10, 0));
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            setBorder(new EmptyBorder(10, 10, 10, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            add(createPosterPanel(), BorderLayout.WEST);
            add(createInfoPanel(), BorderLayout.CENTER);
        }

        private JPanel createPosterPanel() {
            return new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(BG_ELEVATED);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(TEXT_SECONDARY);
                    g2.setFont(new Font("Inter", Font.BOLD, 24));
                    FontMetrics fm = g2.getFontMetrics();
                    String initial = movie.getTitle().substring(0, 1).toUpperCase();
                    int x = (getWidth() - fm.stringWidth(initial)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(initial, x, y);
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(55, 70);
                }
            };
        }

        private JPanel createInfoPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);

            JLabel lblTitle = new JLabel(movie.getTitle());
            lblTitle.setFont(new Font("Inter", Font.BOLD, 14));
            lblTitle.setForeground(TEXT_PRIMARY);
            lblTitle.setAlignmentX(LEFT_ALIGNMENT);

            JLabel lblDuration = new JLabel(movie.getDuration() + " phút");
            lblDuration.setFont(new Font("Inter", Font.PLAIN, 12));
            lblDuration.setForeground(TEXT_SECONDARY);
            lblDuration.setAlignmentX(LEFT_ALIGNMENT);

            String genresText = (movie.getGenres() != null && !movie.getGenres().isEmpty())
                    ? movie.getGenres().stream()
                            .limit(2)
                            .map(g -> g.getName())
                            .collect(Collectors.joining(", "))
                    : "Chưa phân loại";
            JLabel lblGenres = new JLabel(genresText);
            lblGenres.setFont(new Font("Inter", Font.PLAIN, 11));
            lblGenres.setForeground(new Color(0x64748B));
            lblGenres.setAlignmentX(LEFT_ALIGNMENT);

            panel.add(lblTitle);
            panel.add(Box.createVerticalStrut(4));
            panel.add(lblDuration);
            panel.add(Box.createVerticalStrut(2));
            panel.add(lblGenres);

            return panel;
        }

        public void setSelected(boolean sel) {
            this.selected = sel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (selected) {
                g2.setColor(new Color(99, 102, 241, 60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(ACCENT_PRIMARY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
            }
            g2.dispose();
        }
    }
}
