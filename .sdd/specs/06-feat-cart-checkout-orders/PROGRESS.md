# PROGRESS — Giỏ hàng, Thanh toán COD & Vòng đời Đơn hàng (`06-feat-cart-checkout-orders`)

| Trường | Giá trị |
|---|---|
| Feature | `06-feat-cart-checkout-orders` · Tier: Full · Nhánh: `feat/bookhub-mvp-01-08` |
| Pha hiện tại | `verifying` (code & 3/3 integration+concurrency tests xanh + live Tomcat verify; chờ Owner duyệt `REVIEW.md`) |
| Task đang làm | `T-008` (đã hoàn tất code, test `AC-01..09` gồm 2-thread concurrency checkout & cancel; chờ Gate 4 `REVIEW.md`) |
| Cập nhật | 2026-11-23 |

## 1. Plan của task hiện tại
- [x] `T-001`: `OrderStateMachine`, `OrderCodeGenerator` (`BH-YYYYMMDD-xxxxx`) & 6 DTO Cart/Checkout/Order
- [x] `T-002`: `CartRepository`, `OrderRepository`, `BookRepository` (`lockBooksOrdered` với `em.refresh(..., PESSIMISTIC_WRITE)`, `decrementStockAtomically`, `incrementStockAtomically`)
- [x] `T-003`: `CartService` (1 Cart/User, gộp số lượng, kiểm tồn kho, tính tiền `BigDecimal` từ DB)
- [x] `T-004`: `CheckoutService` (COD 1 TX nguyên tử, phí ship 30.000 VND, snapshot `OrderItems`, trừ kho atomic, xóa `CartItems`, chống oversell đồng thời)
- [x] `T-005`: `OrderService` (Customer list/detail ownership check, cancel khóa `Order` hoàn kho đúng 1 lần, Admin chuyển trạng thái theo `OrderStateMachine`)
- [x] `T-006`: `CartServlet`, `CheckoutServlet`, `OrderSuccessServlet` + `customer/{cart,checkout,order-success}.jsp` (`SCR-08..10`)
- [x] `T-007`: `MyOrdersServlet`, `OrderDetailServlet`, `OrderCancelServlet` + `customer/{orders,order-detail}.jsp` (`SCR-11..12b`)
- [ ] `T-008`: `AdminOrderServlet` + `admin/orders.jsp` (`SCR-18`), nối `cartCount` Header & 3/3 tests xanh — **chờ Owner duyệt `REVIEW.md` Gate 4**

## 2. Nhật ký thực thi
| Giờ | Việc đã làm | Kết quả |
|---|---|---|
| 2026-11-23 | Triển khai `T-001`..`T-008` & `CartCheckoutOrdersIntegrationTest` (gồm test 2 luồng tranh 1 cuốn cuối và 2 luồng hủy cùng đơn) | Phát hiện & khắc phục triệt để lỗi stale L1 cache bằng `em.refresh(..., PESSIMISTIC_WRITE)` + conditional `UPDATE` atomic; commit `5102566` |
| 2026-11-23 | Sửa `${cart.empty}` / `${pageResult.empty}` & verify live Tomcat 10.1 | `/cart`, `/checkout`, `/account/orders`, `/account/orders/detail?id=1`, `/admin/orders` đều `200 OK` |

## 3. Kiểm tra
Test ✅ (3/3 JUnit 5 `CartCheckoutOrdersIntegrationTest` kèm 2-thread concurrency) · Build ✅ (`dist/BookHub.war`, 0 warning) · ui-check ✅ · spec-check ✅ · Secret scan ✅

## 4. Blockers / câu hỏi cho người
- ⬜ Chờ Owner review & duyệt `.sdd/specs/06-feat-cart-checkout-orders/REVIEW.md` (Gate 4).

## 5. Quyết định & giả định
- Dùng chiến lược phòng thủ 2 lớp cho tồn kho: `em.refresh(book, LockModeType.PESSIMISTIC_WRITE)` theo `bookId ASC` kết hợp conditional SQL `UPDATE Book SET stockQty = stockQty - :qty WHERE ... AND stockQty >= :qty` (bắt buộc `updated == 1`).

## 6. Handoff
- **Đã xong:** Toàn bộ luồng Cart, Checkout COD và vòng đời đơn hàng (`RT-009`..`RT-014`, `RT-021`).
- **Làm tiếp:** Sau khi Owner duyệt `REVIEW.md` → đánh `APPROVED` và đóng `T-008`.
