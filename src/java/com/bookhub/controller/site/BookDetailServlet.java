package com.bookhub.controller.site;

import com.bookhub.config.AppConstants;
import com.bookhub.dto.BookDetailDTO;
import com.bookhub.dto.SessionUser;
import com.bookhub.exception.NotFoundException;
import com.bookhub.service.CatalogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "BookDetailServlet", urlPatterns = {"/books/detail"})
public class BookDetailServlet extends HttpServlet {

    private final CatalogService catalogService = new CatalogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long bookId = parseLongOrNull(req.getParameter("id"));
        if (bookId == null || bookId <= 0) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        HttpSession session = req.getSession(false);
        Long currentUserId = null;
        if (session != null && session.getAttribute(AppConstants.SESSION_USER_KEY) instanceof SessionUser) {
            currentUserId = ((SessionUser) session.getAttribute(AppConstants.SESSION_USER_KEY)).getUserId();
        }

        try {
            BookDetailDTO detail = catalogService.getBookDetail(bookId, currentUserId);
            req.setAttribute("book", detail);
            req.setAttribute("activeNav", "books");
            req.setAttribute("pageTitle", detail.getTitle() + " — BookHub");
            req.setAttribute("metaDescription",
                    detail.getShortDescription() != null && !detail.getShortDescription().isEmpty()
                            ? detail.getShortDescription()
                            : "Mua sách " + detail.getTitle() + " của tác giả " + detail.getAuthor() + " tại BookHub.");
            req.setAttribute("ogType", "book");
            req.getRequestDispatcher("/WEB-INF/views/site/book-detail.jsp").forward(req, resp);
        } catch (NotFoundException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static Long parseLongOrNull(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
