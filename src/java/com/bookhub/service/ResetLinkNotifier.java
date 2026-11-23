package com.bookhub.service;

/**
 * Pluggable password-reset notification boundary (ADR-009).
 */
public interface ResetLinkNotifier {

    void notifyResetLink(String recipientEmail, String resetUrl);
}
