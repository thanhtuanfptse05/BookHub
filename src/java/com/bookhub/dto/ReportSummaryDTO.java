package com.bookhub.dto;

import com.bookhub.util.FormatUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public final class ReportSummaryDTO {

    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final long totalOrdersInRange;
    private final long deliveredOrderCount;
    private final BigDecimal deliveredRevenue;
    private final BigDecimal avgOrderValue;
    private final long activeBookCount;
    private final long totalActiveStockUnits;
    private final BigDecimal minBookPrice;
    private final BigDecimal maxBookPrice;
    private final BigDecimal avgBookPrice;
    private final List<OrderStatusCountDTO> statusCounts;
    private final List<BestSellerRowDTO> topBestsellers;

    public ReportSummaryDTO(LocalDate fromDate, LocalDate toDate, long totalOrdersInRange,
            long deliveredOrderCount, BigDecimal deliveredRevenue, BigDecimal avgOrderValue,
            long activeBookCount, long totalActiveStockUnits, BigDecimal minBookPrice,
            BigDecimal maxBookPrice, BigDecimal avgBookPrice,
            List<OrderStatusCountDTO> statusCounts, List<BestSellerRowDTO> topBestsellers) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.totalOrdersInRange = totalOrdersInRange;
        this.deliveredOrderCount = deliveredOrderCount;
        this.deliveredRevenue = deliveredRevenue == null ? BigDecimal.ZERO : deliveredRevenue;
        this.avgOrderValue = avgOrderValue == null ? BigDecimal.ZERO : avgOrderValue;
        this.activeBookCount = activeBookCount;
        this.totalActiveStockUnits = totalActiveStockUnits;
        this.minBookPrice = minBookPrice == null ? BigDecimal.ZERO : minBookPrice;
        this.maxBookPrice = maxBookPrice == null ? BigDecimal.ZERO : maxBookPrice;
        this.avgBookPrice = avgBookPrice == null ? BigDecimal.ZERO : avgBookPrice;
        this.statusCounts = statusCounts == null ? Collections.emptyList() : Collections.unmodifiableList(statusCounts);
        this.topBestsellers = topBestsellers == null ? Collections.emptyList() : Collections.unmodifiableList(topBestsellers);
    }

    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public long getTotalOrdersInRange() { return totalOrdersInRange; }
    public long getDeliveredOrderCount() { return deliveredOrderCount; }
    public BigDecimal getDeliveredRevenue() { return deliveredRevenue; }
    public String getFormattedDeliveredRevenue() { return FormatUtil.formatVnd(deliveredRevenue); }
    public BigDecimal getAvgOrderValue() { return avgOrderValue; }
    public String getFormattedAvgOrderValue() { return FormatUtil.formatVnd(avgOrderValue); }
    public long getActiveBookCount() { return activeBookCount; }
    public long getTotalActiveStockUnits() { return totalActiveStockUnits; }
    public BigDecimal getMinBookPrice() { return minBookPrice; }
    public String getFormattedMinBookPrice() { return activeBookCount == 0 ? "\u2014" : FormatUtil.formatVnd(minBookPrice); }
    public BigDecimal getMaxBookPrice() { return maxBookPrice; }
    public String getFormattedMaxBookPrice() { return activeBookCount == 0 ? "\u2014" : FormatUtil.formatVnd(maxBookPrice); }
    public BigDecimal getAvgBookPrice() { return avgBookPrice; }
    public String getFormattedAvgBookPrice() { return activeBookCount == 0 ? "\u2014" : FormatUtil.formatVnd(avgBookPrice); }
    public List<OrderStatusCountDTO> getStatusCounts() { return statusCounts; }
    public List<BestSellerRowDTO> getTopBestsellers() { return topBestsellers; }
}
