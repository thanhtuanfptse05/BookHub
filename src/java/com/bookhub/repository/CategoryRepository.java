package com.bookhub.repository;

import com.bookhub.dto.CategoryShortcutDTO;
import com.bookhub.entity.Category;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for dbo.Categories (public shortcuts + admin CRUD queries).
 */
public class CategoryRepository extends BaseRepository<Category, Integer> {

    public CategoryRepository() {
        super(Category.class);
    }

    public List<Category> findAllActive(EntityManager em) {
        return em.createQuery(
                        "SELECT c FROM Category c WHERE c.active = true ORDER BY c.name ASC",
                        Category.class)
                .getResultList();
    }

    public List<Category> findAllOrdered(EntityManager em, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return em.createQuery("SELECT c FROM Category c ORDER BY c.active DESC, c.name ASC", Category.class)
                    .getResultList();
        }
        String kw = "%" + keyword.trim().toLowerCase() + "%";
        return em.createQuery(
                        "SELECT c FROM Category c WHERE LOWER(c.name) LIKE :kw OR LOWER(c.slug) LIKE :kw "
                                + "ORDER BY c.active DESC, c.name ASC",
                        Category.class)
                .setParameter("kw", kw)
                .getResultList();
    }

    public List<CategoryShortcutDTO> findActiveShortcuts(EntityManager em) {
        return em.createQuery(
                        "SELECT new com.bookhub.dto.CategoryShortcutDTO("
                                + "c.categoryId, c.name, c.slug, COUNT(b.bookId)) "
                                + "FROM Category c LEFT JOIN Book b ON b.category = c AND b.active = true "
                                + "WHERE c.active = true "
                                + "GROUP BY c.categoryId, c.name, c.slug "
                                + "ORDER BY c.name ASC",
                        CategoryShortcutDTO.class)
                .getResultList();
    }

    public Optional<Category> findBySlug(EntityManager em, String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return Optional.empty();
        }
        List<Category> rows = em.createQuery(
                        "SELECT c FROM Category c WHERE c.slug = :slug",
                        Category.class)
                .setParameter("slug", slug.trim())
                .setMaxResults(1)
                .getResultList();
        return rows.stream().findFirst();
    }

    public boolean existsByNameExcludingId(EntityManager em, String name, Integer excludeId) {
        String jpql = excludeId == null
                ? "SELECT COUNT(c) FROM Category c WHERE LOWER(c.name) = LOWER(:name)"
                : "SELECT COUNT(c) FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.categoryId <> :excludeId";
        var q = em.createQuery(jpql, Long.class).setParameter("name", name.trim());
        if (excludeId != null) {
            q.setParameter("excludeId", excludeId);
        }
        return q.getSingleResult() > 0L;
    }

    public boolean existsBySlugExcludingId(EntityManager em, String slug, Integer excludeId) {
        String jpql = excludeId == null
                ? "SELECT COUNT(c) FROM Category c WHERE LOWER(c.slug) = LOWER(:slug)"
                : "SELECT COUNT(c) FROM Category c WHERE LOWER(c.slug) = LOWER(:slug) AND c.categoryId <> :excludeId";
        var q = em.createQuery(jpql, Long.class).setParameter("slug", slug.trim());
        if (excludeId != null) {
            q.setParameter("excludeId", excludeId);
        }
        return q.getSingleResult() > 0L;
    }

    public long countBooksInCategory(EntityManager em, Integer categoryId) {
        Long cnt = em.createQuery("SELECT COUNT(b) FROM Book b WHERE b.category.categoryId = :cid", Long.class)
                .setParameter("cid", categoryId)
                .getSingleResult();
        return cnt != null ? cnt : 0L;
    }
}
