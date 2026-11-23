package com.bookhub.filter;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.SessionUser;
import com.bookhub.util.UrlSafeUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Filter #3 in web.xml: enforces authenticated session on protected customer routes
 * and redirects guests to /login with a validated internal returnUrl (FR-03, EC-01).
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        Object principal = session != null ? session.getAttribute(AppConstants.SESSION_USER_KEY) : null;
        if (principal instanceof SessionUser) {
            chain.doFilter(request, response);
            return;
        }

        String target = buildReturnUrl(req);
        String redirect = req.getContextPath() + "/login";
        if (target != null) {
            redirect += "?returnUrl=" + URLEncoder.encode(target, StandardCharsets.UTF_8);
        }
        resp.sendRedirect(redirect);
    }

    static String buildReturnUrl(HttpServletRequest req) {
        if (!"GET".equalsIgnoreCase(req.getMethod())) {
            String fallback = UrlSafeUtil.sanitizeReturnUrl(req.getParameter("returnUrl"), req.getContextPath());
            return fallback != null ? fallback : "/";
        }
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        String full = (query != null && !query.isEmpty()) ? uri + "?" + query : uri;
        return UrlSafeUtil.sanitizeReturnUrl(full, req.getContextPath());
    }
}
