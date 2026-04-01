package com.f3cinema.app.ui.dashboard.timeline;

import com.f3cinema.app.config.ThemeConfig;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;

import static com.f3cinema.app.ui.dashboard.timeline.TimelineConstants.*;

/**
 * Horizontal time ruler displayed as the JScrollPane column header.
 * Draws hour labels from 08:00 to 24:00, half-hour tick marks, and a NOW indicator.
 */
public class TimeRulerHeader extends JPanel {

    private double pixelsPerMinute = DEFAULT_PIXELS_PER_MINUTE;

    public TimeRulerHeader() {
        setOpaque(true);
        setBackground(BG_MAIN);
    }

    public void setPixelsPerMinute(double ppm) {
        this.pixelsPerMinute = ppm;
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(computeTimelineWidth(pixelsPerMinute), RULER_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth();
        int h = getHeight();
        g2.setColor(ThemeConfig.BG_CARD);
        g2.fillRect(0, 0, w, h);

        g2.setColor(ROW_SEPARATOR);
        g2.drawLine(0, h - 1, w, h - 1);

        for (int hour = TIMELINE_START_HOUR; hour <= TIMELINE_END_HOUR; hour++) {
            int minuteOffset = (hour - TIMELINE_START_HOUR) * 60;
            int x = (int) (minuteOffset * pixelsPerMinute);

            g2.setColor(GRID_LINE_HOUR);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x, h - 16, x, h - 1);

            String label = String.format("%02d:00", hour == 24 ? 0 : hour);
            g2.setFont(ThemeConfig.FONT_SMALL.deriveFont(Font.BOLD));
            g2.setColor(ThemeConfig.TEXT_SECONDARY);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, x - fm.stringWidth(label) / 2, h - 18);

            if (hour < TIMELINE_END_HOUR) {
                int halfX = (int) ((minuteOffset + 30) * pixelsPerMinute);
                g2.setColor(GRID_LINE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(halfX, h - 10, halfX, h - 1);
            }
        }

        drawNowIndicator(g2, h);

        g2.dispose();
    }

    private void drawNowIndicator(Graphics2D g2, int h) {
        LocalTime now = LocalTime.now();
        int nowMinutes = now.getHour() * 60 + now.getMinute();
        int startMinutes = TIMELINE_START_HOUR * 60;
        int endMinutes = TIMELINE_END_HOUR * 60;

        if (nowMinutes >= startMinutes && nowMinutes <= endMinutes) {
            int x = (int) ((nowMinutes - startMinutes) * pixelsPerMinute);
            g2.setColor(COLOR_ERROR);
            int[] xPoints = {x - 5, x + 5, x};
            int[] yPoints = {h - 1, h - 1, h - 8};
            g2.fillPolygon(xPoints, yPoints, 3);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x, 0, x, h);
        }
    }
}
