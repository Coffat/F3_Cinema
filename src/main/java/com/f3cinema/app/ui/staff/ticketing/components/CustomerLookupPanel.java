package com.f3cinema.app.ui.staff.ticketing.components;

import com.f3cinema.app.entity.Customer;
import com.f3cinema.app.service.CustomerService;
import com.f3cinema.app.service.impl.CustomerServiceImpl;
import com.f3cinema.app.ui.common.dialog.AppMessageDialogs;
import com.f3cinema.app.ui.staff.ticketing.TicketOrderState;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Customer lookup panel for loyalty program.
 * Allows staff to search customer by phone and auto-create if new.
 */
public class CustomerLookupPanel extends JPanel {

    private static final Color TEXT_PRIMARY = new Color(0xF8FAFC);
    private static final Color TEXT_SECONDARY = new Color(0x94A3B8);
    private static final Color ACCENT_PRIMARY = new Color(0x6366F1);
    private static final Color SUCCESS = new Color(0x10B981);
    private static final Color WARNING = new Color(0xF59E0B);

    private final TicketOrderState state;
    private final CustomerService customerService;

    private JTextField txtPhone;
    private JTextField txtName;
    private JButton btnLookup;
    private JPanel customerInfoPanel;
    private JLabel lblCustomerName;
    private JLabel lblCustomerPoints;
    private JLabel lblMemberTier;

