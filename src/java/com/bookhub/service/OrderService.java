package com.bookhub.service;

import com.bookhub.config.AppConstants;
import com.bookhub.config.JpaUtil;
import com.bookhub.dto.OrderDetailDTO;
import com.bookhub.dto.OrderItemSnapshotDTO;
import com.bookhub.dto.OrderSummaryDTO;
import com.bookhub.dto.PageResult;
import com.bookhub.entity.Book;
import com.bookhub.entity.Order;
import com.bookhub.entity.OrderItem;
import com.bookhub.entity.OrderStatus;
import com.bookhub.exception.AccessDeniedException;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.NotFoundException;
import com.bookhub.repository.BookRepository;
import com.bookhub.repository.OrderRepository;
import com.bookhub.util.OrderStateMachine;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Customer order history/cancel and Admin order fulfillment state machine service (FR-07..12).
 */
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepo;
    private final BookRepository bookRepo;

    public OrderService() {
        this(new OrderRepository(), new BookRepository());
    }

    public OrderService(OrderRepository orderRepo, BookRepository bookRepo) {
        this.orderRepo = orderRepo;
        this.bookRepo = bookRepo;
    }

    public PageResult<OrderSummaryDTO> listCustomerOrders(Long userId, OrderStatus status, int page) {
        return JpaUtil.executeReadOnly(em -> {
            PageResult<Order> raw = orderRepo.findByUserPaginated(em, userId, status, page, AppConstants.DEFAULT_CATALOG_PAGE_SIZE);
            List<OrderSummaryDTO> rows = toSummaryList(em, raw.getItems());
            return new PageResult<>(rows, raw.getPage(), raw.getPageSize(), raw.getTotalItems());
        });
    }

    public OrderDetailDTO getCustomerOrderDetail(Long userId, Long orderId) {
        return JpaUtil.executeReadOnly(em -> {
            Order order = orderRepo.findByIdWithUser(em, orderId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng."));
            if (!order.getUser().getUserId().equals(userId)) {
                throw new AccessDeniedException("Bạn không có quyền xem đơn hàng này.");
            }
            return toDetailDto(em, order, userId);
        });
    }

    public void cancelByCustomer(Long userId, Long orderId) {
        JpaUtil.executeInTransactionVoid(em -> {
            Order locked = orderRepo.findByIdLocked(em, orderId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng."));
            if (!locked.getUser().getUserId().equals(userId)) {
                throw new AccessDeniedException("Bạn không có quyền hủy đơn hàng này.");
            }
            if (!OrderStateMachine.canCustomerCancel(locked.getStatus())) {
                throw new BusinessException("Chỉ có thể hủy đơn hàng ở trạng thái Chờ xác nhận hoặc Đã xác nhận.");
            }
            cancelAndRestoreStock(em, locked);
            LOGGER.info("Customer userId={} cancelled orderId={}", userId, orderId);
        });
    }

    public PageResult<OrderSummaryDTO> searchAdminOrders(
            String orderCode, OrderStatus status, LocalDate fromDate, LocalDate toDate, int page) {
        LocalDateTime fromDt = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toExcl = toDate != null ? toDate.plusDays(1).atStartOfDay() : null;
        return JpaUtil.executeReadOnly(em -> {
            PageResult<Order> raw = orderRepo.searchAdminOrders(
                    em, orderCode, status, fromDt, toExcl, page, AppConstants.DEFAULT_ADMIN_PAGE_SIZE);
            List<OrderSummaryDTO> rows = toSummaryList(em, raw.getItems());
            return new PageResult<>(rows, raw.getPage(), raw.getPageSize(), raw.getTotalItems());
        });
    }

    public OrderDetailDTO getAdminOrderDetail(Long orderId) {
        return JpaUtil.executeReadOnly(em -> {
            Order order = orderRepo.findByIdWithUser(em, orderId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng."));
            return toDetailDto(em, order, null);
        });
    }

    public void transitionByAdmin(Long adminUserId, Long orderId, OrderStatus targetStatus) {
        if (targetStatus == null) {
            throw new BusinessException("Trạng thái đích không hợp lệ.");
        }
        JpaUtil.executeInTransactionVoid(em -> {
            Order locked = orderRepo.findByIdLocked(em, orderId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng."));
            OrderStatus current = locked.getStatus();
            if (!OrderStateMachine.canTransition(current, targetStatus)) {
                throw new BusinessException(
                        "Không thể chuyển trạng thái đơn từ " + OrderStateMachine.labelVi(current)
                                + " sang " + OrderStateMachine.labelVi(targetStatus) + ".");
            }
            if (targetStatus == OrderStatus.CANCELLED) {
                cancelAndRestoreStock(em, locked);
            } else {
                locked.setStatus(targetStatus);
            }
            LOGGER.info("Admin userId={} transitioned orderId={} {} -> {}", adminUserId, orderId, current, targetStatus);
        });
    }

    private void cancelAndRestoreStock(EntityManager em, Order lockedOrder) {
        lockedOrder.setStatus(OrderStatus.CANCELLED);
        lockedOrder.setCancelledAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        List<OrderItem> items = orderRepo.findItemsByOrderId(em, lockedOrder.getOrderId());
        List<Long> bookIds = items.stream().map(oi -> oi.getBook().getBookId()).sorted().distinct().collect(Collectors.toList());
        for (Long bid : bookIds) {
            int qtyToRestore = items.stream()
                    .filter(oi -> oi.getBook().getBookId().equals(bid))
                    .mapToInt(OrderItem::getQuantity)
                    .sum();
            bookRepo.incrementStockAtomically(em, bid, qtyToRestore);
        }
        em.flush();
    }

    private List<OrderSummaryDTO> toSummaryList(EntityManager em, List<Order> orders) {
        return orders.stream()
                .map(o -> new OrderSummaryDTO(
                        o.getOrderId(), o.getOrderCode(), o.getUser().getFullName(), o.getUser().getEmail(),
                        o.getCreatedAt(), o.getStatus(), o.getSubtotal(), o.getShippingFee(), o.getTotalAmount(),
                        orderRepo.sumItemQuantityByOrderId(em, o.getOrderId())))
                .collect(Collectors.toList());
    }

    private OrderDetailDTO toDetailDto(EntityManager em, Order order, Long reviewingUserId) {
        List<OrderItem> items = orderRepo.findItemsByOrderId(em, order.getOrderId());
        Set<Long> reviewedBookIds = new HashSet<>();
        if (reviewingUserId != null && order.getStatus() == OrderStatus.DELIVERED) {
            List<Long> ids = em.createQuery(
                            "SELECT r.book.bookId FROM Review r WHERE r.user.userId = :uid", Long.class)
                    .setParameter("uid", reviewingUserId)
                    .getResultList();
            reviewedBookIds.addAll(ids);
        }

        List<OrderItemSnapshotDTO> itemDtos = items.stream()
                .map(oi -> {
                    boolean canReview = reviewingUserId != null
                            && order.getStatus() == OrderStatus.DELIVERED
                            && !reviewedBookIds.contains(oi.getBook().getBookId());
                    return new OrderItemSnapshotDTO(
                            oi.getOrderItemId(), oi.getBook().getBookId(), oi.getBookTitleSnapshot(),
                            oi.getUnitPrice(), oi.getQuantity(), oi.getLineTotal(), canReview);
                })
                .collect(Collectors.toList());

        return new OrderDetailDTO(
                order.getOrderId(), order.getOrderCode(), order.getUser().getUserId(),
                order.getUser().getFullName(), order.getUser().getEmail(),
                order.getRecipientName(), order.getRecipientPhone(), order.getShippingAddress(),
                order.getNote(), order.getSubtotal(), order.getShippingFee(), order.getTotalAmount(),
                order.getPaymentMethod(), order.getStatus(), order.getCreatedAt(),
                order.getUpdatedAt(), order.getCancelledAt(), itemDtos);
    }
}
