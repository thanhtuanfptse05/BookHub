package com.bookhub.service;

import com.bookhub.config.JpaUtil;
import com.bookhub.dto.AdminCategoryRowDTO;
import com.bookhub.entity.Category;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.repository.CategoryRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Admin CRUD and soft-deactivate service for dbo.Categories (05-feat-admin-catalog-crud, FR-01..04).
 */
public class AdminCategoryService {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private final CategoryRepository categoryRepo;

    public AdminCategoryService() {
        this(new CategoryRepository());
    }

    public AdminCategoryService(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public List<AdminCategoryRowDTO> listCategories(String keyword) {
        return JpaUtil.executeReadOnly(em -> categoryRepo.findAllOrdered(em, keyword).stream()
                .map(c -> new AdminCategoryRowDTO(
                        c.getCategoryId(), c.getName(), c.getSlug(), c.getDescription(),
                        c.isActive(), categoryRepo.countBooksInCategory(em, c.getCategoryId())))
                .collect(Collectors.toList()));
    }

    public AdminCategoryRowDTO findForEdit(Integer categoryId) {
        return JpaUtil.executeReadOnly(em -> {
            Category c = categoryRepo.findById(em, categoryId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục."));
            return new AdminCategoryRowDTO(
                    c.getCategoryId(), c.getName(), c.getSlug(), c.getDescription(),
                    c.isActive(), categoryRepo.countBooksInCategory(em, c.getCategoryId()));
        });
    }

    public Integer saveCategory(Integer id, String name, String slug, String description, boolean active) {
        String cleanName = name == null ? "" : name.trim();
        String cleanSlug = slug == null ? "" : slug.trim().toLowerCase(Locale.ROOT);
        String cleanDesc = description == null ? "" : description.trim();

        Map<String, String> errors = new LinkedHashMap<>();
        if (cleanName.isEmpty() || cleanName.length() > 80) {
            errors.put("name", "Tên danh mục là bắt buộc (tối đa 80 ký tự).");
        }
        if (cleanSlug.isEmpty() || cleanSlug.length() > 100 || !SLUG_PATTERN.matcher(cleanSlug).matches()) {
            errors.put("slug", "Slug chỉ gồm chữ thường không dấu, số và dấu gạch ngang (VD: lich-su).");
        }
        if (cleanDesc.length() > 300) {
            errors.put("description", "Mô tả tối đa 300 ký tự.");
        }
        if (!errors.isEmpty()) {
            throw new BusinessException("Thông tin danh mục chưa hợp lệ.", errors);
        }

        return JpaUtil.executeInTransaction(em -> {
            Map<String, String> dupErrors = new LinkedHashMap<>();
            if (categoryRepo.existsByNameExcludingId(em, cleanName, id)) {
                dupErrors.put("name", "Tên danh mục đã tồn tại.");
            }
            if (categoryRepo.existsBySlugExcludingId(em, cleanSlug, id)) {
                dupErrors.put("slug", "Slug danh mục đã tồn tại.");
            }
            if (!dupErrors.isEmpty()) {
                throw new BusinessException("Tên hoặc slug danh mục bị trùng.", dupErrors);
            }

            Category entity;
            if (id == null) {
                entity = new Category();
            } else {
                entity = categoryRepo.findById(em, id)
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục để cập nhật."));
            }
            entity.setName(cleanName);
            entity.setSlug(cleanSlug);
            entity.setDescription(cleanDesc.isEmpty() ? null : cleanDesc);
            entity.setActive(active);
            if (id == null) {
                categoryRepo.persist(em, entity);
                em.flush();
            }
            return entity.getCategoryId();
        });
    }

    public boolean toggleActive(Integer categoryId) {
        return JpaUtil.executeInTransaction(em -> {
            Category c = categoryRepo.findById(em, categoryId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục."));
            c.setActive(!c.isActive());
            return c.isActive();
        });
    }
}
