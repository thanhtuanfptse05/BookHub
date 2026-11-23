package com.bookhub.dto;

import com.bookhub.util.FormatUtil;
import java.math.BigDecimal;

public final class AdminBookRowDTO {

    private final Long bookId;
    private final String bookCode;
    private final String isbn;
    private final String title;
    private final String author;
    private final String categoryName;
    private final BigDecimal price;
    private final String formattedPrice;
    private final int stockQty;
    private final String coverUrl;
    private final boolean featured;
    private final boolean active;

    public AdminBookRowDTO(Long bookId, String isbn, String title, String author, String categoryName,
            BigDecimal price, int stockQty, String coverUrl, boolean featured, boolean active) {
        this.bookId = bookId;
        this.bookCode = FormatUtil.formatBookCode(bookId);
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.categoryName = categoryName;
        this.price = price;
        this.formattedPrice = FormatUtil.formatVnd(price);
        this.stockQty = stockQty;
        this.coverUrl = FormatUtil.safeCoverUrl(coverUrl);
        this.featured = featured;
        this.active = active;
    }

    public Long getBookId() { return bookId; }
    public String getBookCode() { return bookCode; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategoryName() { return categoryName; }
    public BigDecimal getPrice() { return price; }
    public String getFormattedPrice() { return formattedPrice; }
    public int getStockQty() { return stockQty; }
    public String getCoverUrl() { return coverUrl; }
    public boolean isFeatured() { return featured; }
    public boolean isActive() { return active; }
}
