package com.bookhub.repository;

import com.bookhub.dto.PageResult;
import com.bookhub.entity.Order;
import com.bookhub.entity.OrderItem;
import com.bookhub.entity.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JPA repository for dbo.Orders and dbo.OrderItems with pessimistic locking and ownership queries.
 */
public class OrderRepository extends BaseRepository<Order, Long> {

    public OrderRepository() {
        super(Order.class);
    }

    public Optional<Order> findByIdLocked(EntityManager em, Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(em.find(Order.class, orderId, LockModeType.PESSIMISTIC_WRITE));
    }

    public Optional<Order> findByIdWithUser(EntityManager em, Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        List<Order> rows = em.createQuery(
                        "SELECT o FROM Order o JOIN FETCH o.user WHERE o.orderId = :id", Order.class)
                .setParameter("id", orderId)
                .setMaxResults(1)
                .getResultList();
        return rows.stream().findFirst();
    }

    public List<OrderItem> findItemsByOrderId(EntityManager em, Long orderId) {
        return em.createQuery(
                        "SELECT oi FROM OrderItem oi JOIN FETCH oi.book WHERE oi.order.orderId = :oid "
                                + "ORDER BY oi.orderItemId ASC",
                        OrderItem.class)
                .setParameter("oid", orderId)
                .getResultList();
    }

    public long sumItemQuantityByOrderId(EntityManager em, Long orderId) {
        Long sum = em.createQuery(
                        "SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.order.orderId = :oid",
                        Long.class)
                .setParameter("oid", orderId)
                .getSingleResult();
        return sum != null ? sum : 0L;
    }

    public boolean existsByOrderCode(EntityManager em, String orderCode) {
        Long cnt = em.createQuery(
                        "SELECT COUNT(o) FROM Order o WHERE o.orderCode = :code", Long.class)
                .setParameter("code", orderCode)
                .getSingleResult();
        return cnt != null && cnt > 0L;
    }

    public PageResult<Order> findByUserPaginated(
            EntityManager em, Long userId, OrderStatus status, int page, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE o.user.userId = :uid ");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("uid", userId);
        if (status != null) {
            where.append("AND o.status = :status ");
            params.put("status", status);
        }
        TypedQuery<Long> countQ = em.createQuery("SELECT COUNT(o) FROM Order o" + where, Long.class);
        params.forEach(countQ::setParameter);
        long total = countQ.getSingleResult();
        if (total == 0L) {
            return new PageResult<>(Collections.emptyList(), 1, pageSize, 0L);
        }
        int totalPages = (int) Math.ceil((double) total / pageSize);
        int safePage = Math.min(Math.max(1, page), totalPages);
        TypedQuery<Order> dataQ = em.createQuery(
                "SELECT o FROM Order o JOIN FETCH o.user" + where + "ORDER BY o.createdAt DESC, o.orderId DESC",
                Order.class);
        params.forEach(dataQ::setParameter);
        List<Order> items = dataQ.setFirstResult((safePage - 1) * pageSize).setMaxResults(pageSize).getResultList();
        return new PageResult<>(items, safePage, pageSize, total);
    }

    public PageResult<Order> searchAdminOrders(
            EntityManager em, String orderCode, OrderStatus status,
            LocalDateTime fromInclusive, LocalDateTime toExclusive, int page, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> params = new LinkedHashMap<>();
        if (orderCode != null && !orderCode.trim().isEmpty()) {
            where.append("AND LOWER(o.orderCode) LIKE :code ");
            params.put("code", "%" + orderCode.trim().toLowerCase() + "%");
        }
        if (status != null) {
            where.append("AND o.status = :status ");
            params.put("status", status);
        }
        if (fromInclusive != null) {
            where.append("AND o.createdAt >= :fromDt ");
            params.put("fromDt", fromInclusive);
        }
        if (toExclusive != null) {
            where.append("AND o.createdAt < :toDt ");
            params.put("toDt", toExclusive);
        }
        TypedQuery<Long> countQ = em.createQuery("SELECT COUNT(o) FROM Order o" + where, Long.class);
        params.forEach(countQ::setParameter);
        long total = countQ.getSingleResult();
        if (total == 0L) {
            return new PageResult<>(Collections.emptyList(), 1, pageSize, 0L);
        }
        int totalPages = (int) Math.ceil((double) total / pageSize);
        int safePage = Math.min(Math.max(1, page), totalPages);
        TypedQuery<Order> dataQ = em.createQuery(
                "SELECT o FROM Order o JOIN FETCH o.user" + where + "ORDER BY o.createdAt DESC, o.orderId DESC",
                Order.class);
        params.forEach(dataQ::setParameter);
        List<Order> rows = dataQ.setFirstResult((safePage - 1) * pageSize).setMaxResults(pageSize).getResultList();
        return new PageResult<>(rows, safePage, pageSize, total);
    }

    public boolean hasDeliveredPurchase(EntityManager em, Long userId, Long bookId) {
        Long cnt = em.createQuery(
                        "SELECT COUNT(oi) FROM OrderItem oi JOIN oi.order o "
                                + "WHERE o.user.userId = :uid AND oi.book.bookId = :bid AND o.status = :delivered",
                        Long.class)
                .setParameter("uid", userId)
                .setParameter("bid", bookId)
                .setParameter("delivered", OrderStatus.DELIVERED)
                .getSingleResult();
        return cnt != null && cnt > 0L;
    }
}
