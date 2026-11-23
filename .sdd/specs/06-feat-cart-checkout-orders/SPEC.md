# SPEC — Giỏ hàng, Thanh toán COD & Vòng đời Đơn hàng (cart-checkout-orders)

> **Pha 1 · Hợp đồng đặc tả.** Agent soạn nháp từ `CONTEXT.md`, **người duyệt & khóa** (Gate 1).
> Sau khi `Status: LOCKED`, sửa đổi chỉ qua `CHANGELOG.md` + người duyệt. **Chưa LOCKED ⇒ cấm code.**

| Trường | Giá trị |
|---|---|
| Feature ID | `06-feat-cart-checkout-orders` |
| Tier | Full |
| UI | Có |
| Status | `LOCKED` |
| Version | 1.0.0 |
| Owner | BookHub Team (PRJ301) |
| Liên quan | `FR-CART-001..010`, `FR-ORD-001..015`, `FR-ADM-005..007`, `BR-CART`, `BR-ORD`, `ADR-005`, `ADR-008`, `shared_context.md` (`RT-009..014`, `RT-021`), `TC-CART-01..06`, `TC-ORD-01..12`, `TC-ADM-06..07` |

---

## 1. Tổng quan (Overview)
Xây dựng trọn vẹn luồng mua hàng của BookHub gồm Giỏ hàng bền vững theo tài khoản Customer (`/cart`), Thanh toán COD nguyên tử (`/checkout` + `/orders/success`), Lịch sử/Chi tiết & Hủy đơn phía Customer (`/account/orders*`), và Quản lý/Cập nhật trạng thái đơn phía Admin (`/admin/orders`). Trọng tâm của feature là tính toán tiền tại server bằng `BigDecimal`, trừ/hoàn tồn kho an toàn dưới truy cập đồng thời trong một JPA transaction và bảo toàn snapshot đơn hàng bất biến. Xem `CONTEXT.md`.

## 2. Actors & User Stories
| ID | Câu chuyện |
|---|---|
| US-01 | Là **Customer**, tôi muốn thêm sách vào giỏ, cập nhật số lượng, xóa từng dòng hoặc xóa cả giỏ và thấy tạm tính chính xác theo giá hiện tại. |
| US-02 | Là **Customer**, tôi muốn đặt hàng COD bằng cách nhập thông tin người nhận, xem rõ phí ship 30.000 ₫ và nhận mã đơn xác nhận. |
| US-03 | Là **Customer**, tôi muốn xem danh sách/chi tiết đơn hàng của riêng mình và hủy đơn khi chưa sang trạng thái giao vận. |
| US-04 | Là **Admin**, tôi muốn tìm kiếm/lọc mọi đơn hàng, xem snapshot chi tiết và cập nhật trạng thái đơn theo đúng luồng xử lý. |

