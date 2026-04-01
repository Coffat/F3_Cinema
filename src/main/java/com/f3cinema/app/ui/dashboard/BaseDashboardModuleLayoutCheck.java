package com.f3cinema.app.ui.dashboard;

import javax.swing.*;
import java.awt.*;

/**
 * Lightweight layout sanity check for BaseDashboardModule.
 * This is not a UI test framework; it simply verifies structure and spacing
 * without requiring a visible window.
 */
public final class BaseDashboardModuleLayoutCheck {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BaseDashboardModule module = new BaseDashboardModule("ShouldNotRender", "ShouldNotRender") {};

            // Force layout to run
            module.setSize(new Dimension(1200, 800));
            module.doLayout();

            int rootCount = module.getComponentCount();
            if (rootCount != 1) {
                System.err.println("FAIL: Expected BaseDashboardModule root to contain only contentBody. Found: " + rootCount);
                System.exit(1);
            }

            Component rootChild = module.getComponent(0);
            if (!(rootChild instanceof JPanel)) {
                System.err.println("FAIL: Expected root child to be a JPanel contentBody. Found: " + rootChild.getClass());
                System.exit(1);
            }

            Insets insets = module.getInsets();
            if (insets.top > 16 || insets.left > 20) {
                System.err.println("FAIL: Expected tight padding. Insets: " + insets);
                System.exit(1);
            }

            Rectangle bounds = rootChild.getBounds();
            if (bounds.y > insets.top + 1) {
                System.err.println("FAIL: contentBody should start near top. contentBody.y=" + bounds.y + " insets.top=" + insets.top);
                System.exit(1);
            }

            System.out.println("OK: BaseDashboardModule header removed; content expanded.");
            System.exit(0);
        });
    }
}

