package com.bookhub.controller.admin;

import com.bookhub.entity.ReviewStatus;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.service.ReviewService;
import com.bookhub.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AdminReviewServlet", urlPatterns = {"/admin/reviews"})
public class AdminReviewServlet extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ReviewStatus status = parseStatus(req.getParameter("status"));
        int page = parsePage(req.getParameter("page"));
        req.setAttribute("pageResult", reviewService.listAdminReviews(status, page));
        req.setAttribute("counts", reviewService.countByStatus());
        req.setAttribute("selectedStatus", status != null ? status.name() : "");
        req.setAttribute("paginationPath", "/admin/reviews");
        req.setAttribute("adminNav", "reviews");
        req.setAttribute("pageTitle", "Duyệt đánh giá — BookHub Admin");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/admin/reviews.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long reviewId = parseLong(req.getParameter("reviewId"));
        String action = req.getParameter("action");
        if (reviewId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            ReviewStatus target = "approve".equals(action) ? ReviewStatus.APPROVED : ReviewStatus.HIDDEN;
            reviewService.moderateReview(reviewId, target);
            FlashUtil.putSuccess(req, target == ReviewStatus.APPROVED ? "Đã duyệt hiển thị đánh giá." : "Đã ẩn đánh giá khỏi trang công khai.");
            resp.sendRedirect(req.getContextPath() + "/admin/reviews");
        } catch (BusinessException ex) {
            FlashUtil.putError(req, ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/reviews");
        } catch (NotFoundException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static ReviewStatus parseStatus(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try { return ReviewStatus.valueOf(raw.trim().toUpperCase()); } catch (IllegalArgumentException ex) { return null; }
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
