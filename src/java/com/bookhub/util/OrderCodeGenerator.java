package com.bookhub.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates unique order codes formatted as BH-YYYYMMDD-xxxxx (BR-ORD-004).
 */
public final class OrderCodeGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final SecureRandom RANDOM = new SecureRandom();

    private OrderCodeGenerator() {
    }

    public static String generate() {
        return generate(LocalDate.now());
    }

    public static String generate(LocalDate date) {
        int suffix = 10000 + RANDOM.nextInt(90000);
        return "BH-" + date.format(DATE_FMT) + "-" + suffix;
    }
}
