package com.bookhub.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Filter #1 in web.xml: enforces UTF-8 request and response encoding for dynamic routes (FR-02).
 * Skips forcing response charset on static assets so Tomcat DefaultServlet streams raw UTF-8 bytes
 * instead of transcoding through Windows default file.encoding (Cp1252).
 */
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (!isStaticAsset(request)) {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        }
        chain.doFilter(request, response);
    }

    private static boolean isStaticAsset(ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            return false;
        }
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String uri = httpReq.getRequestURI();
        if (uri == null) {
            return false;
        }
        String ctx = httpReq.getContextPath();
        String path = (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx))
                ? uri.substring(ctx.length())
                : uri;
        return path.startsWith("/assets/") || "/robots.txt".equals(path);
    }
}
