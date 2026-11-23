# SPEC — Đánh giá Sách (Review) & Hỏi đáp (Q&A) kèm Kiểm duyệt (reviews-qa)

> **Pha 1 · Hợp đồng đặc tả.** Agent soạn nháp từ `CONTEXT.md`, **người duyệt & khóa** (Gate 1).
> Sau khi `Status: LOCKED`, sửa đổi chỉ qua `CHANGELOG.md` + người duyệt. **Chưa LOCKED ⇒ cấm code.**

| Trường | Giá trị |
|---|---|
| Feature ID | `07-feat-reviews-qa` |
| Tier | Full |
| UI | Có |
| Status | `LOCKED` |
| Version | 1.0.0 |
| Owner | BookHub Team (PRJ301) |
| Liên quan | `FR-REV-001..007`, `FR-QA-001..009`, `FR-ADM-009..010`, `BR-REV`, `BR-QA`, `ADR-006`, `shared_context.md` (`RT-016..017`, `RT-023..024`), `TC-REV-01..05`, `TC-QA-01..05`, `TC-UI-02` |

---

## 1. Tổng quan (Overview)
Hoàn thiện tương tác giữa khách hàng và cửa hàng BookHub gồm: (1) Customer gửi đánh giá sách đã mua & đã giao (`POST /books/review`) và Admin kiểm duyệt (`SCR-20` `/admin/reviews`), và (2) Customer đặt câu hỏi theo sách (`POST /books/question`) và Admin lọc, trả lời chính thức hoặc ẩn câu hỏi (`SCR-21` `/admin/questions`). Mọi nội dung do người dùng gửi đều được validate tại server và encode chống XSS khi render. Xem `CONTEXT.md`.

## 2. Actors & User Stories
| ID | Câu chuyện |
|---|---|
| US-01 | Là **Customer đã nhận sách**, tôi muốn gửi đánh giá 1–5 sao kèm nhận xét để chia sẻ trải nghiệm với độc giả khác. |
| US-02 | Là **Admin**, tôi muốn duyệt hoặc ẩn các đánh giá chờ xử lý để đảm bảo nội dung công khai lịch sự và điểm trung bình chính xác. |
| US-03 | Là **Customer**, tôi muốn đặt câu hỏi trên trang chi tiết sách để nhận giải đáp chính thức từ cửa hàng. |
| US-04 | Là **Admin**, tôi muốn lọc hàng chờ câu hỏi, viết câu trả lời công khai hoặc ẩn câu hỏi không phù hợp. |

## 3. Functional Requirements (EARS)
| ID | Loại | Mệnh đề | Liên kết (US / BR) |
|---|---|---|---|
| FR-01 | Event | WHEN an authenticated `CUSTOMER` submits `POST /books/review` for an active book with integer `rating` (`1..5`), optional `title` (≤ 150 chars), and non-blank `content` (≤ 2000 chars), AND the user owns at least one `DELIVERED` order containing that book AND has not previously reviewed that book, the system SHALL persist a `Reviews` record with `status = PENDING` and PRG-redirect back to `/books/detail?id={bookId}` with a pending-approval notice. | US-01, FR-REV-001..004, BR-REV-001..004 |
| FR-02 | Unwanted | IF `POST /books/review` is submitted by a Guest, a user without a `DELIVERED` purchase of that book, a user who already reviewed that book (`UQ_Reviews_user_book`), OR with `rating` outside `1..5` or invalid content length, THEN the system SHALL reject the submission with an explicit error message and create no review record. | US-01, FR-REV-001..003, TC-REV-02..04 |
| FR-03 | Event | WHEN an `ADMIN` opens `GET /admin/reviews` (filterable by `PENDING`, `APPROVED`, `HIDDEN`, all; paginated) and submits `POST /admin/reviews` to approve or hide a review, the system SHALL update `Reviews.status` (`APPROVED` or `HIDDEN`) and `updated_at` without hard-deleting the record. | US-02, FR-REV-005, FR-ADM-009, BR-REV-005..007 |
| FR-04 | Ubiquitous | The system SHALL compute a book's public average rating and review list on `SCR-03` strictly from `Reviews` with `status = APPROVED` (excluding `PENDING` and `HIDDEN`). | US-02, FR-REV-006, BR-REV-006, TC-REV-05 |
| FR-05 | Event | WHEN an authenticated `CUSTOMER` submits `POST /books/question` for an active book with non-blank `subject` (≤ 150 chars) and `content` (≤ 1500 chars), the system SHALL create a `Questions` record with `status = OPEN` linked to `currentUser.id` and `bookId`, and PRG-redirect to `/books/detail?id={bookId}`. | US-03, FR-QA-001..002, BR-QA-001..003 |
| FR-06 | Unwanted | IF a Guest attempts `POST /books/question`, OR a Customer submits blank/oversized `subject`/`content` or an inactive/non-existent `bookId`, THEN the system SHALL redirect Guests to `/login` or reject invalid payloads without creating a question. | US-03, FR-QA-001..002, TC-QA-02 |
| FR-07 | Event | WHEN an `ADMIN` opens `GET /admin/questions` (filterable by `OPEN`, `ANSWERED`, `HIDDEN`, all; paginated) and submits `POST /admin/questions` (`action=answer`) for an `OPEN` question with valid `content` (≤ 2000 chars), the system SHALL verify responder role `ADMIN` and that no `Answer` exists yet (`UQ_Answers_question`), create the `Answers` record (`responder_user_id = currentAdmin.id`), and set `Questions.status = ANSWERED` within **one atomic JPA transaction**. | US-04, FR-QA-003..005, FR-QA-009, FR-ADM-010, BR-QA-004..006, ADR-006 |
| FR-08 | Event | WHEN an `ADMIN` submits `POST /admin/questions` (`action=updateAnswer`) for an already-answered question with valid `content` (≤ 2000 chars), the system SHALL update the single existing `Answers` row (`content`, `updated_at`) without inserting a second row; IF `action=answer` attempts to insert a duplicate answer OR `content` is blank/oversized, THEN the system SHALL reject the request. | US-04, FR-QA-005, TC-QA-04 |
| FR-09 | Event | WHEN an `ADMIN` submits `POST /admin/questions` (`action=hide` or `unhide`) on a question, the system SHALL update `Questions.status = HIDDEN` (removing it from public `SCR-03` display without hard-deleting). | US-04, FR-QA-006..007, BR-QA-007, TC-QA-05 |
| FR-10 | Ubiquitous | The system SHALL HTML-escape all user-supplied review and Q&A text (`title`, `subject`, `content`, answer `content`) using `<c:out>` across both public (`SCR-03`) and admin (`SCR-20`, `SCR-21`) views, never rendering raw HTML. | US-01..04, FR-REV-007, FR-QA-008, BR-QA-008, TC-UI-02 |

