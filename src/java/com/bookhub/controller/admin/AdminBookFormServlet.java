package com.bookhub.controller.admin;

import com.bookhub.dto.BookFormDTO;
import com.bookhub.exception.NotFoundException;
import com.bookhub.service.AdminBookService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AdminBookFormServlet", urlPatterns = {"/admin/books/form"})
public class AdminBookFormServlet extends HttpServlet {

    private final AdminBookService bookService = new AdminBookService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String rawId = req.getParameter("id");
        BookFormDTO form;
        if (rawId != null && !rawId.trim().isEmpty()) {
            try {
                Long id = Long.parseLong(rawId.trim());
                form = bookService.getForEdit(id);
            } catch (NumberFormatException | NotFoundException ex) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } else {
            form = new BookFormDTO();
            form.setStockQty("0");
            form.setActive(true);
        }

        req.setAttribute("form", form);
        req.setAttribute("categories", bookService.listCategoriesForForm(form.getCategoryId()));
        req.setAttribute("adminNav", "books");
        req.setAttribute("pageTitle", (form.getBookId() == null ? "Thêm sách mới" : "Chỉnh sửa sách") + " — BookHub Admin");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/admin/book-form.jsp").forward(req, resp);
    }
}
