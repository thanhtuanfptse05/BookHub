package com.bookhub.controller.customer;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.CartViewDTO;
import com.bookhub.dto.SessionUser;
import com.bookhub.exception.AccessDeniedException;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.service.CartService;
import com.bookhub.util.FlashUtil;
import com.bookhub.util.UrlSafeUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "CartServlet", urlPatterns = {"/cart"})
public class CartServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SessionUser user = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        CartViewDTO cart = cartService.getCartView(user.getUserId());
        req.getSession().setAttribute("cartCount", cart.getTotalQuantity());
        req.setAttribute("cart", cart);
        req.setAttribute("pageTitle", "Giỏ hàng — BookHub");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/customer/cart.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser user = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        String action = req.getParameter("action");
        String safeReturn = UrlSafeUtil.sanitizeReturnUrl(req.getParameter("returnUrl"), req.getContextPath());
        String redirectTarget = safeReturn != null ? safeReturn : "/cart";

        try {
            int newCount;
            if ("add".equals(action)) {
                Long bookId = parseLong(req.getParameter("bookId"));
                int qty = parseInt(req.getParameter("quantity"), 1);
                newCount = cartService.addToCart(user.getUserId(), bookId, qty);
                FlashUtil.putSuccess(req, "Đã thêm sách vào giỏ hàng.");
            } else if ("update".equals(action)) {
                Long cartItemId = parseLong(req.getParameter("cartItemId"));
                int qty = parseInt(req.getParameter("quantity"), 0);
                newCount = cartService.updateItemQuantity(user.getUserId(), cartItemId, qty);
                FlashUtil.putSuccess(req, "Đã cập nhật số lượng trong giỏ hàng.");
                redirectTarget = "/cart";
            } else if ("remove".equals(action)) {
                Long cartItemId = parseLong(req.getParameter("cartItemId"));
                newCount = cartService.removeItem(user.getUserId(), cartItemId);
                FlashUtil.putSuccess(req, "Đã xóa sách khỏi giỏ hàng.");
                redirectTarget = "/cart";
            } else if ("clear".equals(action)) {
                newCount = cartService.clearCart(user.getUserId());
                FlashUtil.putSuccess(req, "Đã xóa toàn bộ giỏ hàng.");
                redirectTarget = "/cart";
            } else {
                throw new BusinessException("Thao tác giỏ hàng không hợp lệ.");
            }
            req.getSession().setAttribute("cartCount", newCount);
            resp.sendRedirect(req.getContextPath() + redirectTarget);
        } catch (BusinessException ex) {
            FlashUtil.putError(req, ex.getMessage());
            resp.sendRedirect(req.getContextPath() + redirectTarget);
        } catch (AccessDeniedException ex) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (NotFoundException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static Long parseLong(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try { return Long.parseLong(raw.trim()); } catch (NumberFormatException ex) { return null; }
    }

    private static int parseInt(String raw, int fallback) {
        if (raw == null || raw.trim().isEmpty()) return fallback;
        try { return Integer.parseInt(raw.trim()); } catch (NumberFormatException ex) { return fallback; }
    }
}
