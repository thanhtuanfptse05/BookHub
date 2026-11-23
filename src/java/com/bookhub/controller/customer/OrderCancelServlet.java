package com.bookhub.controller.customer;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.SessionUser;
import com.bookhub.exception.AccessDeniedException;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.service.OrderService;
import com.bookhub.util.FlashUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "OrderCancelServlet", urlPatterns = {"/account/orders/cancel"})
public class OrderCancelServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser user = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        Long orderId = parseLong(req.getParameter("orderId"));
        if (orderId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            orderService.cancelByCustomer(user.getUserId(), orderId);
            FlashUtil.putSuccess(req, "Đã hủy đơn hàng và hoàn lại tồn kho thành công.");
            resp.sendRedirect(req.getContextPath() + "/account/orders/detail?id=" + orderId);
        } catch (BusinessException ex) {
            FlashUtil.putError(req, ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/account/orders/detail?id=" + orderId);
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
}
