package com.bookhub.controller.admin;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.SessionUser;
import com.bookhub.entity.UserStatus;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.service.AdminUserService;
import com.bookhub.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AdminUserServlet", urlPatterns = {"/admin/users"})
public class AdminUserServlet extends HttpServlet {

    private final AdminUserService userService = new AdminUserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        UserStatus status = parseStatus(req.getParameter("status"));
        int page = parsePage(req.getParameter("page"));
        req.setAttribute("pageResult", userService.searchUsers(keyword, status, page));
        req.setAttribute("allStatuses", UserStatus.values());
        req.setAttribute("paginationPath", "/admin/users");
        req.setAttribute("adminNav", "users");
        req.setAttribute("pageTitle", "Quản lý người dùng — BookHub Admin");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser admin = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        Long targetUserId = parseLong(req.getParameter("userId"));
        String action = req.getParameter("action");
        if (targetUserId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            UserStatus next = userService.toggleLock(admin.getUserId(), targetUserId, action);
            FlashUtil.putSuccess(req, next == UserStatus.LOCKED ? "Đã khóa tài khoản thành công." : "Đã mở khóa tài khoản thành công.");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
        } catch (BusinessException ex) {
            FlashUtil.putError(req, ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/users");
        } catch (NotFoundException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static UserStatus parseStatus(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try { return UserStatus.valueOf(raw.trim().toUpperCase()); } catch (IllegalArgumentException ex) { return null; }
    }

    private static Long parseLong(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try { return Long.parseLong(raw.trim()); } catch (NumberFormatException ex) { return null; }
    }

    private static int parsePage(String raw) {
        if (raw == null || raw.trim().isEmpty()) return 1;
        try { return Math.max(1, Integer.parseInt(raw.trim())); } catch (NumberFormatException ex) { return 1; }
    }
}
