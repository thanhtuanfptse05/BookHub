package com.bookhub.dto;

import com.bookhub.entity.OrderStatus;
import com.bookhub.util.OrderStateMachine;

public final class OrderStatusCountDTO {

    private final OrderStatus status;
    private final long count;
    private final int percentage;

    public OrderStatusCountDTO(OrderStatus status, long count, long totalOrders) {
        this.status = status;
        this.count = Math.max(0L, count);
        this.percentage = totalOrders <= 0L ? 0 : (int) Math.round((this.count * 100.0) / totalOrders);
    }

    public OrderStatus getStatus() { return status; }
    public String getLabel() { return OrderStateMachine.labelVi(status); }
    public String getBadgeClass() { return OrderStateMachine.badgeClass(status); }
    public long getCount() { return count; }
    public int getPercentage() { return percentage; }
}
