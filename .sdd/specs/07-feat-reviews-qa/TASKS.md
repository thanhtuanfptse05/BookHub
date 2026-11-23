# TASKS — Đánh giá Sách (Review) & Hỏi đáp (Q&A) kèm Kiểm duyệt (`07-feat-reviews-qa`)

> **Pha 3 · Phân rã từ `PLAN.md`.** Task atomic ≤ 4h · gắn thẻ `@spec reviews-qa/AC-xx`.

**Trạng thái:** `⬜ todo` · `🔄 doing` · `✅ done` · `⛔ blocked`

| ID | Task | Phụ thuộc | Ước lượng | Truy vết | Trạng thái |
|---|---|---|---|---|---|
| T-001 | Tạo `ReviewRepository`, `QuestionRepository`, DTOs và `OrderRepository.hasDeliveredPurchase()` | — | 2.5h | FR-01..09 | ✅ |
| T-002 | Tạo `ReviewService` (eligibility `DELIVERED`, 1/user/book, `PENDING`→`APPROVED`/`HIDDEN`) + tests (`AC-01..03`) | T-001 | 3h | FR-01..04, EC-01, EC-04, AC-01..03 | ✅ |
| T-003 | Tạo `QuestionService` (ask `OPEN`, transactional `answer` → `ANSWERED`, `updateAnswer`, `hide`) + tests (`AC-04..07`) | T-001 | 3h | FR-05..09, EC-03, AC-04..07 | ✅ |
| T-004 | Tạo `ReviewServlet`, `QuestionServlet`, cập nhật form Review/Q&A trên `book-detail.jsp` & link `Viết đánh giá` ở `order-detail.jsp` (`SCR-03`, `SCR-12b`) | T-002, T-003 | 3h | FR-01..02, FR-05..06, FR-10, AC-08 | ✅ |
| T-005 | Tạo `AdminReviewServlet`, `AdminQuestionServlet` + `views/admin/{reviews,questions}.jsp` (`SCR-20`, `SCR-21`) + test XSS (Gate 4 `APPROVED`) | T-002, T-003 | 3.5h | FR-03..04, FR-07..10, EC-02, AC-08, VAC-01..02 | ✅ |

## Chi tiết task

### T-001 — Repositories & Eligibility Query
- **Mục tiêu:** Viết truy vấn JPA cho `Reviews`, `Questions`/`Answers` (lọc trạng thái, phân trang DB, đếm tab) và `hasDeliveredPurchase(userId, bookId)`.
- **File dự kiến:** `repository/{ReviewRepository,QuestionRepository,OrderRepository}.java`, `dto/{AdminReviewRowDTO,AdminQuestionRowDTO,ReviewEligibilityDTO}.java`.
- **Acceptance:**
  - [ ] 100% named parameters, không N+1 khi nạp `Question` + `Answer` + `Book` + `User`.

### T-002 — `ReviewService` & Integration Tests
- **Mục tiêu:** Chặn review khi chưa có đơn `DELIVERED`, chặn duplicate `(user_id, book_id)`, validate rating `1..5`, mặc định `PENDING`, và Admin duyệt `APPROVED`/`HIDDEN`.
- **File dự kiến:** `service/ReviewService.java`, `tests/integration/ReviewServiceIntegrationTest.java`.
- **Acceptance:**
  - [ ] Pass `@spec reviews-qa/AC-01`, `AC-02`, `AC-03` (`TC-REV-01..05`).

### T-003 — `QuestionService` & Integration Tests
- **Mục tiêu:** Tạo câu hỏi `OPEN` (`subject <= 150`, `content <= 1500`); Admin trả lời (`answer` tạo 1 `Answers` + đổi `ANSWERED` trong 1 TX), sửa câu trả lời duy nhất (`updateAnswer`), hoặc ẩn (`HIDDEN`).
- **File dự kiến:** `service/QuestionService.java`, `tests/integration/QuestionServiceIntegrationTest.java`.
- **Acceptance:**
  - [ ] Pass `@spec reviews-qa/AC-04`..`AC-07` (`TC-QA-01..05`).

### T-004 & T-005 — Servlets, JSP Views (`SCR-03`, `SCR-20`, `SCR-21`) & Kiểm thử XSS
- **Mục tiêu:** Nối form gửi Review (bộ chọn sao radio a11y) và Q&A (đếm ký tự) trên `SCR-03`, dựng màn hình quản trị `SCR-20` & `SCR-21` với tab đếm, `ConfirmModal` ẩn nội dung, escape `<c:out>` 100% và chạy `scripts/verify.ps1`.
- **File dự kiến:** `controller/customer/{Review,Question}Servlet.java`, `controller/admin/{AdminReview,AdminQuestion}Servlet.java`, `web/WEB-INF/views/admin/{reviews,questions}.jsp`, cập nhật `book-detail.jsp` & `order-detail.jsp`.
- **Acceptance:**
  - [ ] Payload `<script>alert(1)</script>` bị escape hoàn toàn (`@spec reviews-qa/AC-08`, `TC-UI-02`); đạt `VAC-01..02`.

## Đồ thị phụ thuộc
```text
T-001 ─┬─► T-002 ─┬─► T-004
       └─► T-003 ─┴─► T-005
```

## Phê duyệt (Gate 3)
Owner (BookHub) · 2026-11-23
