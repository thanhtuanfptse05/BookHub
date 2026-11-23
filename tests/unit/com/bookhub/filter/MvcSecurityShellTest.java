package com.bookhub.filter;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.SessionUser;
import com.bookhub.entity.RoleCode;
import com.bookhub.util.CsrfUtil;
import com.bookhub.util.PasswordUtil;
import com.bookhub.util.UrlSafeUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit and static policy tests for 02-feat-mvc-security-shell (AC-01..AC-07).
 */
class MvcSecurityShellTest {

    // @spec mvc-security-shell/AC-01
    @Test
    void webXmlEnforcesScriptingInvalidAndZeroScriptletsExistInViews() throws IOException {
        String webXml = Files.readString(Paths.get("web", "WEB-INF", "web.xml"), StandardCharsets.UTF_8);
        assertTrue(webXml.contains("<scripting-invalid>true</scripting-invalid>"));
        assertTrue(webXml.contains("<http-only>true</http-only>"));
        assertTrue(webXml.contains("<error-code>403</error-code>"));
        assertTrue(webXml.contains("<error-code>404</error-code>"));
        assertTrue(webXml.contains("<error-code>500</error-code>"));

        Path viewsDir = Paths.get("web", "WEB-INF", "views");
        List<Path> jsps;
        try (Stream<Path> walk = Files.walk(viewsDir)) {
            jsps = walk.filter(p -> p.toString().endsWith(".jsp")).collect(Collectors.toList());
        }
        assertFalse(jsps.isEmpty());
        for (Path jsp : jsps) {
            String content = Files.readString(jsp, StandardCharsets.UTF_8);
            assertFalse(content.contains("<% "), "Scriptlet found in " + jsp);
            assertFalse(content.contains("<%="), "Expression scriptlet found in " + jsp);
            assertFalse(content.contains("<%!"), "Declaration scriptlet found in " + jsp);
            assertFalse(content.contains("<sql:"), "Forbidden JSTL SQL tag in " + jsp);
        }
    }

    // @spec mvc-security-shell/AC-02
    @Test
    void encodingFilterForcesUtf8OnRequestAndResponse() throws Exception {
        EncodingFilter filter = new EncodingFilter();
        AtomicReference<String> reqEnc = new AtomicReference<>();
        AtomicReference<String> respEnc = new AtomicReference<>();
        AtomicBoolean chained = new AtomicBoolean(false);

        HttpServletRequest req = mockRequest("GET", "/books", null, null, null, reqEnc);
        HttpServletResponse resp = mockResponse(respEnc, new AtomicInteger(), new AtomicReference<>());
        FilterChain chain = (r, s) -> chained.set(true);

        filter.doFilter(req, resp, chain);

        assertEquals("UTF-8", reqEnc.get());
        assertEquals("UTF-8", respEnc.get());
        assertTrue(chained.get());
    }

    // @spec mvc-security-shell/AC-03
    @Test
    void authenticationFilterRedirectsGuestsWithSafeReturnUrlAndRejectsOpenRedirect() throws Exception {
        assertNull(UrlSafeUtil.sanitizeReturnUrl("https://evil.com/phish", "/BookHub"));
        assertNull(UrlSafeUtil.sanitizeReturnUrl("//evil.com/phish", "/BookHub"));
        assertNull(UrlSafeUtil.sanitizeReturnUrl("javascript:alert(1)", "/BookHub"));
        assertEquals("/cart?from=detail", UrlSafeUtil.sanitizeReturnUrl("/BookHub/cart?from=detail", "/BookHub"));

        AuthenticationFilter filter = new AuthenticationFilter();
        AtomicReference<String> redirect = new AtomicReference<>();
        AtomicBoolean chained = new AtomicBoolean(false);
        HttpServletRequest req = mockRequest("GET", "/BookHub/cart", "page=1", null, null, new AtomicReference<>());
        HttpServletResponse resp = mockResponse(new AtomicReference<>(), new AtomicInteger(), redirect);

        filter.doFilter(req, resp, (r, s) -> chained.set(true));

        assertFalse(chained.get());
        assertTrue(redirect.get().startsWith("/BookHub/login?returnUrl="));
        assertTrue(redirect.get().contains("%2Fcart%3Fpage%3D1"));
    }

    // @spec mvc-security-shell/AC-04
    @Test
    void adminFilterBlocksNonAdminCustomerWith403AndAllowsAdmin() throws Exception {
        AdminAuthorizationFilter filter = new AdminAuthorizationFilter();
        HttpSession customerSession = mockSession();
        customerSession.setAttribute(
                AppConstants.SESSION_USER_KEY,
                new SessionUser(3L, "Minh Anh", "minhanh@bookhub.vn", RoleCode.CUSTOMER));

        AtomicInteger status = new AtomicInteger();
        AtomicBoolean chained = new AtomicBoolean(false);
        HttpServletRequest req = mockRequest("GET", "/BookHub/admin/books", null, customerSession, null, new AtomicReference<>());
        HttpServletResponse resp = mockResponse(new AtomicReference<>(), status, new AtomicReference<>());

        filter.doFilter(req, resp, (r, s) -> chained.set(true));
        assertFalse(chained.get());
        assertEquals(403, status.get());

        HttpSession adminSession = mockSession();
        adminSession.setAttribute(
                AppConstants.SESSION_USER_KEY,
                new SessionUser(1L, "Admin", "admin@bookhub.vn", RoleCode.ADMIN));
        HttpServletRequest adminReq = mockRequest("GET", "/BookHub/admin/books", null, adminSession, null, new AtomicReference<>());
        AtomicBoolean adminChained = new AtomicBoolean(false);
        filter.doFilter(adminReq, resp, (r, s) -> adminChained.set(true));
        assertTrue(adminChained.get());
    }

