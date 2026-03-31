package com.f3cinema.app.util;

import com.f3cinema.app.entity.User;

/**
 * SessionManager - Lưu trữ người dùng đang đăng nhập trong luồng ứng dụng.
 */
public class SessionManager {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}
