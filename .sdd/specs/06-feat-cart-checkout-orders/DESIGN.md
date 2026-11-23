# DESIGN — Giỏ hàng, Thanh toán COD & Vòng đời Đơn hàng (cart-checkout-orders)

> **Bản thiết kế gọn cho feature có UI.** Tuân thủ `.sdd/design/DESIGN.md`, `tokens.css` và `components.md`.

| Trường | Giá trị |
|---|---|
| Feature | `06-feat-cart-checkout-orders` |
| Status | `LOCKED` |
| Tham chiếu Stitch/Figma | `https://agenda-kale-62994833.figma.site/#/cart` (`SCR-08`..`12b`, `SCR-18`) · `.sdd/design/references/baseline/scr-08__cart__1280.png`, `scr-09__checkout-cod__1280.png`, `scr-18__admin-orders__1280.png` |

## 1. Màn hình & Luồng đi
| ID | Màn hình / View | Vào từ đâu | Mục đích |
|---|---|---|---|
| SCR-08 | Giỏ hàng (`views/customer/cart.jsp`) | Icon giỏ ở Header / sau thêm giỏ | Sửa số lượng, xóa dòng, xem tạm tính và đi checkout |
| SCR-09 | Thanh toán COD (`views/customer/checkout.jsp`) | Nút `Tiến hành đặt hàng` ở `SCR-08` | Nhập người nhận, xác nhận COD + phí ship 30.000 ₫ |
| SCR-10 | Đặt hàng thành công (`views/customer/order-success.jsp`) | PRG sau `POST /checkout` | Hiển thị mã đơn `BH-...`, trạng thái `PENDING` và CTA |
| SCR-11 | Đơn hàng của tôi (`views/customer/orders.jsp`) | Menu tài khoản / từ `SCR-10` | Lọc theo trạng thái & xem danh sách đơn của mình |
| SCR-12 / 12b | Chi tiết đơn hàng (`views/customer/order-detail.jsp`) | Click `Xem chi tiết` ở `SCR-11` | Stepper 4 bước, snapshot, nút `Hủy đơn` (`PENDING`/`CONFIRMED`) hoặc link `Viết đánh giá` (`DELIVERED`) |
| SCR-18 | Admin Quản lý đơn (`views/admin/orders.jsp`) | Sidebar Admin `Đơn hàng` | Lọc mã/trạng thái/ngày, xem panel snapshot & chuyển trạng thái hợp lệ |

```text
SCR-08 Cart ──► SCR-09 Checkout COD ──(POST/PRG)──► SCR-10 Success ──► SCR-12 Order Detail
                                                                           ├──► ConfirmModal Hủy đơn (PENDING/CONFIRMED)
                                                                           └──► Link Viết đánh giá (SCR-12b DELIVERED)
Admin Sidebar ──► SCR-18 Orders List ──► Panel Chi tiết Snapshot + Nút chuyển trạng thái theo State Machine
```

## 2. Component & Token dùng
- **Dùng lại từ `components.md`:** `StorefrontHeader`/`Footer`, `AdminShell`, `QuantityStepper`, `FormControl`, `Button` (`primary-teal`, `danger-outline`, `danger-solid`), `StatusBadge` (đủ 5 trạng thái đơn), `OrderStepper`, `AlertBanner`, `ConfirmModal`, `EmptyState`, `Pagination`.
- **Component mới:** Không có.
- **Token mới ngoài `tokens.css`?** Không.

## 3. Trạng thái, Responsive & Copy (chỉ ghi điểm riêng)
- **Trạng thái:**
  - **Empty:** `SCR-08` (`"Giỏ hàng của bạn đang trống"` + `"Khám phá sách"`); `SCR-11` (`"Bạn chưa có đơn hàng nào"` + `"Khám phá sách"`).
  - **Loading:** Nút `"Đặt hàng"` ở `SCR-09` disable sau lần bấm đầu tiên để hỗ trợ chống double-click (kèm guard ở server).
  - **Warning/Error:** `SCR-09` hiển thị `AlertBanner` vàng khi giá hoặc tồn kho thay đổi kể từ lúc thêm giỏ; lỗi thiếu thông tin nhận hàng hiện inline dưới từng ô.
- **Responsive (375 vs 1280):** `SCR-08` & `SCR-09` bố cục 2 cột (danh sách/form trái + tóm tắt phải) xếp dọc 1 cột trên 375px; `SCR-11` & `SCR-18` bọc `.table-responsive`; `OrderStepper` co gọn chữ đẹp trên 375px.
- **Copy / i18n chính:** `"Phí vận chuyển 30.000 ₫ sẽ được tính tại bước thanh toán."`, `"Thanh toán khi nhận hàng (COD)"`, `"Đặt hàng thành công!"`, `"Bạn chắc chắn muốn hủy đơn này? Tồn kho sẽ được hoàn lại."`, `"Quy tắc chuyển trạng thái"`.
- **a11y cần lưu ý:** Không có ô nhập giá/voucher; nút hủy đơn mở `ConfirmModal` bẫy focus và hỗ trợ `Esc`.
- **SEO/AEO/GEO:** Toàn bộ `SCR-08`..`12b` và `SCR-18` đặt `<meta name="robots" content="noindex, nofollow">`.

## 4. Tiêu chí nghiệm thu bằng mắt (VAC) & Duyệt
- [ ] **VAC-01:** `SCR-08`, `SCR-09`, `SCR-18` khớp ảnh baseline trong `.sdd/design/references/baseline/` (không có ô voucher, không có cổng thanh toán online).
- [ ] **VAC-02:** Hiển thị đủ Empty Cart, `OrderStepper` 4 bước, `ConfirmModal` hủy đơn, biến thể `SCR-12b` (`DELIVERED`) và nút state machine trên `SCR-18`.
- **Duyệt (cùng Gate 1):** Owner (BookHub) · 2026-11-23
