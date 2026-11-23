package com.bookhub.repository;

import com.bookhub.dto.BestSellerRowDTO;
import com.bookhub.entity.Order;
import com.bookhub.entity.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Executes SQL Server aggregate queries (COUNT, SUM, AVG, MIN, MAX) via JPA without loading rows into RAM (NFR-01).
 */
public class ReportRepository {

    public Map<OrderStatus, Long> countOrdersByStatus(
            EntityManager em, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        StringBuilder jpql = new StringBuilder("SELECT o.status, COUNT(o) FROM Order o WHERE 1=1 ");
        if (fromInclusive != null) { jpql.append("AND o.createdAt >= :fromDt "); }
        if (toExclusive != null) { jpql.append("AND o.createdAt < :toDt "); }
        jpql.append("GROUP BY o.status");

        TypedQuery<Object[]> q = em.createQuery(jpql.toString(), Object[].class);
        if (fromInclusive != null) { q.setParameter("fromDt", fromInclusive); }
        if (toExclusive != null) { q.setParameter("toDt", toExclusive); }

        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus st : OrderStatus.values()) {
            counts.put(st, 0L);
        }
        for (Object[] row : q.getResultList()) {
            counts.put((OrderStatus) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    public BigDecimal sumDeliveredRevenue(
            EntityManager em, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        StringBuilder jpql = new StringBuilder(
                "SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :delivered ");
        if (fromInclusive != null) { jpql.append("AND o.createdAt >= :fromDt "); }
        if (toExclusive != null) { jpql.append("AND o.createdAt < :toDt "); }

        TypedQuery<BigDecimal> q = em.createQuery(jpql.toString(), BigDecimal.class)
                .setParameter("delivered", OrderStatus.DELIVERED);
        if (fromInclusive != null) { q.setParameter("fromDt", fromInclusive); }
        if (toExclusive != null) { q.setParameter("toDt", toExclusive); }
        return toMoney(q.getSingleResult());
    }

    public BigDecimal avgDeliveredOrderValue(
            EntityManager em, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        StringBuilder jpql = new StringBuilder(
                "SELECT AVG(o.totalAmount) FROM Order o WHERE o.status = :delivered ");
        if (fromInclusive != null) { jpql.append("AND o.createdAt >= :fromDt "); }
        if (toExclusive != null) { jpql.append("AND o.createdAt < :toDt "); }

        TypedQuery<Double> q = em.createQuery(jpql.toString(), Double.class)
                .setParameter("delivered", OrderStatus.DELIVERED);
        if (fromInclusive != null) { q.setParameter("fromDt", fromInclusive); }
        if (toExclusive != null) { q.setParameter("toDt", toExclusive); }
        return toMoney(q.getSingleResult());
    }

    /**
     * Returns [COUNT, SUM(stockQty), MIN(price), MAX(price), AVG(price)] strictly for active books (BR-ADM-002).
     */
    public Object[] findActiveBookStats(EntityManager em) {
        return em.createQuery(
                        "SELECT COUNT(b), COALESCE(SUM(b.stockQty), 0), MIN(b.price), MAX(b.price), AVG(b.price) "
                                + "FROM Book b WHERE b.active = true",
                        Object[].class)
                .getSingleResult();
    }

    public List<BestSellerRowDTO> findTopBestsellers(
            EntityManager em, LocalDateTime fromInclusive, LocalDateTime toExclusive, int limit) {
        StringBuilder jpql = new StringBuilder(
                "SELECT new com.bookhub.dto.BestSellerRowDTO("
                        + "b.bookId, b.title, b.author, SUM(oi.quantity), SUM(oi.lineTotal)) "
                        + "FROM OrderItem oi JOIN oi.order o JOIN oi.book b "
                        + "WHERE o.status = :delivered ");
        if (fromInclusive != null) { jpql.append("AND o.createdAt >= :fromDt "); }
        if (toExclusive != null) { jpql.append("AND o.createdAt < :toDt "); }
        jpql.append("GROUP BY b.bookId, b.title, b.author ORDER BY SUM(oi.quantity) DESC, SUM(oi.lineTotal) DESC");

        TypedQuery<BestSellerRowDTO> q = em.createQuery(jpql.toString(), BestSellerRowDTO.class)
                .setParameter("delivered", OrderStatus.DELIVERED);
        if (fromInclusive != null) { q.setParameter("fromDt", fromInclusive); }
        if (toExclusive != null) { q.setParameter("toDt", toExclusive); }
        return q.setMaxResults(limit).getResultList();
    }

    public List<Order> findRecentOrders(EntityManager em, int limit) {
        return em.createQuery(
                        "SELECT o FROM Order o JOIN FETCH o.user ORDER BY o.createdAt DESC, o.orderId DESC",
                        Order.class)
                .setMaxResults(limit)
                .getResultList();
    }

    public static BigDecimal toMoney(Object raw) {
        if (raw == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (raw instanceof BigDecimal) {
            return ((BigDecimal) raw).setScale(2, RoundingMode.HALF_UP);
        }
        if (raw instanceof Number) {
            return BigDecimal.valueOf(((Number) raw).doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(raw.toString()).setScale(2, RoundingMode.HALF_UP);
    }
}
