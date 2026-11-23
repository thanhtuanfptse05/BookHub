package com.bookhub.controller.customer;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.CartViewDTO;
import com.bookhub.dto.CheckoutRequest;
import com.bookhub.dto.SessionUser;
import com.bookhub.dto.UserProfileDTO;
import com.bookhub.exception.BusinessException;
import com.bookhub.service.CartService;
import com.bookhub.service.CheckoutService;
import com.bookhub.service.UserService;
import com.bookhub.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "CheckoutServlet", urlPatterns = {"/checkout"})
public class CheckoutServlet extends HttpServlet {

    private final CartService cartService = new CartService();
    private final CheckoutService checkoutService = new CheckoutService();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SessionUser user = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        CartViewDTO cart = cartService.getCartView(user.getUserId());
        if (cart.isEmpty()) {
            FlashUtil.putWarning(req, "Giỏ hàng đang trống. Vui lòng chọn sách trước khi thanh toán.");
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }
        UserProfileDTO profile = userService.getProfile(user.getUserId());
        req.setAttribute("cart", cart);
        req.setAttribute("formRecipientName", profile.getFullName());
        req.setAttribute("formRecipientPhone", profile.getPhone());
        req.setAttribute("pageTitle", "Thanh toán COD — BookHub");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/customer/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SessionUser user = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        CheckoutRequest checkoutReq = new CheckoutRequest(
                req.getParameter("recipientName"),
                req.getParameter("recipientPhone"),
                req.getParameter("shippingAddress"),
                req.getParameter("note"));

        try {
            Long orderId = checkoutService.placeCodOrder(user.getUserId(), checkoutReq);
            req.getSession().setAttribute("cartCount", 0);
            resp.sendRedirect(req.getContextPath() + "/orders/success?id=" + orderId);
        } catch (BusinessException ex) {
            CartViewDTO cart = cartService.getCartView(user.getUserId());
            if (cart.isEmpty()) {
                FlashUtil.putError(req, ex.getMessage());
                resp.sendRedirect(req.getContextPath() + "/cart");
                return;
            }
            req.setAttribute("cart", cart);
            req.setAttribute("errorMessage", ex.getMessage());
            req.setAttribute("errors", ex.getFieldErrors());
            req.setAttribute("formRecipientName", checkoutReq.getRecipientName());
            req.setAttribute("formRecipientPhone", checkoutReq.getRecipientPhone());
            req.setAttribute("formShippingAddress", checkoutReq.getShippingAddress());
            req.setAttribute("formNote", checkoutReq.getNote());
            req.setAttribute("pageTitle", "Thanh toán COD — BookHub");
            req.setAttribute("noindex", Boolean.TRUE);
            req.getRequestDispatcher("/WEB-INF/views/customer/checkout.jsp").forward(req, resp);
        }
    }
}
