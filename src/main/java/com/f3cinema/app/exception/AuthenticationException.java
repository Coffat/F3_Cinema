package com.f3cinema.app.exception;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends CinemaException {
    public AuthenticationException(String message) {
        super(message);
    }
}