    // @spec mvc-security-shell/AC-05
    @Test
    void csrfFilterBlocksMissingOrWrongTokenAndAllowsMatchingToken() throws Exception {
        CsrfFilter filter = new CsrfFilter();
        HttpSession session = mockSession();
        String validToken = CsrfUtil.getOrCreateToken(session);

        AtomicInteger badStatus = new AtomicInteger();
        AtomicBoolean badChain = new AtomicBoolean(false);
        Map<String, String> wrongParams = new HashMap<>();
        wrongParams.put(CsrfUtil.PARAM_NAME, "wrong-token");
        HttpServletRequest badReq = mockRequest("POST", "/BookHub/cart", null, session, wrongParams, new AtomicReference<>());
        HttpServletResponse badResp = mockResponse(new AtomicReference<>(), badStatus, new AtomicReference<>());

        filter.doFilter(badReq, badResp, (r, s) -> badChain.set(true));
        assertFalse(badChain.get());
        assertEquals(403, badStatus.get());

        Map<String, String> goodParams = new HashMap<>();
        goodParams.put(CsrfUtil.PARAM_NAME, validToken);
        AtomicBoolean goodChain = new AtomicBoolean(false);
        HttpServletRequest goodReq = mockRequest("POST", "/BookHub/cart", null, session, goodParams, new AtomicReference<>());
        HttpServletResponse goodResp = mockResponse(new AtomicReference<>(), new AtomicInteger(), new AtomicReference<>());

        filter.doFilter(goodReq, goodResp, (r, s) -> goodChain.set(true));
        assertTrue(goodChain.get());
    }

    // @spec mvc-security-shell/AC-06
    @Test
    void passwordUtilHashesWithDistinctSaltsAndVerifiesCorrectly() {
        String h1 = PasswordUtil.hash("BookHub123");
        String h2 = PasswordUtil.hash("BookHub123");
        assertTrue(h1.startsWith("$2a$") || h1.startsWith("$2b$"));
        assertNotEquals(h1, h2, "Salted BCrypt hashes must differ");
        assertTrue(PasswordUtil.verify("BookHub123", h1));
        assertFalse(PasswordUtil.verify("WrongPass123", h1));
        assertTrue(PasswordUtil.meetsPolicy("BookHub123"));
        assertFalse(PasswordUtil.meetsPolicy("1234567"));
        assertFalse(PasswordUtil.meetsPolicy("onlyletters"));
        assertEquals(64, PasswordUtil.sha256Hex("opaque-token").length());
    }

    // @spec mvc-security-shell/AC-07
    @Test
    void sharedAssetsErrorPagesAndTokensExistWithoutHardcodedColorsInBookhubCss() throws IOException {
        assertTrue(Files.isRegularFile(Paths.get("web", "assets", "vendor", "bootstrap", "bootstrap.min.css")));
        assertTrue(Files.isRegularFile(Paths.get("web", "assets", "css", "tokens.css")));
        assertTrue(Files.isRegularFile(Paths.get("web", "assets", "images", "book-placeholder.svg")));
        assertTrue(Files.isRegularFile(Paths.get("web", "WEB-INF", "views", "error", "403.jsp")));
        assertTrue(Files.isRegularFile(Paths.get("web", "WEB-INF", "views", "error", "404.jsp")));
        assertTrue(Files.isRegularFile(Paths.get("web", "WEB-INF", "views", "error", "500.jsp")));

        String bookhubCss = Files.readString(Paths.get("web", "assets", "css", "bookhub.css"), StandardCharsets.UTF_8);
        assertFalse(bookhubCss.matches("(?s).*#[0-9a-fA-F]{3,8}\\b.*"), "bookhub.css must use CSS variables from tokens.css");
        assertTrue(bookhubCss.contains("var(--color-primary)"));
        assertTrue(bookhubCss.contains("white-space: nowrap"));
    }

    private static HttpSession mockSession() {
        Map<String, Object> attrs = new HashMap<>();
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getAttribute": return attrs.get(args[0]);
                        case "setAttribute": attrs.put((String) args[0], args[1]); return null;
                        case "removeAttribute": attrs.remove(args[0]); return null;
                        default: return null;
                    }
                });
    }

    private static HttpServletRequest mockRequest(
            String methodName, String uri, String query, HttpSession session,
            Map<String, String> params, AtomicReference<String> encodingOut) {
        Map<String, Object> attrs = new HashMap<>();
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getMethod": return methodName;
                        case "getRequestURI": return uri;
                        case "getContextPath": return "/BookHub";
                        case "getQueryString": return query;
                        case "getSession": return session;
                        case "getParameter": return params != null ? params.get(args[0]) : null;
                        case "setCharacterEncoding": encodingOut.set((String) args[0]); return null;
                        case "setAttribute": attrs.put((String) args[0], args[1]); return null;
                        case "getAttribute": return attrs.get(args[0]);
                        default: return null;
                    }
                });
    }

    private static HttpServletResponse mockResponse(
            AtomicReference<String> encodingOut, AtomicInteger errorStatusOut, AtomicReference<String> redirectOut) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "setCharacterEncoding": encodingOut.set((String) args[0]); return null;
                        case "sendError": errorStatusOut.set((Integer) args[0]); return null;
                        case "sendRedirect": redirectOut.set((String) args[0]); return null;
                        default: return null;
                    }
                });
    }
}
