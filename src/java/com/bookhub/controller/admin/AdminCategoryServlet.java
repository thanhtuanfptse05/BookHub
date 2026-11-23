package com.bookhub.controller.admin;

import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.service.AdminCategoryService;
import com.bookhub.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AdminCategoryServlet", urlPatterns = {"/admin/categories"})
public class AdminCategoryServlet extends HttpServlet {

    private final AdminCategoryService categoryService = new AdminCategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        Integer editId = parseInt(req.getParameter("editId"));
        if (editId != null) {
            try {
                req.setAttribute("editCategory", categoryService.findForEdit(editId));
            } catch (NotFoundException ex) {
                FlashUtil.putError(req, ex.getMessage());
                resp.sendRedirect(req.getContextPath() + "/admin/categories");
                return;
            }
        }
        req.setAttribute("categories", categoryService.listCategories(keyword));
        req.setAttribute("keyword", keyword);
        req.setAttribute("adminNav", "categories");
        req.setAttribute("pageTitle", "Quản lý danh mục — BookHub Admin");
        req.setAttribute("noindex", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/admin/categories.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("toggleActive".equals(action)) {
                Integer id = parseInt(req.getParameter("id"));
                boolean nowActive = categoryService.toggleActive(id);
                FlashUtil.putSuccess(req, nowActive ? "Đã kích hoạt lại danh mục." : "Đã ngừng hoạt động danh mục.");
                resp.sendRedirect(req.getContextPath() + "/admin/categories");
                return;
            }
            Integer id = parseInt(req.getParameter("id"));
            String name = req.getParameter("name");
            String slug = req.getParameter("slug");
            String description = req.getParameter("description");
            boolean active = "true".equalsIgnoreCase(req.getParameter("isActive"))
                    || "on".equalsIgnoreCase(req.getParameter("isActive"))
                    || "1".equals(req.getParameter("isActive"));
            categoryService.saveCategory(id, name, slug, description, active);
            FlashUtil.putSuccess(req, id == null ? "Thêm danh mục thành công." : "Cập nhật danh mục thành công.");
            resp.sendRedirect(req.getContextPath() + "/admin/categories");
        } catch (BusinessException ex) {
            req.setAttribute("categories", categoryService.listCategories(null));
            req.setAttribute("errorMessage", ex.getMessage());
            req.setAttribute("errors", ex.getFieldErrors());
            req.setAttribute("formId", req.getParameter("id"));
            req.setAttribute("formName", req.getParameter("name"));
            req.setAttribute("formSlug", req.getParameter("slug"));
            req.setAttribute("formDescription", req.getParameter("description"));
            req.setAttribute("formActive", req.getParameter("isActive") != null);
            req.setAttribute("adminNav", "categories");
            req.setAttribute("pageTitle", "Quản lý danh mục — BookHub Admin");
            req.setAttribute("noindex", Boolean.TRUE);
            req.getRequestDispatcher("/WEB-INF/views/admin/categories.jsp").forward(req, resp);
        } catch (NotFoundException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static Integer parseInt(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
