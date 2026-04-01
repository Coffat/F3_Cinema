package com.f3cinema.app.ui.dashboard.timeline;

import com.f3cinema.app.entity.Showtime;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import static com.f3cinema.app.ui.dashboard.timeline.TimelineConstants.*;

/**
 * Visual block representing a single showtime on the timeline.
 * Custom-painted with a colored left accent bar, rounded corners, and hover effects.
 */
public class ShowtimeBlock extends JPanel {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int ARC = 12;
    private static final int LEFT_BAR_WIDTH = 4;

    private final Showtime showtime;
    private final Color accentColor;
    private final Color bgNormal;
    private final Color bgHover;
    private boolean hovered = false;
    private boolean conflict = false;
    private String conflictInfo = null;
    private boolean selected = false;

    public ShowtimeBlock(Showtime showtime, MovieColorPalette palette) {
        this.showtime = showtime;
        Long movieId = showtime.getMovie().getId();
        this.accentColor = palette.getColor(movieId);
        this.bgNormal = palette.getBlockBackground(movieId);
        this.bgHover = palette.getBlockHoverBackground(movieId);

        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setLayout(new BorderLayout());

        buildTooltip();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public void setConflict(boolean conflict, String conflictInfo) {
        this.conflict = conflict;
        this.conflictInfo = conflictInfo;
        buildTooltip();
        repaint();
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    // ── Tooltip ──────────────────────────────────────────────────────────────

    private void buildTooltip() {
        String movieTitle = showtime.getMovie().getTitle();
        String roomName = showtime.getRoom().getName() + " — " + showtime.getRoom().getRoomType().getLabel();
        String startStr = showtime.getStartTime().format(TIME_FMT);
        String endStr = showtime.getEndTime().format(TIME_FMT);
        long durationMin = Duration.between(showtime.getStartTime(), showtime.getEndTime()).toMinutes();
        String price = String.format("%,.0f", showtime.getBasePrice());

        String genres;
        try {
            var genreList = showtime.getMovie().getGenres();
            genres = (genreList != null && !genreList.isEmpty())
                    ? genreList.stream().map(g -> g.getName()).collect(Collectors.joining(", "))
                    : "N/A";
        } catch (Exception e) {
            genres = "N/A";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><div style='padding:6px; font-family:Inter; width:230px'>");
        sb.append("<b style='font-size:13px; color:#F8FAFC'>").append(movieTitle).append("</b><br/>");
        sb.append("<span style='color:#94A3B8; font-size:11px'>").append(genres).append("</span><br/>");
        sb.append("<hr style='border-color:#334155; margin:4px 0'/>");
        sb.append("<table cellpadding='2'>");
        sb.append("<tr><td style='color:#94A3B8'>Phòng:</td><td style='color:#F8FAFC'>").append(roomName).append("</td></tr>");
        sb.append("<tr><td style='color:#94A3B8'>Thời gian:</td><td style='color:#F8FAFC'>").append(startStr).append(" — ").append(endStr).append(" (").append(durationMin).append(" phút)</td></tr>");
        sb.append("<tr><td style='color:#94A3B8'>Giá vé:</td><td style='color:#F8FAFC'>").append(price).append(" VNĐ</td></tr>");
        sb.append("</table>");

        if (conflict && conflictInfo != null) {
            sb.append("<hr style='border-color:#EF4444; margin:4px 0'/>");
            sb.append("<span style='color:#EF4444; font-size:11px'>⚠ ").append(conflictInfo).append("</span>");
        }

        sb.append("</div></html>");
        setToolTipText(sb.toString());
    }

    // ── Custom Painting ──────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth();
        int h = getHeight();

        // Determine if this is a past showtime
        boolean isPast = showtime.getEndTime().isBefore(LocalDateTime.now());
        float alpha = isPast ? 0.5f : 1.0f;
        if (isPast) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        // Background fill
        Color bgColor = hovered ? bgHover : bgNormal;
        if (conflict) {
            bgColor = new Color(239, 68, 68, hovered ? 50 : 35);
        }
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, w, h, ARC, ARC);

        // Border
        if (conflict) {
            g2.setColor(COLOR_ERROR);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, ARC, ARC);
        } else if (selected) {
            g2.setColor(ACCENT);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, ARC, ARC);
        } else if (hovered) {
            g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 80));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, ARC, ARC);
        }

        // Left accent bar
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, LEFT_BAR_WIDTH + 4, h, ARC, ARC);
        g2.fillRect(LEFT_BAR_WIDTH, 0, 4, h);

        // Text content
        int textX = LEFT_BAR_WIDTH + 10;
        int availableWidth = w - textX - 8;

        if (availableWidth > 20) {
            // Movie title
            g2.setFont(FONT_BLOCK_TITLE);
            g2.setColor(isPast ? TEXT_MUTED : TEXT_PRIMARY);
            String title = truncateText(g2, showtime.getMovie().getTitle(), availableWidth);
            FontMetrics fm = g2.getFontMetrics();
            int titleY = h / 2 - 2;
            g2.drawString(title, textX, titleY);

            // Time range
            g2.setFont(FONT_BLOCK_TIME);
            g2.setColor(TEXT_MUTED);
            String timeStr = showtime.getStartTime().format(TIME_FMT)
                    + " — " + showtime.getEndTime().format(TIME_FMT);
            String timeTruncated = truncateText(g2, timeStr, availableWidth);
            g2.drawString(timeTruncated, textX, titleY + fm.getHeight());
        }

        // Conflict warning icon
        if (conflict) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            g2.setColor(COLOR_ERROR);
            g2.drawString("⚠", w - 20, 16);
        }

        g2.dispose();
    }

    private String truncateText(Graphics2D g2, String text, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        for (int i = text.length() - 1; i > 0; i--) {
            if (fm.stringWidth(text.substring(0, i)) + ellipsisWidth <= maxWidth) {
                return text.substring(0, i) + ellipsis;
            }
        }
        return ellipsis;
    }
}
