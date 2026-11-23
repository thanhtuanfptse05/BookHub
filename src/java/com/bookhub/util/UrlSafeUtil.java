package com.bookhub.util;

import java.util.Locale;

/**
 * Strict URL allow-list helpers preventing open redirects and unsafe cover image schemes (EC-01, EC-03).
 */
public final class UrlSafeUtil {

    private UrlSafeUtil() {
    }

    /**
     * Returns an internal application-relative path starting with '/' (never '//' or external scheme),
     * or null when the candidate URL is unsafe.
     */
    public static String sanitizeReturnUrl(String rawUrl, String contextPath) {
        if (rawUrl == null) {
            return null;
        }
        String trimmed = rawUrl.trim();
        if (trimmed.isEmpty() || trimmed.contains("\r") || trimmed.contains("\n") || trimmed.contains("\\")) {
            return null;
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("javascript:") || lower.startsWith("data:")
                || trimmed.startsWith("//") || !trimmed.startsWith("/")) {
            return null;
        }
        if (contextPath != null && !contextPath.isEmpty() && trimmed.startsWith(contextPath + "/")) {
            trimmed = trimmed.substring(contextPath.length());
        } else if (contextPath != null && !contextPath.isEmpty() && trimmed.equals(contextPath)) {
            trimmed = "/";
        }
        if (trimmed.startsWith("//") || trimmed.contains("://")) {
            return null;
        }
        return trimmed;
    }

    /**
     * Validates book cover_url: allows blank, internal /assets/..., http://, or https://.
     */
    public static boolean isSafeCoverUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return true;
        }
        String trimmed = rawUrl.trim();
        if (trimmed.contains("\r") || trimmed.contains("\n")) {
            return false;
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return true;
        }
        return trimmed.startsWith("/assets/") && !trimmed.contains("..");
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
