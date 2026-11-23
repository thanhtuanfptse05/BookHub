package com.bookhub.config;

import com.bookhub.entity.Book;
import com.bookhub.entity.PasswordResetToken;
import com.bookhub.entity.User;
import com.bookhub.entity.UserStatus;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for AppConfig fail-fast/redaction rules, entity invariants, and Jakarta EE 10 packaging constraints.
 */
class AppConfigTest {

    // @spec foundation-jpa/AC-05
    @Test
    void loadExplicitMissingFileFailsFastWithClearMessage(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("does-not-exist.properties");
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> AppConfig.load(missing));
        assertTrue(ex.getMessage().contains("not found"));
    }

    // @spec foundation-jpa/AC-05
    @Test
    void missingRequiredKeysFailsFastWithoutLeakingPassword() {
        Properties props = new Properties();
        props.setProperty(AppConfig.KEY_DB_USERNAME, "sa");
        props.setProperty(AppConfig.KEY_DB_PASSWORD, "SuperSecretValue_9988");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> AppConfig.fromProperties(props, Collections.emptyMap(), "unit-test"));

        assertTrue(ex.getMessage().contains(AppConfig.KEY_DB_URL));
        assertFalse(ex.getMessage().contains("SuperSecretValue_9988"), "Password must never appear in error log");
    }

    // @spec foundation-jpa/AC-05
    @Test
    void missingPasswordKeyFailsFast() {
        Properties props = new Properties();
        props.setProperty(AppConfig.KEY_DB_URL, "jdbc:sqlserver://localhost:1433;databaseName=BookHubDB");
        props.setProperty(AppConfig.KEY_DB_USERNAME, "sa");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> AppConfig.fromProperties(props, Collections.emptyMap(), "unit-test"));

        assertTrue(ex.getMessage().contains(AppConfig.KEY_DB_PASSWORD));
    }

    // @spec foundation-jpa/AC-05
    @Test
    void validConfigLoadsDefaultsAndRedactsSecretsInToString() {
        Properties props = new Properties();
        props.setProperty(
                AppConfig.KEY_DB_URL,
                "jdbc:sqlserver://localhost:1433;databaseName=BookHubDB;password=InlineSecret123;encrypt=true");
        props.setProperty(AppConfig.KEY_DB_USERNAME, "bookhub_app");
        props.setProperty(AppConfig.KEY_DB_PASSWORD, "TopSecretPwd_42");

        AppConfig cfg = AppConfig.fromProperties(props, Collections.emptyMap(), "unit-test");

        assertEquals(new BigDecimal("30000.00"), cfg.getShippingFee());
        assertEquals(15, cfg.getResetTokenTtlMinutes());
        assertFalse(cfg.isShowSql());
        assertEquals("TopSecretPwd_42", cfg.toJpaProperties().get("jakarta.persistence.jdbc.password"));

        String rendered = cfg.toString();
        assertFalse(rendered.contains("TopSecretPwd_42"), "toString must redact db.password");
        assertFalse(rendered.contains("InlineSecret123"), "toString must redact inline JDBC password");
        assertTrue(cfg.getSanitizedDbUrl().contains("password=****"));
    }

    // @spec foundation-jpa/AC-05
    @Test
    void envVariablesOverrideFileProperties() {
        Properties props = new Properties();
        props.setProperty(AppConfig.KEY_DB_URL, "jdbc:sqlserver://file-host:1433;databaseName=BookHubDB");
        props.setProperty(AppConfig.KEY_DB_USERNAME, "file_user");
        props.setProperty(AppConfig.KEY_DB_PASSWORD, "file_pass");

        Map<String, String> env = new HashMap<>();
        env.put("BOOKHUB_DB_URL", "jdbc:sqlserver://env-host:1433;databaseName=BookHubDB");
        env.put("BOOKHUB_SHIPPING_FEE", "25000");

        AppConfig cfg = AppConfig.fromProperties(props, env, "unit-test");
        assertEquals("jdbc:sqlserver://env-host:1433;databaseName=BookHubDB", cfg.getDbUrl());
        assertEquals(new BigDecimal("25000.00"), cfg.getShippingFee());
    }

    // @spec foundation-jpa/AC-03
    @Test
    void loadForWebAppDiscoversConfigAtProjectRootFromBuildWeb(@TempDir Path tempProjectRoot) throws IOException {
        Path buildWebDir = Files.createDirectories(tempProjectRoot.resolve("build").resolve("web"));
        Path configFile = tempProjectRoot.resolve(AppConstants.DEFAULT_CONFIG_FILE);
        Files.writeString(
                configFile,
                "db.url=jdbc:sqlserver://netbeans-host:1433;databaseName=BookHubDB\n"
                        + "db.username=nb_dev\n"
                        + "db.password=NbSecret_123\n",
                StandardCharsets.UTF_8);

        AppConfig cfg = AppConfig.load(new Properties(), Collections.emptyMap(), buildWebDir);
        assertEquals("jdbc:sqlserver://netbeans-host:1433;databaseName=BookHubDB", cfg.getDbUrl());
        assertEquals("nb_dev", cfg.getDbUsername());
        assertEquals(configFile.toAbsolutePath().toString(), cfg.getSourceDescription());
    }

    @Test
    void entityEqualsHashCodeAndSafeToStringFollowInvariants() {
        User u1 = new User();
        u1.setUserId(10L);
        u1.setEmail("a@bookhub.vn");
        u1.setPasswordHash("$2a$10$NeverPrintThisHashValue");
        u1.setStatus(UserStatus.ACTIVE);

        User u2 = new User();
        u2.setUserId(10L);

        User transientUser = new User();

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
        assertNotEquals(u1, transientUser);
        assertFalse(u1.toString().contains("NeverPrintThisHashValue"));

        PasswordResetToken token = new PasswordResetToken();
        token.setResetId(1L);
        token.setTokenHash("sha256_secret_token_hash_value");
        assertFalse(token.toString().contains("sha256_secret_token_hash_value"));

        Book b = new Book();
        b.setPrice(new BigDecimal("125000.00"));
        assertEquals(2, b.getPrice().scale());
    }

    // @spec foundation-jpa/AC-07
    @Test
    void sourceCodeAndWebInfLibConformToJakartaEe10AndJpaOnlyRules() throws IOException {
        Path srcJava = Paths.get("src", "java");
        assertTrue(Files.isDirectory(srcJava), "src/java directory must exist");

        List<Path> javaFiles;
        try (Stream<Path> walk = Files.walk(srcJava)) {
            javaFiles = walk.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
        }
        assertFalse(javaFiles.isEmpty(), "Expected Java source files under src/java");

        for (Path file : javaFiles) {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            assertFalse(content.contains("javax.servlet"), "Forbidden javax.servlet import in " + file);
            assertFalse(content.contains("javax.persistence"), "Forbidden javax.persistence import in " + file);

            String normalizedPath = file.toString().replace('\\', '/');
            if (normalizedPath.contains("/repository/")) {
                assertFalse(content.contains("DriverManager"), "Forbidden JDBC DriverManager in " + file);
                assertFalse(content.contains("PreparedStatement"), "Forbidden JDBC PreparedStatement in " + file);
            }
        }

        Path libDir = Paths.get("web", "WEB-INF", "lib");
        assertTrue(Files.isDirectory(libDir), "web/WEB-INF/lib directory must exist");
        Set<String> jarNames;
        try (Stream<Path> list = Files.list(libDir)) {
            jarNames = list.filter(p -> p.getFileName().toString().endsWith(".jar"))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toSet());
        }

        assertTrue(jarNames.contains("hibernate-core-6.6.59.Final.jar"));
        assertTrue(jarNames.contains("jakarta.persistence-api-3.1.0.jar"));
        assertTrue(jarNames.contains("mssql-jdbc-13.6.2.jre11.jar"));
        assertTrue(jarNames.contains("jakarta.servlet.jsp.jstl-api-3.0.2.jar"));
        assertTrue(jarNames.contains("jakarta.servlet.jsp.jstl-3.0.1.jar"));
        assertTrue(jarNames.contains("jbcrypt-0.4.jar"));
        assertTrue(jarNames.contains("slf4j-api-2.0.16.jar"));

        for (String jar : jarNames) {
            String lower = jar.toLowerCase();
            assertFalse(lower.contains("javax."), "Forbidden javax JAR packaged: " + jar);
            assertFalse(lower.contains("sqljdbc42"), "Obsolete sqljdbc42 JAR packaged: " + jar);
            assertFalse(lower.contains("jaxb-api-2"), "Legacy JAXB 2 JAR packaged: " + jar);
            assertFalse(lower.startsWith("jakarta.servlet-api"), "Must not package Tomcat-provided servlet-api: " + jar);
            assertFalse(lower.startsWith("jakarta.servlet.jsp-api"), "Must not package Tomcat-provided jsp-api: " + jar);
            assertFalse(lower.startsWith("jakarta.el-api"), "Must not package Tomcat-provided el-api: " + jar);
            assertFalse(lower.contains("junit"), "Must not package test JAR into WAR: " + jar);
        }

        String persistenceXml = Files.readString(
                Paths.get("src", "conf", "META-INF", "persistence.xml"), StandardCharsets.UTF_8);
        assertTrue(persistenceXml.contains("name=\"BookHubPU\""));
        assertTrue(persistenceXml.contains("transaction-type=\"RESOURCE_LOCAL\""));
        assertTrue(persistenceXml.contains("value=\"validate\""));
        assertFalse(persistenceXml.contains("jakarta.persistence.jdbc.password"));
    }
}
