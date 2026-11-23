package com.bookhub.util;

import com.bookhub.entity.OrderStatus;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Authoritative MVP Order State Machine (constraints/business.md §3.6, FR-11..12).
 */
public final class OrderStateMachine {

    private OrderStateMachine() {
    }

    public static boolean canTransition(OrderStatus from, OrderStatus to) {
        if (from == null || to == null || from == to) {
            return false;
        }
        switch (from) {
            case PENDING:
                return to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED:
                return to == OrderStatus.SHIPPING || to == OrderStatus.CANCELLED;
            case SHIPPING:
                return to == OrderStatus.DELIVERED;
            default:
                return false;
        }
    }

    public static boolean canCustomerCancel(OrderStatus status) {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    public static List<OrderStatus> nextAllowedStatuses(OrderStatus current) {
        if (current == null) {
            return Collections.emptyList();
        }
        switch (current) {
            case PENDING:
                return Arrays.asList(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
            case CONFIRMED:
                return Arrays.asList(OrderStatus.SHIPPING, OrderStatus.CANCELLED);
            case SHIPPING:
                return Collections.singletonList(OrderStatus.DELIVERED);
            default:
                return Collections.emptyList();
        }
    }

    public static String labelVi(OrderStatus status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case PENDING: return "Chờ xác nhận";
            case CONFIRMED: return "Đã xác nhận";
            case SHIPPING: return "Đang giao hàng";
            case DELIVERED: return "Đã giao hàng";
            case CANCELLED: return "Đã hủy";
            default: return status.name();
        }
    }

    public static String badgeClass(OrderStatus status) {
        if (status == null) {
            return "bh-badge-neutral";
        }
        switch (status) {
            case PENDING: return "bh-badge-warning";
            case CONFIRMED: return "bh-badge-info";
            case SHIPPING: return "bh-badge-teal";
            case DELIVERED: return "bh-badge-success";
            case CANCELLED: return "bh-badge-danger";
            default: return "bh-badge-neutral";
        }
    }
}
