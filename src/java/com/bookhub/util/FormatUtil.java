package com.bookhub.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Vietnamese currency (VND) and LocalDateTime display formatters used in DTOs.
 */
public final class FormatUtil {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FormatUtil() {
    }

    public static String formatVnd(BigDecimal amount) {
        BigDecimal safe = amount == null ? BigDecimal.ZERO : amount.setScale(0, RoundingMode.HALF_UP);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,##0", symbols);
        return df.format(safe) + " \u20AB";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "\u2014" : dateTime.format(DATE_TIME_FMT);
    }

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "\u2014" : dateTime.format(DATE_FMT);
    }

    public static String formatBookCode(Long bookId) {
        return bookId == null ? "" : String.format("BK-%05d", bookId);
    }

    public static String safeCoverUrl(String coverUrl) {
        if (coverUrl == null || coverUrl.trim().isEmpty()) {
            return "/assets/images/book-placeholder.svg";
        }
        return coverUrl.trim();
    }
}
