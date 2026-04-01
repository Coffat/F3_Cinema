package com.f3cinema.app.ui.dashboard.timeline;

import com.f3cinema.app.entity.Room;
import com.f3cinema.app.entity.Showtime;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

import static com.f3cinema.app.ui.dashboard.timeline.TimelineConstants.*;

/**
 * Main timeline viewport containing two layers:
 *   Layer 0 (GridLayer)   — custom-painted grid lines, row backgrounds, NOW line
 *   Layer 1 (BlocksLayer) — null-layout JPanel holding absolutely positioned ShowtimeBlocks
 */
public class TimelinePanel extends JLayeredPane {

    private final GridLayer gridLayer = new GridLayer();
    private final JPanel blocksLayer = new JPanel(null);
    private final MovieColorPalette palette = new MovieColorPalette();

    private double pixelsPerMinute = DEFAULT_PIXELS_PER_MINUTE;
    private int roomCount = 0;
    private final List<ShowtimeBlock> blocks = new ArrayList<>();

    public TimelinePanel() {
        setOpaque(false);
        blocksLayer.setOpaque(false);
        add(gridLayer, JLayeredPane.DEFAULT_LAYER);
        add(blocksLayer, JLayeredPane.PALETTE_LAYER);
    }

    public MovieColorPalette getPalette() {
        return palette;
    }

    public List<ShowtimeBlock> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public void setPixelsPerMinute(double ppm) {
        this.pixelsPerMinute = ppm;
        relayout();
    }

    public double getPixelsPerMinute() {
        return pixelsPerMinute;
    }

    /**
     * Rebuild the timeline with fresh data.
     * @param roomList  ordered list of rooms (defines row index)
     * @param grouped   showtimes grouped by room id
     */
    public void setData(List<Room> roomList, Map<Long, List<Showtime>> grouped) {
        blocksLayer.removeAll();
        blocks.clear();
        palette.reset();
        this.roomCount = roomList.size();

        for (int rowIdx = 0; rowIdx < roomList.size(); rowIdx++) {
            Room room = roomList.get(rowIdx);
            List<Showtime> showtimes = grouped.getOrDefault(room.getId(), List.of());

            for (Showtime st : showtimes) {
                ShowtimeBlock block = new ShowtimeBlock(st, palette);
                blocks.add(block);
                blocksLayer.add(block);
                positionBlock(block, rowIdx);
            }
        }

        detectConflicts(roomList, grouped);
        offsetConflictingBlocks();
        relayout();
    }

    // ── Position calculation ─────────────────────────────────────────────────

    private void positionBlock(ShowtimeBlock block, int rowIndex) {
        Showtime st = block.getShowtime();
        int startMinutes = st.getStartTime().getHour() * 60 + st.getStartTime().getMinute();
        int endMinutes = st.getEndTime().getHour() * 60 + st.getEndTime().getMinute();
        // Handle past-midnight edge: clamp to timeline bounds
        int timelineStartMin = TIMELINE_START_HOUR * 60;
        int timelineEndMin = TIMELINE_END_HOUR * 60;
        startMinutes = Math.max(startMinutes, timelineStartMin);
        endMinutes = Math.min(endMinutes, timelineEndMin);
        int durationMin = endMinutes - startMinutes;
        if (durationMin <= 0) return;

        int x = (int) ((startMinutes - timelineStartMin) * pixelsPerMinute);
        int width = (int) (durationMin * pixelsPerMinute);
        int y = rowIndex * ROW_HEIGHT + BLOCK_VERTICAL_PADDING;
        int height = ROW_HEIGHT - BLOCK_VERTICAL_PADDING * 2;

        block.setBounds(x, y, Math.max(width, 30), height);
        block.putClientProperty("rowIndex", rowIndex);
    }

    /** Recalculate bounds for all blocks and resize layers. */
    private void relayout() {
        int totalWidth = computeTimelineWidth(pixelsPerMinute);
        int totalHeight = Math.max(roomCount * ROW_HEIGHT, ROW_HEIGHT);

        Dimension size = new Dimension(totalWidth, totalHeight);
        gridLayer.setBounds(0, 0, totalWidth, totalHeight);
        blocksLayer.setBounds(0, 0, totalWidth, totalHeight);
        gridLayer.setPreferredSize(size);
        blocksLayer.setPreferredSize(size);
        setPreferredSize(size);

        // Reposition all blocks
        for (ShowtimeBlock block : blocks) {
            Integer rowIdx = (Integer) block.getClientProperty("rowIndex");
            if (rowIdx != null) positionBlock(block, rowIdx);
        }

        gridLayer.setRoomCount(roomCount);
        gridLayer.setPixelsPerMinute(pixelsPerMinute);

        revalidate();
        repaint();
    }

    // ── Conflict detection ───────────────────────────────────────────────────

