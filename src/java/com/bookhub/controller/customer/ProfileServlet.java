package com.bookhub.controller.customer;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.SessionUser;
import com.bookhub.exception.BusinessException;
import com.bookhub.service.UserService;
import com.bookhub.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/account/profile"})
public class ProfileServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SessionUser current = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        req.setAttribute("profile", userService.getProfile(current.getUserId()));
        req.setAttribute("accountTab", "profile");
        req.setAttribute("pageTitle", "Hồ sơ cá nhân — BookHub");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/customer/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SessionUser current = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        String action = req.getParameter("action");

        try {
            if ("changePassword".equals(action)) {
                userService.changePassword(
                        current.getUserId(),
                        req.getParameter("currentPassword"),
                        req.getParameter("newPassword"),
                        req.getParameter("confirmPassword"));
                FlashUtil.putSuccess(req, "Đổi mật khẩu thành công.");
            } else {
                // Strictly pass allow-listed fields only (fullName, phone)
                SessionUser updated = userService.updateProfile(
                        current.getUserId(),
                        req.getParameter("fullName"),
                        req.getParameter("phone"));
                req.getSession().setAttribute(AppConstants.SESSION_USER_KEY, updated);
                FlashUtil.putSuccess(req, "Cập nhật thông tin cá nhân thành công.");
            }
            resp.sendRedirect(req.getContextPath() + "/account/profile");
        } catch (BusinessException ex) {
            req.setAttribute("profile", userService.getProfile(current.getUserId()));
            req.setAttribute("accountTab", "profile");
            req.setAttribute("pageTitle", "Hồ sơ cá nhân — BookHub");
            req.setAttribute("noindex", Boolean.TRUE);
            req.setAttribute("errorMessage", ex.getMessage());
            req.setAttribute("errors", ex.getFieldErrors());
            if (!"changePassword".equals(action)) {
                req.setAttribute("formFullName", req.getParameter("fullName"));
                req.setAttribute("formPhone", req.getParameter("phone"));
            }
            req.getRequestDispatcher("/WEB-INF/views/customer/profile.jsp").forward(req, resp);
        }
    }
}
