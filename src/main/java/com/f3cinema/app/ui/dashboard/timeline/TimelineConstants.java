package com.f3cinema.app.ui.dashboard.timeline;

import java.awt.*;

public final class TimelineConstants {

    private TimelineConstants() {}

    // Timeline range
    public static final int TIMELINE_START_HOUR = 8;
    public static final int TIMELINE_END_HOUR = 24;
    public static final int TOTAL_MINUTES = (TIMELINE_END_HOUR - TIMELINE_START_HOUR) * 60;

    // Layout dimensions
    public static final int ROW_HEIGHT = 80;
    public static final int BLOCK_VERTICAL_PADDING = 8;
    public static final int SIDEBAR_WIDTH = 160;
    public static final int RULER_HEIGHT = 48;
    public static final double DEFAULT_PIXELS_PER_MINUTE = 2.0;
    public static final double MIN_PIXELS_PER_MINUTE = 1.0;
    public static final double MAX_PIXELS_PER_MINUTE = 5.0;
    public static final double ZOOM_STEP = 0.5;

    // Design tokens — Midnight palette
    public static final Color BG_MAIN = new Color(0x0F172A);
    public static final Color BG_SURFACE = new Color(0x1E293B);
    public static final Color BG_ELEVATED = new Color(0x334155);
    public static final Color BG_ROW_ALT = new Color(0x162032);
    public static final Color TEXT_PRIMARY = new Color(0xF8FAFC);
    public static final Color TEXT_MUTED = new Color(0x94A3B8);
    public static final Color ACCENT = new Color(0x6366F1);
    public static final Color COLOR_ERROR = new Color(0xEF4444);
    public static final Color NOW_LINE = new Color(239, 68, 68, 200);
    public static final Color GRID_LINE = new Color(255, 255, 255, 8);
    public static final Color GRID_LINE_HOUR = new Color(255, 255, 255, 18);
    public static final Color ROW_SEPARATOR = new Color(255, 255, 255, 12);

    // Fonts
    public static final Font FONT_BLOCK_TITLE = new Font("Inter", Font.BOLD, 12);
    public static final Font FONT_BLOCK_TIME = new Font("Inter", Font.PLAIN, 11);
    public static final Font FONT_RULER = new Font("Inter", Font.PLAIN, 12);
    public static final Font FONT_RULER_BOLD = new Font("Inter", Font.BOLD, 12);
    public static final Font FONT_ROOM_NAME = new Font("Inter", Font.BOLD, 14);
    public static final Font FONT_ROOM_TYPE = new Font("Inter", Font.PLAIN, 12);

    public static int computeTimelineWidth(double pixelsPerMinute) {
        return (int) (TOTAL_MINUTES * pixelsPerMinute);
    }
}