## 4. Non-Functional Requirements
| ID | Hạng mục | Yêu cầu (số cụ thể) |
|---|---|---|
| NFR-01 | Bảo mật XSS & AuthZ | 100% dữ liệu người dùng render bằng `<c:out>`; kiểm tra role `ADMIN` ở cả Filter lẫn `QuestionService`/`ReviewService`; mọi POST đều kiểm tra CSRF. |
| NFR-02 | Toàn vẹn giao dịch | Tạo `Answers` + cập nhật `Questions.status = ANSWERED` chạy trong cùng 1 transaction; rollback toàn bộ nếu 1 trong 2 bước lỗi. |
| NFR-03 | Phân trang | Danh sách quản trị Review (`SCR-20`) và Question (`SCR-21`) phân trang tại DB, kèm số đếm từng tab trạng thái. |

## 5. Data & API Contract
**Bảng thao tác:** `Reviews`, `Questions`, `Answers` (và đọc kiểm tra từ `Orders`, `OrderItems`, `Books`).

**Servlet Routes (`shared_context.md` `RT-016..017`, `RT-023..024`):**
| Method + Path | Auth | Request Params | Response / Redirect | Lỗi |
|---|---|---|---|---|
| `POST /books/review` | Customer + CSRF | `bookId`, `rating` (1–5), `title?`, `content` | PRG `302 /books/detail?id={bookId}#reviews` | Flash lỗi điều kiện mua / duplicate / validation |
| `POST /books/question` | Customer + CSRF | `bookId`, `subject`, `content` | PRG `302 /books/detail?id={bookId}#qa` | Flash lỗi validation / `404` |
| `GET /admin/reviews` | Admin | `status?`, `page?` | Render `views/admin/reviews.jsp` | `403` |
| `POST /admin/reviews` | Admin + CSRF | `reviewId`, `action=approve\|hide` | PRG `302 /admin/reviews` | Flash lỗi / `404` |
| `GET /admin/questions` | Admin | `status?`, `page?` | Render `views/admin/questions.jsp` | `403` |
| `POST /admin/questions` | Admin + CSRF | `questionId`, `action=answer\|hide`, `content?` | PRG `302 /admin/questions` | Flash lỗi duplicate/validation / `404` |

## 6. Edge Cases & Xử lý lỗi
| ID | Tình huống | Hành vi mong đợi |
|---|---|---|
| EC-01 | Customer có đơn chứa sách ở trạng thái `SHIPPING` hoặc `CANCELLED` (chưa `DELIVERED`) cố gửi POST `/books/review` trực tiếp | Server kiểm tra DB `Orders.status = 'DELIVERED'` và từ chối (`TC-REV-02`). |
| EC-02 | Customer gửi payload `<script>alert(1)</script>` trong nội dung Review hoặc Question | Lưu dạng plain text, khi render ở `SCR-03`, `SCR-20`, `SCR-21` hiển thị nguyên chuỗi chữ, không thực thi JS (`TC-UI-02`). |
| EC-03 | Admin muốn chỉnh sửa lại câu trả lời đã gửi trên `SCR-21` (nếu có) | Trong MVP: có thể cho phép sửa nội dung bản ghi `Answers` duy nhất hiện có (hoặc chỉ trả lời 1 lần — chốt tại Q1 bên dưới), nhưng tuyệt đối không tạo bản ghi `Answers` thứ 2. |
| EC-04 | Review đang `APPROVED` bị Admin chuyển sang `HIDDEN` | Ngay lập tức bị loại khỏi danh sách public và điểm trung bình sao trên `SCR-03`. |

