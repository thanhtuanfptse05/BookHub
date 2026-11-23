# PROGRESS — Giỏ hàng, Thanh toán COD & Vòng đời Đơn hàng (`06-feat-cart-checkout-orders`)

| Trường | Giá trị |
|---|---|
| Feature | `06-feat-cart-checkout-orders` · Tier: Full · Nhánh: `feat/cart-checkout-orders` |
| Pha hiện tại | `tasking` (SPEC & DESIGN `LOCKED` v1.0.0 · PLAN & TASKS `DRAFT` chờ duyệt Gate 2–3) |
| Task đang làm | Chưa bắt đầu (phụ thuộc `03` & `04`) |
| Cập nhật | 2026-11-23 |

## 1. Plan của task hiện tại
- **Mục tiêu (đo được):** Sau khi xong `03` & `04` và duyệt Gate 2–3, triển khai `T-001` → `T-008`.
- [ ] Duyệt `PLAN.md` (Gate 2) & `TASKS.md` (Gate 3)

## 2. Nhật ký thực thi
| Giờ | Việc đã làm | Kết quả |
|---|---|---|
| 2026-11-23 | Khóa SPEC + DESIGN v1.0.0 (Gate 1); lập `PLAN.md` và `TASKS.md` (`T-001`..`T-008`) | Xong Pha 1–3 |

## 3. Kiểm tra
Lint ⬜ · Test ⬜ · Build ⬜ · spec-check ✅ · Secret scan ✅ · Coverage mới ⬜ %

## 4. Blockers / câu hỏi cho người
- ⬜ Chờ hoàn tất `03-feat-auth-account`, `04-feat-public-catalog` và duyệt Gate 2–3.

## 5. Quyết định & giả định
- Dùng `PESSIMISTIC_WRITE` khóa `Books` theo thứ tự `bookId ASC` khi checkout và khóa `Orders` khi cancel để triệt tiêu âm kho và hoàn kho lặp.

## 6. Handoff
- **Đã xong:** CONTEXT, SPEC (LOCKED), DESIGN (LOCKED), PLAN, TASKS. · **Làm tiếp:** Triển khai sau khi xong `03` & `04`. · **Cẩn thận:** Không bao giờ dùng giá/tổng tiền client gửi lên.
