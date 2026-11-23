package com.bookhub.controller.site;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.SessionUser;
import com.bookhub.exception.BusinessException;
import com.bookhub.service.AuthService;
import com.bookhub.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(AppConstants.SESSION_USER_KEY) instanceof SessionUser) {
            SessionUser user = (SessionUser) session.getAttribute(AppConstants.SESSION_USER_KEY);
            resp.sendRedirect(req.getContextPath() + (user.isAdmin() ? "/admin" : "/"));
            return;
        }
        req.setAttribute("pageTitle", "Đăng ký tài khoản — BookHub");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        try {
            authService.register(fullName, email, phone, password, confirmPassword);
            FlashUtil.putSuccess(req, "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (BusinessException ex) {
            req.setAttribute("pageTitle", "Đăng ký tài khoản — BookHub");
            req.setAttribute("noindex", Boolean.TRUE);
            req.setAttribute("errorMessage", ex.getMessage());
            req.setAttribute("errors", ex.getFieldErrors());
            req.setAttribute("formFullName", fullName);
            req.setAttribute("formEmail", email);
            req.setAttribute("formPhone", phone);
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
        }
    }
}
