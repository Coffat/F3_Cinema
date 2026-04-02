package com.f3cinema.app.ui.staff;

import com.f3cinema.app.ui.dashboard.ShowtimePanel;

import javax.swing.*;
import java.awt.*;

/**
 * Staff: cùng UI lịch chiếu như admin ({@link ShowtimePanel}), chỉ xem.
 * Bọc trong {@link BorderLayout} + nền cố định để nội dung luôn lấp đầy vùng CardLayout (tránh màn hình trống).
 */
public class SearchShowtimePanel extends JPanel {

    public SearchShowtimePanel() {
        super(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(0x0F172A));
        add(new ShowtimePanel(true), BorderLayout.CENTER);
    }
}
