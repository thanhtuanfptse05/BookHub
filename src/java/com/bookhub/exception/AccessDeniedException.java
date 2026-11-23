package com.bookhub.exception;

/**
 * Thrown on unauthorized access or ownership violation (IDOR / non-admin action).
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