    private void detectConflicts(List<Room> roomList, Map<Long, List<Showtime>> grouped) {
        for (Room room : roomList) {
            List<Showtime> showtimes = grouped.getOrDefault(room.getId(), List.of());
            if (showtimes.size() < 2) continue;

            List<Showtime> sorted = new ArrayList<>(showtimes);
            sorted.sort(Comparator.comparing(Showtime::getStartTime));

            for (int i = 0; i < sorted.size(); i++) {
                for (int j = i + 1; j < sorted.size(); j++) {
                    Showtime a = sorted.get(i);
                    Showtime b = sorted.get(j);
                    if (a.getEndTime().isAfter(b.getStartTime())) {
                        markConflict(a, b);
                        markConflict(b, a);
                    }
                }
            }
        }
    }

    /**
     * Offsets conflicting blocks vertically within their row so both remain visible.
     * The first conflict block stays at normal Y, subsequent overlapping ones shift down.
     */
    private void offsetConflictingBlocks() {
        for (ShowtimeBlock block : blocks) {
            if (!block.isConflict()) continue;
            Integer rowIdx = (Integer) block.getClientProperty("rowIndex");
            if (rowIdx == null) continue;

            int conflictOffset = 0;
            for (ShowtimeBlock other : blocks) {
                if (other == block) break;
                if (!other.isConflict()) continue;
                Integer otherRow = (Integer) other.getClientProperty("rowIndex");
                if (otherRow == null || !otherRow.equals(rowIdx)) continue;

                // Check if they truly overlap horizontally
                Rectangle a = block.getBounds();
                Rectangle b = other.getBounds();
                if (a.x < b.x + b.width && a.x + a.width > b.x) {
                    conflictOffset += 10;
                }
            }

            if (conflictOffset > 0) {
                Rectangle r = block.getBounds();
                int maxOffset = ROW_HEIGHT / 3;
                int offset = Math.min(conflictOffset, maxOffset);
                block.setBounds(r.x, r.y + offset, r.width, r.height - offset);
            }
        }
    }

    private void markConflict(Showtime src, Showtime other) {
        for (ShowtimeBlock block : blocks) {
            if (block.getShowtime().getId().equals(src.getId())) {
                String info = "Trùng lịch với: " + other.getMovie().getTitle()
                        + " (" + other.getStartTime().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                        + " — " + other.getEndTime().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + ")";
                block.setConflict(true, info);
            }
        }
    }

    // ── Grid Layer (background painting) ─────────────────────────────────────

    private static class GridLayer extends JPanel {

        private int roomCount = 0;
        private double pixelsPerMinute = DEFAULT_PIXELS_PER_MINUTE;

        GridLayer() {
            setOpaque(true);
            setBackground(BG_MAIN);
        }

        void setRoomCount(int count) {
            this.roomCount = count;
        }

        void setPixelsPerMinute(double ppm) {
            this.pixelsPerMinute = ppm;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Alternating row backgrounds
            for (int i = 0; i < roomCount; i++) {
                if (i % 2 == 1) {
                    g2.setColor(BG_ROW_ALT);
                    g2.fillRect(0, i * ROW_HEIGHT, w, ROW_HEIGHT);
                }
            }

            // Horizontal row separators
            g2.setColor(ROW_SEPARATOR);
            for (int i = 1; i < roomCount; i++) {
                int y = i * ROW_HEIGHT;
                g2.drawLine(0, y, w, y);
            }

            // Vertical hour lines
            for (int hour = TIMELINE_START_HOUR; hour <= TIMELINE_END_HOUR; hour++) {
                int minuteOffset = (hour - TIMELINE_START_HOUR) * 60;
                int x = (int) (minuteOffset * pixelsPerMinute);

                g2.setColor(GRID_LINE_HOUR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(x, 0, x, h);

                // Half-hour dashed line
                if (hour < TIMELINE_END_HOUR) {
                    int halfX = (int) ((minuteOffset + 30) * pixelsPerMinute);
                    g2.setColor(GRID_LINE);
                    g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                            10f, new float[]{4f, 4f}, 0f));
                    g2.drawLine(halfX, 0, halfX, h);
                }
            }

            // NOW vertical line
            g2.setStroke(new BasicStroke(2f));
            drawNowLine(g2, h);

            g2.dispose();
        }

        private void drawNowLine(Graphics2D g2, int h) {
            LocalTime now = LocalTime.now();
            int nowMinutes = now.getHour() * 60 + now.getMinute();
            int startMinutes = TIMELINE_START_HOUR * 60;
            int endMinutes = TIMELINE_END_HOUR * 60;

            if (nowMinutes >= startMinutes && nowMinutes <= endMinutes) {
                int x = (int) ((nowMinutes - startMinutes) * pixelsPerMinute);
                g2.setColor(NOW_LINE);
                g2.drawLine(x, 0, x, h);
            }
        }
    }
}
