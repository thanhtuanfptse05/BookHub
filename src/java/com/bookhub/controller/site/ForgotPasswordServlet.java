package com.bookhub.controller.site;

import com.bookhub.exception.BusinessException;
import com.bookhub.service.AuthService;
import com.bookhub.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ForgotPasswordServlet", urlPatterns = {"/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    private static final String NEUTRAL_MSG =
            "Nếu email tồn tại trong hệ thống, liên kết đặt lại mật khẩu (hiệu lực 15 phút) đã được tạo. "
                    + "Trong môi trường demo cục bộ, vui lòng kiểm tra server log của Tomcat.";

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("pageTitle", "Quên mật khẩu — BookHub");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        try {
            String baseUrl = req.getScheme() + "://" + req.getServerName()
                    + ((req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : ":" + req.getServerPort())
                    + req.getContextPath();
            authService.requestPasswordReset(email, baseUrl);
            FlashUtil.putSuccess(req, NEUTRAL_MSG);
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
        } catch (BusinessException ex) {
            req.setAttribute("pageTitle", "Quên mật khẩu — BookHub");
            req.setAttribute("noindex", Boolean.TRUE);
            req.setAttribute("errorMessage", ex.getMessage());
            req.setAttribute("errors", ex.getFieldErrors());
            req.setAttribute("formEmail", email);
            req.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(req, resp);
        }
    }
}
