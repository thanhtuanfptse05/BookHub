package com.bookhub.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unified friendly error page controller for HTTP 403, 404, and 500 without leaking stack traces (FR-09, SCR-23).
 */
@WebServlet(name = "ErrorPageServlet", urlPatterns = {"/403", "/404", "/500"})
public class ErrorPageServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorPageServlet.class);

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        req.setAttribute("noindex", Boolean.TRUE);
        if ("/403".equals(path)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("pageTitle", "403 — Không có quyền truy cập");
            req.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(req, resp);
            return;
        }
        if ("/404".equals(path)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            req.setAttribute("pageTitle", "404 — Không tìm thấy trang");
            req.getRequestDispatcher("/WEB-INF/views/error/404.jsp").forward(req, resp);
            return;
        }
        Throwable cause = (Throwable) req.getAttribute("jakarta.servlet.error.exception");
        Object origUri = req.getAttribute("jakarta.servlet.error.request_uri");
        if (cause != null) {
            LOGGER.error("Unhandled server error at {}: {}", origUri, cause.getMessage(), cause);
        }
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.setAttribute("pageTitle", "500 — Đã có lỗi hệ thống");
        req.getRequestDispatcher("/WEB-INF/views/error/500.jsp").forward(req, resp);
    }
}
