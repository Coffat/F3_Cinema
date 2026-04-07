package com.f3cinema.app.ui.common.dialog;

import java.awt.Component;

/** Nhung trang MoMo payUrl vao dialog trong app. */
public final class MomoWebDialog {

    private MomoWebDialog() {}

    public static boolean showAndConfirm(
            Component parent,
            String payUrl,
            String fallbackQrUrl,
            String amountText,
            String noteText,
            MomoQrDialog.AutoCheckFn autoCheckFn,
            int timeoutSec,
            int pollSec
    ) {
        if (payUrl == null || payUrl.isBlank() || !isHttpUrl(payUrl)) {
            return MomoQrDialog.showAndConfirm(parent, fallbackQrUrl, amountText, noteText, autoCheckFn, timeoutSec, pollSec);
        }

        try {
            return showWebDialog(parent, payUrl, fallbackQrUrl, amountText, noteText, autoCheckFn, timeoutSec, pollSec);
        } catch (Throwable t) {
            return MomoQrDialog.showAndConfirm(parent, fallbackQrUrl, amountText, noteText, autoCheckFn, timeoutSec, pollSec);
        }
    }

    private static boolean showWebDialog(
            Component parent,
            String payUrl,
            String fallbackQrUrl,
            String amountText,
            String noteText,
            MomoQrDialog.AutoCheckFn autoCheckFn,
            int timeoutSec,
            int pollSec
    ) {
        // Project hiện không dùng JavaFX. Fallback về QR dialog để đảm bảo compile/runtime ổn định.
        return MomoQrDialog.showAndConfirm(
                parent,
                fallbackQrUrl,
                amountText,
                noteText,
                autoCheckFn,
                timeoutSec,
                pollSec
        );
    }

    private static boolean isHttpUrl(String url) {
        String u = url.toLowerCase();
        return u.startsWith("http://") || u.startsWith("https://");
    }
}
