package com.bookhub.service;

import com.bookhub.config.JpaUtil;
import com.bookhub.dto.CartViewDTO;
import com.bookhub.dto.CheckoutRequest;
import com.bookhub.dto.OrderDetailDTO;
import com.bookhub.dto.SessionUser;
import com.bookhub.entity.Book;
import com.bookhub.entity.OrderStatus;
import com.bookhub.entity.PaymentMethod;
import com.bookhub.exception.AccessDeniedException;
import com.bookhub.exception.BusinessException;
import com.bookhub.exception.OutOfStockException;
import com.bookhub.util.OrderStateMachine;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration & concurrency tests for 06-feat-cart-checkout-orders (AC-01..AC-09).
 */
class CartCheckoutOrdersIntegrationTest {

    private final AuthService authService = new AuthService();
    private final CartService cartService = new CartService();
    private final CheckoutService checkoutService = new CheckoutService();
    private final OrderService orderService = new OrderService();

    @BeforeAll
    static void init() {
        JpaUtil.init();
    }

    @AfterAll
    static void close() {
        JpaUtil.shutdown();
    }

    // @spec cart-checkout-orders/AC-01
    // @spec cart-checkout-orders/AC-02
    @Test
    void cartMergeQuantitiesOwnershipCheckAndValidation() {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        Long userA = null;
        Long userB = null;
        try {
            userA = authService.register("Cart A", "carta." + marker + "@bookhub.vn", null, "Pass1234", "Pass1234").getUserId();
            userB = authService.register("Cart B", "cartb." + marker + "@bookhub.vn", null, "Pass1234", "Pass1234").getUserId();

            Book book = JpaUtil.executeReadOnly(em -> em.createQuery(
                            "SELECT b FROM Book b WHERE b.active = true AND b.category.active = true AND b.stockQty >= 10",
                            Book.class).setMaxResults(1).getSingleResult());

            // AC-01: add 2 then add 3 merges into 1 row with quantity 5
            assertEquals(2, cartService.addToCart(userA, book.getBookId(), 2));
            assertEquals(5, cartService.addToCart(userA, book.getBookId(), 3));
            CartViewDTO cart = cartService.getCartView(userA);
            assertEquals(1, cart.getItems().size());
            assertEquals(5, cart.getItems().get(0).getQuantity());
            assertEquals(book.getPrice().multiply(BigDecimal.valueOf(5)), cart.getSubtotal());

            // AC-02: reject qty <= 0, qty > stock, and cross-user tampering (IDOR)
            Long ciId = cart.getItems().get(0).getCartItemId();
            Long finalA = userA;
            Long finalB = userB;
            assertThrows(BusinessException.class, () -> cartService.updateItemQuantity(finalA, ciId, 0));
            assertThrows(OutOfStockException.class, () -> cartService.updateItemQuantity(finalA, ciId, book.getStockQty() + 5));
            assertThrows(AccessDeniedException.class, () -> cartService.updateItemQuantity(finalB, ciId, 1));
            assertThrows(AccessDeniedException.class, () -> cartService.removeItem(finalB, ciId));

            // Owner removes item and clears cleanly
            assertEquals(0, cartService.removeItem(userA, ciId));
            assertTrue(cartService.getCartView(userA).isEmpty());
        } finally {
            cleanupTestUsersAndOrders(userA, userB);
        }
    }

