# PLAN — Đánh giá Sách (Review) & Hỏi đáp (Q&A) kèm Kiểm duyệt (`07-feat-reviews-qa`)

> **Pha 2 · Thiết kế kiến trúc** sau khi SPEC & DESIGN v1.0.0 đã `LOCKED`. Duyệt (Gate 2) cùng `TASKS.md`.

| Trường | Giá trị |
|---|---|
| SPEC version | 1.0.0 |
| Status | `DRAFT` |

## 1. Tóm tắt giải pháp
Triển khai `ReviewService` + `ReviewRepository` và `QuestionService` + `QuestionRepository` (kèm thao tác `Answers`) trên nền các bảng `Reviews`, `Questions`, `Answers`. Kiểm tra điều kiện đã mua & đã giao (`Orders.status = 'DELIVERED'` nối `OrderItems`) tại DB ngay trong `ReviewService.submitReview()`. Phản hồi Q&A phía Admin hỗ trợ cả `action=answer` (tạo 1 `Answers` + chuyển `Questions.status = ANSWERED` trong 1 transaction, từ chối trùng theo `UQ_Answers_question`) và `action=updateAnswer` (sửa nội dung bản ghi `Answers` duy nhất hiện có theo quyết định Gate 1 Q1, `ADR-006`). Mọi đầu ra render bắt buộc dùng `<c:out>` chống Stored XSS.

## 2. Phương án đã cân nhắc
| Phương án | Ưu | Nhược | Chọn? |
|---|---|---|---|
| A: Tách `ReviewService`/`QuestionService` + kiểm tra quyền mua hàng bằng `EXISTS` JPQL + 1 transaction tạo Answer & đổi `ANSWERED` | Đáp ứng chặt ràng buộc 1-to-1 Q&A, dễ kiểm thử từng luồng nghiệp vụ và XSS | Thêm 2 Repository/Service gọn | ✅ |
| B: Cho phép chèn trực tiếp Review rồi dựa vào UI ẩn form khi chưa mua | Nhanh phía UI | Dễ bị bypass bằng POST trực tiếp, vi phạm `BR-REV-001` & `H-06` | ❌ |

## 3. Kiến trúc & luồng xử lý
```text
POST /books/review   → [AuthFilter + CsrfFilter] → ReviewServlet → ReviewService.submitReview(userId, bookId, rating, title, content)
                     → verify existsDeliveredPurchase(userId, bookId) & !existsByUserAndBook → persist Reviews(PENDING) → PRG /books/detail?id=#reviews
GET/POST /admin/reviews   → AdminReviewServlet → ReviewService.listByStatus(page) | moderate(reviewId, APPROVED|HIDDEN) → views/admin/reviews.jsp (SCR-20)

POST /books/question → [AuthFilter + CsrfFilter] → QuestionServlet → QuestionService.askQuestion(userId, bookId, subject, content)
                     → verify book active → persist Questions(OPEN) → PRG /books/detail?id=#qa
GET/POST /admin/questions → AdminQuestionServlet → QuestionService.listByStatus(page)
                          | answerQuestion(adminId, qId, content) [1 TX: verify role ADMIN & no answer → insert Answer + set Question=ANSWERED]
                          | updateAnswer(adminId, qId, content) [update single existing Answer row]
                          | hideQuestion(qId) [set HIDDEN] → views/admin/questions.jsp (SCR-21)
```

## 4. Thay đổi theo tầng
| Tầng | File tạo mới | File sửa | Ghi chú |
|---|---|---|---|
| `dto/` | `AdminReviewRowDTO`, `AdminQuestionRowDTO`, `ReviewEligibilityDTO` | — | Chứa bộ đếm từng tab trạng thái |
| `repository/` | `ReviewRepository`, `QuestionRepository` | `OrderRepository` (thêm `hasDeliveredPurchase(userId, bookId)`) | Named parameters 100% |
| `service/` | `ReviewService`, `QuestionService` | `CatalogService` (bổ sung `ReviewEligibilityDTO` cho `SCR-03` khi đã login) | Atomic TX cho Answer + `ANSWERED` |
| `controller/` | `customer/{ReviewServlet,QuestionServlet}`, `admin/{AdminReviewServlet,AdminQuestionServlet}` | `customer/OrderDetailServlet` (cờ `canReview` cho từng item ở `SCR-12b`) | PRG + CSRF |
| `web/views/` | `admin/{reviews,questions}.jsp` | `site/book-detail.jsp` (nối form submit Review & Q&A + bộ chọn sao 1–5 + đếm ký tự), `customer/order-detail.jsp` (link `Viết đánh giá`) | `<c:out>` 100% chống Stored XSS |
| `tests/` | `tests/integration/{ReviewServiceIntegrationTest,QuestionServiceIntegrationTest}.java` | — | Gắn `@spec reviews-qa/AC-01..08` |

