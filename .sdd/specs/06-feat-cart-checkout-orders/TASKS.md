# TASKS — Giỏ hàng, Thanh toán COD & Vòng đời Đơn hàng (`06-feat-cart-checkout-orders`)

> **Pha 3 · Phân rã từ `PLAN.md`.** Task atomic ≤ 4h · gắn thẻ `@spec cart-checkout-orders/AC-xx`.

**Trạng thái:** `⬜ todo` · `🔄 doing` · `✅ done` · `⛔ blocked`

| ID | Task | Phụ thuộc | Ước lượng | Truy vết | Trạng thái |
|---|---|---|---|---|---|
| T-001 | Tạo `OrderStateMachine`, `OrderCodeGenerator` & bộ DTO Cart/Checkout/Order + unit tests | — | 2.5h | FR-05, FR-11..12, AC-08..09 | ⬜ |
| T-002 | Tạo `CartRepository`, `OrderRepository`, bổ sung lock `findByIdsForUpdate` trong `BookRepository` | T-001 | 3h | FR-01..12 | ⬜ |
| T-003 | Tạo `CartService` + integration tests (`AC-01..02`) | T-002 | 2.5h | FR-01..03, AC-01..02 | ⬜ |
| T-004 | Tạo `CheckoutService` (atomic TX, lock sách theo ID tăng dần, snapshot, xóa cart) + concurrency/rollback tests (`AC-03..04`) | T-002 | 4h | FR-04..06, EC-01..02, EC-04..05, AC-03..04 | ⬜ |
| T-005 | Tạo `OrderService` (Customer list/detail/cancel hoàn kho 1 lần + Admin state transition) + concurrency tests (`AC-05..09`) | T-002 | 3.5h | FR-07..12, EC-03, AC-05..09 | ⬜ |
| T-006 | Tạo `CartServlet`, `CheckoutServlet`, `OrderSuccessServlet` + `cart.jsp`, `checkout.jsp`, `order-success.jsp` (`SCR-08..10`) | T-003, T-004 | 3.5h | FR-01..06, AC-01..04 | ⬜ |
| T-007 | Tạo `MyOrdersServlet`, `OrderDetailServlet`, `OrderCancelServlet` + `orders.jsp`, `order-detail.jsp`, `order-stepper.jsp` (`SCR-11..12b`) | T-005 | 3h | FR-07..09, AC-05..07 | ⬜ |
| T-008 | Tạo `AdminOrderServlet` + `views/admin/orders.jsp` (`SCR-18`) + nối badge đếm giỏ Header + kiểm trực quan 375/1280 | T-005, T-006 | 3h | FR-10..12, AC-08..09, VAC-01..02 | ⬜ |

## Chi tiết task

### T-001 — `OrderStateMachine`, `OrderCodeGenerator` & DTOs
- **Mục tiêu:** Đóng gói toàn bộ luật chuyển trạng thái 5 bước và sinh mã đơn `BH-YYYYMMDD-xxxxx` trong lớp thuần Java để unit test phủ 100%.
- **File dự kiến:** `src/java/com/bookhub/util/{OrderStateMachine,OrderCodeGenerator}.java`, `src/java/com/bookhub/dto/{Cart*,Checkout*,Order*}.java`, `tests/unit/{OrderStateMachineTest,OrderPricingTest}.java`.
- **Acceptance:**
  - [ ] Mọi chuyển trạng thái hợp lệ trả `true`, mọi chuyển ngược/nhảy cóc trả `false` (`TC-ADM-06`, `TC-ADM-07`).

### T-002 & T-003 — Repositories & `CartService`
- **Mục tiêu:** Quản lý 1 Cart/User, 1 dòng/Book, validate ownership & tồn kho, tính tạm tính từ bảng `Books`.
- **File dự kiến:** `repository/{CartRepository,OrderRepository,BookRepository}.java`, `service/CartService.java`, `tests/integration/CartServiceTest.java`.
- **Acceptance:**
  - [ ] Pass `@spec cart-checkout-orders/AC-01`, `AC-02` (`TC-CART-01..06`).

### T-004 — `CheckoutService` (Atomic Transaction & Concurrency)
- **Mục tiêu:** Thực thi checkout COD trong 1 transaction khóa `Books` (`PESSIMISTIC_WRITE` theo `bookId ASC`), bỏ qua giá từ client, lưu snapshot `OrderItems`, trừ kho atomic, retry mã đơn trùng và xóa `CartItems`.
- **File dự kiến:** `service/CheckoutService.java`, `tests/integration/CheckoutConcurrencyTest.java`.
- **Acceptance:**
  - [ ] Pass `@spec cart-checkout-orders/AC-03`, `AC-04` (`TC-ORD-01..06`, kể cả test 2 thread tranh 1 cuốn cuối và test rollback lỗi mô phỏng).
- **Không làm:** Không tin `subtotal`/`totalAmount` từ request.

### T-005 — `OrderService` (Customer Cancel & Admin State Machine)
- **Mục tiêu:** Kiểm tra ownership (`user_id = currentUser.id`), hủy đơn `PENDING`/`CONFIRMED` khóa `Order` để hoàn kho đúng 1 lần và chuyển trạng thái phía Admin.
- **File dự kiến:** `service/OrderService.java`, `tests/integration/OrderLifecycleTest.java`.
- **Acceptance:**
  - [ ] Pass `@spec cart-checkout-orders/AC-05`..`AC-09` (`TC-ORD-07..12`, `TC-ADM-06..07`).

### T-006, T-007, T-008 — Servlets & JSP Views (`SCR-08..12b`, `SCR-18`)
- **Mục tiêu:** Hoàn thiện toàn bộ 7 màn hình Cart/Checkout/Orders phía Customer & Admin (PRG, CSRF, `OrderStepper`, `ConfirmModal` hủy đơn, cảnh báo đổi giá/stock, panel state machine Admin), kiểm ảnh 375/1280 so với baseline và chạy `scripts/verify.ps1`.
- **File dự kiến:** `controller/customer/*.java`, `controller/admin/AdminOrderServlet.java`, `web/WEB-INF/views/customer/*.jsp`, `web/WEB-INF/views/admin/orders.jsp`.
- **Acceptance:**
  - [ ] Đạt `VAC-01..02`; không ô voucher hay cổng thanh toán online; `scripts/verify.ps1` xanh.

## Đồ thị phụ thuộc
```text
T-001 ──► T-002 ─┬─► T-003 ──┬─► T-006 ──┬─► T-008
                 ├─► T-004 ──┘           │
                 └─► T-005 ──┬─► T-007   │
                             └───────────┘
```

## Phê duyệt (Gate 3)
(chờ duyệt) · —
