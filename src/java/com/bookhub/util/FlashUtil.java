package com.bookhub.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * PRG (Post/Redirect/Get) one-shot flash message helper stored in HttpSession and consumed on next GET.
 */
public final class FlashUtil {

    private static final String KEY_SUCCESS = "flashSuccess";
    private static final String KEY_ERROR = "flashError";
    private static final String KEY_WARNING = "flashWarning";
    private static final String KEY_INFO = "flashInfo";

    private FlashUtil() {
    }

    public static void putSuccess(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute(KEY_SUCCESS, message);
    }

    public static void putError(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute(KEY_ERROR, message);
    }

    public static void putWarning(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute(KEY_WARNING, message);
    }

    public static void putInfo(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute(KEY_INFO, message);
    }

    public static void consumeToRequest(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        moveAttr(session, req, KEY_SUCCESS);
        moveAttr(session, req, KEY_ERROR);
        moveAttr(session, req, KEY_WARNING);
        moveAttr(session, req, KEY_INFO);
    }

    private static void moveAttr(HttpSession session, HttpServletRequest req, String key) {
        Object val = session.getAttribute(key);
        if (val != null) {
            req.setAttribute(key, val);
            session.removeAttribute(key);
        }
    }
}
