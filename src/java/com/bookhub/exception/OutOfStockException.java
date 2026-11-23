package com.bookhub.exception;

import java.util.Collections;
import java.util.List;

/**
 * Thrown when requested quantity exceeds available stock during cart update or atomic checkout.
 */
public class OutOfStockException extends BusinessException {

    private final List<String> affectedTitles;

    public OutOfStockException(String message) {
        super(message);
        this.affectedTitles = Collections.emptyList();
    }

    public OutOfStockException(String message, List<String> affectedTitles) {
        super(message);
        this.affectedTitles = affectedTitles == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(affectedTitles);
    }

    public List<String> getAffectedTitles() {
        return affectedTitles;
    }
}
