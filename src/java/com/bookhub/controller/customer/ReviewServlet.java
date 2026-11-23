package com.bookhub.controller.customer;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.SessionUser;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.service.ReviewService;
import com.bookhub.util.FlashUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ReviewServlet", urlPatterns = {"/books/review"})
public class ReviewServlet extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser user = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        Long bookId = parseLong(req.getParameter("bookId"));
        int rating = parseInt(req.getParameter("rating"), 0);
        if (bookId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            reviewService.submitReview(user.getUserId(), bookId, rating,
                    req.getParameter("title"), req.getParameter("content"));
            FlashUtil.putSuccess(req, "Cảm ơn bạn đã đánh giá! Nhận xét của bạn đang chờ quản trị viên duyệt hiển thị.");
            resp.sendRedirect(req.getContextPath() + "/books/detail?id=" + bookId + "#reviews");
        } catch (BusinessException ex) {
            FlashUtil.putError(req, ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/books/detail?id=" + bookId + "#reviews");
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
