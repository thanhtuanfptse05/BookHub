package com.bookhub.dto;

import com.bookhub.util.FormatUtil;
import java.time.LocalDateTime;

public final class PublicReviewDTO {

    private final Long reviewId;
    private final String reviewerName;
    private final int rating;
    private final String title;
    private final String content;
    private final String createdAtFormatted;

    public PublicReviewDTO(Long reviewId, String reviewerName, int rating, String title,
            String content, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.reviewerName = reviewerName;
        this.rating = rating;
        this.title = title;
        this.content = content;
        this.createdAtFormatted = FormatUtil.formatDateTime(createdAt);
    }

    public Long getReviewId() { return reviewId; }
    public String getReviewerName() { return reviewerName; }
    public int getRating() { return rating; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCreatedAtFormatted() { return createdAtFormatted; }
}
