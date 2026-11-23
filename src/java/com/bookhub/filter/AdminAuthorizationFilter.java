package com.bookhub.filter;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.SessionUser;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter #4 in web.xml: restricts /admin and /admin/* strictly to authenticated ADMIN accounts (FR-04).
 */
public class AdminAuthorizationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminAuthorizationFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        Object principal = session != null ? session.getAttribute(AppConstants.SESSION_USER_KEY) : null;
        if (!(principal instanceof SessionUser)) {
            String target = AuthenticationFilter.buildReturnUrl(req);
            String redirect = req.getContextPath() + "/login";
            if (target != null) {
                redirect += "?returnUrl=" + URLEncoder.encode(target, StandardCharsets.UTF_8);
            }
            resp.sendRedirect(redirect);
            return;
        }

        SessionUser user = (SessionUser) principal;
        if (!user.isAdmin()) {
            LOGGER.warn("Forbidden admin access attempt by userId={} (path={})", user.getUserId(), req.getRequestURI());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }
}
