package com.bookhub.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Domain rule or server-side validation failure presented cleanly to the user.
 */
public class BusinessException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public BusinessException(String message) {
        super(message);
        this.fieldErrors = Collections.emptyMap();
    }

    public BusinessException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors));
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}