## 7. Acceptance Criteria (Given / When / Then)
> Mỗi FR xuất hiện ở cột "Kiểm chứng FR" của ≥ 1 AC. Test gắn thẻ `@spec reviews-qa/AC-xx`.

| ID | Kịch bản | Kiểm chứng FR |
|---|---|---|
| AC-01 | **Given** Customer có đơn `DELIVERED` chứa sách X và chưa từng review sách X **When** `POST /books/review` (`rating=5`, nội dung hợp lệ) **Then** tạo `Reviews` với `status=PENDING` và chưa xuất hiện public trên `SCR-03` (`TC-REV-01`). | FR-01 |
| AC-02 | **Given** Customer chưa mua/chưa `DELIVERED` sách X, đã review sách X, hoặc nhập `rating=0` / `rating=6` **When** `POST /books/review` **Then** server từ chối và không chèn bản ghi nào (`TC-REV-02`, `TC-REV-03`, `TC-REV-04`). | FR-02, EC-01 |
| AC-03 | **Given** có review ở trạng thái `PENDING` **When** Admin `APPROVE` rồi sau đó `HIDE` trên `SCR-20` **Then** review chỉ hiện trên `SCR-03` và được tính vào điểm TB khi là `APPROVED`; khi chuyển `HIDDEN` thì biến mất khỏi public và điểm TB cập nhật lại (`TC-REV-05`). | FR-03, FR-04, EC-04 |
| AC-04 | **Given** Customer đã đăng nhập và sách X đang active **When** `POST /books/question` với `subject` ≤ 150 và `content` ≤ 1500 **Then** tạo `Questions` trạng thái `OPEN` (`TC-QA-01`); nếu Guest gửi hoặc thiếu nội dung thì bị chuyển Login/từ chối (`TC-QA-02`). | FR-05, FR-06 |
| AC-05 | **Given** có Question trạng thái `OPEN` **When** Admin gửi câu trả lời hợp lệ trên `SCR-21` **Then** trong 1 transaction tạo 1 `Answers` gắn `responder_user_id=Admin` và chuyển `Questions.status=ANSWERED`, hiển thị công khai trên `SCR-03` (`TC-QA-03`). | FR-07 |
| AC-06 | **Given** Question đã có `Answer` **When** gửi yêu cầu tạo Answer lần hai **Then** hệ thống từ chối, không tạo bản ghi trùng (`TC-QA-04`). | FR-08 |
| AC-07 | **Given** Question đang `OPEN` hoặc `ANSWERED` **When** Admin bấm ẩn trên `SCR-21` **Then** `status` chuyển `HIDDEN` và không còn hiển thị trên `SCR-03` (`TC-QA-05`). | FR-09 |
| AC-08 | **Given** nội dung Review và Question chứa `<script>alert(1)</script>` **When** render `SCR-03`, `SCR-20`, `SCR-21` **Then** HTML output đã được escape an toàn (`&lt;script&gt;...`), không thực thi script (`TC-UI-02`). | FR-10, EC-02 |

## 8. Out of Scope (🚫 TUYỆT ĐỐI KHÔNG LÀM)
- Không cho phép Customer chỉnh sửa hoặc xóa Review sau khi đã gửi trong MVP.
- Không cho phép Customer trả lời câu hỏi của Customer khác (chỉ Admin trả lời — ADR-006).
- Không cho phép nhiều câu trả lời (thread thảo luận nhiều tầng) cho cùng 1 câu hỏi.
- Không hỗ trợ đính kèm ảnh trong đánh giá hay câu hỏi.

---
## Phụ lục A: Ghi chú thiết kế
Tier Full — thiết kế chi tiết lập tại `PLAN.md` sau Gate 1.

## Phụ lục B: Câu hỏi mở & Phê duyệt
- [x] Q1: Trên `SCR-21`, cho phép Admin nút `"Cập nhật phản hồi"` sửa nội dung bản ghi `Answers` duy nhất hiện có (`action=updateAnswer`, vẫn giữ ràng buộc 1 Answer/Question và từ chối chèn Answer thứ 2) — _người trả lời:_ Đã chốt (2026-11-23).

### Definition of Ready — chỉ khóa (LOCKED) khi đủ cả
- [x] Giải quyết xong Q1 ở Phụ lục B
- [x] Mỗi FR có ≥ 1 AC; `DESIGN.md` đồng duyệt cùng Gate 1
- [x] Out of Scope cụ thể; `scripts/spec-check` không báo lỗi

- **Duyệt (Gate 1):** Owner (BookHub) · 2026-11-23 · Version khóa: 1.0.0
