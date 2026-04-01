package com.f3cinema.app.ui.staff.ticketing.components;

import com.f3cinema.app.entity.Customer;
import com.f3cinema.app.entity.enums.PointRedemptionTier;
import com.f3cinema.app.ui.staff.ticketing.TicketOrderState;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Point redemption panel for loyalty program.
 * Displays tier buttons and allows customers to redeem points for discounts.
 */
public class PointRedemptionPanel extends JPanel implements PropertyChangeListener {

    private static final Color TEXT_PRIMARY = new Color(0xF8FAFC);
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);
    private static final Color SUCCESS = new Color(0x10B981);

    private final TicketOrderState state;

    private JButton btn500;
    private JButton btn1000;
    private JButton btn2000;
    private JLabel lblSelectedInfo;
    private JPanel buttonsPanel;

    public PointRedemptionPanel() {
        this.state = TicketOrderState.getInstance();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        initUI();
        attachListeners();
        setEnabled(false);
    }

    private void initUI() {
        JLabel lblTitle = new JLabel("Đổi điểm lấy voucher");
        lblTitle.setFont(new Font("Inter", Font.PLAIN, 13));
        lblTitle.setForeground(TEXT_SECONDARY);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);

        add(lblTitle);
        add(Box.createVerticalStrut(12));

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);

        btn500 = createTierButton(PointRedemptionTier.TIER_500);
        btn1000 = createTierButton(PointRedemptionTier.TIER_1000);
        btn2000 = createTierButton(PointRedemptionTier.TIER_2000);

        buttonsPanel.add(btn500);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(btn1000);
        buttonsPanel.add(Box.createVerticalStrut(8));
        buttonsPanel.add(btn2000);

        add(buttonsPanel);
        add(Box.createVerticalStrut(10));

        lblSelectedInfo = new JLabel("");
        lblSelectedInfo.setFont(new Font("Inter", Font.BOLD, 13));
        lblSelectedInfo.setForeground(SUCCESS);
        lblSelectedInfo.setAlignmentX(LEFT_ALIGNMENT);
        add(lblSelectedInfo);
    }

    private JButton createTierButton(PointRedemptionTier tier) {
        String text = String.format("%,d điểm → -%d%%", tier.getRequiredPoints(), tier.getDiscountPercent());
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 13));
        btn.setForeground(TEXT_PRIMARY);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; background: #0F172A; borderWidth: 1; borderColor: #334155; " +
                "hoverBackground: #6366F1; hoverBorderColor: #6366F1;");
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setEnabled(false);
        
        btn.addActionListener(e -> {
            if (state.getSelectedTier() == tier) {
                state.setPointRedemption(null);
                lblSelectedInfo.setText("");
                lblSelectedInfo.setIcon(null);
                updateButtonStyles();
            } else {
                state.setPointRedemption(tier);
                FlatSVGIcon checkIcon = new FlatSVGIcon("icons/check.svg", 12, 12);
                checkIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> SUCCESS));
                lblSelectedInfo.setIcon(checkIcon);
                lblSelectedInfo.setText(String.format("Áp dụng giảm %d%% (%,d điểm)", 
                        tier.getDiscountPercent(), tier.getRequiredPoints()));
                updateButtonStyles();
            }
        });

        return btn;
    }

    private void updateButtonStyles() {
        PointRedemptionTier selected = state.getSelectedTier();
        
        updateButtonStyle(btn500, PointRedemptionTier.TIER_500, selected);
        updateButtonStyle(btn1000, PointRedemptionTier.TIER_1000, selected);
        updateButtonStyle(btn2000, PointRedemptionTier.TIER_2000, selected);
    }

    private void updateButtonStyle(JButton btn, PointRedemptionTier tier, PointRedemptionTier selected) {
        if (tier == selected) {
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "arc: 10; background: #6366F1; borderWidth: 0; foreground: #FFFFFF;");
        } else {
            btn.putClientProperty(FlatClientProperties.STYLE,
                    "arc: 10; background: #0F172A; borderWidth: 1; borderColor: #334155; " +
                    "hoverBackground: #6366F1; hoverBorderColor: #6366F1;");
        }
        btn.repaint();
    }

    private void attachListeners() {
        state.addPropertyChangeListener("customer", this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("customer".equals(evt.getPropertyName())) {
            SwingUtilities.invokeLater(this::updateAvailableTiers);
        }
    }

    private void updateAvailableTiers() {
        Customer customer = state.getCustomer();
        
        if (customer == null) {
            setEnabled(false);
            lblSelectedInfo.setText("");
            return;
        }

        int points = customer.getPoints() != null ? customer.getPoints() : 0;
        
        btn500.setEnabled(points >= PointRedemptionTier.TIER_500.getRequiredPoints());
        btn1000.setEnabled(points >= PointRedemptionTier.TIER_1000.getRequiredPoints());
        btn2000.setEnabled(points >= PointRedemptionTier.TIER_2000.getRequiredPoints());
        
        setEnabled(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        buttonsPanel.setVisible(enabled);
        if (!enabled) {
            btn500.setEnabled(false);
            btn1000.setEnabled(false);
            btn2000.setEnabled(false);
        }
    }

    public void reset() {
        lblSelectedInfo.setText("");
        lblSelectedInfo.setIcon(null);
        state.setPointRedemption(null);
        updateButtonStyles();
        setEnabled(false);
    }
}
