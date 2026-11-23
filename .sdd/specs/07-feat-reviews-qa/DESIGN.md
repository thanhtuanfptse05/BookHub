# DESIGN — Đánh giá Sách (Review) & Hỏi đáp (Q&A) kèm Kiểm duyệt (reviews-qa)

> **Bản thiết kế gọn cho feature có UI.** Tuân thủ `.sdd/design/DESIGN.md`, `tokens.css` và `components.md`.

| Trường | Giá trị |
|---|---|
| Feature | `07-feat-reviews-qa` |
| Status | `LOCKED` |
| Tham chiếu Stitch/Figma | `https://agenda-kale-62994833.figma.site/#/admin/reviews`, `#/admin/questions`, `#/books/detail` · `.sdd/design/references/baseline/scr-03__book-detail__1280.png` |

## 1. Màn hình & Luồng đi
| ID | Màn hình / View | Vào từ đâu | Mục đích |
|---|---|---|---|
| SCR-03 (Sections) | Khu Review & Q&A trong `views/site/book-detail.jsp` | `GET /books/detail?id={id}` / CTA từ `SCR-12b` | Form gửi đánh giá 1–5 sao, form đặt câu hỏi (đếm ký tự `0/150`, `0/1.500`), danh sách công khai |
| SCR-20 | Admin Kiểm duyệt đánh giá (`views/admin/reviews.jsp`) | Sidebar Admin `Đánh giá` / thẻ chờ duyệt ở `SCR-14` | Lọc theo tab (`PENDING`/`APPROVED`/`HIDDEN`) và bấm `Duyệt`/`Ẩn` |
| SCR-21 | Admin Quản lý Q&A (`views/admin/questions.jsp`) | Sidebar Admin `Hỏi đáp` / thẻ chờ trả lời ở `SCR-14` | Lọc theo tab (`OPEN`/`ANSWERED`/`HIDDEN`), nhập câu trả lời chính thức hoặc ẩn |

```text
SCR-12b Order Delivered ──("Viết đánh giá")──► SCR-03#reviews ──(POST/PRG)──► Flash "Đánh giá đang chờ duyệt"
SCR-03#qa ("Gửi câu hỏi") ──(POST/PRG)──► Flash "Câu hỏi đã gửi"
Admin ──► SCR-20 Reviews (Duyệt / Ẩn) · SCR-21 Questions (Trả lời → ANSWERED / Ẩn → HIDDEN)
```

## 2. Component & Token dùng
- **Dùng lại từ `components.md`:** `AdminShell`, `FormControl`, `Button` (`primary-teal`, `outline-neutral`, `danger-outline`), `StatusBadge` (Review 3 trạng thái, Question 3 trạng thái), `AlertBanner` (`info`, `success`, `error`), `ConfirmModal` (khi ẩn nội dung), `Pagination`, `EmptyState`.
- **Component mới:** Bộ chọn sao tương tác 1–5 bằng `<input type="radio">` ngữ nghĩa + thanh phân bố 5→1 sao màu `--color-accent` (`#c85a32`) trên `SCR-03` (ghi bổ sung vào `components.md` nếu tách fragment riêng).
- **Token mới ngoài `tokens.css`?** Không.

## 3. Trạng thái, Responsive & Copy (chỉ ghi điểm riêng)
- **Trạng thái:**
  - **Empty:** Tab chờ duyệt không có review/câu hỏi nào → `EmptyState` gọn (`"Không có đánh giá nào đang chờ duyệt."`).
  - **Ineligible (SCR-03):** Với Customer chưa mua hoặc đã review sách → hộp `AlertBanner` xanh giải thích rõ điều kiện (`"Chỉ áp dụng cho khách hàng đã nhận sách này..."` hoặc `"Bạn đã đánh giá cuốn sách này."`).
  - **Error:** Thiếu số sao, nội dung rỗng hoặc quá giới hạn ký tự → báo lỗi inline.
- **Responsive (375 vs 1280):** Khối 2 cột Review & Q&A trên `SCR-03` xếp dọc 1 cột ở 375px; danh sách kiểm duyệt `SCR-20`/`SCR-21` dùng card/table cuộn ngang an toàn.
- **Copy / i18n chính:** `"Chỉ áp dụng cho khách hàng đã nhận sách này; đánh giá sẽ hiển thị sau khi quản trị viên duyệt."`, `"✓ BookHub phản hồi"`, `"Gửi đánh giá"`, `"Gửi câu hỏi"`, `"Gửi phản hồi"`, `"Ẩn câu hỏi"`.
- **a11y cần lưu ý:** Radio chọn 1–5 sao dùng bàn phím mũi tên chọn được và có nhãn `"1 sao"`..`"5 sao"`; bộ đếm ký tự không dựa duy nhất vào JS (server luôn validate).
- **SEO/AEO/GEO:** `SCR-20` & `SCR-21` đặt `noindex, nofollow`; nội dung công khai trên `SCR-03` render SSR bằng `<c:out>`.

## 4. Tiêu chí nghiệm thu bằng mắt (VAC) & Duyệt
- [ ] **VAC-01:** Khu Review & Q&A trên `SCR-03` khớp `scr-03__book-detail__1280.png` (có hộp phản hồi xanh nhạt `✓ BookHub phản hồi`, thanh 5→1 sao terracotta).
- [ ] **VAC-02:** `SCR-20` và `SCR-21` hiển thị đủ tab trạng thái kèm bộ đếm, form phản hồi Admin rõ ràng và không vỡ ở 375px/1280px.
- **Duyệt (cùng Gate 1):** Owner (BookHub) · 2026-11-23
