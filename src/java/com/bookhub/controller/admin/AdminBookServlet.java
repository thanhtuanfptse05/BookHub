package com.bookhub.controller.admin;

import com.bookhub.dto.BookFormDTO;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.service.AdminBookService;
import com.bookhub.service.AdminCategoryService;
import com.bookhub.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AdminBookServlet", urlPatterns = {"/admin/books"})
public class AdminBookServlet extends HttpServlet {

    private final AdminBookService bookService = new AdminBookService();
    private final AdminCategoryService categoryService = new AdminCategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        Integer categoryId = parseInt(req.getParameter("categoryId"));
        String status = req.getParameter("status");
        int page = parsePage(req.getParameter("page"));

        req.setAttribute("pageResult", bookService.searchAdminBooks(keyword, categoryId, status, page));
        req.setAttribute("categories", categoryService.listCategories(null));
        req.setAttribute("paginationPath", "/admin/books");
        req.setAttribute("adminNav", "books");
        req.setAttribute("pageTitle", "Quản lý sách — BookHub Admin");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/admin/books.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("toggleActive".equals(action)) {
            try {
                Long id = parseLong(req.getParameter("bookId"));
                boolean nowActive = bookService.toggleActive(id);
                FlashUtil.putSuccess(req, nowActive ? "Đã mở bán lại cuốn sách." : "Đã ngừng kinh doanh cuốn sách (giữ nguyên lịch sử đơn).");
                resp.sendRedirect(req.getContextPath() + "/admin/books");
                return;
            } catch (NotFoundException ex) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        BookFormDTO form = readForm(req);
        try {
            bookService.saveBook(form);
            FlashUtil.putSuccess(req, form.getBookId() == null ? "Thêm sách mới thành công." : "Cập nhật thông tin sách thành công.");
            resp.sendRedirect(req.getContextPath() + "/admin/books");
        } catch (BusinessException ex) {
            req.setAttribute("form", form);
            req.setAttribute("categories", bookService.listCategoriesForForm(form.getCategoryId()));
            req.setAttribute("errorMessage", ex.getMessage());
            req.setAttribute("errors", ex.getFieldErrors());
            req.setAttribute("adminNav", "books");
            req.setAttribute("pageTitle", (form.getBookId() == null ? "Thêm sách mới" : "Chỉnh sửa sách") + " — BookHub Admin");
            req.setAttribute("noindex", Boolean.TRUE);
            req.getRequestDispatcher("/WEB-INF/views/admin/book-form.jsp").forward(req, resp);
        } catch (NotFoundException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static BookFormDTO readForm(HttpServletRequest req) {
        BookFormDTO dto = new BookFormDTO();
        dto.setBookId(parseLong(req.getParameter("bookId")));
        dto.setCategoryId(parseInt(req.getParameter("categoryId")));
        dto.setIsbn(req.getParameter("isbn"));
        dto.setTitle(req.getParameter("title"));
        dto.setAuthor(req.getParameter("author"));
        dto.setPublisher(req.getParameter("publisher"));
        dto.setPublishedYear(req.getParameter("publishedYear"));
        dto.setPrice(req.getParameter("price"));
        dto.setStockQty(req.getParameter("stockQty"));
        dto.setShortDescription(req.getParameter("shortDescription"));
        dto.setDescription(req.getParameter("description"));
        dto.setCoverUrl(req.getParameter("coverUrl"));
        dto.setFeatured(req.getParameter("featured") != null);
        dto.setActive(req.getParameter("active") != null);
        return dto;
    }

    private static Integer parseInt(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try { return Integer.parseInt(raw.trim()); } catch (NumberFormatException ex) { return null; }
    }

    private static Long parseLong(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try { return Long.parseLong(raw.trim()); } catch (NumberFormatException ex) { return null; }
    }

    private static int parsePage(String raw) {
        Integer p = parseInt(raw);
        return (p != null && p > 0) ? p : 1;
    }
}
