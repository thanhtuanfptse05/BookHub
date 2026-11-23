package com.bookhub.dto;

import com.bookhub.entity.OrderStatus;
import com.bookhub.util.FormatUtil;
import com.bookhub.util.OrderStateMachine;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class OrderSummaryDTO {

    private final Long orderId;
    private final String orderCode;
    private final String customerName;
    private final String customerEmail;
    private final LocalDateTime createdAt;
    private final OrderStatus status;
    private final BigDecimal subtotal;
    private final BigDecimal shippingFee;
    private final BigDecimal totalAmount;
    private final long itemCount;

    public OrderSummaryDTO(Long orderId, String orderCode, String customerName, String customerEmail,
            LocalDateTime createdAt, OrderStatus status, BigDecimal subtotal,
            BigDecimal shippingFee, BigDecimal totalAmount, long itemCount) {
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.createdAt = createdAt;
        this.status = status;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
    }

    public Long getOrderId() { return orderId; }
    public String getOrderCode() { return orderCode; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCreatedAtFormatted() { return FormatUtil.formatDateTime(createdAt); }
    public OrderStatus getStatus() { return status; }
    public String getStatusLabel() { return OrderStateMachine.labelVi(status); }
    public String getStatusBadgeClass() { return OrderStateMachine.badgeClass(status); }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getFormattedTotal() { return FormatUtil.formatVnd(totalAmount); }
    public long getItemCount() { return itemCount; }
}
