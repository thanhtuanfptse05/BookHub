package com.bookhub.dto;

import com.bookhub.util.FormatUtil;
import java.math.BigDecimal;

/**
 * Lightweight card DTO for Home, Catalogue, and Similar Books (prevents LazyInitializationException).
 */
public final class BookCardDTO {

    private final Long bookId;
    private final String title;
    private final String author;
    private final BigDecimal price;
    private final String formattedPrice;
    private final int stockQty;
    private final String coverUrl;
    private final String categoryName;
    private final boolean featured;

    public BookCardDTO(Long bookId, String title, String author, BigDecimal price,
            int stockQty, String coverUrl, String categoryName, boolean featured) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.price = price;
        this.formattedPrice = FormatUtil.formatVnd(price);
        this.stockQty = stockQty;
        this.coverUrl = FormatUtil.safeCoverUrl(coverUrl);
        this.categoryName = categoryName;
        this.featured = featured;
    }

    public Long getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public BigDecimal getPrice() { return price; }
    public String getFormattedPrice() { return formattedPrice; }
    public int getStockQty() { return stockQty; }
    public String getCoverUrl() { return coverUrl; }
    public String getCategoryName() { return categoryName; }
    public boolean isFeatured() { return featured; }
}
