package com.bookhub.dto;

import java.math.BigDecimal;

/**
 * Validated catalog search & filter criteria for /books.
 */
public final class BookFilterCriteria {

    private final String keyword;
    private final Integer categoryId;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final BookSortOption sort;

    public BookFilterCriteria(String keyword, Integer categoryId, BigDecimal minPrice,
            BigDecimal maxPrice, BookSortOption sort) {
        this.keyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        this.categoryId = categoryId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.sort = sort == null ? BookSortOption.NEWEST : sort;
    }

    public String getKeyword() { return keyword; }
    public Integer getCategoryId() { return categoryId; }
    public BigDecimal getMinPrice() { return minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public BookSortOption getSort() { return sort; }

    public boolean hasActiveFilters() {
        return keyword != null || categoryId != null || minPrice != null || maxPrice != null
                || sort != BookSortOption.NEWEST;
    }
}
