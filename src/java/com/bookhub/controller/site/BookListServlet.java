package com.bookhub.controller.site;

import com.bookhub.dto.BookCardDTO;
import com.bookhub.dto.BookFilterCriteria;
import com.bookhub.dto.BookSortOption;
import com.bookhub.dto.PageResult;
import com.bookhub.service.CatalogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet(name = "BookListServlet", urlPatterns = {"/books"})
public class BookListServlet extends HttpServlet {

    private final CatalogService catalogService = new CatalogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        Integer categoryId = parsePositiveIntOrNull(req.getParameter("categoryId"));
        BookSortOption sort = BookSortOption.fromParam(req.getParameter("sort"));
        int page = parsePage(req.getParameter("page"));

        String filterError = null;
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        String rawMin = req.getParameter("minPrice");
        String rawMax = req.getParameter("maxPrice");
        if (rawMin != null && !rawMin.trim().isEmpty()) {
            minPrice = parseNonNegativeDecimal(rawMin);
            if (minPrice == null) {
                filterError = "Giá tối thiểu phải là số không âm hợp lệ.";
            }
        }
        if (rawMax != null && !rawMax.trim().isEmpty()) {
            maxPrice = parseNonNegativeDecimal(rawMax);
            if (maxPrice == null) {
                filterError = "Giá tối đa phải là số không âm hợp lệ.";
            }
        }
        if (filterError == null && minPrice != null && maxPrice != null && maxPrice.compareTo(minPrice) < 0) {
            filterError = "Giá tối đa phải lớn hơn hoặc bằng giá tối thiểu.";
            minPrice = null;
            maxPrice = null;
        }

        BookFilterCriteria criteria = new BookFilterCriteria(keyword, categoryId, minPrice, maxPrice, sort);
        PageResult<BookCardDTO> pageResult = catalogService.searchCatalog(criteria, page);

        req.setAttribute("categories", catalogService.listActiveCategories());
        req.setAttribute("criteria", criteria);
        req.setAttribute("sortOptions", BookSortOption.values());
        req.setAttribute("pageResult", pageResult);
        req.setAttribute("paginationPath", "/books");
        req.setAttribute("filterError", filterError);
        req.setAttribute("activeNav", "books");
        req.setAttribute("pageTitle", "Danh mục sách — BookHub");
        req.setAttribute("metaDescription",
                "Tìm kiếm và lọc sách theo thể loại, khoảng giá và mức độ phổ biến tại BookHub.");
        req.getRequestDispatcher("/WEB-INF/views/site/books.jsp").forward(req, resp);
    }

    private static Integer parsePositiveIntOrNull(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            int val = Integer.parseInt(raw.trim());
            return val > 0 ? val : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static int parsePage(String raw) {
        Integer parsed = parsePositiveIntOrNull(raw);
        return parsed != null ? parsed : 1;
    }

    private static BigDecimal parseNonNegativeDecimal(String raw) {
        try {
            BigDecimal val = new BigDecimal(raw.trim());
            return val.compareTo(BigDecimal.ZERO) >= 0 ? val : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