## 3. Functional Requirements (EARS)
| ID | Loại | Mệnh đề | Liên kết (US / BR) |
|---|---|---|---|
| FR-01 | Event | WHEN an authenticated `CUSTOMER` submits `POST /cart` (`action=add`) with an active, in-stock `bookId` and positive integer `quantity`, the system SHALL create a single `Carts` row for the user if none exists, insert a `CartItems` row or increment the existing row's quantity (never exceeding `book.stock_qty`), and PRG-redirect with updated cart count. | US-01, FR-CART-001..002, FR-CART-010, BR-CART-001..004 |
| FR-02 | Event | WHEN an authenticated `CUSTOMER` updates quantity (`1 .. book.stock_qty`), removes one `CartItem`, or clears the cart via `POST /cart`, the system SHALL verify ownership (`cart.user_id == currentUser.id`), apply the change via JPA, and recalculate line totals and subtotal strictly from current `Books.price` in DB. | US-01, FR-CART-003..008, BR-CART-005..008 |
| FR-03 | Unwanted | IF a `CUSTOMER` attempts to add an inactive/out-of-stock book, sets `quantity <= 0` or `quantity > book.stock_qty`, OR tampers with another user's `cartItemId`, THEN the system SHALL reject the operation with a clear validation error or HTTP `403`/`404` without altering unauthorized records. | US-01, FR-CART-008, TC-CART-04, TC-CART-06 |
| FR-04 | Event | WHEN a `CUSTOMER` opens `GET /checkout`, the system SHALL reload cart items from DB, validate that the cart is non-empty and all books are active with sufficient stock, compute `subtotal`, fixed `shipping_fee = 30000.00` VND, and `total_amount = subtotal + shipping_fee` using `BigDecimal`, and display `SCR-09` (flagging any price/stock discrepancy notice if detected). | US-02, FR-ORD-001, FR-ORD-003..005 |
| FR-05 | Event | WHEN a `CUSTOMER` submits `POST /checkout` with valid `recipientName` (≤ 100), `recipientPhone` (≤ 20), `shippingAddress` (≤ 500), optional `note` (≤ 500), and valid CSRF token, the system SHALL execute **one atomic JPA transaction** that: (1) reloads cart items & locks/checks `Books` rows, (2) recalculates prices strictly from DB ignoring any client totals, (3) deducts `Books.stock_qty` atomically (`stock_qty >= requestedQty`), (4) creates `Orders` (`status=PENDING`, `payment_method=COD`, unique `order_code` format `BH-YYYYMMDD-xxxxx` with retry on collision) and immutable `OrderItems` snapshots (`book_title_snapshot`, `unit_price`, `line_total`), (5) deletes `CartItems`, and (6) commits and PRG-redirects to `GET /orders/success?id={orderId}`. | US-02, FR-ORD-002..010, BR-ORD-001..011, ADR-005, ADR-008 |
| FR-06 | Unwanted | IF during `POST /checkout` the cart is empty, any book is inactive, any book has insufficient stock (`stock_qty < quantity`), recipient fields fail server validation, OR any DB write fails mid-transaction, THEN the system SHALL roll back the entire transaction (0 partial orders, 0 negative stock, cart preserved) and display an actionable error message. | US-02, FR-ORD-008..009, BR-ORD-010..011, TC-ORD-02..06 |
| FR-07 | Event | WHEN a `CUSTOMER` requests `GET /account/orders` (with optional status filter & pagination) or `GET /account/orders/detail?id={id}`, the system SHALL query ONLY orders where `Orders.user_id = currentUser.id`, returning `403`/`404` for any other user's order ID. | US-03, FR-ORD-011..012, BR-ORD-012, TC-ORD-07..08 |
| FR-08 | Event | WHEN a `CUSTOMER` submits `POST /account/orders/cancel` for an order owned by `currentUser.id` whose locked DB status is `PENDING` or `CONFIRMED`, the system SHALL transition `status` to `CANCELLED`, set `cancelled_at = now`, and restore `Books.stock_qty` for every `OrderItem` **exactly once** within a single transaction. | US-03, FR-ORD-013, BR-ORD-013..014, TC-ORD-09..10, TC-ORD-12 |
| FR-09 | Unwanted | IF a `CUSTOMER` attempts to cancel an order belonging to another user OR an order already in `SHIPPING`, `DELIVERED`, or `CANCELLED` status (including duplicate concurrent cancel clicks), THEN the system SHALL reject the transition without restoring stock a second time. | US-03, BR-ORD-014..015, TC-ORD-11..12 |
| FR-10 | Event | WHEN an `ADMIN` requests `GET /admin/orders` with optional `orderCode`, `status`, `fromDate`, `toDate`, and `page`, the system SHALL display a DB-paginated list of all orders and an order detail view showing immutable recipient & `OrderItems` snapshots. | US-04, FR-ADM-005..006, BR-ADM-004 |
| FR-11 | Event | WHEN an `ADMIN` submits `POST /admin/orders` to transition an order status, the system SHALL enforce the Order State Machine (`PENDING → CONFIRMED | CANCELLED`, `CONFIRMED → SHIPPING | CANCELLED`, `SHIPPING → DELIVERED`), restoring stock exactly once if transitioning to `CANCELLED`, and update `updated_at`. | US-04, FR-ORD-014, FR-ADM-007, BR-ADM-005, TC-ADM-06 |
| FR-12 | Unwanted | IF an `ADMIN` attempts an invalid transition (e.g. `PENDING → DELIVERED`, `SHIPPING → CONFIRMED`, or modifying terminal `DELIVERED`/`CANCELLED` orders or `OrderItems` snapshots), THEN the system SHALL reject the request with a business error without mutating the order. | US-04, BR-ORD-016, BR-ADM-004..005, TC-ADM-07 |

## 4. Non-Functional Requirements
| ID | Hạng mục | Yêu cầu (số cụ thể) |
|---|---|---|
| NFR-01 | Tính nguyên tử & đồng thời | Checkout & Cancel chạy trong transaction với pessimistic lock (`LockModeType.PESSIMISTIC_WRITE`) hoặc conditional update có kiểm tra `updatedRows == 1`, đảm bảo `stock_qty >= 0` và hoàn kho đúng 1 lần dưới 2 request đồng thời. |
| NFR-02 | Chính xác tài chính | 100% tính toán bằng `BigDecimal` (`scale = 2`, `RoundingMode.HALF_UP`); thỏa ràng buộc DB `total_amount = subtotal + shipping_fee` và `line_total = unit_price * quantity`. |
| NFR-03 | Chống gửi trùng & IDOR | PRG sau mọi POST; chặn double-submit kết hợp guard kiểm tra cart rỗng/khóa dòng tại Service; mọi query Customer đều ràng buộc `user_id = currentUser.id`. |

