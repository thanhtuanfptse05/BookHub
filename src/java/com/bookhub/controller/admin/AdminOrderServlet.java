package com.bookhub.controller.admin;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.OrderDetailDTO;
import com.bookhub.dto.SessionUser;
import com.bookhub.entity.OrderStatus;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.service.OrderService;
import com.bookhub.util.FlashUtil;
import com.bookhub.util.OrderStateMachine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet(name = "AdminOrderServlet", urlPatterns = {"/admin/orders"})
public class AdminOrderServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String orderCode = req.getParameter("orderCode");
        OrderStatus status = parseStatus(req.getParameter("status"));
        LocalDate fromDate = parseDate(req.getParameter("fromDate"));
        LocalDate toDate = parseDate(req.getParameter("toDate"));
        int page = parsePage(req.getParameter("page"));
        Long detailId = parseLong(req.getParameter("detailId"));

        req.setAttribute("pageResult", orderService.searchAdminOrders(orderCode, status, fromDate, toDate, page));
        if (detailId != null) {
            try {
                OrderDetailDTO selected = orderService.getAdminOrderDetail(detailId);
                req.setAttribute("selectedOrder", selected);
            } catch (NotFoundException ex) {
                FlashUtil.putError(req, ex.getMessage());
            }
        }
        req.setAttribute("allStatuses", OrderStatus.values());
        req.setAttribute("paginationPath", "/admin/orders");
        req.setAttribute("adminNav", "orders");
        req.setAttribute("pageTitle", "Quản lý đơn hàng — BookHub Admin");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/admin/orders.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser admin = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        Long orderId = parseLong(req.getParameter("orderId"));
        OrderStatus targetStatus = parseStatus(req.getParameter("targetStatus"));
        if (orderId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            orderService.transitionByAdmin(admin.getUserId(), orderId, targetStatus);
            FlashUtil.putSuccess(req, "Đã cập nhật trạng thái đơn sang " + OrderStateMachine.labelVi(targetStatus) + ".");
            resp.sendRedirect(req.getContextPath() + "/admin/orders?detailId=" + orderId);
        } catch (BusinessException ex) {
            FlashUtil.putError(req, ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/orders?detailId=" + orderId);
        } catch (NotFoundException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static OrderStatus parseStatus(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try { return OrderStatus.valueOf(raw.trim().toUpperCase()); } catch (IllegalArgumentException ex) { return null; }
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try { return LocalDate.parse(raw.trim()); } catch (DateTimeParseException ex) { return null; }
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
