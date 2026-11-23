package com.bookhub.repository;

import com.bookhub.entity.Book;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for dbo.Books. Fetches category eagerly per page query to prevent N+1 (A-09).
 */
public class BookRepository extends BaseRepository<Book, Long> {

    public BookRepository() {
        super(Book.class);
    }

    public List<Book> findActivePage(EntityManager em, int offset, int limit) {
        int safeOffset = Math.max(0, offset);
        int safeLimit = Math.max(1, limit);
        return em.createQuery(
                        "SELECT b FROM Book b JOIN FETCH b.category c "
                                + "WHERE b.active = true AND c.active = true "
                                + "ORDER BY b.createdAt DESC, b.bookId DESC",
                        Book.class)
                .setFirstResult(safeOffset)
                .setMaxResults(safeLimit)
                .getResultList();
    }

    public long countActive(EntityManager em) {
        Long count = em.createQuery(
                        "SELECT COUNT(b) FROM Book b WHERE b.active = true AND b.category.active = true",
                        Long.class)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    public Optional<Book> findByIsbn(EntityManager em, String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return Optional.empty();
        }
        List<Book> rows = em.createQuery(
                        "SELECT b FROM Book b WHERE b.isbn = :isbn",
                        Book.class)
                .setParameter("isbn", isbn.trim())
                .setMaxResults(1)
                .getResultList();
        return rows.stream().findFirst();
    }
}
