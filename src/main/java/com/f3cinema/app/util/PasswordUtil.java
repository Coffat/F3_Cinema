package com.f3cinema.app.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Utility class for BCrypt password hashing and verification.
 * Cost factor = 10 for balanced security and performance.
 */
public final class PasswordUtil {
    private static final int BCRYPT_COST = 10;

    private PasswordUtil() {}

    /**
     * Hash a plain-text password.
     */
    public static String hash(String plainPassword) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
    }

    /**
     * Verify a plain-text password against a stored BCrypt hash.
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}
