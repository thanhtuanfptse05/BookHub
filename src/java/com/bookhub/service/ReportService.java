package com.bookhub.service;

import com.bookhub.config.JpaUtil;
import com.bookhub.dto.BestSellerRowDTO;
import com.bookhub.dto.DashboardDTO;
import com.bookhub.dto.OrderStatusCountDTO;
import com.bookhub.dto.OrderSummaryDTO;
import com.bookhub.dto.ReportSummaryDTO;
import com.bookhub.entity.OrderStatus;
import com.bookhub.entity.QuestionStatus;
import com.bookhub.entity.ReviewStatus;
import com.bookhub.entity.RoleCode;
import com.bookhub.exception.BusinessException;
import com.bookhub.repository.OrderRepository;
import com.bookhub.repository.QuestionRepository;
import com.bookhub.repository.ReportRepository;
import com.bookhub.repository.ReviewRepository;
import com.bookhub.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dashboard KPI and SQL aggregate reporting service (08-feat-admin-users-reports, FR-01, FR-05..08).
 */
public class ReportService {

    private final ReportRepository reportRepo;
    private final UserRepository userRepo;
    private final OrderRepository orderRepo;
    private final ReviewRepository reviewRepo;
    private final QuestionRepository questionRepo;

    public ReportService() {
        this(new ReportRepository(), new UserRepository(), new OrderRepository(),
                new ReviewRepository(), new QuestionRepository());
    }

    public ReportService(ReportRepository reportRepo, UserRepository userRepo, OrderRepository orderRepo,
            ReviewRepository reviewRepo, QuestionRepository questionRepo) {
        this.reportRepo = reportRepo;
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
        this.reviewRepo = reviewRepo;
        this.questionRepo = questionRepo;
    }

    public DashboardDTO getDashboard() {
        return JpaUtil.executeReadOnly(em -> {
            Object[] bookStats = reportRepo.findActiveBookStats(em);
            long activeBooks = ((Number) bookStats[0]).longValue();
            long totalCustomers = userRepo.countByRole(em, RoleCode.CUSTOMER);
            Map<OrderStatus, Long> statusMap = reportRepo.countOrdersByStatus(em, null, null);
            long totalOrders = statusMap.values().stream().mapToLong(Long::longValue).sum();
            long pendingOrders = statusMap.getOrDefault(OrderStatus.PENDING, 0L);
            long deliveredOrders = statusMap.getOrDefault(OrderStatus.DELIVERED, 0L);
            BigDecimal revenue = reportRepo.sumDeliveredRevenue(em, null, null);
            long pendingReviews = reviewRepo.countByStatus(em, ReviewStatus.PENDING);
            long openQuestions = questionRepo.countByStatus(em, QuestionStatus.OPEN);

            List<OrderSummaryDTO> recent = reportRepo.findRecentOrders(em, 5).stream()
                    .map(o -> new OrderSummaryDTO(
                            o.getOrderId(), o.getOrderCode(), o.getUser().getFullName(), o.getUser().getEmail(),
                            o.getCreatedAt(), o.getStatus(), o.getSubtotal(), o.getShippingFee(), o.getTotalAmount(),
                            orderRepo.sumItemQuantityByOrderId(em, o.getOrderId())))
                    .collect(Collectors.toList());
            List<BestSellerRowDTO> top5 = reportRepo.findTopBestsellers(em, null, null, 5);

            return new DashboardDTO(
                    activeBooks, totalCustomers, totalOrders, pendingOrders, deliveredOrders,
                    revenue, pendingReviews, openQuestions, recent, top5);
        });
    }

    public ReportSummaryDTO getReport(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessException("Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        }
        LocalDateTime fromInclusive = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toExclusive = toDate != null ? toDate.plusDays(1).atStartOfDay() : null;

        return JpaUtil.executeReadOnly(em -> {
            Map<OrderStatus, Long> countsMap = reportRepo.countOrdersByStatus(em, fromInclusive, toExclusive);
            long totalOrders = countsMap.values().stream().mapToLong(Long::longValue).sum();
            long deliveredCount = countsMap.getOrDefault(OrderStatus.DELIVERED, 0L);

            List<OrderStatusCountDTO> statusRows = new ArrayList<>();
            for (OrderStatus st : OrderStatus.values()) {
                statusRows.add(new OrderStatusCountDTO(st, countsMap.getOrDefault(st, 0L), totalOrders));
            }

            BigDecimal revenue = reportRepo.sumDeliveredRevenue(em, fromInclusive, toExclusive);
            BigDecimal aov = reportRepo.avgDeliveredOrderValue(em, fromInclusive, toExclusive);

            Object[] bookStats = reportRepo.findActiveBookStats(em);
            long activeBookCount = ((Number) bookStats[0]).longValue();
            long totalStock = ((Number) bookStats[1]).longValue();
            BigDecimal minPrice = ReportRepository.toMoney(bookStats[2]);
            BigDecimal maxPrice = ReportRepository.toMoney(bookStats[3]);
            BigDecimal avgPrice = ReportRepository.toMoney(bookStats[4]);

            List<BestSellerRowDTO> top10 = reportRepo.findTopBestsellers(em, fromInclusive, toExclusive, 10);

            return new ReportSummaryDTO(
                    fromDate, toDate, totalOrders, deliveredCount, revenue, aov,
                    activeBookCount, totalStock, minPrice, maxPrice, avgPrice, statusRows, top10);
        });
    }
}
