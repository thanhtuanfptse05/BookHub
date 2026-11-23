package com.bookhub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local PRJ301 demo implementation outputting reset URLs to server log (ADR-009).
 */
public class LocalLogResetLinkNotifier implements ResetLinkNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalLogResetLinkNotifier.class);

    @Override
    public void notifyResetLink(String recipientEmail, String resetUrl) {
        LOGGER.info("[BOOKHUB-LOCAL-DEMO] Password reset link generated (valid 15 min): {}", resetUrl);
    }
}
