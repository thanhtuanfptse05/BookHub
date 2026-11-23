package com.bookhub.dto;

import com.bookhub.util.FormatUtil;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class DashboardDTO {

    private final long activeBooks;
    private final long totalCustomers;
    private final long totalOrders;
    private final long pendingOrders;
    private final long deliveredOrders;
    private final BigDecimal deliveredRevenue;
    private final long pendingReviews;
    private final long openQuestions;
    private final List<OrderSummaryDTO> recentOrders;
    private final List<BestSellerRowDTO> topBestsellers;

    public DashboardDTO(long activeBooks, long totalCustomers, long totalOrders, long pendingOrders,
            long deliveredOrders, BigDecimal deliveredRevenue, long pendingReviews, long openQuestions,
            List<OrderSummaryDTO> recentOrders, List<BestSellerRowDTO> topBestsellers) {
        this.activeBooks = activeBooks;
        this.totalCustomers = totalCustomers;
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.deliveredOrders = deliveredOrders;
        this.deliveredRevenue = deliveredRevenue == null ? BigDecimal.ZERO : deliveredRevenue;
        this.pendingReviews = pendingReviews;
        this.openQuestions = openQuestions;
        this.recentOrders = recentOrders == null ? Collections.emptyList() : Collections.unmodifiableList(recentOrders);
        this.topBestsellers = topBestsellers == null ? Collections.emptyList() : Collections.unmodifiableList(topBestsellers);
    }

    public long getActiveBooks() { return activeBooks; }
    public long getTotalCustomers() { return totalCustomers; }
    public long getTotalOrders() { return totalOrders; }
    public long getPendingOrders() { return pendingOrders; }
    public long getDeliveredOrders() { return deliveredOrders; }
    public BigDecimal getDeliveredRevenue() { return deliveredRevenue; }
    public String getFormattedDeliveredRevenue() { return FormatUtil.formatVnd(deliveredRevenue); }
    public long getPendingReviews() { return pendingReviews; }
    public long getOpenQuestions() { return openQuestions; }
    public List<OrderSummaryDTO> getRecentOrders() { return recentOrders; }
    public List<BestSellerRowDTO> getTopBestsellers() { return topBestsellers; }
}
