package com.bookhub.controller.customer;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.SessionUser;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.service.QuestionService;
import com.bookhub.util.FlashUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "QuestionServlet", urlPatterns = {"/books/question"})
public class QuestionServlet extends HttpServlet {

    private final QuestionService questionService = new QuestionService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser user = (SessionUser) req.getSession().getAttribute(AppConstants.SESSION_USER_KEY);
        Long bookId = parseLong(req.getParameter("bookId"));
        if (bookId == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            questionService.askQuestion(user.getUserId(), bookId,
                    req.getParameter("subject"), req.getParameter("content"));
            FlashUtil.putSuccess(req, "Câu hỏi của bạn đã được gửi tới BookHub. Chúng tôi sẽ phản hồi sớm nhất.");
            resp.sendRedirect(req.getContextPath() + "/books/detail?id=" + bookId + "#qa");
        } catch (BusinessException ex) {
            FlashUtil.putError(req, ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/books/detail?id=" + bookId + "#qa");
        } catch (NotFoundException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static Long parseLong(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try { return Long.parseLong(raw.trim()); } catch (NumberFormatException ex) { return null; }
    }
}
