package com.f3cinema.app.ui.dashboard;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Assigns a consistent color to each movie in the schedule view.
 */
public final class MovieColorPalette {

    private static final Color[] PALETTE = {
        new Color(0x6366F1),
        new Color(0x10B981),
        new Color(0xF59E0B),
        new Color(0xF43F5E),
        new Color(0x0EA5E9),
        new Color(0x8B5CF6),
        new Color(0xF97316),
        new Color(0x14B8A6),
        new Color(0xEC4899),
        new Color(0x84CC16),
    };

    private final Map<Long, Integer> movieIndexMap = new HashMap<>();
    private int nextIndex = 0;

    public Color getColor(Long movieId) {
        int idx = movieIndexMap.computeIfAbsent(movieId, k -> nextIndex++);
        return PALETTE[idx % PALETTE.length];
    }

    public Color getBlockBackground(Long movieId) {
        Color c = getColor(movieId);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 50);
    }

    public Color getBlockHoverBackground(Long movieId) {
        Color c = getColor(movieId);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 76);
    }

    public void reset() {
        movieIndexMap.clear();
        nextIndex = 0;
    }
}
