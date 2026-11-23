package com.bookhub.repository;

import com.bookhub.dto.BookCardDTO;
import com.bookhub.dto.BookFilterCriteria;
import com.bookhub.dto.BookSortOption;
import com.bookhub.dto.PageResult;
import com.bookhub.entity.Book;
import com.bookhub.entity.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA repository for dbo.Books supporting public catalog, admin CRUD, and pessimistic stock locking.
 */
public class BookRepository extends BaseRepository<Book, Long> {

    private static final String CARD_PROJECTION =
            "SELECT new com.bookhub.dto.BookCardDTO("
                    + "b.bookId, b.title, b.author, b.price, b.stockQty, b.coverUrl, c.name, b.featured) ";

    public BookRepository() {
        super(Book.class);
    }

    public List<Book> findActivePage(EntityManager em, int offset, int limit) {
        return em.createQuery(
                        "SELECT b FROM Book b JOIN FETCH b.category c "
                                + "WHERE b.active = true AND c.active = true "
                                + "ORDER BY b.createdAt DESC, b.bookId DESC",
                        Book.class)
                .setFirstResult(Math.max(0, offset))
                .setMaxResults(Math.max(1, limit))
                .getResultList();
    }

    public long countActive(EntityManager em) {
        Long count = em.createQuery(
                        "SELECT COUNT(b) FROM Book b WHERE b.active = true AND b.category.active = true",
                        Long.class)
                .getSingleResult();
        return count != null ? count : 0L;
    }

    public List<BookCardDTO> findFeaturedActive(EntityManager em, int limit) {
        return em.createQuery(
                        CARD_PROJECTION + "FROM Book b JOIN b.category c "
                                + "WHERE b.active = true AND c.active = true AND b.featured = true "
                                + "ORDER BY b.createdAt DESC, b.bookId DESC",
                        BookCardDTO.class)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<BookCardDTO> findNewestActive(EntityManager em, int limit) {
        return em.createQuery(
                        CARD_PROJECTION + "FROM Book b JOIN b.category c "
                                + "WHERE b.active = true AND c.active = true "
                                + "ORDER BY b.createdAt DESC, b.bookId DESC",
                        BookCardDTO.class)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<BookCardDTO> findBestsellersActive(EntityManager em, int limit) {
        return em.createQuery(
                        CARD_PROJECTION + "FROM Book b JOIN b.category c "
                                + "LEFT JOIN OrderItem oi ON oi.book = b "
                                + "LEFT JOIN oi.order o ON o.status = :delivered "
                                + "WHERE b.active = true AND c.active = true "
                                + "GROUP BY b.bookId, b.title, b.author, b.price, b.stockQty, b.coverUrl, c.name, b.featured, b.createdAt "
                                + "ORDER BY COALESCE(SUM(CASE WHEN o.orderId IS NOT NULL THEN oi.quantity ELSE 0 END), 0) DESC, "
                                + "b.createdAt DESC, b.bookId DESC",
                        BookCardDTO.class)
                .setParameter("delivered", OrderStatus.DELIVERED)
                .setMaxResults(limit)
                .getResultList();
    }

    public PageResult<BookCardDTO> searchActiveBooks(
            EntityManager em, BookFilterCriteria criteria, int page, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE b.active = true AND c.active = true ");
        Map<String, Object> params = new LinkedHashMap<>();
        appendPublicFilters(where, params, criteria);

        TypedQuery<Long> countQ = em.createQuery(
                "SELECT COUNT(b.bookId) FROM Book b JOIN b.category c" + where, Long.class);
        params.forEach(countQ::setParameter);
        long total = countQ.getSingleResult();
        if (total == 0L) {
            return new PageResult<>(Collections.emptyList(), 1, pageSize, 0L);
        }

        int totalPages = (int) Math.ceil((double) total / pageSize);
        int safePage = Math.min(Math.max(1, page), totalPages);
        int offset = (safePage - 1) * pageSize;

        TypedQuery<BookCardDTO> dataQ;
        if (criteria.getSort() == BookSortOption.BESTSELLER) {
            String jpql = CARD_PROJECTION + "FROM Book b JOIN b.category c "
                    + "LEFT JOIN OrderItem oi ON oi.book = b "
                    + "LEFT JOIN oi.order o ON o.status = :delivered" + where
                    + "GROUP BY b.bookId, b.title, b.author, b.price, b.stockQty, b.coverUrl, c.name, b.featured, b.createdAt "
                    + "ORDER BY COALESCE(SUM(CASE WHEN o.orderId IS NOT NULL THEN oi.quantity ELSE 0 END), 0) DESC, "
                    + "b.createdAt DESC, b.bookId DESC";
            dataQ = em.createQuery(jpql, BookCardDTO.class).setParameter("delivered", OrderStatus.DELIVERED);
        } else {
            String orderBy = resolveOrderBy(criteria.getSort());
            dataQ = em.createQuery(CARD_PROJECTION + "FROM Book b JOIN b.category c" + where + orderBy, BookCardDTO.class);
        }
        params.forEach(dataQ::setParameter);
        List<BookCardDTO> items = dataQ.setFirstResult(offset).setMaxResults(pageSize).getResultList();
        return new PageResult<>(items, safePage, pageSize, total);
    }

    public Optional<Book> findActiveDetailById(EntityManager em, Long bookId) {
        if (bookId == null) {
            return Optional.empty();
        }
        List<Book> rows = em.createQuery(
                        "SELECT b FROM Book b JOIN FETCH b.category c "
                                + "WHERE b.bookId = :id AND b.active = true AND c.active = true",
                        Book.class)
                .setParameter("id", bookId)
                .setMaxResults(1)
                .getResultList();
        return rows.stream().findFirst();
    }

    public List<BookCardDTO> findSimilarActive(EntityManager em, Integer categoryId, Long excludeBookId, int limit) {
        return em.createQuery(
                        CARD_PROJECTION + "FROM Book b JOIN b.category c "
                                + "WHERE b.active = true AND c.active = true "
                                + "AND c.categoryId = :cid AND b.bookId <> :excludeId "
                                + "ORDER BY b.createdAt DESC, b.bookId DESC",
                        BookCardDTO.class)
                .setParameter("cid", categoryId)
                .setParameter("excludeId", excludeBookId)
                .setMaxResults(limit)
                .getResultList();
    }

    public Optional<Book> findByIdWithCategory(EntityManager em, Long bookId) {
        if (bookId == null) {
            return Optional.empty();
        }
        List<Book> rows = em.createQuery(
                        "SELECT b FROM Book b JOIN FETCH b.category WHERE b.bookId = :id", Book.class)
                .setParameter("id", bookId)
                .setMaxResults(1)
                .getResultList();
        return rows.stream().findFirst();
    }

    public Optional<Book> findByIsbn(EntityManager em, String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return Optional.empty();
        }
        List<Book> rows = em.createQuery("SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class)
                .setParameter("isbn", isbn.trim())
                .setMaxResults(1)
                .getResultList();
        return rows.stream().findFirst();
    }

    public boolean existsByIsbnExcludingId(EntityManager em, String isbn, Long excludeId) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        String jpql = excludeId == null
                ? "SELECT COUNT(b) FROM Book b WHERE b.isbn = :isbn"
                : "SELECT COUNT(b) FROM Book b WHERE b.isbn = :isbn AND b.bookId <> :excludeId";
        var q = em.createQuery(jpql, Long.class).setParameter("isbn", isbn.trim());
        if (excludeId != null) {
            q.setParameter("excludeId", excludeId);
        }
        return q.getSingleResult() > 0L;
    }

    public PageResult<Book> searchAdminBooks(
            EntityManager em, String keyword, Integer categoryId, Boolean activeFilter, int page, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> params = new LinkedHashMap<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append("AND (LOWER(b.title) LIKE :kw OR LOWER(b.author) LIKE :kw OR LOWER(COALESCE(b.isbn, '')) LIKE :kw) ");
            params.put("kw", "%" + keyword.trim().toLowerCase() + "%");
        }
        if (categoryId != null) {
            where.append("AND c.categoryId = :cid ");
            params.put("cid", categoryId);
        }
        if (activeFilter != null) {
            where.append("AND b.active = :active ");
            params.put("active", activeFilter);
        }
        TypedQuery<Long> countQ = em.createQuery("SELECT COUNT(b.bookId) FROM Book b JOIN b.category c" + where, Long.class);
        params.forEach(countQ::setParameter);
        long total = countQ.getSingleResult();
        if (total == 0L) {
            return new PageResult<>(Collections.emptyList(), 1, pageSize, 0L);
        }
        int totalPages = (int) Math.ceil((double) total / pageSize);
        int safePage = Math.min(Math.max(1, page), totalPages);
        TypedQuery<Book> dataQ = em.createQuery(
                "SELECT b FROM Book b JOIN FETCH b.category c" + where + "ORDER BY b.bookId DESC", Book.class);
        params.forEach(dataQ::setParameter);
        List<Book> rows = dataQ.setFirstResult((safePage - 1) * pageSize).setMaxResults(pageSize).getResultList();
        return new PageResult<>(rows, safePage, pageSize, total);
    }