    public CustomerLookupPanel() {
        this.state = TicketOrderState.getInstance();
        this.customerService = CustomerServiceImpl.getInstance();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE, "arc: 16; background: #1E293B");
        setBorder(new EmptyBorder(20, 24, 20, 24));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        initUI();
    }

    private void initUI() {
        JLabel lblTitle = new JLabel("Thông tin khách hàng");
        FlatSVGIcon icon = new FlatSVGIcon("icons/user.svg", 16, 16);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> TEXT_PRIMARY));
        lblTitle.setIcon(icon);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);

        add(lblTitle);
        add(Box.createVerticalStrut(12));

        JPanel phoneRow = new JPanel(new BorderLayout(10, 0));
        phoneRow.setOpaque(false);
        phoneRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lblPhone = new JLabel("SĐT:");
        lblPhone.setFont(new Font("Inter", Font.PLAIN, 13));
        lblPhone.setForeground(TEXT_SECONDARY);
        lblPhone.setPreferredSize(new Dimension(40, 40));

        txtPhone = new JTextField();
        txtPhone.setFont(new Font("Inter", Font.PLAIN, 14));
        txtPhone.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập số điện thoại...");
        txtPhone.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; background: #0F172A; borderColor: #334155;");
        txtPhone.setForeground(TEXT_PRIMARY);
        txtPhone.setCaretColor(TEXT_PRIMARY);

        btnLookup = new JButton("Tra cứu");
        btnLookup.setFont(new Font("Inter", Font.BOLD, 13));
        btnLookup.setForeground(Color.WHITE);
        btnLookup.setBackground(ACCENT_PRIMARY);
        btnLookup.setPreferredSize(new Dimension(100, 40));
        btnLookup.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0;");
        btnLookup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLookup.addActionListener(e -> lookupCustomer());

        phoneRow.add(lblPhone, BorderLayout.WEST);
        phoneRow.add(txtPhone, BorderLayout.CENTER);
        phoneRow.add(btnLookup, BorderLayout.EAST);

        add(phoneRow);
        add(Box.createVerticalStrut(10));

        txtName = new JTextField();
        txtName.setFont(new Font("Inter", Font.PLAIN, 14));
        txtName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tên khách hàng (nếu là khách mới)...");
        txtName.putClientProperty(FlatClientProperties.STYLE,
                "arc: 10; background: #0F172A; borderColor: #334155;");
        txtName.setForeground(TEXT_PRIMARY);
        txtName.setCaretColor(TEXT_PRIMARY);
        txtName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtName.setVisible(false);

        add(txtName);
        add(Box.createVerticalStrut(10));

        customerInfoPanel = createCustomerInfoPanel();
        customerInfoPanel.setVisible(false);
        add(customerInfoPanel);
    }

    private JPanel createCustomerInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        lblCustomerName = new JLabel();
        lblCustomerName.setFont(new Font("Inter", Font.BOLD, 15));
        lblCustomerName.setForeground(TEXT_PRIMARY);
        lblCustomerName.setAlignmentX(LEFT_ALIGNMENT);

        lblCustomerPoints = new JLabel();
        lblCustomerPoints.setFont(new Font("Inter", Font.PLAIN, 14));
        lblCustomerPoints.setForeground(SUCCESS);
        lblCustomerPoints.setAlignmentX(LEFT_ALIGNMENT);

        lblMemberTier = new JLabel();
        lblMemberTier.setFont(new Font("Inter", Font.BOLD, 13));
        lblMemberTier.setForeground(WARNING);
        lblMemberTier.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(lblCustomerName);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lblCustomerPoints);
        panel.add(Box.createVerticalStrut(2));
        panel.add(lblMemberTier);

        return panel;
    }

    private void lookupCustomer() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            AppMessageDialogs.showWarning(this, "Thông báo", "Vui lòng nhập số điện thoại!");
            return;
        }

        btnLookup.setEnabled(false);
        btnLookup.setText("Đang tìm...");

        new SwingWorker<Customer, Void>() {
            @Override
            protected Customer doInBackground() throws Exception {
                try {
                    return customerService.findOrCreateByPhone(phone, null);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    Customer customer = get();
                    
                    if (customer == null) {
                        txtName.setVisible(true);
                        txtName.requestFocus();
                        btnLookup.setText("Tạo mới");
                        btnLookup.setEnabled(true);
                        btnLookup.removeActionListener(btnLookup.getActionListeners()[0]);
                        btnLookup.addActionListener(e -> createNewCustomer(phone));
                        AppMessageDialogs.showInfo(CustomerLookupPanel.this, "Thông báo",
                                "Khách hàng mới! Vui lòng nhập tên để tạo tài khoản.");
                    } else {
                        state.setCustomer(customer);
                        displayCustomerInfo(customer);
                        txtName.setVisible(false);
                        btnLookup.setText("Tra cứu");
                        btnLookup.setEnabled(true);
                    }
                } catch (Exception e) {
                    AppMessageDialogs.showError(CustomerLookupPanel.this, "Lỗi", "Lỗi: " + e.getMessage());
                    btnLookup.setEnabled(true);
                    btnLookup.setText("Tra cứu");
                }
            }
        }.execute();
    }

    private void createNewCustomer(String phone) {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            AppMessageDialogs.showWarning(this, "Thông báo", "Vui lòng nhập tên khách hàng!");
            return;
        }

        btnLookup.setEnabled(false);
        btnLookup.setText("...");

        new SwingWorker<Customer, Void>() {
            @Override
            protected Customer doInBackground() throws Exception {
                return customerService.findOrCreateByPhone(phone, name);
            }

            @Override
            protected void done() {
                try {
                    Customer customer = get();
                    state.setCustomer(customer);
                    displayCustomerInfo(customer);
                    txtName.setVisible(false);
                    
                    btnLookup.removeActionListener(btnLookup.getActionListeners()[0]);
                    btnLookup.addActionListener(e -> lookupCustomer());
                    btnLookup.setText("Tra cứu");
                    btnLookup.setEnabled(true);
                    
                    AppMessageDialogs.showInfo(CustomerLookupPanel.this, "Thành công",
                            "Tạo tài khoản thành công cho " + customer.getFullName() + "!");
                } catch (Exception e) {
                    AppMessageDialogs.showError(CustomerLookupPanel.this, "Lỗi", "Lỗi tạo tài khoản: " + e.getMessage());
                    btnLookup.setEnabled(true);
                    btnLookup.setText("Tạo mới");
                }
            }
        }.execute();
    }

    private void displayCustomerInfo(Customer customer) {
        lblCustomerName.setText(customer.getFullName());
        FlatSVGIcon checkIcon = new FlatSVGIcon("icons/check.svg", 14, 14);
        checkIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> SUCCESS));
        lblCustomerName.setIcon(checkIcon);
        
        int points = customer.getPoints() != null ? customer.getPoints() : 0;
        lblCustomerPoints.setText(String.format("Điểm hiện có: %,d điểm", points));

        String tier;
        if (points >= 2000) {
            tier = "Thành viên Vàng";
        } else if (points >= 1000) {
            tier = "Thành viên Bạc";
        } else if (points >= 500) {
            tier = "Thành viên Đồng";
        } else {
            tier = "Thành viên Mới";
        }
        
        FlatSVGIcon starIcon = new FlatSVGIcon("icons/star.svg", 12, 12);
        starIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> WARNING));
        lblMemberTier.setIcon(starIcon);
        lblMemberTier.setText(tier);

        customerInfoPanel.setVisible(true);
        revalidate();
        repaint();
    }

    public void reset() {
        txtPhone.setText("");
        txtName.setText("");
        txtName.setVisible(false);
        customerInfoPanel.setVisible(false);
        state.clearCustomer();
        
        if (btnLookup.getActionListeners().length > 0) {
            btnLookup.removeActionListener(btnLookup.getActionListeners()[0]);
        }
        btnLookup.addActionListener(e -> lookupCustomer());
        btnLookup.setText("Tra cứu");
        btnLookup.setEnabled(true);
    }
}
