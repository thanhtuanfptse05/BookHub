package com.bookhub.service;

import com.bookhub.config.JpaUtil;
import com.bookhub.dto.CheckoutRequest;
import com.bookhub.entity.Book;
import com.bookhub.entity.CartItem;
import com.bookhub.entity.Order;
import com.bookhub.entity.OrderItem;
import com.bookhub.entity.OrderStatus;
import com.bookhub.entity.PaymentMethod;
import com.bookhub.entity.User;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.OutOfStockException;
import com.bookhub.repository.BookRepository;
import com.bookhub.repository.CartRepository;
import com.bookhub.repository.OrderRepository;
import com.bookhub.util.OrderCodeGenerator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Atomic COD checkout transaction service (ADR-005, ADR-008, FR-04..06, EC-01..05).
 */
public class CheckoutService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutService.class);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s()]{8,20}$");
    private static final int MAX_CODE_RETRIES = 6;

    private final CartRepository cartRepo;
    private final BookRepository bookRepo;
    private final OrderRepository orderRepo;

    public CheckoutService() {
        this(new CartRepository(), new BookRepository(), new OrderRepository());
    }

    public CheckoutService(CartRepository cartRepo, BookRepository bookRepo, OrderRepository orderRepo) {
        this.cartRepo = cartRepo;
        this.bookRepo = bookRepo;
        this.orderRepo = orderRepo;
    }

    public Long placeCodOrder(Long userId, CheckoutRequest request) {
        String name = trim(request != null ? request.getRecipientName() : null);
        String phone = trim(request != null ? request.getRecipientPhone() : null);
        String address = trim(request != null ? request.getShippingAddress() : null);
        String note = trim(request != null ? request.getNote() : null);

        Map<String, String> errors = new LinkedHashMap<>();
        if (name.isEmpty() || name.length() > 100) {
            errors.put("recipientName", "Họ tên người nhận là bắt buộc (tối đa 100 ký tự).");
        }
        if (phone.isEmpty() || !PHONE_PATTERN.matcher(phone).matches()) {
            errors.put("recipientPhone", "Số điện thoại người nhận không hợp lệ (8–20 ký tự).");
        }
        if (address.isEmpty() || address.length() > 500) {
            errors.put("shippingAddress", "Địa chỉ nhận hàng là bắt buộc (tối đa 500 ký tự).");
        }
        if (note.length() > 500) {
            errors.put("note", "Ghi chú đơn hàng tối đa 500 ký tự.");
        }
        if (!errors.isEmpty()) {
            throw new BusinessException("Vui lòng kiểm tra lại thông tin giao hàng.", errors);
        }

        BigDecimal shippingFee = CartService.resolveShippingFee().setScale(2, RoundingMode.HALF_UP);

        return JpaUtil.executeInTransaction(em -> {
            List<CartItem> cartItems = cartRepo.findItemsByUserId(em, userId);
            if (cartItems.isEmpty()) {
                throw new BusinessException("Giỏ hàng của bạn đang trống, không thể đặt hàng.");
            }

            List<Long> bookIds = cartItems.stream()
                    .map(ci -> ci.getBook().getBookId())
                    .collect(Collectors.toList());
            Map<Long, Book> lockedBooks = bookRepo.lockBooksOrdered(em, bookIds);

            List<String> problems = new ArrayList<>();
            for (CartItem ci : cartItems) {
                Book locked = lockedBooks.get(ci.getBook().getBookId());
                if (locked == null || !locked.isActive() || !locked.getCategory().isActive()) {
                    problems.add(ci.getBook().getTitle() + " (đã ngừng kinh doanh)");
                } else if (locked.getStockQty() < ci.getQuantity()) {
                    problems.add(locked.getTitle() + " (chỉ còn " + locked.getStockQty() + " cuốn)");
                }
            }
            if (!problems.isEmpty()) {
                throw new OutOfStockException(
                        "Không thể đặt hàng do tồn kho hoặc trạng thái sách đã thay đổi: " + String.join(", ", problems),
                        problems);
            }

            BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            List<OrderItem> orderItems = new ArrayList<>();

            for (CartItem ci : cartItems) {
                Book locked = lockedBooks.get(ci.getBook().getBookId());
                int updated = bookRepo.decrementStockAtomically(em, locked.getBookId(), ci.getQuantity());
                if (updated != 1) {
                    throw new OutOfStockException(
                            "Không đủ tồn kho cho sách \"" + locked.getTitle() + "\". Vui lòng kiểm tra lại giỏ hàng.");
                }
                BigDecimal unitPrice = locked.getPrice().setScale(2, RoundingMode.HALF_UP);
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()))
                        .setScale(2, RoundingMode.HALF_UP);
                subtotal = subtotal.add(lineTotal).setScale(2, RoundingMode.HALF_UP);

                OrderItem oi = new OrderItem();
                oi.setBook(locked);
                oi.setBookTitleSnapshot(locked.getTitle());
                oi.setUnitPrice(unitPrice);
                oi.setQuantity(ci.getQuantity());
                oi.setLineTotal(lineTotal);
                orderItems.add(oi);
            }

            BigDecimal totalAmount = subtotal.add(shippingFee).setScale(2, RoundingMode.HALF_UP);
            String orderCode = generateUniqueCode(em);

            Order order = new Order();
            order.setOrderCode(orderCode);
            order.setUser(em.getReference(User.class, userId));
            order.setRecipientName(name);
            order.setRecipientPhone(phone);
            order.setShippingAddress(address);
            order.setNote(note.isEmpty() ? null : note);
            order.setSubtotal(subtotal);
            order.setShippingFee(shippingFee);
            order.setTotalAmount(totalAmount);
            order.setPaymentMethod(PaymentMethod.COD);
            order.setStatus(OrderStatus.PENDING);

            orderRepo.persist(em, order);
            em.flush();

            for (OrderItem oi : orderItems) {
                oi.setOrder(order);
                em.persist(oi);
            }

            cartRepo.clearItemsByUserId(em, userId);
            em.flush();

            LOGGER.info("Placed COD order code={} orderId={} userId={} total={}",
                    orderCode, order.getOrderId(), userId, totalAmount);
            return order.getOrderId();
        });
    }

    private String generateUniqueCode(jakarta.persistence.EntityManager em) {
        for (int i = 0; i < MAX_CODE_RETRIES; i++) {
            String candidate = OrderCodeGenerator.generate();
            if (!orderRepo.existsByOrderCode(em, candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate unique order_code after retries");
    }

    private static String trim(String v) {
        return v == null ? "" : v.trim();
    }
}
