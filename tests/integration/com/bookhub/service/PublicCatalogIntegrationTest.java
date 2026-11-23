package com.bookhub.service;

import com.bookhub.config.JpaUtil;
import com.bookhub.dto.BookCardDTO;
import com.bookhub.dto.BookDetailDTO;
import com.bookhub.dto.BookFilterCriteria;
import com.bookhub.dto.BookSortOption;
import com.bookhub.dto.HomePageDTO;
import com.bookhub.dto.PageResult;
import com.bookhub.entity.Book;
import com.bookhub.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for 04-feat-public-catalog (AC-01..AC-08) against live SQL Server BookHubDB.
 */
class PublicCatalogIntegrationTest {

    private final CatalogService catalogService = new CatalogService();

    @BeforeAll
    static void init() {
        JpaUtil.init();
    }

    @AfterAll
    static void close() {
        JpaUtil.shutdown();
    }

    // @spec public-catalog/AC-01
    @Test
    void homeDataLoadsFeaturedNewestBestsellerShelvesAndExcludesInactiveBooks() {
        HomePageDTO home = catalogService.getHomeData();
        assertTrue(home.getFeaturedBooks().size() > 0 && home.getFeaturedBooks().size() <= 8);
        assertEquals(8, home.getNewestBooks().size());
        assertEquals(8, home.getBestsellerBooks().size());
        assertEquals(6, home.getCategories().size(), "Inactive archived category must be excluded");

        List<BookCardDTO> allShown = home.getNewestBooks();
        assertTrue(allShown.stream().noneMatch(b -> b.getPrice().compareTo(new BigDecimal("15000.00")) == 0),
                "Inactive book (15,000 VND) must never appear publicly");
    }

    // @spec public-catalog/AC-02
    // @spec public-catalog/AC-03
    // @spec public-catalog/AC-04
    // @spec public-catalog/AC-05
    @Test
    void searchFilterSortWhitelistAndPaginationWorkCorrectly() {
        // AC-04: sort allow-list rejects SQL injection payload safely
        assertEquals(BookSortOption.NEWEST, BookSortOption.fromParam("price; DROP TABLE Books;--"));
        assertEquals(BookSortOption.PRICE_ASC, BookSortOption.fromParam("price_asc"));
        assertEquals(BookSortOption.BESTSELLER, BookSortOption.fromParam("bestseller"));

        // AC-02: keyword + price range + DB pagination (12 per page across 24 active books)
        PageResult<BookCardDTO> page1 = catalogService.searchCatalog(
                new BookFilterCriteria(null, null, null, null, BookSortOption.PRICE_ASC), 1);
        assertEquals(24L, page1.getTotalItems());
        assertEquals(2, page1.getTotalPages());
        assertEquals(12, page1.getItems().size());
        assertTrue(page1.getItems().get(0).getPrice().compareTo(page1.getItems().get(11).getPrice()) <= 0);

        // AC-03: out-of-range page safely clamps to last page
        PageResult<BookCardDTO> clamped = catalogService.searchCatalog(
                new BookFilterCriteria(null, null, null, null, BookSortOption.NEWEST), 999);
        assertEquals(2, clamped.getPage());
        assertEquals(12, clamped.getItems().size());

        // AC-02: case-insensitive search by title/ISBN
        PageResult<BookCardDTO> cleanCode = catalogService.searchCatalog(
                new BookFilterCriteria("clean code", null, new BigDecimal("100000"), new BigDecimal("250000"), BookSortOption.NEWEST), 1);
        assertEquals(1L, cleanCode.getTotalItems());

        // AC-04: bestseller sort executes cleanly with DELIVERED order aggregation
        PageResult<BookCardDTO> best = catalogService.searchCatalog(
                new BookFilterCriteria(null, null, null, null, BookSortOption.BESTSELLER), 1);
        assertEquals(12, best.getItems().size());

        // AC-05: unmatched keyword returns empty result cleanly
        PageResult<BookCardDTO> empty = catalogService.searchCatalog(
                new BookFilterCriteria("khong-ton-tai-xyz-999", null, null, null, BookSortOption.NEWEST), 1);
        assertTrue(empty.isEmpty());
        assertEquals(0L, empty.getTotalItems());
    }

    // @spec public-catalog/AC-06
    // @spec public-catalog/AC-07
    // @spec public-catalog/AC-08
    @Test
    void bookDetailLoadsApprovedReviewsAnsweredQaSimilarOutOfStockAndRejectsInactive() {
        Long cleanCodeId = JpaUtil.executeReadOnly(em -> em.createQuery(
                        "SELECT b.bookId FROM Book b WHERE b.isbn = '978-0132350884'", Long.class)
                .getSingleResult());
        BookDetailDTO detail = catalogService.getBookDetail(cleanCodeId, null);

        // AC-06: similar books exclude self, only APPROVED reviews and ANSWERED Q&A included
        assertFalse(detail.getSimilarBooks().isEmpty());
        assertTrue(detail.getSimilarBooks().size() <= 4);
        assertTrue(detail.getSimilarBooks().stream().noneMatch(b -> b.getBookId().equals(cleanCodeId)));
        assertTrue(detail.getReviewCount() >= 1);
        assertTrue(detail.getAverageRating() >= 1.0 && detail.getAverageRating() <= 5.0);
        assertFalse(detail.getAnsweredQuestions().isEmpty());

        // AC-07: out-of-stock active book still viewable with inStock = false
        Long outOfStockId = JpaUtil.executeReadOnly(em -> em.createQuery(
                        "SELECT b.bookId FROM Book b WHERE b.active = true AND b.category.active = true AND b.stockQty = 0",
                        Long.class)
                .setMaxResults(1)
                .getSingleResult());
        BookDetailDTO outDetail = catalogService.getBookDetail(outOfStockId, null);
        assertFalse(outDetail.isInStock());
        assertEquals(0, outDetail.getStockQty());

        // AC-08: inactive book or book in inactive category throws NotFoundException (-> HTTP 404)
        Long inactiveBookId = JpaUtil.executeReadOnly(em -> em.createQuery(
                        "SELECT b.bookId FROM Book b WHERE b.active = false", Long.class)
                .setMaxResults(1)
                .getSingleResult());
        assertThrows(NotFoundException.class, () -> catalogService.getBookDetail(inactiveBookId, null));
        assertThrows(NotFoundException.class, () -> catalogService.getBookDetail(999999L, null));
    }
}
