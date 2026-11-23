package com.bookhub.dto;

import com.bookhub.entity.ReviewStatus;
import com.bookhub.util.FormatUtil;
import java.time.LocalDateTime;

public final class AdminReviewRowDTO {

    private final Long reviewId;
    private final Long bookId;
    private final String bookTitle;
    private final String reviewerName;
    private final String reviewerEmail;
    private final int rating;
    private final String title;
    private final String content;
    private final ReviewStatus status;
    private final String createdAtFormatted;

    public AdminReviewRowDTO(Long reviewId, Long bookId, String bookTitle, String reviewerName,
            String reviewerEmail, int rating, String title, String content,
            ReviewStatus status, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.reviewerName = reviewerName;
        this.reviewerEmail = reviewerEmail;
        this.rating = rating;
        this.title = title;
        this.content = content;
        this.status = status;
        this.createdAtFormatted = FormatUtil.formatDateTime(createdAt);
    }

    public Long getReviewId() { return reviewId; }
    public Long getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public String getReviewerName() { return reviewerName; }
    public String getReviewerEmail() { return reviewerEmail; }
    public int getRating() { return rating; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public ReviewStatus getStatus() { return status; }
    public String getCreatedAtFormatted() { return createdAtFormatted; }
}
