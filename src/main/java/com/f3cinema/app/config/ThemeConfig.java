package com.f3cinema.app.config;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import lombok.extern.log4j.Log4j2;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Static Utility to initialize FlatLaf with Modern Midnight theme.
 * Loads tokens from Midnight.properties and invokes FlatLaf.setup().
 */
@Log4j2
public class ThemeConfig {

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
