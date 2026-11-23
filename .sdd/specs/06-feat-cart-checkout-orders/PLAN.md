# PLAN — Giỏ hàng, Thanh toán COD & Vòng đời Đơn hàng (`06-feat-cart-checkout-orders`)

> **Pha 2 · Thiết kế kiến trúc** sau khi SPEC & DESIGN v1.0.0 đã `LOCKED`. Duyệt (Gate 2) cùng `TASKS.md`.

| Trường | Giá trị |
|---|---|
| SPEC version | 1.0.0 |
| Status | `DRAFT` |

## 1. Tóm tắt giải pháp
Tách thành 3 Service chuyên biệt theo trách nhiệm nghiệp vụ: `CartService` (CRUD giỏ hàng theo `currentUserId`, tính giá từ DB), `CheckoutService` (giao dịch COD nguyên tử: khóa dòng sách theo thứ tự `book_id ASC` chống deadlock, tính lại `BigDecimal` từ DB, trừ `stock_qty` atomic, lưu `Orders` + `OrderItems` snapshot, xóa `CartItems` — `ADR-005`, `ADR-008`), và `OrderService` (xem/hủy đơn phía Customer + quản lý/chuyển trạng thái theo `OrderStateMachine` phía Admin). Chống double-cancel bằng `LockModeType.PESSIMISTIC_WRITE` trên `Orders` kết hợp kiểm tra trạng thái hợp lệ ngay trong transaction.

## 2. Phương án đã cân nhắc
| Phương án | Ưu | Nhược | Chọn? |
|---|---|---|---|
| A: Khóa `PESSIMISTIC_WRITE` trên `Books` (sắp theo `book_id ASC` chống deadlock) + conditional stock check trong 1 JPA TX; khóa `PESSIMISTIC_WRITE` trên `Orders` khi cancel | Ngăn tuyệt đối `stock_qty < 0` và hoàn kho 2 lần ngay cả khi 2 thread chạy song song trên SQL Server | Giữ row lock trong thời gian ngắn của transaction checkout/cancel | ✅ |
| B: Đọc `stock_qty` bình thường trong Java rồi gọi `em.merge(book)` không lock | Code đơn giản | Dính race condition mất cập nhật (lost update), có thể gây âm kho hoặc hoàn kho 2 lần (`TC-ORD-06`, `TC-ORD-12`) | ❌ |

## 3. Kiến trúc & luồng xử lý
```text
POST /cart     → CartServlet → CartService (add/update/remove/clear; verify cart.user_id == currentUserId; qty 1..stock) → PRG
GET  /checkout → CheckoutServlet → CheckoutService.previewCheckout(userId) → phát hiện lệch giá/stock (nếu có) → checkout.jsp (SCR-09)
POST /checkout → CheckoutServlet → CheckoutService.placeCodOrder(userId, req):
                 [1 JPA TX]
                 1. Reload CartItems (nếu rỗng → BusinessException)
                 2. Sort bookIds ASC & lock Books (PESSIMISTIC_WRITE)
                 3. Verify isActive=1 & stock_qty >= qty cho mọi item (thiếu → rollback + OutOfStockException)
                 4. Tính unit_price, line_total, subtotal, shipping_fee=30000.00, total_amount bằng BigDecimal
                 5. Generate unique order_code BH-YYYYMMDD-xxxxx (retry nếu trùng) → persist Orders + OrderItems snapshots
                 6. Trừ book.stock_qty -= qty → xóa CartItems → COMMIT
                 → PRG GET /orders/success?id={orderId}

POST /account/orders/cancel → OrderDetailServlet → OrderService.cancelByCustomer(userId, orderId):
                 [1 JPA TX] Lock Order (PESSIMISTIC_WRITE) → verify owner & status IN (PENDING, CONFIRMED)
                 → set CANCELLED + cancelled_at → lock Books ASC & hoàn stock_qty += item.quantity → COMMIT

POST /admin/orders → AdminOrderServlet → OrderService.transitionStatus(orderId, targetStatus):
                 [1 JPA TX] Lock Order → verify OrderStateMachine.canTransition(current, target)
                 → cập nhật status (+ hoàn stock 1 lần nếu chuyển CANCELLED) → COMMIT
```