## 5. Data & API Contract
**Bảng thao tác:** `Carts`, `CartItems`, `Books`, `Orders`, `OrderItems`.

**Servlet Routes (`shared_context.md` `RT-009..014`, `RT-021`):**
| Method + Path | Auth | Request Params | Response / Redirect | Lỗi |
|---|---|---|---|---|
| `GET /cart` | Customer | — | Render `views/customer/cart.jsp` | `302 /login` nếu Guest |
| `POST /cart` | Customer + CSRF | `action=add\|update\|remove\|clear`, `bookId?`, `cartItemId?`, `quantity?` | PRG `302 /cart` (hoặc trang trước) | Flash lỗi tồn kho / `403` |
| `GET /checkout` | Customer | — | Render `views/customer/checkout.jsp` | Cart rỗng → `302 /cart` + flash |
| `POST /checkout` | Customer + CSRF | `recipientName`, `recipientPhone`, `shippingAddress`, `note?` | PRG `302 /orders/success?id={id}` | Forward `checkout.jsp` + lỗi |
| `GET /orders/success` | Customer | `id` | Render `views/customer/order-success.jsp` | `403`/`404` nếu không phải chủ đơn |
| `GET /account/orders` | Customer | `status?`, `page?` | Render `views/customer/orders.jsp` | `302 /login` |
| `GET /account/orders/detail` | Customer | `id` | Render `views/customer/order-detail.jsp` | `403`/`404` |
| `POST /account/orders/cancel` | Customer + CSRF | `orderId` | PRG `302 /account/orders/detail?id={id}` | Flash lỗi trạng thái / `403` |
| `GET/POST /admin/orders` | Admin + CSRF | Filter params hoặc `orderId`, `targetStatus` | Render/PRG `views/admin/orders.jsp` | Flash lỗi state machine / `403` |

## 6. Edge Cases & Xử lý lỗi
| ID | Tình huống | Hành vi mong đợi |
|---|---|---|
| EC-01 | Hai Customer đồng thời checkout cuốn sách chỉ còn 1 bản | Khóa/cập nhật có điều kiện đảm bảo đúng 1 đơn thành công (`stock_qty = 0`), đơn thứ 2 rollback sạch với `OutOfStockException` (`TC-ORD-04`, `TC-ORD-06`). |
| EC-02 | Client chỉnh DOM/DevTools gửi `totalAmount=1000` hoặc `unitPrice=1` lên `POST /checkout` | Server bỏ qua hoàn toàn tham số giá/tổng tiền từ client, tính lại 100% từ bảng `Books` (`TC-ORD-03`). |
| EC-03 | Customer bấm `Hủy đơn` 2 lần liên tiếp rất nhanh (concurrent requests) | Khóa bản ghi `Order` trong transaction; request thứ 1 chuyển `CANCELLED` & hoàn kho, request thứ 2 thấy status đã `CANCELLED` và từ chối không hoàn kho lại (`TC-ORD-12`). |
| EC-04 | Sinh `order_code` bị trùng unique trong cùng ngày | Service bắt xung đột và tự động sinh lại `order_code` khác (tối đa N lần thử) trước khi commit (`BR-ORD-017`). |
| EC-05 | Sách trong giỏ bị Admin giảm tồn kho hoặc ngừng kinh doanh trước lúc checkout | Checkout từ chối tạo đơn, báo rõ cuốn sách nào bị ảnh hưởng và giữ giỏ để khách điều chỉnh. |

## 7. Acceptance Criteria (Given / When / Then)
> Mỗi FR xuất hiện ở cột "Kiểm chứng FR" của ≥ 1 AC. Test gắn thẻ `@spec cart-checkout-orders/AC-xx`.

