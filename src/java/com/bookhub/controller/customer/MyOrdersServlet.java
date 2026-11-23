package com.bookhub.controller.customer;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.SessionUser;
import com.bookhub.entity.OrderStatus;
import com.bookhub.service.OrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "MyOrdersServlet", urlPatterns = {"/account/orders"})
public class MyOrdersServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SessionUser user = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        OrderStatus status = parseStatus(req.getParameter("status"));
        int page = parsePage(req.getParameter("page"));

        req.setAttribute("pageResult", orderService.listCustomerOrders(user.getUserId(), status, page));
        req.setAttribute("selectedStatus", status != null ? status.name() : "");
        req.setAttribute("allStatuses", OrderStatus.values());
        req.setAttribute("paginationPath", "/account/orders");
        req.setAttribute("accountTab", "orders");
        req.setAttribute("pageTitle", "Đơn hàng của tôi — BookHub");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/customer/orders.jsp").forward(req, resp);
    }

    private static OrderStatus parseStatus(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try { return OrderStatus.valueOf(raw.trim().toUpperCase()); } catch (IllegalArgumentException ex) { return null; }
    }

    private static int parsePage(String raw) {
        if (raw == null || raw.trim().isEmpty()) return 1;
        try { int p = Integer.parseInt(raw.trim()); return Math.max(1, p); } catch (NumberFormatException ex) { return 1; }
    }
}
