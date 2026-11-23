package com.bookhub.dto;

import com.bookhub.entity.OrderStatus;
import com.bookhub.entity.PaymentMethod;
import com.bookhub.util.FormatUtil;
import com.bookhub.util.OrderStateMachine;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public final class OrderDetailDTO {

    private final Long orderId;
    private final String orderCode;
    private final Long userId;
    private final String customerName;
    private final String customerEmail;
    private final String recipientName;
    private final String recipientPhone;
    private final String shippingAddress;
    private final String note;
    private final BigDecimal subtotal;
    private final BigDecimal shippingFee;
    private final BigDecimal totalAmount;
    private final PaymentMethod paymentMethod;
    private final OrderStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime cancelledAt;
    private final List<OrderItemSnapshotDTO> items;

    public OrderDetailDTO(Long orderId, String orderCode, Long userId, String customerName,
            String customerEmail, String recipientName, String recipientPhone, String shippingAddress,
            String note, BigDecimal subtotal, BigDecimal shippingFee, BigDecimal totalAmount,
            PaymentMethod paymentMethod, OrderStatus status, LocalDateTime createdAt,
            LocalDateTime updatedAt, LocalDateTime cancelledAt, List<OrderItemSnapshotDTO> items) {
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.userId = userId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.shippingAddress = shippingAddress;
        this.note = note;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.cancelledAt = cancelledAt;
        this.items = items == null ? Collections.emptyList() : Collections.unmodifiableList(items);
    }

    public Long getOrderId() { return orderId; }
    public String getOrderCode() { return orderCode; }
    public Long getUserId() { return userId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public String getShippingAddress() { return shippingAddress; }
    public String getNote() { return note; }
    public BigDecimal getSubtotal() { return subtotal; }
    public String getFormattedSubtotal() { return FormatUtil.formatVnd(subtotal); }
    public BigDecimal getShippingFee() { return shippingFee; }
    public String getFormattedShippingFee() { return FormatUtil.formatVnd(shippingFee); }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getFormattedTotalAmount() { return FormatUtil.formatVnd(totalAmount); }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public OrderStatus getStatus() { return status; }
    public String getStatusLabel() { return OrderStateMachine.labelVi(status); }
    public String getStatusBadgeClass() { return OrderStateMachine.badgeClass(status); }
    public String getCreatedAtFormatted() { return FormatUtil.formatDateTime(createdAt); }
    public String getUpdatedAtFormatted() { return FormatUtil.formatDateTime(updatedAt); }
    public String getCancelledAtFormatted() { return FormatUtil.formatDateTime(cancelledAt); }
    public boolean isCanCustomerCancel() { return OrderStateMachine.canCustomerCancel(status); }
    public List<OrderStatus> getAllowedAdminTransitions() { return OrderStateMachine.nextAllowedStatuses(status); }
    public List<OrderItemSnapshotDTO> getItems() { return items; }
}
