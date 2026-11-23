package com.bookhub.dto;

import com.bookhub.util.FormatUtil;
import java.math.BigDecimal;

public final class BestSellerRowDTO {

    private final Long bookId;
    private final String bookCode;
    private final String title;
    private final String author;
    private final long unitsSold;
    private final BigDecimal revenue;

    public BestSellerRowDTO(Long bookId, String title, String author, long unitsSold, BigDecimal revenue) {
        this.bookId = bookId;
        this.bookCode = FormatUtil.formatBookCode(bookId);
        this.title = title;
        this.author = author;
        this.unitsSold = unitsSold;
        this.revenue = revenue == null ? BigDecimal.ZERO : revenue;
    }

    public Long getBookId() { return bookId; }
    public String getBookCode() { return bookCode; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public long getUnitsSold() { return unitsSold; }
    public BigDecimal getRevenue() { return revenue; }
    public String getFormattedRevenue() { return FormatUtil.formatVnd(revenue); }
}
