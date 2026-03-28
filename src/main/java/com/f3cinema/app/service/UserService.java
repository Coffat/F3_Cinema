package com.f3cinema.app.service;

import com.f3cinema.app.entity.User;
import com.f3cinema.app.entity.enums.UserRole;
import com.f3cinema.app.exception.AuthenticationException;
import com.f3cinema.app.repository.UserRepository;
import com.f3cinema.app.util.PasswordUtil;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;

/**
 * Service for User authentication and management.
 */
@Log4j2
public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    /**
     * Authenticate a user by username and password.
     * Throws AuthenticationException on failure. Returns User on success.
     */
    public User authenticate(String username, String password) {
        log.info("Authentication attempt for user: {}", username);

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AuthenticationException("Tài khoản hoặc mật khẩu không được bỏ trống.");
        }

        Optional<User> userOpt = userRepository.findByUsername(username.trim());

        if (userOpt.isEmpty()) {
            log.warn("Authentication failed - user not found: {}", username);
            throw new AuthenticationException("Tài khoản hoặc mật khẩu không chính xác.");
        }

        User user = userOpt.get();
        if (!PasswordUtil.verify(password, user.getPassword())) {
            log.warn("Authentication failed - wrong password for user: {}", username);
            throw new AuthenticationException("Tài khoản hoặc mật khẩu không chính xác.");
        }

        log.info("Authentication successful for user: {} (role: {})", user.getUsername(), user.getRole());
        return user;
    }

    public List<User> getAllStaff(String keyword) {
        return userRepository.searchStaff(keyword);
    }

    public User createStaff(String username, String fullName, String plainPasswordOrNull) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username không được để trống.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        String normalizedUsername = username.trim();

        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại.");
        }

        String pwd = (plainPasswordOrNull == null || plainPasswordOrNull.isBlank()) ? "1" : plainPasswordOrNull;
        User staff = User.builder()
                .username(normalizedUsername)
                .fullName(fullName.trim())
                .password(PasswordUtil.hash(pwd))
                .role(UserRole.STAFF)
                .build();
        return userRepository.save(staff);
    }

    public User updateStaff(Long id, String username, String fullName, String newPlainPasswordOrNull) {
        if (id == null) throw new IllegalArgumentException("ID không hợp lệ.");

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên."));

        if (existing.getRole() != UserRole.STAFF) {
            throw new IllegalArgumentException("Chỉ cho phép chỉnh sửa tài khoản STAFF.");
        }

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username không được để trống.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }

        String normalizedUsername = username.trim();
        userRepository.findByUsername(normalizedUsername).ifPresent(u -> {
            if (u.getId() != null && !u.getId().equals(id)) {
                throw new IllegalArgumentException("Username đã tồn tại.");
            }
        });

        existing.setUsername(normalizedUsername);
        existing.setFullName(fullName.trim());
        if (newPlainPasswordOrNull != null && !newPlainPasswordOrNull.isBlank()) {
            existing.setPassword(PasswordUtil.hash(newPlainPasswordOrNull));
        }
        return userRepository.update(existing);
    }

    public void deleteStaff(Long id) {
        if (id == null) throw new IllegalArgumentException("ID không hợp lệ.");

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên."));

        if (existing.getRole() != UserRole.STAFF) {
            throw new IllegalArgumentException("Chỉ cho phép xóa tài khoản STAFF.");
        }
        userRepository.delete(existing);
    }
}
