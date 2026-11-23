package com.bookhub.dto;

import com.bookhub.util.FormatUtil;

/**
 * Form state carrier for SCR-16 Admin Book Create/Edit (preserves raw user strings on validation failure).
 */
public class BookFormDTO {

    private Long bookId;
    private Integer categoryId;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String publishedYear;
    private String price;
    private String stockQty;
    private String shortDescription;
    private String description;
    private String coverUrl;
    private boolean featured;
    private boolean active = true;

    public String getBookCode() { return FormatUtil.formatBookCode(bookId); }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getPublishedYear() { return publishedYear; }
    public void setPublishedYear(String publishedYear) { this.publishedYear = publishedYear; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getStockQty() { return stockQty; }
    public void setStockQty(String stockQty) { this.stockQty = stockQty; }
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
