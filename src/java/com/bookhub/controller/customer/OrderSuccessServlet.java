package com.bookhub.controller.customer;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.OrderDetailDTO;
import com.bookhub.dto.SessionUser;
import com.bookhub.exception.AccessDeniedException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.service.OrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "OrderSuccessServlet", urlPatterns = {"/orders/success"})
public class OrderSuccessServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SessionUser user = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        Long orderId = parseLong(req.getParameter("id"));
        if (orderId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            OrderDetailDTO order = orderService.getCustomerOrderDetail(user.getUserId(), orderId);
            req.setAttribute("order", order);
            req.setAttribute("pageTitle", "Đặt hàng thành công — BookHub");
            req.setAttribute("noindex", Boolean.TRUE);
            req.getRequestDispatcher("/WEB-INF/views/customer/order-success.jsp").forward(req, resp);
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
