package com.bookhub.dto;

import com.bookhub.util.FormatUtil;
import java.math.BigDecimal;

public final class OrderItemSnapshotDTO {

    private final Long orderItemId;
    private final Long bookId;
    private final String bookTitleSnapshot;
    private final BigDecimal unitPrice;
    private final int quantity;
    private final BigDecimal lineTotal;
    private final boolean canWriteReview;

    public OrderItemSnapshotDTO(Long orderItemId, Long bookId, String bookTitleSnapshot,
            BigDecimal unitPrice, int quantity, BigDecimal lineTotal, boolean canWriteReview) {
        this.orderItemId = orderItemId;
        this.bookId = bookId;
        this.bookTitleSnapshot = bookTitleSnapshot;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.canWriteReview = canWriteReview;
    }

    public Long getOrderItemId() { return orderItemId; }
    public Long getBookId() { return bookId; }
    public String getBookTitleSnapshot() { return bookTitleSnapshot; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getFormattedUnitPrice() { return FormatUtil.formatVnd(unitPrice); }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public String getFormattedLineTotal() { return FormatUtil.formatVnd(lineTotal); }
    public boolean isCanWriteReview() { return canWriteReview; }
}