    // @spec cart-checkout-orders/AC-03
    // @spec cart-checkout-orders/AC-04
    @Test
    void atomicCodCheckoutAndConcurrentLastCopyRacePrevention() throws Exception {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        Long userA = null;
        Long userB = null;
        Long tempBookId = null;

        try {
            userA = authService.register("Buyer A", "buyera." + marker + "@bookhub.vn", null, "Pass1234", "Pass1234").getUserId();
            userB = authService.register("Buyer B", "buyerb." + marker + "@bookhub.vn", null, "Pass1234", "Pass1234").getUserId();
            tempBookId = createTempBook("Race Book " + marker, new BigDecimal("100000.00"), 3);

            // AC-03: normal COD checkout computes server price + 30,000 shipping, deducts stock, clears cart
            cartService.addToCart(userA, tempBookId, 2);
            CheckoutRequest reqA = new CheckoutRequest("Người Nhận A", "0912345678", "123 Cầu Giấy, Hà Nội", "Giao nhanh");
            Long orderIdA = checkoutService.placeCodOrder(userA, reqA);

            OrderDetailDTO orderA = orderService.getCustomerOrderDetail(userA, orderIdA);
            assertEquals(OrderStatus.PENDING, orderA.getStatus());
            assertEquals(PaymentMethod.COD, orderA.getPaymentMethod());
            assertEquals(new BigDecimal("200000.00"), orderA.getSubtotal());
            assertEquals(new BigDecimal("30000.00"), orderA.getShippingFee());
            assertEquals(new BigDecimal("230000.00"), orderA.getTotalAmount());
            assertTrue(orderA.getOrderCode().startsWith("BH-"));
            assertTrue(cartService.getCartView(userA).isEmpty());
            assertEquals(1, readBookStock(tempBookId), "Stock should drop from 3 to 1");

            // AC-04: empty cart rejected
            Long finalA = userA;
            assertThrows(BusinessException.class, () -> checkoutService.placeCodOrder(finalA, reqA));

            // AC-04: 2 concurrent buyers race for the LAST 1 copy -> exactly 1 succeeds, stock == 0 (never negative)
            cartService.addToCart(userA, tempBookId, 1);
            cartService.addToCart(userB, tempBookId, 1);

            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService pool = Executors.newFixedThreadPool(2);
            Long finalB = userB;
            Callable<Boolean> taskA = () -> {
                ready.countDown();
                start.await();
                try {
                    checkoutService.placeCodOrder(finalA, reqA);
                    return true;
                } catch (OutOfStockException ex) {
                    return false;
                }
            };
            Callable<Boolean> taskB = () -> {
                ready.countDown();
                start.await();
                try {
                    checkoutService.placeCodOrder(finalB, new CheckoutRequest("Người Nhận B", "0988777666", "456 Ba Đình", null));
                    return true;
                } catch (OutOfStockException ex) {
                    return false;
                }
            };
            Future<Boolean> f1 = pool.submit(taskA);
            Future<Boolean> f2 = pool.submit(taskB);
            ready.await();
            start.countDown();
            int winners = (f1.get() ? 1 : 0) + (f2.get() ? 1 : 0);
            pool.shutdown();

            assertEquals(1, winners, "Exactly one concurrent checkout must succeed for the last copy");
            assertEquals(0, readBookStock(tempBookId), "Stock must never go negative");
        } finally {
            cleanupTestUsersAndOrders(userA, userB);
            deleteTempBook(tempBookId);
        }
    }