## 4. Thay đổi theo tầng
| Tầng | File tạo mới | File sửa | Ghi chú |
|---|---|---|---|
| `util/` | `OrderCodeGenerator`, `OrderStateMachine` | — | Thuần Java, dễ unit test 100% nhánh |
| `dto/` | `CartViewDTO`, `CartItemDTO`, `CheckoutRequest`, `CheckoutPreviewDTO`, `OrderSummaryDTO`, `OrderDetailDTO`, `OrderItemSnapshotDTO` | — | Bất biến, không nhận tổng tiền từ client |
| `repository/` | `CartRepository`, `OrderRepository` | `BookRepository` (thêm `findByIdsForUpdate` lock `PESSIMISTIC_WRITE`) | `CartRepository` lọc theo `userId` |
| `service/` | `CartService`, `CheckoutService`, `OrderService` | — | 100% `BigDecimal` scale 2, atomic TX |
| `controller/` | `customer/{CartServlet,CheckoutServlet,OrderSuccessServlet,MyOrdersServlet,OrderDetailServlet,OrderCancelServlet}`, `admin/AdminOrderServlet` | `common/header.jsp` (hiển thị badge đếm giỏ thật từ session/filter) | Tách riêng POST cancel khỏi GET detail |
| `web/views/` | `customer/{cart,checkout,order-success,orders,order-detail,order-stepper}.jsp`, `admin/orders.jsp` | — | Khớp `SCR-08..12b`, `SCR-18` |
| `tests/` | `tests/unit/{OrderStateMachineTest,OrderPricingTest}.java`, `tests/integration/{CartServiceTest,CheckoutConcurrencyTest,OrderLifecycleTest}.java` | — | Gắn `@spec cart-checkout-orders/AC-01..09` |

## 5. Data model & Migration
- Dùng nguyên vẹn `Carts`, `CartItems`, `Books`, `Orders`, `OrderItems` trong `01_schema.sql` (kèm `CK_Orders_total`, `CK_OrderItems_line_total`, `CK_Orders_payment = 'COD'`).

## 6. API / Contract & ảnh hưởng chéo
- Hiện thực hóa `RT-009`..`RT-014`, `RT-021` trong `.sdd/shared_context.md`.
- Cung cấp trạng thái `Orders.status = 'DELIVERED'` làm điều kiện tiền đề cho `07-feat-reviews-qa` và doanh thu/bestseller cho `08-feat-admin-users-reports`.

## 7. Truy vết SPEC → thiết kế
| Yêu cầu | Thành phần đáp ứng | Test dự kiến |
|---|---|---|
| FR-01..03 | `CartServlet` + `CartService` + `CartRepository` | AC-01, AC-02 |
| FR-04..06, EC-01..02, EC-04..05 | `CheckoutServlet` + `CheckoutService` + `OrderCodeGenerator` | AC-03, AC-04 |
| FR-07 | `MyOrdersServlet` + `OrderDetailServlet` + `OrderService` (owner filter) | AC-05 |
| FR-08, FR-09, EC-03 | `OrderCancelServlet` + `OrderService.cancelByCustomer()` (PESSIMISTIC_WRITE) | AC-06, AC-07 |
| FR-10..12 | `AdminOrderServlet` + `OrderService.transitionStatus()` + `OrderStateMachine` | AC-08, AC-09 |

## 8. Chiến lược kiểm thử
- **Unit:** `OrderStateMachine` (toàn bộ 5×5 cặp trạng thái hợp lệ/cấm), tính tiền `BigDecimal` + shipping `30000.00`, format `BH-YYYYMMDD-xxxxx`.
- **Integration & Concurrency (SQL Server test DB):** Đủ `TC-CART-01..06`, `TC-ORD-01..12`, `TC-ADM-06..07` — gồm test 2 thread đồng thời checkout 1 cuốn cuối (`stock_qty` dừng đúng `0`) và 2 thread đồng thời cancel 1 đơn (hoàn kho đúng 1 lần), giả lập lỗi giữa chừng verify rollback toàn phần.
- **Visual:** Đối chiếu `SCR-08`, `SCR-09`, `SCR-18` với 3 ảnh baseline 1280px và kiểm responsive 375px.

## 9. Rủi ro & giảm thiểu
| # | Rủi ro | Xác suất | Tác động | Giảm thiểu / Rollback |
|---|---|---|---|---|
| R1 | Deadlock SQL Server khi 2 checkout khóa các sách theo thứ tự ngược nhau | Trung bình | Cao | Luôn sắp xếp danh sách `bookId` tăng dần trước khi `findById(..., PESSIMISTIC_WRITE)` |
| R2 | Double-click nút `"Đặt hàng"` tạo 2 đơn liên tiếp | Cao | Cao | Giao dịch đầu xóa sạch `CartItems` trong cùng TX khóa dòng → giao dịch thứ 2 thấy cart rỗng và từ chối sạch + UI disable nút + PRG |
| R3 | Lệch làm tròn `BigDecimal` với `CK_OrderItems_line_total` | Thấp | Cao | Luôn chuẩn hóa `setScale(2, RoundingMode.HALF_UP)` trước khi nhân và cộng |

## 10. Rollout & Quan sát
- Log `INFO` tạo đơn (`orderCode`, `userId`, `totalAmount`) và đổi trạng thái đơn (`orderId`, `fromStatus`, `toStatus`, `actorId`). Rollback qua nhánh `feat/cart-checkout-orders`.

## 11. ❓ Câu hỏi cho con người
- Không có câu hỏi mở.

## 12. Phê duyệt (Gate 2)
(chờ duyệt) · —
