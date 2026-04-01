package com.f3cinema.app.ui;

import com.f3cinema.app.entity.User;
import javax.swing.*;
import java.awt.*;

/**
 * Placeholder Main Dashboard Frame.
 * Full implementation to be built by the team.
 */
public class MainDashboardFrame extends JFrame {

    private final User loggedInUser;

    public MainDashboardFrame(User user) {
        this.loggedInUser = user;
        initialize();
    }

    private void initialize() {
        setTitle("F3 Cinema - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở full màn hình mặc định
        getContentPane().setBackground(Color.decode("#0f172a"));
        setLayout(new BorderLayout());

        JLabel label = new JLabel(
            "Chào mừng, " + loggedInUser.getFullName() + " (" + loggedInUser.getRole().getLabel() + ")",
            SwingConstants.CENTER
        );
        label.setFont(new Font("Inter", Font.BOLD, 24));
        label.setForeground(Color.decode("#f8fafc"));
        add(label, BorderLayout.CENTER);
    }
}
