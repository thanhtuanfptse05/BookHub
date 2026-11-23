package com.bookhub.controller.admin;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.SessionUser;
import com.bookhub.entity.QuestionStatus;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.service.QuestionService;
import com.bookhub.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AdminQuestionServlet", urlPatterns = {"/admin/questions"})
public class AdminQuestionServlet extends HttpServlet {

    private final QuestionService questionService = new QuestionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        QuestionStatus status = parseStatus(req.getParameter("status"));
        int page = parsePage(req.getParameter("page"));
        req.setAttribute("pageResult", questionService.listAdminQuestions(status, page));
        req.setAttribute("counts", questionService.countByStatus());
        req.setAttribute("selectedStatus", status != null ? status.name() : "");
        req.setAttribute("paginationPath", "/admin/questions");
        req.setAttribute("adminNav", "questions");
        req.setAttribute("pageTitle", "Quản lý Hỏi đáp Q&A — BookHub Admin");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/admin/questions.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser admin = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        Long questionId = parseLong(req.getParameter("questionId"));
        String action = req.getParameter("action");
        if (questionId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            if ("answer".equals(action)) {
                questionService.answerQuestion(admin.getUserId(), questionId, req.getParameter("content"));
                FlashUtil.putSuccess(req, "Đã gửi phản hồi chính thức và công khai câu hỏi.");
            } else if ("updateAnswer".equals(action)) {
                questionService.updateAnswer(admin.getUserId(), questionId, req.getParameter("content"));
                FlashUtil.putSuccess(req, "Đã cập nhật nội dung phản hồi.");
            } else if ("hide".equals(action)) {
                questionService.hideQuestion(questionId);
                FlashUtil.putSuccess(req, "Đã ẩn câu hỏi khỏi trang sản phẩm.");
            } else {
                throw new BusinessException("Thao tác Q&A không hợp lệ.");
            }
            resp.sendRedirect(req.getContextPath() + "/admin/questions");
        } catch (BusinessException ex) {
            FlashUtil.putError(req, ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/questions");
        } catch (NotFoundException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static QuestionStatus parseStatus(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try { return QuestionStatus.valueOf(raw.trim().toUpperCase()); } catch (IllegalArgumentException ex) { return null; }
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
