# REVIEW — cart-checkout-orders

> Review **độc lập** (agent/session khác, hoặc người) — prompt: `.sdd/prompts/06-review.md`. Reviewer **không sửa code**.
> Tier Full: bắt buộc `APPROVED` trước khi merge.

| Trường | Giá trị |
|---|---|
| Reviewer | Chờ Owner duyệt (bản nháp bằng chứng từ implementer) · Ngày: 2026-11-23 |
| Commit/nhánh | `feat/bookhub-mvp-01-08` (commit `5102566` + fix JSP EL) |
| **Kết luận** | `PENDING` (chờ Owner xác nhận `APPROVED`) |

## 1. Bằng chứng chạy thật
```text
$ ant clean dist test-junit5
- CartCheckoutOrdersIntegrationTest: 3/3 OK (SQL Server 2022 BookHubDB)
  - cartMergeQuantitiesOwnershipCheckAndValidation() [OK]
  - atomicCodCheckoutAndConcurrentLastCopyRacePrevention() [OK]  (2 threads đua 1 cuốn cuối: đúng 1 thành công, 1 OutOfStockException, stock=0)
  - orderOwnershipConcurrentCancelSingleRefundAndAdminStateMachine() [OK] (2 threads hủy cùng đơn: hoàn kho đúng 1 lần)
- Live Tomcat 10.1: /cart, /checkout, /account/orders, /account/orders/detail?id=1, /admin/orders → 200 OK
```

## 2. Truy vết
| Yêu cầu | Code | Test (`@spec`) | OK? |
|---|---|---|---|
| FR-01..03 | `CartRepository`, `CartService`, `CartServlet`, `customer/cart.jsp` | `cart-checkout-orders/AC-01..02` | ✅ |
| FR-04..06 | `CheckoutService`, `BookRepository` (`lockBooksOrdered` + `decrementStockAtomically`), `CheckoutServlet`, `OrderSuccessServlet` | `cart-checkout-orders/AC-03..04` | ✅ |
| FR-07..09 | `OrderService`, `MyOrdersServlet`, `OrderDetailServlet`, `OrderCancelServlet`, `customer/{orders,order-detail}.jsp` | `cart-checkout-orders/AC-05..07` | ✅ |
| FR-10..12 | `OrderStateMachine`, `OrderService.updateOrderStatusByAdmin`, `AdminOrderServlet`, `admin/orders.jsp` | `cart-checkout-orders/AC-08..09` | ✅ |

## 3. Kiểm tra theo hiến pháp
- [x] Tính tiền 100% bằng `BigDecimal` từ giá DB (`Books.price`), phí ship cố định `30.000 VND`, phương thức duy nhất `COD`, bỏ qua mọi giá client gửi lên
- [x] Lưu snapshot `book_title_snapshot` và `unit_price` trong `OrderItems` (ADR-008)
- [x] Chống oversell & hoàn kho kép bằng khóa `PESSIMISTIC_WRITE` + `em.refresh` + conditional SQL `UPDATE` (`stockQty >= :qty`) và khóa `Order` khi hủy
- [x] Chống IDOR: mọi truy vấn Cart/Order phía Customer lọc theo `currentUser.id`

## 4. Phát hiện
| # | Mức | File:dòng | Vấn đề | Cách sửa đề xuất | Trạng thái |
|---|---|---|---|---|---|
| 1 | 🔴 | `BookRepository.lockBooksOrdered` | Test 2-thread ban đầu phát hiện Hibernate L1 cache giữ `stockQty` cũ từ `JOIN FETCH` giỏ hàng | Đã bổ sung `em.refresh(..., PESSIMISTIC_WRITE)` + conditional `decrementStockAtomically` (`updated == 1`) & test 2-thread PASS | Đã sửa |
| 2 | 🟡 | `customer/{cart,orders}.jsp`, `admin/orders.jsp` | Biểu thức EL `.empty` xung đột từ khóa JSP EL 5.0 | Đã thay bằng `${empty ...items}` & xác minh live Tomcat 200 OK | Đã sửa |

## 5. Phê duyệt cuối
_(Chờ Owner xác nhận `APPROVED` để chốt Gate 4)_
