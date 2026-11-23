package com.bookhub.dto;

import com.bookhub.util.FormatUtil;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Complete view model for SCR-03 / SCR-03b Book Detail page.
 */
public final class BookDetailDTO {

    private final Long bookId;
    private final String bookCode;
    private final String isbn;
    private final String title;
    private final String author;
    private final String publisher;
    private final Short publishedYear;
    private final BigDecimal price;
    private final String formattedPrice;
    private final int stockQty;
    private final String shortDescription;
    private final String description;
    private final String coverUrl;
    private final Integer categoryId;
    private final String categoryName;
    private final double averageRating;
    private final long reviewCount;
    private final List<BookCardDTO> similarBooks;
    private final List<PublicReviewDTO> approvedReviews;
    private final List<PublicQaDTO> answeredQuestions;
    private final boolean canWriteReview;
    private final boolean alreadyReviewed;

    public BookDetailDTO(Long bookId, String isbn, String title, String author, String publisher,
            Short publishedYear, BigDecimal price, int stockQty, String shortDescription,
            String description, String coverUrl, Integer categoryId, String categoryName,
            double averageRating, long reviewCount, List<BookCardDTO> similarBooks,
            List<PublicReviewDTO> approvedReviews, List<PublicQaDTO> answeredQuestions,
            boolean canWriteReview, boolean alreadyReviewed) {
        this.bookId = bookId;
        this.bookCode = FormatUtil.formatBookCode(bookId);
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publishedYear = publishedYear;
        this.price = price;
        this.formattedPrice = FormatUtil.formatVnd(price);
        this.stockQty = stockQty;
        this.shortDescription = shortDescription;
        this.description = description;
        this.coverUrl = FormatUtil.safeCoverUrl(coverUrl);
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.similarBooks = similarBooks == null ? Collections.emptyList() : Collections.unmodifiableList(similarBooks);
        this.approvedReviews = approvedReviews == null ? Collections.emptyList() : Collections.unmodifiableList(approvedReviews);
        this.answeredQuestions = answeredQuestions == null ? Collections.emptyList() : Collections.unmodifiableList(answeredQuestions);
        this.canWriteReview = canWriteReview;
        this.alreadyReviewed = alreadyReviewed;
    }

    public Long getBookId() { return bookId; }
    public String getBookCode() { return bookCode; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPublisher() { return publisher; }
    public Short getPublishedYear() { return publishedYear; }
    public BigDecimal getPrice() { return price; }
    public String getFormattedPrice() { return formattedPrice; }
    public int getStockQty() { return stockQty; }
    public boolean isInStock() { return stockQty > 0; }
    public String getShortDescription() { return shortDescription; }
    public String getDescription() { return description; }
    public String getCoverUrl() { return coverUrl; }
    public Integer getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public double getAverageRating() { return averageRating; }
    public String getFormattedAverageRating() { return String.format(Locale.US, "%.1f", averageRating); }
    public long getReviewCount() { return reviewCount; }
    public List<BookCardDTO> getSimilarBooks() { return similarBooks; }
    public List<PublicReviewDTO> getApprovedReviews() { return approvedReviews; }
    public List<PublicQaDTO> getAnsweredQuestions() { return answeredQuestions; }
    public boolean isCanWriteReview() { return canWriteReview; }
    public boolean isAlreadyReviewed() { return alreadyReviewed; }
}
