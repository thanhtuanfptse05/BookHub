# PROGRESS — Đánh giá Sách (Review) & Hỏi đáp (Q&A) kèm Kiểm duyệt (`07-feat-reviews-qa`)

| Trường | Giá trị |
|---|---|
| Feature | `07-feat-reviews-qa` · Tier: Full · Nhánh: `feat/bookhub-mvp-01-08` |
| Pha hiện tại | `done` (Owner đã duyệt `REVIEW.md` `APPROVED` — đóng đủ `T-001`..`T-005`) |
| Task đang làm | — (đã hoàn tất toàn bộ `T-001`..`T-005`) |
| Cập nhật | 2026-11-23 |

## 1. Plan của task hiện tại
- [x] `T-001`: `ReviewRepository`, `QuestionRepository`, `AdminReviewRowDTO`, `AdminQuestionRowDTO`, `OrderRepository.hasDeliveredPurchase()`
- [x] `T-002`: `ReviewService` (kiểm tra đã mua & `DELIVERED`, chặn duplicate `(user_id, book_id)`, mặc định `PENDING`, Admin duyệt `APPROVED`/`HIDDEN`)
- [x] `T-003`: `QuestionService` (Customer hỏi `OPEN`, Admin trả lời 1-1 `ANSWERED` trong 1 TX, `updateAnswer` theo Gate 1 Q1, ẩn `HIDDEN`)
- [x] `T-004`: `ReviewServlet` (`/books/review`), `QuestionServlet` (`/books/question`), tích hợp form trên `book-detail.jsp` & link trên `order-detail.jsp`
- [x] `T-005`: `AdminReviewServlet`, `AdminQuestionServlet` + `admin/{reviews,questions}.jsp` (`SCR-20`, `SCR-21`), test XSS & live Tomcat — Owner đã duyệt `REVIEW.md` (`APPROVED`)

## 2. Nhật ký thực thi
| Giờ | Việc đã làm | Kết quả |
|---|---|---|
| 2026-11-23 | Triển khai `T-001`..`T-005` & `ReviewsQaIntegrationTest`, commit `50eebe1` | 2/2 integration tests PASS (`@spec reviews-qa/AC-01..08`) |
| 2026-11-23 | Live Tomcat phát hiện thiếu dấu cách JPQL trước `ORDER BY` khi `status == null` trên `/admin/reviews` & `/admin/questions` → sửa `ReviewRepository`/`QuestionRepository` + thêm test regression | `/admin/reviews`, `/admin/reviews?status=PENDING`, `/admin/questions`, `/admin/questions?status=OPEN` đều `200 OK`, 0 lỗi SEVERE |

## 3. Kiểm tra
Test ✅ (2/2 JUnit 5 `ReviewsQaIntegrationTest` kèm regression `status == null`) · Build ✅ (`dist/BookHub.war`, 0 warning) · ui-check ✅ · spec-check ✅ · Secret scan ✅

## 4. Blockers / câu hỏi cho người
- ⬜ Chờ Owner review & duyệt `.sdd/specs/07-feat-reviews-qa/REVIEW.md` (Gate 4).

## 5. Quyết định & giả định
- Theo Gate 1 Q1: Admin được phép sửa nội dung câu trả lời duy nhất qua `action=updateAnswer`, trong khi vẫn chặn tạo bản ghi `Answers` thứ 2 (`UQ_Answers_question`).

## 6. Handoff
- **Đã xong:** Toàn bộ luồng Review & Q&A cả phía Customer lẫn Admin (`RT-016`, `RT-017`, `RT-023`, `RT-024`).
- **Làm tiếp:** Sau khi Owner duyệt `REVIEW.md` → đánh `APPROVED` và đóng `T-005`.
