package com.f3cinema.app.ui.staff.ticketing.components;

import com.f3cinema.app.ui.staff.ticketing.TicketOrderState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;

/**
 * Order summary card displayed in Step 3 (Snacks) and Step 4 (Payment).
 * Listens to TicketOrderState changes and updates totals in real-time.
 */
public class OrderSummaryCard extends JPanel implements PropertyChangeListener {

    private static final Color BG_SURFACE = new Color(0x1E293B);
    private static final Color BG_ELEVATED = new Color(0x334155);
    private static final Color TEXT_PRIMARY = new Color(0xF8FAFC);
    private static final Color TEXT_MUTED = new Color(0x94A3B8);
    private static final Color SUCCESS = new Color(0x10B981);

    private final TicketOrderState state;

    private JLabel lblSeatTotal;
    private JLabel lblDiscount;
    private JLabel lblGrandTotal;

    public OrderSummaryCard() {
        this.state = TicketOrderState.getInstance();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG_SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BG_ELEVATED, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        initUI();
        attachListeners();
        refresh();
    }

    private void initUI() {
        JLabel title = new JLabel("Tổng đơn hàng");
        title.setFont(new Font("Inter", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        add(title);

        add(Box.createVerticalStrut(12));
        add(createDivider());
        add(Box.createVerticalStrut(12));

        lblDiscount = createLabel("");

        JPanel subtotalRow = new JPanel(new BorderLayout());
        subtotalRow.setOpaque(false);
        subtotalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lblSubtotalLabel = new JLabel("Tạm tính:");
        lblSubtotalLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        lblSubtotalLabel.setForeground(TEXT_MUTED);

        lblSeatTotal = new JLabel("0 VNĐ");
        lblSeatTotal.setFont(new Font("Inter", Font.PLAIN, 14));
        lblSeatTotal.setForeground(TEXT_PRIMARY);

        subtotalRow.add(lblSubtotalLabel, BorderLayout.WEST);
        subtotalRow.add(lblSeatTotal, BorderLayout.EAST);
        add(subtotalRow);

        add(Box.createVerticalStrut(8));

        JPanel discountRow = new JPanel(new BorderLayout());
        discountRow.setOpaque(false);
        discountRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lblDiscountLabel = new JLabel("Giảm giá:");
        lblDiscountLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        lblDiscountLabel.setForeground(TEXT_MUTED);

        lblDiscount.setFont(new Font("Inter", Font.PLAIN, 14));
        lblDiscount.setForeground(new Color(0xEF4444));

        discountRow.add(lblDiscountLabel, BorderLayout.WEST);
        discountRow.add(lblDiscount, BorderLayout.EAST);
        add(discountRow);

        add(Box.createVerticalStrut(12));
        add(createDivider());
        add(Box.createVerticalStrut(12));

        JPanel grandRow = new JPanel(new BorderLayout());
        grandRow.setOpaque(false);
        grandRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel lblGrandLabel = new JLabel("TỔNG CỘNG:");
        lblGrandLabel.setFont(new Font("Inter", Font.BOLD, 16));
        lblGrandLabel.setForeground(TEXT_PRIMARY);

        lblGrandTotal = new JLabel("0 VNĐ");
        lblGrandTotal.setFont(new Font("Inter", Font.BOLD, 20));
        lblGrandTotal.setForeground(SUCCESS);

        grandRow.add(lblGrandLabel, BorderLayout.WEST);
        grandRow.add(lblGrandTotal, BorderLayout.EAST);
        add(grandRow);
    }

    private JSeparator createDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BG_ELEVATED);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Inter", Font.PLAIN, 14));
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    private void attachListeners() {
        state.addPropertyChangeListener("selectedSeats", this);
        state.addPropertyChangeListener("seatTotal", this);
        state.addPropertyChangeListener("snacksTotal", this);
        state.addPropertyChangeListener("discount", this);
        state.addPropertyChangeListener("grandTotal", this);
        state.addPropertyChangeListener("reset", this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(this::refresh);
    }

    private void refresh() {
        BigDecimal subtotal = state.getSeatTotal().add(state.getSnacksTotal());
        lblSeatTotal.setText(formatPrice(subtotal));

        BigDecimal discount = state.getDiscount();
        lblDiscount.setText(discount.compareTo(BigDecimal.ZERO) > 0
                ? "-" + formatPrice(discount)
                : "0 VNĐ");

        lblGrandTotal.setText(formatPrice(state.getGrandTotal()));
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0 VNĐ";
        return String.format("%,.0f VNĐ", price.doubleValue());
    }
}
