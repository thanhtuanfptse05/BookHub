package com.bookhub.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Centralized runtime configuration loaded from uncommitted local properties or environment variables.
 * Never exposes db.password in exception messages or toString output (Constitution H-01, H-05).
 */
public final class AppConfig {

    public static final String KEY_DB_URL = "db.url";
    public static final String KEY_DB_USERNAME = "db.username";
    public static final String KEY_DB_PASSWORD = "db.password";
    public static final String KEY_SHIPPING_FEE = "app.shipping.fee";
    public static final String KEY_RESET_TTL_MINUTES = "app.reset.token.ttl.minutes";
    public static final String KEY_SHOW_SQL = "app.show.sql";
    public static final String KEY_BASE_URL = "app.base.url";

    private static final Pattern PASSWORD_PARAM_PATTERN =
            Pattern.compile("(?i)(;\\s*password\\s*=)[^;]*");

    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final BigDecimal shippingFee;
    private final int resetTokenTtlMinutes;
    private final boolean showSql;
    private final String baseUrl;
    private final String sourceDescription;

    private AppConfig(String dbUrl, String dbUsername, String dbPassword, BigDecimal shippingFee,
            int resetTokenTtlMinutes, boolean showSql, String baseUrl, String sourceDescription) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.shippingFee = shippingFee;
        this.resetTokenTtlMinutes = resetTokenTtlMinutes;
        this.showSql = showSql;
        this.baseUrl = baseUrl;
        this.sourceDescription = sourceDescription;
    }

    public static AppConfig load() {
        return load(System.getProperties(), System.getenv());
    }

    public static AppConfig load(Path explicitFile) {
        if (explicitFile == null || !Files.isRegularFile(explicitFile)) {
            throw new IllegalStateException("BookHub configuration file not found: "
                    + (explicitFile == null ? "<null>" : explicitFile.toAbsolutePath())
                    + ". Copy bookhub-local.properties.example to bookhub-local.properties.");
        }
        return fromProperties(readFile(explicitFile), Collections.emptyMap(), explicitFile.toAbsolutePath().toString());
    }

    static AppConfig load(Properties sysProps, Map<String, String> env) {
        String explicitPath = firstNonBlank(
                sysProps.getProperty(AppConstants.CONFIG_PATH_SYS_PROP), env.get(AppConstants.CONFIG_PATH_ENV));
        if (explicitPath != null) {
            Path path = Paths.get(explicitPath);
            if (!Files.isRegularFile(path)) {
                throw new IllegalStateException("Configured BookHub properties file does not exist: " + path.toAbsolutePath());
            }
            return fromProperties(readFile(path), env, path.toAbsolutePath().toString());
        }
        Path discovered = discoverDefaultFile(sysProps);
        if (discovered != null) {
            return fromProperties(readFile(discovered), env, discovered.toAbsolutePath().toString());
        }
        Properties classpathProps = readClasspathProperties();
        if (classpathProps != null) {
            return fromProperties(classpathProps, env, "classpath:/" + AppConstants.DEFAULT_CONFIG_FILE);
        }
        if (!isBlank(env.get("BOOKHUB_DB_URL")) || !isBlank(env.get("BOOKHUB_DB_USERNAME"))
                || !isBlank(env.get("BOOKHUB_DB_PASSWORD"))) {
            return fromProperties(new Properties(), env, "environment-variables");
        }
        throw new IllegalStateException("Missing BookHub configuration: bookhub-local.properties was not found and "
                + "BOOKHUB_DB_* environment variables are not set.");
    }

    public static AppConfig fromProperties(Properties props, Map<String, String> envOverrides, String source) {
        Properties safeProps = props != null ? props : new Properties();
        Map<String, String> safeEnv = envOverrides != null ? envOverrides : Collections.emptyMap();

        String dbUrl = resolve(safeProps, KEY_DB_URL, safeEnv, "BOOKHUB_DB_URL");
        String dbUsername = resolve(safeProps, KEY_DB_USERNAME, safeEnv, "BOOKHUB_DB_USERNAME");
        String dbPassword = resolve(safeProps, KEY_DB_PASSWORD, safeEnv, "BOOKHUB_DB_PASSWORD");

        List<String> missing = new ArrayList<>();
        if (isBlank(dbUrl)) { missing.add(KEY_DB_URL); }
        if (isBlank(dbUsername)) { missing.add(KEY_DB_USERNAME); }
        if (isBlank(dbPassword)) { missing.add(KEY_DB_PASSWORD); }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required BookHub configuration keys in " + source + ": " + missing);
        }

        BigDecimal shippingFee = parseShippingFee(resolve(safeProps, KEY_SHIPPING_FEE, safeEnv, "BOOKHUB_SHIPPING_FEE"));
        int resetTtl = parsePositiveInt(
                resolve(safeProps, KEY_RESET_TTL_MINUTES, safeEnv, "BOOKHUB_RESET_TTL_MINUTES"),
                AppConstants.DEFAULT_RESET_TOKEN_TTL_MINUTES, KEY_RESET_TTL_MINUTES);
        boolean showSql = Boolean.parseBoolean(resolve(safeProps, KEY_SHOW_SQL, safeEnv, "BOOKHUB_SHOW_SQL"));
        String baseUrl = firstNonBlank(resolve(safeProps, KEY_BASE_URL, safeEnv, "BOOKHUB_BASE_URL"), AppConstants.DEFAULT_BASE_URL);

        return new AppConfig(dbUrl.trim(), dbUsername.trim(), dbPassword, shippingFee, resetTtl, showSql, baseUrl.trim(), source);
    }

    public Map<String, Object> toJpaProperties() {
        Map<String, Object> map = new HashMap<>();
        map.put("jakarta.persistence.jdbc.url", dbUrl);
        map.put("jakarta.persistence.jdbc.user", dbUsername);
        map.put("jakarta.persistence.jdbc.password", dbPassword);
        map.put("hibernate.show_sql", String.valueOf(showSql));
        return Collections.unmodifiableMap(map);
    }

    public String getDbUrl() { return dbUrl; }
    public String getDbUsername() { return dbUsername; }
    public String getDbPassword() { return dbPassword; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public int getResetTokenTtlMinutes() { return resetTokenTtlMinutes; }
    public boolean isShowSql() { return showSql; }
    public String getBaseUrl() { return baseUrl; }
    public String getSourceDescription() { return sourceDescription; }

    public String getSanitizedDbUrl() {
        return PASSWORD_PARAM_PATTERN.matcher(dbUrl).replaceAll("$1****");
    }

    @Override
    public String toString() {
        return "AppConfig{dbUrl='" + getSanitizedDbUrl() + "', dbPassword='****', shippingFee=" + shippingFee
                + ", resetTokenTtlMinutes=" + resetTokenTtlMinutes + ", showSql=" + showSql
                + ", baseUrl='" + baseUrl + "', source='" + sourceDescription + "'}";
    }

    private static Path discoverDefaultFile(Properties sysProps) {
        List<Path> candidates = new ArrayList<>();
        candidates.add(Paths.get(AppConstants.DEFAULT_CONFIG_FILE));
        String catalinaBase = sysProps.getProperty("catalina.base");
        if (!isBlank(catalinaBase)) {
            candidates.add(Paths.get(catalinaBase, "conf", AppConstants.DEFAULT_CONFIG_FILE));
        }
        String userHome = sysProps.getProperty("user.home");
        if (!isBlank(userHome)) {
            candidates.add(Paths.get(userHome, ".bookhub", AppConstants.DEFAULT_CONFIG_FILE));
        }
        return candidates.stream().filter(Files::isRegularFile).findFirst().orElse(null);
    }

    private static Properties readFile(Path path) {
        try (InputStream in = Files.newInputStream(path);
             InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            Properties props = new Properties();
            props.load(reader);
            return props;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read BookHub configuration file: " + path.toAbsolutePath(), ex);
        }
    }

    private static Properties readClasspathProperties() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = AppConfig.class.getClassLoader();
        }
        try (InputStream in = cl.getResourceAsStream(AppConstants.DEFAULT_CONFIG_FILE)) {
            if (in == null) {
                return null;
            }
            Properties props = new Properties();
            try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                props.load(reader);
            }
            return props;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read classpath " + AppConstants.DEFAULT_CONFIG_FILE, ex);
        }
    }

    private static String resolve(Properties props, String propKey, Map<String, String> env, String envKey) {
        String envVal = env.get(envKey);
        return !isBlank(envVal) ? envVal : props.getProperty(propKey);
    }

    private static BigDecimal parseShippingFee(String raw) {
        if (isBlank(raw)) {
            return AppConstants.DEFAULT_SHIPPING_FEE;
        }
        try {
            BigDecimal fee = new BigDecimal(raw.trim()).setScale(2, RoundingMode.HALF_UP);
            if (fee.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException(KEY_SHIPPING_FEE + " must be >= 0");
            }
            return fee;
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Invalid decimal value for " + KEY_SHIPPING_FEE, ex);
        }
    }

    private static int parsePositiveInt(String raw, int defaultValue, String keyName) {
        if (isBlank(raw)) {
            return defaultValue;
        }
        try {
            int val = Integer.parseInt(raw.trim());
            if (val <= 0) {
                throw new IllegalStateException(keyName + " must be > 0");
            }
            return val;
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Invalid integer value for " + keyName, ex);
        }
    }

    private static String firstNonBlank(String first, String second) {
        return !isBlank(first) ? first : (isBlank(second) ? null : second);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
