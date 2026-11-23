package com.bookhub.dto;

import com.bookhub.util.FormatUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

public final class CartViewDTO {

    private final List<CartItemDTO> items;
    private final int totalQuantity;
    private final BigDecimal subtotal;
    private final BigDecimal shippingFee;
    private final BigDecimal totalAmount;
    private final List<String> warnings;

    public CartViewDTO(List<CartItemDTO> items, BigDecimal shippingFee, List<String> warnings) {
        this.items = items == null ? Collections.emptyList() : Collections.unmodifiableList(items);
        this.totalQuantity = this.items.stream().mapToInt(CartItemDTO::getQuantity).sum();
        this.subtotal = this.items.stream()
                .map(CartItemDTO::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        this.shippingFee = this.items.isEmpty()
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : shippingFee.setScale(2, RoundingMode.HALF_UP);
        this.totalAmount = this.subtotal.add(this.shippingFee).setScale(2, RoundingMode.HALF_UP);
        this.warnings = warnings == null ? Collections.emptyList() : Collections.unmodifiableList(warnings);
    }

    public List<CartItemDTO> getItems() { return items; }
    public boolean isEmpty() { return items.isEmpty(); }
    public int getTotalQuantity() { return totalQuantity; }
    public BigDecimal getSubtotal() { return subtotal; }
    public String getFormattedSubtotal() { return FormatUtil.formatVnd(subtotal); }
    public BigDecimal getShippingFee() { return shippingFee; }
    public String getFormattedShippingFee() { return FormatUtil.formatVnd(shippingFee); }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getFormattedTotalAmount() { return FormatUtil.formatVnd(totalAmount); }
    public List<String> getWarnings() { return warnings; }
    public boolean hasWarnings() { return !warnings.isEmpty(); }
}
