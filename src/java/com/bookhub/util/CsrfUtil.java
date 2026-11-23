package com.bookhub.util;

import com.bookhub.config.AppConstants;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Synchronizer-token CSRF utility using constant-time digest comparison (FR-05, FR-06).
 */
public final class CsrfUtil {

    public static final String PARAM_NAME = "_csrf";

    private CsrfUtil() {
    }

    public static String getOrCreateToken(HttpSession session) {
        if (session == null) {
            throw new IllegalArgumentException("HttpSession must not be null");
        }
        Object existing = session.getAttribute(AppConstants.SESSION_CSRF_TOKEN_KEY);
        if (existing instanceof String && !((String) existing).isEmpty()) {
            return (String) existing;
        }
        String token = PasswordUtil.generateOpaqueToken();
        session.setAttribute(AppConstants.SESSION_CSRF_TOKEN_KEY, token);
        return token;
    }

    public static boolean isValid(HttpSession session, String submittedToken) {
        if (session == null || submittedToken == null || submittedToken.isEmpty()) {
            return false;
        }
        Object expectedObj = session.getAttribute(AppConstants.SESSION_CSRF_TOKEN_KEY);
        if (!(expectedObj instanceof String)) {
            return false;
        }
        String expected = (String) expectedObj;
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                submittedToken.getBytes(StandardCharsets.UTF_8));
    }
}