| ID | Kịch bản | Kiểm chứng FR |
|---|---|---|
| AC-01 | **Given** Customer đăng nhập và sách còn 10 bản **When** thêm sách đó 2 lần (`qty=2` rồi `qty=3`) qua `POST /cart` **Then** tồn tại đúng 1 dòng `CartItems` với `quantity=5` và tạm tính bằng `5 × Books.price` (`TC-CART-01`, `TC-CART-02`, `TC-CART-03`). | FR-01, FR-02 |
| AC-02 | **Given** Customer A sở hữu `CartItem` **When** Customer A xóa dòng/xóa giỏ thì thành công; khi đặt `quantity=0`, vượt tồn kho, hoặc Customer B cố sửa `cartItemId` của A **Then** server từ chối và DB không bị thay đổi trái phép (`TC-CART-04`, `TC-CART-05`, `TC-CART-06`). | FR-02, FR-03 |
| AC-03 | **Given** Customer có giỏ hợp lệ **When** mở `GET /checkout` và gửi `POST /checkout` với thông tin nhận hàng hợp lệ (kể cả kèm param `totalAmount=1` giả mạo) **Then** tạo 1 `Order` (`PENDING`, `COD`, `shipping_fee=30000`, `total_amount` đúng giá DB), tạo `OrderItems` snapshot, giảm `Books.stock_qty`, xóa `CartItems` và redirect `/orders/success` (`TC-ORD-01`, `TC-ORD-03`). | FR-04, FR-05, EC-02, EC-04 |
| AC-04 | **Given** giỏ rỗng, tồn kho không đủ, hoặc lỗi ghi xảy ra giữa transaction checkout **When** gửi `POST /checkout` (kể cả 2 request đồng thời tranh 1 cuốn cuối) **Then** giao dịch không đủ kho rollback hoàn toàn, `stock_qty` không bao giờ `< 0` và không tạo đơn dở dang (`TC-ORD-02`, `TC-ORD-04`, `TC-ORD-05`, `TC-ORD-06`). | FR-06, EC-01, EC-05 |
| AC-05 | **Given** Customer A và Customer B đều có đơn hàng **When** Customer A xem `/account/orders` hoặc thử mở `/account/orders/detail?id={orderB}` **Then** chỉ thấy đơn của A và bị chặn `403`/`404` khi truy cập đơn của B (`TC-ORD-07`, `TC-ORD-08`). | FR-07 |
| AC-06 | **Given** đơn của Customer đang ở `PENDING` hoặc `CONFIRMED` **When** `POST /account/orders/cancel` (kể cả gửi 2 lần đồng thời) **Then** chuyển `CANCELLED`, ghi `cancelled_at` và hoàn `stock_qty` **đúng 1 lần** (`TC-ORD-09`, `TC-ORD-10`, `TC-ORD-12`). | FR-08, EC-03 |
| AC-07 | **Given** đơn ở trạng thái `SHIPPING`, `DELIVERED`, hoặc `CANCELLED` **When** Customer gửi `POST /account/orders/cancel` **Then** bị từ chối và tồn kho không thay đổi (`TC-ORD-11`). | FR-09 |
| AC-08 | **Given** Admin đăng nhập **When** mở `/admin/orders` lọc theo mã/trạng thái/ngày và chuyển đơn theo chuỗi `PENDING → CONFIRMED → SHIPPING → DELIVERED` **Then** xem đúng snapshot và các bước hợp lệ thành công (`TC-ADM-06`). | FR-10, FR-11 |
| AC-09 | **Given** Admin cố chuyển `PENDING → DELIVERED` hoặc hủy đơn `DELIVERED` **When** gửi `POST /admin/orders` **Then** bị từ chối và trạng thái/tồn kho không đổi (`TC-ADM-07`). | FR-12 |

## 8. Out of Scope (🚫 TUYỆT ĐỐI KHÔNG LÀM)
- Không tích hợp cổng thanh toán online (VNPay, MoMo, ZaloPay, Stripe) hay hoàn tiền trực tuyến.
- Không làm mã giảm giá/voucher, điểm thưởng, tính phí ship theo vùng/cân nặng.
- Không tích hợp đơn vị vận chuyển/mã vận đơn tracking real-time hay giỏ hàng cho Guest chưa đăng nhập.

---
## Phụ lục A: Ghi chú thiết kế
Tier Full — thiết kế chi tiết lập tại `PLAN.md` sau Gate 1.

## Phụ lục B: Câu hỏi mở & Phê duyệt
- Không có câu hỏi mở — các quyết định COD, phí ship 30.000 ₫, snapshot và state machine đã khóa tại ADR-005 & ADR-008.

### Definition of Ready — chỉ khóa (LOCKED) khi đủ cả
- [x] Người duyệt xác nhận `SPEC.md` và `DESIGN.md` (Gate 1)
- [x] Mỗi FR có ≥ 1 AC; mỗi AC đo được (Given/When/Then)
- [x] Out of Scope cụ thể; `scripts/spec-check` không báo lỗi

- **Duyệt (Gate 1):** Owner (BookHub) · 2026-11-23 · Version khóa: 1.0.0
