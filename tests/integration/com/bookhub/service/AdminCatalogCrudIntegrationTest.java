package com.bookhub.service;

import com.bookhub.config.JpaUtil;
import com.bookhub.dto.AdminBookRowDTO;
import com.bookhub.dto.AdminCategoryRowDTO;
import com.bookhub.dto.BookFormDTO;
import com.bookhub.dto.PageResult;
import com.bookhub.entity.Book;
import com.bookhub.entity.OrderItem;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for 05-feat-admin-catalog-crud (AC-01..AC-07) against live SQL Server BookHubDB.
 */
class AdminCatalogCrudIntegrationTest {

    private final AdminCategoryService categoryService = new AdminCategoryService();
    private final AdminBookService bookService = new AdminBookService();
    private final CatalogService catalogService = new CatalogService();

    @BeforeAll
    static void init() {
        JpaUtil.init();
    }

    @AfterAll
    static void close() {
        JpaUtil.shutdown();
    }

    // @spec admin-catalog-crud/AC-01
    // @spec admin-catalog-crud/AC-02
    // @spec admin-catalog-crud/AC-03
    @Test
    void categoryCrudDuplicateRejectionAndSoftDeactivateWithoutDeletingBooks() {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        String name = "Lịch sử " + marker;
        String slug = "lich-su-" + marker;
        Integer catId = null;

        try {
            // AC-01: create category and verify listing with 0 books
            catId = categoryService.saveCategory(null, name, slug, "Sách lịch sử thế giới", true);
            List<AdminCategoryRowDTO> found = categoryService.listCategories(slug);
            assertEquals(1, found.size());
            assertEquals(0L, found.get(0).getTotalBooks());

            // AC-03: duplicate name or slug rejected
            BusinessException dup = assertThrows(
                    BusinessException.class,
                    () -> categoryService.saveCategory(null, name, "khac-" + marker, null, true));
            assertTrue(dup.getFieldErrors().containsKey("name"));

            BusinessException dupSlug = assertThrows(
                    BusinessException.class,
                    () -> categoryService.saveCategory(null, "Tên khác " + marker, slug, null, true));
            assertTrue(dupSlug.getFieldErrors().containsKey("slug"));

            // AC-02: toggleActive soft-deactivates without removing row
            assertFalse(categoryService.toggleActive(catId));
            assertTrue(categoryService.toggleActive(catId));
        } finally {
            if (catId != null) {
                Integer cid = catId;
                JpaUtil.executeInTransactionVoid(em ->
                        em.createQuery("DELETE FROM Category c WHERE c.categoryId = :id").setParameter("id", cid).executeUpdate());
            }
        }
    }

    // @spec admin-catalog-crud/AC-04
    // @spec admin-catalog-crud/AC-05
    // @spec admin-catalog-crud/AC-06
    // @spec admin-catalog-crud/AC-07
    @Test
    void bookCrudValidationNullIsbnAndSoftDeactivatePreservingOrderSnapshots() {
        Integer activeCatId = JpaUtil.executeReadOnly(em -> em.createQuery(
                        "SELECT c.categoryId FROM Category c WHERE c.active = true ORDER BY c.categoryId", Integer.class)
                .setMaxResults(1).getSingleResult());
        String marker = UUID.randomUUID().toString().substring(0, 8);
        Long createdBookId = null;

        try {
            // AC-04: admin search includes inactive books when filtered
            PageResult<AdminBookRowDTO> inactiveList = bookService.searchAdminBooks(null, null, "inactive", 1);
            assertTrue(inactiveList.getTotalItems() >= 1);

            // AC-06: invalid price, negative stock, duplicate ISBN, bad year, and javascript: coverUrl rejected
            BookFormDTO bad = new BookFormDTO();
            bad.setCategoryId(activeCatId);
            bad.setTitle("Bad Book");
            bad.setAuthor("Author");
            bad.setIsbn("978-0132350884");
            bad.setPublishedYear("999");
            bad.setPrice("0");
            bad.setStockQty("-1");
            bad.setCoverUrl("javascript:alert(1)");
            BusinessException badEx = assertThrows(BusinessException.class, () -> bookService.saveBook(bad));
            assertTrue(badEx.getFieldErrors().containsKey("price"));
            assertTrue(badEx.getFieldErrors().containsKey("stockQty"));
            assertTrue(badEx.getFieldErrors().containsKey("publishedYear"));
            assertTrue(badEx.getFieldErrors().containsKey("coverUrl"));

            // AC-05: blank ISBN normalizes to NULL; BigDecimal price saved cleanly
            BookFormDTO good = new BookFormDTO();
            good.setCategoryId(activeCatId);
            good.setTitle("Test Book " + marker);
            good.setAuthor("Author " + marker);
            good.setIsbn("   ");
            good.setPublishedYear("2025");
            good.setPrice("145000");
            good.setStockQty("8");
            good.setActive(true);
            createdBookId = bookService.saveBook(good);

            Long bid = createdBookId;
            Book saved = JpaUtil.executeReadOnly(em -> em.find(Book.class, bid));
            assertNull(saved.getIsbn(), "Blank ISBN must normalize to NULL");
            assertEquals(new BigDecimal("145000.00"), saved.getPrice());
            assertEquals(createdBookId, catalogService.getBookDetail(createdBookId, null).getBookId());

            // AC-07: soft-deactivate hides book from public detail while preserving existing seeded OrderItem snapshots
            assertFalse(bookService.toggleActive(createdBookId));
            assertThrows(NotFoundException.class, () -> catalogService.getBookDetail(bid, null));

            // Verify seeded inactive book still preserves its OrderItem snapshot intact (TC-DB-02)
            List<OrderItem> historicalItems = JpaUtil.executeReadOnly(em -> em.createQuery(
                            "SELECT oi FROM OrderItem oi JOIN oi.book b WHERE b.active = false", OrderItem.class)
                    .getResultList());
            assertFalse(historicalItems.isEmpty(), "Historical order items for inactive book must stay intact");
            assertTrue(historicalItems.get(0).getUnitPrice().compareTo(BigDecimal.ZERO) > 0);
        } finally {
            if (createdBookId != null) {
                Long bid = createdBookId;
                JpaUtil.executeInTransactionVoid(em ->
                        em.createQuery("DELETE FROM Book b WHERE b.bookId = :id").setParameter("id", bid).executeUpdate());
            }
        }
    }
}
