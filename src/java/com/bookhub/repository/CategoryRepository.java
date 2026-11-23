package com.bookhub.repository;

import com.bookhub.entity.Category;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for dbo.Categories.
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
}
