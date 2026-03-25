package com.f3cinema.app.exception;

/**
 * Base custom runtime exception for F3 Cinema system.
 */
public class CinemaException extends RuntimeException {
    public CinemaException(String message) {
        super(message);
    }

    public CinemaException(String message, Throwable cause) {
        super(message, cause);
    }
}
