package com.f3cinema.app.ui.staff.ticketing.components;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Reusable header component for each step in the ticketing flow.
 * Shows step progress, title, and back button.
 */
public class StepHeader extends JPanel {

    private static final Color BG_SURFACE = new Color(0x1E293B);
    private static final Color TEXT_PRIMARY = new Color(0xF8FAFC);
    private static final Color TEXT_MUTED = new Color(0x94A3B8);

    public StepHeader(int currentStep, int totalSteps, String stepTitle, ActionListener backAction) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JButton btnBack = new JButton("Quay lại");
        FlatSVGIcon backIcon = new FlatSVGIcon("icons/arrow-left.svg", 14, 14);
        backIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> TEXT_PRIMARY));
        btnBack.setIcon(backIcon);
        btnBack.setFont(new Font("Inter", Font.PLAIN, 14));
        btnBack.setForeground(TEXT_PRIMARY);
        btnBack.setBackground(BG_SURFACE);
        btnBack.setPreferredSize(new Dimension(130, 40));
        btnBack.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0;");
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (currentStep == 1) {
            btnBack.setEnabled(false);
        } else {
            btnBack.addActionListener(backAction);
        }

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        centerPanel.setOpaque(false);

        JLabel lblStep = new JLabel(String.format("Bước %d/%d:", currentStep, totalSteps));
        lblStep.setFont(new Font("Inter", Font.PLAIN, 16));
        lblStep.setForeground(TEXT_MUTED);

        JLabel lblTitle = new JLabel(stepTitle);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_PRIMARY);

        centerPanel.add(lblStep);
        centerPanel.add(lblTitle);

        add(btnBack, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
    }
}
