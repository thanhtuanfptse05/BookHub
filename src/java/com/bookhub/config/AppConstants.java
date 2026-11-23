package com.bookhub.config;

import java.math.BigDecimal;

/**
 * Shared immutable constants for BookHub runtime configuration and pagination.
 */
public final class AppConstants {

    public static final String PERSISTENCE_UNIT_NAME = "BookHubPU";
    public static final String DEFAULT_CONFIG_FILE = "bookhub-local.properties";
    public static final String CONFIG_PATH_SYS_PROP = "bookhub.config.path";
    public static final String CONFIG_PATH_ENV = "BOOKHUB_CONFIG_PATH";

    public static final String CONTEXT_ATTR_CONFIG = "bookhubConfig";
    public static final String SESSION_USER_KEY = "currentUser";
    public static final String SESSION_CSRF_TOKEN_KEY = "csrfToken";

    public static final BigDecimal DEFAULT_SHIPPING_FEE = new BigDecimal("30000.00");
    public static final int DEFAULT_RESET_TOKEN_TTL_MINUTES = 15;
    public static final int DEFAULT_CATALOG_PAGE_SIZE = 12;
    public static final int DEFAULT_ADMIN_PAGE_SIZE = 15;
    public static final String DEFAULT_BASE_URL = "http://localhost:8080/BookHub";

    private AppConstants() {
    }
}