## 5. Data model & Migration
- Dùng nguyên vẹn `Reviews`, `Questions`, `Answers` trong `01_schema.sql` (`UQ_Reviews_user_book`, `CK_Reviews_rating 1..5`, `UQ_Answers_question`).

## 6. API / Contract & ảnh hưởng chéo
- Hiện thực hóa `RT-016`, `RT-017`, `RT-023`, `RT-024` trong `.sdd/shared_context.md`.
- Cung cấp số đếm `PENDING` reviews và `OPEN` questions cho thẻ cảnh báo ở `08-feat-admin-users-reports` (`SCR-14`).

## 7. Truy vết SPEC → thiết kế
| Yêu cầu | Thành phần đáp ứng | Test dự kiến |
|---|---|---|
| FR-01, FR-02, EC-01 | `ReviewServlet` + `ReviewService.submitReview()` + `hasDeliveredPurchase()` | AC-01, AC-02 |
| FR-03, FR-04, EC-04 | `AdminReviewServlet` + `ReviewService.moderate()` + query avg `APPROVED` | AC-03 |
| FR-05, FR-06 | `QuestionServlet` + `QuestionService.askQuestion()` | AC-04 |
| FR-07 | `AdminQuestionServlet` + `QuestionService.answerQuestion()` (1 TX) | AC-05 |
| FR-08, EC-03 | `QuestionService.updateAnswer()` + chặn `answer` trùng | AC-06 |
| FR-09 | `QuestionService.hideQuestion()` | AC-07 |
| FR-10, EC-02 | `<c:out>` trên `book-detail.jsp`, `admin/reviews.jsp`, `admin/questions.jsp` | AC-08 |

## 8. Chiến lược kiểm thử
- **Integration (SQL Server test DB):** Đủ `TC-REV-01..05`, `TC-QA-01..05` (gồm test `action=answer` trùng bị từ chối và `action=updateAnswer` sửa đúng bản ghi duy nhất).
- **Security / XSS (`TC-UI-02`):** Gửi `<script>alert(1)</script>` vào Review & Question, kiểm tra HTML response của `SCR-03`, `SCR-20`, `SCR-21` đã được escape thành `&lt;script&gt;`.
- **Visual:** Kiểm `SCR-03` (phần Review & Q&A), `SCR-20`, `SCR-21` ở 375/1280 so với `scr-03__book-detail__1280.png`.

## 9. Rủi ro & giảm thiểu
| # | Rủi ro | Xác suất | Tác động | Giảm thiểu / Rollback |
|---|---|---|---|---|
| R1 | Stored XSS khi render nội dung người dùng trong JSP bằng `${item.content}` thô | Trung bình | Cao | Bắt buộc `<c:out value="${...}"/>` ở mọi trường nội dung; test tự động với payload `<script>` |
| R2 | Hai Admin bấm trả lời cùng 1 câu hỏi `OPEN` đồng thời | Thấp | Trung bình | Khóa bản ghi `Question` + ràng buộc `UQ_Answers_question` ở DB; bắt xung đột trả thông báo thân thiện |

## 10. Rollout & Quan sát
- Log `INFO` thao tác duyệt/ẩn Review và trả lời/sửa/ẩn Q&A kèm `adminUserId` và `id` (không log nội dung dài). Rollback qua nhánh `feat/reviews-qa`.

## 11. ❓ Câu hỏi cho con người
- Không còn câu hỏi mở (Q1 đã khóa ở Gate 1: cho phép `updateAnswer` trên bản ghi `Answers` duy nhất).

## 12. Phê duyệt (Gate 2)
(chờ duyệt) · —
