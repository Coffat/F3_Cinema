package com.f3cinema.app.ui.common.dialog;

import com.f3cinema.app.config.ThemeConfig;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Dialog hien thi QR MoMo test, ho tro auto-check/polling. */
public final class MomoQrDialog {

    @FunctionalInterface
    public interface AutoCheckFn {
        boolean isPaid();
    }

    private MomoQrDialog() {}

    public static boolean showAndConfirm(Component parent, String qrImageUrl, String amountText, String noteText,
                                         AutoCheckFn autoCheckFn, int timeoutSec, int pollSec) {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(parent), "MoMo Test", Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(ThemeConfig.BG_CARD);

        JLabel title = new JLabel("Thanh toán MoMo (Test)");
        title.setForeground(ThemeConfig.TEXT_PRIMARY);
        title.setFont(new Font("Inter", Font.BOLD, 16));

        JLabel info = new JLabel("<html>Số tiền: <b>" + amountText + "</b><br/>Mã đơn: <b>" + noteText + "</b></html>");
        info.setForeground(ThemeConfig.TEXT_SECONDARY);

        JLabel status = new JLabel(autoCheckFn == null
                ? "Chưa bật auto-check. Bấm 'Đã thanh toán' sau khi xác nhận trên MoMo."
                : "Đang tự động kiểm tra trạng thái thanh toán...");
        status.setForeground(ThemeConfig.TEXT_SECONDARY);

        JLabel qrLabel = new JLabel("Đang tải QR...", SwingConstants.CENTER);
        qrLabel.setForeground(ThemeConfig.TEXT_PRIMARY);
        qrLabel.setPreferredSize(new Dimension(300, 300));
        try {
            ImageIcon icon = new ImageIcon(java.net.URI.create(qrImageUrl).toURL());
            qrLabel.setText("");
            qrLabel.setIcon(icon);
        } catch (Exception e) {
            qrLabel.setText("Không tải được QR: " + e.getMessage());
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        JButton btnCancel = new JButton("Hủy");
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        JButton btnPaid = new JButton("Đã thanh toán");
        btnPaid.putClientProperty(FlatClientProperties.STYLE, "arc: 10; background: #10B981; foreground: #FFFFFF;");

        final boolean[] ok = {false};
        btnCancel.addActionListener(e -> d.dispose());
        btnPaid.addActionListener(e -> {
            ok[0] = true;
            d.dispose();
        });

        buttons.add(btnCancel);
        buttons.add(btnPaid);

        root.add(title, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(info, BorderLayout.NORTH);
        center.add(qrLabel, BorderLayout.CENTER);
        center.add(status, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);

        d.setContentPane(root);
        d.pack();
        d.setLocationRelativeTo(parent);

        if (autoCheckFn != null) {
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    long endAt = System.currentTimeMillis() + Math.max(timeoutSec, 10) * 1000L;
                    long sleepMs = Math.max(pollSec, 1) * 1000L;
                    while (System.currentTimeMillis() < endAt && !isCancelled()) {
                        if (autoCheckFn.isPaid()) {
                            return true;
                        }
                        Thread.sleep(sleepMs);
                    }
                    return false;
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            ok[0] = true;
                            status.setText("Đã nhận thanh toán. Tự động hoàn tất...");
                            d.dispose();
                        } else {
                            status.setText("Hết thời gian auto-check. Bạn có thể bấm 'Đã thanh toán' thủ công.");
                        }
                    } catch (Exception ex) {
                        status.setText("Auto-check lỗi. Bạn có thể bấm 'Đã thanh toán' thủ công.");
                    }
                }
            }.execute();
        }

        d.setVisible(true);
        return ok[0];
    }
}
