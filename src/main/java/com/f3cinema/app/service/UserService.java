package com.f3cinema.app.service;

import com.f3cinema.app.entity.User;
import com.f3cinema.app.exception.AuthenticationException;
import com.f3cinema.app.repository.UserRepository;
import com.f3cinema.app.util.PasswordUtil;
import lombok.extern.log4j.Log4j2;

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
}