    // @spec cart-checkout-orders/AC-05
    // @spec cart-checkout-orders/AC-06
    // @spec cart-checkout-orders/AC-07
    // @spec cart-checkout-orders/AC-08
    // @spec cart-checkout-orders/AC-09
    @Test
    void orderOwnershipConcurrentCancelSingleRefundAndAdminStateMachine() throws Exception {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        Long userA = null;
        Long userB = null;
        Long tempBookId = null;

        try {
            userA = authService.register("Order A", "ordera." + marker + "@bookhub.vn", null, "Pass1234", "Pass1234").getUserId();
            userB = authService.register("Order B", "orderb." + marker + "@bookhub.vn", null, "Pass1234", "Pass1234").getUserId();
            tempBookId = createTempBook("Lifecycle Book " + marker, new BigDecimal("80000.00"), 10);

            // Place order 1 (qty 3 -> stock becomes 7)
            cartService.addToCart(userA, tempBookId, 3);
            Long order1 = checkoutService.placeCodOrder(userA, new CheckoutRequest("Khách A", "0901222333", "Hà Nội", null));
            assertEquals(7, readBookStock(tempBookId));

            // AC-05: User B cannot view or cancel User A's order
            Long finalB = userB;
            assertThrows(AccessDeniedException.class, () -> orderService.getCustomerOrderDetail(finalB, order1));
            assertThrows(AccessDeniedException.class, () -> orderService.cancelByCustomer(finalB, order1));

            // AC-06: 2 concurrent cancel calls on order1 restore stock EXACTLY ONCE (7 -> 10, never 13)
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService pool = Executors.newFixedThreadPool(2);
            Long finalA = userA;
            List<Future<Boolean>> futures = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        orderService.cancelByCustomer(finalA, order1);
                        return true;
                    } catch (BusinessException ex) {
                        return false;
                    }
                }));
            }
            ready.await();
            start.countDown();
            int cancelSuccess = (futures.get(0).get() ? 1 : 0) + (futures.get(1).get() ? 1 : 0);
            pool.shutdown();

            assertEquals(1, cancelSuccess, "Duplicate concurrent cancel must succeed exactly once");
            assertEquals(10, readBookStock(tempBookId), "Stock must be restored exactly once");
            assertEquals(OrderStatus.CANCELLED, orderService.getCustomerOrderDetail(userA, order1).getStatus());

            // AC-08 & AC-09 & AC-07: Place order 2, walk full admin state machine, reject invalid skips & customer cancel on SHIPPING/DELIVERED
            cartService.addToCart(userA, tempBookId, 2);
            Long order2 = checkoutService.placeCodOrder(userA, new CheckoutRequest("Khách A", "0901222333", "Hà Nội", null));
            assertEquals(8, readBookStock(tempBookId));

            // AC-09: Admin cannot jump PENDING -> DELIVERED
            assertFalse(OrderStateMachine.canTransition(OrderStatus.PENDING, OrderStatus.DELIVERED));
            assertThrows(BusinessException.class, () -> orderService.transitionByAdmin(1L, order2, OrderStatus.DELIVERED));

            // AC-08: Valid progression PENDING -> CONFIRMED -> SHIPPING -> DELIVERED
            orderService.transitionByAdmin(1L, order2, OrderStatus.CONFIRMED);
            orderService.transitionByAdmin(1L, order2, OrderStatus.SHIPPING);

            // AC-07: Customer cannot cancel SHIPPING order
            assertThrows(BusinessException.class, () -> orderService.cancelByCustomer(finalA, order2));

            orderService.transitionByAdmin(1L, order2, OrderStatus.DELIVERED);
            assertEquals(OrderStatus.DELIVERED, orderService.getAdminOrderDetail(order2).getStatus());

            // AC-09: Terminal DELIVERED order cannot be cancelled by admin or customer
            assertThrows(BusinessException.class, () -> orderService.transitionByAdmin(1L, order2, OrderStatus.CANCELLED));
            assertThrows(BusinessException.class, () -> orderService.cancelByCustomer(finalA, order2));
            assertEquals(8, readBookStock(tempBookId));
        } finally {
            cleanupTestUsersAndOrders(userA, userB);
            deleteTempBook(tempBookId);
        }
    }

    private Long createTempBook(String title, BigDecimal price, int stock) {
        return JpaUtil.executeInTransaction(em -> {
            com.bookhub.entity.Category cat = em.createQuery(
                    "SELECT c FROM Category c WHERE c.active = true", com.bookhub.entity.Category.class)
                    .setMaxResults(1).getSingleResult();
            Book b = new Book();
            b.setCategory(cat);
            b.setTitle(title);
            b.setAuthor("Test Author");
            b.setPrice(price);
            b.setStockQty(stock);
            b.setActive(true);
            em.persist(b);
            em.flush();
            return b.getBookId();
        });
    }

    private int readBookStock(Long bookId) {
        return JpaUtil.executeReadOnly(em -> em.find(Book.class, bookId).getStockQty());
    }

    private void deleteTempBook(Long bookId) {
        if (bookId == null) return;
        JpaUtil.executeInTransactionVoid(em ->
                em.createQuery("DELETE FROM Book b WHERE b.bookId = :id").setParameter("id", bookId).executeUpdate());
    }

    private void cleanupTestUsersAndOrders(Long... userIds) {
        for (Long uid : userIds) {
            if (uid == null) continue;
            JpaUtil.executeInTransactionVoid(em -> {
                em.createQuery("DELETE FROM OrderItem oi WHERE oi.order.orderId IN (SELECT o.orderId FROM Order o WHERE o.user.userId = :u)")
                        .setParameter("u", uid).executeUpdate();
                em.createQuery("DELETE FROM Order o WHERE o.user.userId = :u").setParameter("u", uid).executeUpdate();
                em.createQuery("DELETE FROM CartItem ci WHERE ci.cart.cartId IN (SELECT c.cartId FROM Cart c WHERE c.user.userId = :u)")
                        .setParameter("u", uid).executeUpdate();
                em.createQuery("DELETE FROM Cart c WHERE c.user.userId = :u").setParameter("u", uid).executeUpdate();
                em.createQuery("DELETE FROM User u WHERE u.userId = :u").setParameter("u", uid).executeUpdate();
            });
        }
    }
}
