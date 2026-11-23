package com.bookhub.filter;

import com.bookhub.util.CsrfUtil;
import com.bookhub.util.FlashUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter #2 in web.xml: ensures per-session CSRF token, validates _csrf on all POST requests,
 * and exposes one-shot PRG flash messages on GET requests (FR-05, FR-06).
 */
public class CsrfFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrfFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = getRelativePath(req);
        if (path.startsWith("/assets/")) {
            chain.doFilter(request, response);
            return;
        }

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            HttpSession session = req.getSession(false);
            String submitted = req.getParameter(CsrfUtil.PARAM_NAME);
            if (!CsrfUtil.isValid(session, submitted)) {
                LOGGER.warn("Blocked POST request due to missing or invalid CSRF token (path={})", path);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            req.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(session));
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(true);
        String token = CsrfUtil.getOrCreateToken(session);
        req.setAttribute("csrfToken", token);
        if ("GET".equalsIgnoreCase(req.getMethod()) && !path.equals("/403") && !path.equals("/404") && !path.equals("/500")) {
            FlashUtil.consumeToRequest(req);
        }
        chain.doFilter(request, response);
    }

    private static String getRelativePath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }
}
