package com.bookhub.controller.site;

import com.bookhub.config.AppConstants;
import com.bookhub.config.JpaUtil;
import com.bookhub.dto.SessionUser;
import com.bookhub.exception.BusinessException;
import com.bookhub.service.AuthService;
import com.bookhub.util.CsrfUtil;
import com.bookhub.util.UrlSafeUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(AppConstants.SESSION_USER_KEY) instanceof SessionUser) {
            SessionUser user = (SessionUser) session.getAttribute(AppConstants.SESSION_USER_KEY);
            resp.sendRedirect(req.getContextPath() + (user.isAdmin() ? "/admin" : "/"));
            return;
        }
        String safeReturn = UrlSafeUtil.sanitizeReturnUrl(req.getParameter("returnUrl"), req.getContextPath());
        req.setAttribute("returnUrl", safeReturn);
        req.setAttribute("pageTitle", "Đăng nhập — BookHub");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String safeReturn = UrlSafeUtil.sanitizeReturnUrl(req.getParameter("returnUrl"), req.getContextPath());

        try {
            SessionUser user = authService.authenticate(email, password);
            HttpSession session = req.getSession(true);
            req.changeSessionId();
            session.setAttribute(AppConstants.SESSION_USER_KEY, user);
            CsrfUtil.getOrCreateToken(session);
            session.setAttribute("cartCount", loadCartCount(user.getUserId()));

            if (user.isAdmin()) {
                resp.sendRedirect(req.getContextPath() + "/admin");
            } else if (safeReturn != null && !safeReturn.startsWith("/admin")) {
                resp.sendRedirect(req.getContextPath() + safeReturn);
            } else {
                resp.sendRedirect(req.getContextPath() + "/");
            }
        } catch (BusinessException ex) {
            req.setAttribute("pageTitle", "Đăng nhập — BookHub");
            req.setAttribute("noindex", Boolean.TRUE);
            req.setAttribute("errorMessage", ex.getMessage());
            req.setAttribute("formEmail", email);
            req.setAttribute("returnUrl", safeReturn);
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        }
    }

    private static int loadCartCount(Long userId) {
        Long sum = JpaUtil.executeReadOnly(em -> em.createQuery(
                        "SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.user.userId = :uid",
                        Long.class)
                .setParameter("uid", userId)
                .getSingleResult());
        return sum != null ? sum.intValue() : 0;
    }
}
