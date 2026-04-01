package com.f3cinema.app.config;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import lombok.extern.log4j.Log4j2;
import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Static Utility to initialize FlatLaf with Modern Midnight theme.
 * Loads tokens from Midnight.properties and invokes FlatLaf.setup().
 * Provides centralized Design System constants.
 */
@Log4j2
public class ThemeConfig {

    // Color Palette
    public static final Color ACCENT_COLOR = Color.decode("#6366F1");
    public static final Color ACCENT_HOVER = Color.decode("#818CF8");
    public static final Color BG_MAIN = Color.decode("#0F172A");
    public static final Color BG_CONTENT = Color.decode("#0F172A");
    public static final Color BG_CARD = Color.decode("#1E293B");
    public static final Color BG_CARD_HOVER = Color.decode("#293548");
    
    // Text Colors
    public static final Color TEXT_PRIMARY = Color.decode("#F8FAFC");
    public static final Color TEXT_SECONDARY = Color.decode("#94A3B8");
    public static final Color TEXT_MUTED = Color.decode("#64748B");
    public static final Color TEXT_SUCCESS = Color.decode("#10B981");
    public static final Color TEXT_DANGER = Color.decode("#F43F5E");
    
    // Border
    public static final Color BORDER_COLOR = Color.decode("#334155");
    
    // Chart Colors
    public static final Color CHART_GRID = Color.decode("#334155");
    
    // Typography Scale
    public static final Font FONT_H1 = new Font("Inter", Font.BOLD, 20);
    public static final Font FONT_H2 = new Font("Inter", Font.BOLD, 16);
    public static final Font FONT_H3 = new Font("Inter", Font.BOLD, 14);
    public static final Font FONT_STAT = new Font("Inter", Font.BOLD, 28);
    public static final Font FONT_BODY = new Font("Inter", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Inter", Font.PLAIN, 12);
    
    // Spacing System
    public static final int PADDING_CARD = 18;
    public static final int GAP_SECTION = 16;
    public static final int GAP_SMALL = 8;
    public static final int MARGIN_PAGE = 24;
    
    // Component Dimensions
    public static final int RADIUS_CARD = 20;
    public static final int RADIUS_BUTTON = 15;

    public static void setup() {
        try {
            // Load custom midnight tokens from properties file
            try (InputStream in = ThemeConfig.class.getResourceAsStream("/themes/Midnight.properties")) {
                if (in != null) {
                    Properties props = new Properties();
                    props.load(in);
                    // Convert Properties → Map<String, String> for FlatLaf API
                    Map<String, String> extraDefaults = new HashMap<>();
                    props.forEach((k, v) -> extraDefaults.put(k.toString(), v.toString()));
                    FlatLaf.setGlobalExtraDefaults(extraDefaults);
                } else {
                    log.warn("Midnight.properties not found, using default FlatMacDarkLaf.");
                }
            }

            // Execute FlatLaf setup AFTER applying extra defaults
            FlatMacDarkLaf.setup();
            log.info("FlatLaf Modern Midnight Theme initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize Modern Midnight theme.", e);
        }
    }
}
