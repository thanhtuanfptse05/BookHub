package com.bookhub.exception;

/**
 * Thrown when a requested book, category, order, review, or question cannot be found or is inactive publicly.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