    /**
     * Locks books in deterministic ascending ID order using PESSIMISTIC_WRITE to prevent deadlocks (NFR-01).
     */
    public Map<Long, Book> lockBooksOrdered(EntityManager em, List<Long> bookIds) {
        List<Long> sorted = bookIds.stream().distinct().sorted().collect(Collectors.toList());
        Map<Long, Book> locked = new LinkedHashMap<>();
        for (Long id : sorted) {
            Book b = em.find(Book.class, id, LockModeType.PESSIMISTIC_WRITE);
            if (b != null) {
                locked.put(id, b);
            }
        }
        return locked;
    }

    private static void appendPublicFilters(StringBuilder where, Map<String, Object> params, BookFilterCriteria c) {
        if (c.getKeyword() != null) {
            where.append("AND (LOWER(b.title) LIKE :kw OR LOWER(b.author) LIKE :kw OR LOWER(COALESCE(b.isbn, '')) LIKE :kw) ");
            params.put("kw", "%" + c.getKeyword().toLowerCase() + "%");
        }
        if (c.getCategoryId() != null) {
            where.append("AND c.categoryId = :cid ");
            params.put("cid", c.getCategoryId());
        }
        if (c.getMinPrice() != null) {
            where.append("AND b.price >= :minPrice ");
            params.put("minPrice", c.getMinPrice());
        }
        if (c.getMaxPrice() != null) {
            where.append("AND b.price <= :maxPrice ");
            params.put("maxPrice", c.getMaxPrice());
        }
    }

    private static String resolveOrderBy(BookSortOption sort) {
        switch (sort) {
            case PRICE_ASC: return "ORDER BY b.price ASC, b.bookId ASC";
            case PRICE_DESC: return "ORDER BY b.price DESC, b.bookId DESC";
            case TITLE_ASC: return "ORDER BY b.title ASC, b.bookId ASC";
            default: return "ORDER BY b.createdAt DESC, b.bookId DESC";
        }
    }
}
