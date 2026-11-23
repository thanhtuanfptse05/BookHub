# PROGRESS — Đánh giá Sách (Review) & Hỏi đáp (Q&A) kèm Kiểm duyệt (`07-feat-reviews-qa`)

| Trường | Giá trị |
|---|---|
| Feature | `07-feat-reviews-qa` · Tier: Full · Nhánh: `feat/reviews-qa` |
| Pha hiện tại | `tasking` (SPEC & DESIGN `LOCKED` v1.0.0 · PLAN & TASKS `DRAFT` chờ duyệt Gate 2–3) |
| Task đang làm | Chưa bắt đầu (phụ thuộc `06-feat-cart-checkout-orders`) |
| Cập nhật | 2026-11-23 |

## 1. Plan của task hiện tại
- **Mục tiêu (đo được):** Sau khi xong `06` và duyệt Gate 2–3, triển khai `T-001` → `T-005`.
- [ ] Duyệt `PLAN.md` (Gate 2) & `TASKS.md` (Gate 3)

## 2. Nhật ký thực thi
| Giờ | Việc đã làm | Kết quả |
|---|---|---|
| 2026-11-23 | Chốt Q1 & khóa SPEC + DESIGN v1.0.0 (Gate 1); lập `PLAN.md` và `TASKS.md` (`T-001`..`T-005`) | Xong Pha 1–3 |

## 3. Kiểm tra
Lint ⬜ · Test ⬜ · Build ⬜ · spec-check ✅ · Secret scan ✅ · Coverage mới ⬜ %

## 4. Blockers / câu hỏi cho người
- ⬜ Chờ hoàn tất `06-feat-cart-checkout-orders` và duyệt Gate 2–3.

## 5. Quyết định & giả định
- Admin có thể sửa nội dung câu trả lời duy nhất (`action=updateAnswer`) nhưng không bao giờ tạo dòng `Answers` thứ 2.

## 6. Handoff
- **Đã xong:** CONTEXT, SPEC (LOCKED), DESIGN (LOCKED), PLAN, TASKS. · **Làm tiếp:** Triển khai sau khi xong `06`. · **Cẩn thận:** 100% `<c:out>` chống Stored XSS.
