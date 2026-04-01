package com.f3cinema.app.ui.dashboard.timeline;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Assigns a consistent color to each movie across the timeline session.
 * Cycles through 10 distinct colors when more than 10 movies exist.
 */
public final class MovieColorPalette {

    private static final Color[] PALETTE = {
        new Color(0x6366F1), // Indigo
        new Color(0x10B981), // Emerald
        new Color(0xF59E0B), // Amber
        new Color(0xF43F5E), // Rose
        new Color(0x0EA5E9), // Sky
        new Color(0x8B5CF6), // Violet
        new Color(0xF97316), // Orange
        new Color(0x14B8A6), // Teal
        new Color(0xEC4899), // Pink
        new Color(0x84CC16), // Lime
    };

    private final Map<Long, Integer> movieIndexMap = new HashMap<>();
    private int nextIndex = 0;

    public Color getColor(Long movieId) {
        int idx = movieIndexMap.computeIfAbsent(movieId, k -> nextIndex++);
        return PALETTE[idx % PALETTE.length];
    }

    /** Background fill: base color at 20% opacity. */
    public Color getBlockBackground(Long movieId) {
        Color c = getColor(movieId);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 50);
    }

    /** Hover background: base color at 30% opacity. */
    public Color getBlockHoverBackground(Long movieId) {
        Color c = getColor(movieId);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 76);
    }

    public void reset() {
        movieIndexMap.clear();
        nextIndex = 0;
    }
}
