package com.bookhub.repository;

import com.bookhub.dto.AdminReviewRowDTO;
import com.bookhub.dto.PageResult;
import com.bookhub.entity.Review;
import com.bookhub.entity.ReviewStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

public class ReviewRepository extends BaseRepository<Review, Long> {

    public ReviewRepository() {
        super(Review.class);
    }

    public boolean existsByUserAndBook(EntityManager em, Long userId, Long bookId) {
        Long cnt = em.createQuery(
                        "SELECT COUNT(r) FROM Review r WHERE r.user.userId = :uid AND r.book.bookId = :bid",
                        Long.class)
                .setParameter("uid", userId)
                .setParameter("bid", bookId)
                .getSingleResult();
        return cnt != null && cnt > 0L;
    }

    public long countByStatus(EntityManager em, ReviewStatus status) {
        if (status == null) {
            return countAll(em);
        }
        Long cnt = em.createQuery("SELECT COUNT(r) FROM Review r WHERE r.status = :st", Long.class)
                .setParameter("st", status)
                .getSingleResult();
        return cnt != null ? cnt : 0L;
    }

    public PageResult<AdminReviewRowDTO> searchAdminReviews(
            EntityManager em, ReviewStatus status, int page, int pageSize) {
        long total = countByStatus(em, status);
        if (total == 0L) {
            return new PageResult<>(Collections.emptyList(), 1, pageSize, 0L);
        }
        int totalPages = (int) Math.ceil((double) total / pageSize);
        int safePage = Math.min(Math.max(1, page), totalPages);
        String where = status == null ? "" : " WHERE r.status = :st ";
        TypedQuery<AdminReviewRowDTO> q = em.createQuery(
                "SELECT new com.bookhub.dto.AdminReviewRowDTO("
                        + "r.reviewId, b.bookId, b.title, u.fullName, u.email, "
                        + "CAST(r.rating AS integer), r.title, r.content, r.status, r.createdAt) "
                        + "FROM Review r JOIN r.book b JOIN r.user u" + where
                        + "ORDER BY r.createdAt DESC, r.reviewId DESC",
                AdminReviewRowDTO.class);
        if (status != null) {
            q.setParameter("st", status);
        }
        List<AdminReviewRowDTO> items = q.setFirstResult((safePage - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
        return new PageResult<>(items, safePage, pageSize, total);
    }
}
