package com.bookhub.dto;

import com.bookhub.util.FormatUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CartItemDTO {

    private final Long cartItemId;
    private final Long bookId;
    private final String title;
    private final String author;
    private final String coverUrl;
    private final BigDecimal unitPrice;
    private final int stockQty;
    private final boolean available;
    private final int quantity;
    private final BigDecimal lineTotal;

    public CartItemDTO(Long cartItemId, Long bookId, String title, String author, String coverUrl,
            BigDecimal unitPrice, int stockQty, boolean available, int quantity) {
        this.cartItemId = cartItemId;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.coverUrl = FormatUtil.safeCoverUrl(coverUrl);
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        this.stockQty = stockQty;
        this.available = available;
        this.quantity = quantity;
        this.lineTotal = this.unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getCartItemId() { return cartItemId; }
    public Long getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverUrl() { return coverUrl; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getFormattedUnitPrice() { return FormatUtil.formatVnd(unitPrice); }
    public int getStockQty() { return stockQty; }
    public boolean isAvailable() { return available; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public String getFormattedLineTotal() { return FormatUtil.formatVnd(lineTotal); }
}
