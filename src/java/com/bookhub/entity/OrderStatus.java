package com.bookhub.entity;

/**
 * Order fulfillment states matching dbo.Orders.status.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPING,
    DELIVERED,
    CANCELLED
}
