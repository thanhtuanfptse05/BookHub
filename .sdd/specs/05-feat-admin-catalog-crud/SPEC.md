# SPEC — Quản trị Sách & Danh mục (admin-catalog-crud)

> **Pha 1 · Hợp đồng đặc tả.** Agent soạn nháp từ `CONTEXT.md`, **người duyệt & khóa** (Gate 1).
> Sau khi `Status: LOCKED`, sửa đổi chỉ qua `CHANGELOG.md` + người duyệt. **Chưa LOCKED ⇒ cấm code.**

| Trường | Giá trị |
|---|---|
| Feature ID | `05-feat-admin-catalog-crud` |
| Tier | Full |
| UI | Có |
| Status | `LOCKED` |
| Version | 1.0.0 |
| Owner | BookHub Team (PRJ301) |
| Liên quan | `FR-ADM-002..004`, `BR-CAT-001..008`, `BR-ADM-001..003`, `shared_context.md` (`RT-019`, `RT-020`), `ADR-007`, `TC-ADM-01..05` |

---

## 1. Tổng quan (Overview)
Cung cấp khu vực quản trị Danh mục (`/admin/categories`) và Sách (`/admin/books`, `/admin/books/form`) cho Admin với đầy đủ tính năng tìm kiếm, lọc, phân trang DB, thêm mới, chỉnh sửa và chuyển đổi trạng thái kinh doanh (soft-deactivate / reactivate) qua JPA transaction. Feature đảm bảo tính toàn vẹn khóa ngoại và lịch sử đơn hàng khi cập nhật hoặc ngừng bán sách/danh mục. Xem `CONTEXT.md`.

## 2. Actors & User Stories
| ID | Câu chuyện |
|---|---|
| US-01 | Là **Admin**, tôi muốn thêm, sửa và ngừng hoạt động/kích hoạt lại danh mục sách mà không làm hỏng dữ liệu sách hiện có. |
| US-02 | Là **Admin**, tôi muốn tìm kiếm, lọc theo danh mục/trạng thái và phân trang danh sách sách để quản lý kho thuận tiện. |
| US-03 | Là **Admin**, tôi muốn tạo mới, cập nhật thông tin/giá/tồn kho/ảnh bìa URL và ngừng kinh doanh sách mà vẫn bảo toàn lịch sử đơn hàng đã bán. |

## 3. Functional Requirements (EARS)
| ID | Loại | Mệnh đề | Liên kết (US / BR) |
|---|---|---|---|
| FR-01 | Event | WHEN an `ADMIN` requests `GET /admin/categories`, the system SHALL return a searchable list of categories with active/total book counts and status (`is_active`), rendering `SCR-17`. | US-01, FR-ADM-002 |
| FR-02 | Event | WHEN an `ADMIN` submits `POST /admin/categories` to create or update a category with valid unique `name` (≤ 80 chars), unique `slug` (≤ 100 chars, URL-safe), and optional `description` (≤ 300 chars), the system SHALL persist changes via JPA transaction and PRG-redirect with a success message. | US-01, FR-ADM-002, US-A-01 |
| FR-03 | Event | WHEN an `ADMIN` triggers deactivate/reactivate (`POST /admin/categories`) on an existing category, the system SHALL toggle `is_active` without physically deleting the row or cascading delete to its books. | US-01, BR-ADM-002, ADR-007 |
| FR-04 | Unwanted | IF `POST /admin/categories` submits a duplicate `name`, duplicate `slug`, or blank required fields, THEN the system SHALL reject the save and render inline field errors without modifying DB. | US-01, US-A-01 |
| FR-05 | Event | WHEN an `ADMIN` requests `GET /admin/books` with optional `keyword` (title/author/ISBN), `categoryId`, `status` (`active`/`inactive`/all), and `page`, the system SHALL return a DB-paginated list of books (`SCR-15`) including both active and inactive records. | US-02, FR-ADM-004 |
| FR-06 | Event | WHEN an `ADMIN` submits `POST /admin/books` (`SCR-16`) to create or update a book with valid `title` (≤ 200), `author` (≤ 150), active `categoryId` (for new books or when changing category), optional unique `isbn` (≤ 20), optional `publishedYear` (`1000..2200`), `price > 0` (`BigDecimal`), `stockQty >= 0`, optional safe `coverUrl`, `isFeatured`, and `isActive`, the system SHALL persist via JPA transaction and PRG-redirect to `/admin/books`. | US-03, FR-ADM-003, BR-CAT-002..005 |
| FR-07 | Unwanted | IF `POST /admin/books` receives `price <= 0`, `stockQty < 0`, duplicate non-null `isbn`, invalid `publishedYear`, an inactive/non-existent category for a new book, or an unsafe `coverUrl` scheme (e.g. `javascript:`), THEN the system SHALL reject persistence and re-render `SCR-16` with field errors and preserved form inputs. | US-03, FR-ADM-003, TC-ADM-03 |
| FR-08 | Event | WHEN an `ADMIN` toggles deactivate/reactivate on a book (`POST /admin/books`), the system SHALL update `Books.is_active` (`0` or `1`) without deleting the book row, preserving all historical `OrderItems` references and snapshots. | US-03, BR-CAT-007, BR-ADM-003, TC-ADM-04 |

## 4. Non-Functional Requirements
| ID | Hạng mục | Yêu cầu (số cụ thể) |
|---|---|---|
| NFR-01 | Phân quyền & CSRF | Mọi GET/POST đi qua `AdminAuthorizationFilter`; mọi POST qua `CsrfFilter`; không dùng GET để tạo/sửa/ngừng bán. |
| NFR-02 | Toàn vẹn dữ liệu | Dùng `BigDecimal` scale 2 cho giá; kiểm tra trùng ISBN/name/slug tại Service kết hợp bắt `PersistenceException` unique constraint từ DB. |
| NFR-03 | Hiệu năng | Danh sách Admin Books phân trang tại DB (`pageSize = 12` hoặc `15`), tránh N+1 bằng `JOIN FETCH` category hoặc DTO projection. |

## 5. Data & API Contract
**Bảng thao tác:** `Categories`, `Books` (và đọc kiểm tra FK với `OrderItems`).

**Servlet Routes (`shared_context.md` `RT-019`, `RT-020`):**
| Method + Path | Auth | Request Params | Response / Redirect | Lỗi |
|---|---|---|---|---|
| `GET /admin/categories` | Admin | `keyword?`, `editId?` | Render `views/admin/categories.jsp` | `403` nếu không phải Admin |
| `POST /admin/categories` | Admin + CSRF | `action=save\|toggleActive`, `id?`, `name`, `slug`, `description?`, `isActive?` | PRG `302 /admin/categories` | Forward kèm lỗi inline |
| `GET /admin/books` | Admin | `keyword?`, `categoryId?`, `status?`, `page?` | Render `views/admin/books.jsp` | `403` |
| `GET /admin/books/form` | Admin | `id?` (null = thêm mới, có id = sửa) | Render `views/admin/book-form.jsp` | `404` nếu `id` không tồn tại |
| `POST /admin/books` | Admin + CSRF | `action=save\|toggleActive`, fields sách | PRG `302 /admin/books` | Forward `book-form.jsp` kèm `errors` |

## 6. Edge Cases & Xử lý lỗi
| ID | Tình huống | Hành vi mong đợi |
|---|---|---|
| EC-01 | Admin để trống ô `isbn` (chuỗi trắng `""`) khi lưu 2 cuốn sách khác nhau | Chuẩn hóa chuỗi trắng về `NULL` trước khi persist để không đụng `UX_Books_isbn_not_null`. |
| EC-02 | Admin sửa một sách cũ mà danh mục của nó sau đó đã bị `is_active = 0` | Cho phép giữ nguyên danh mục hiện tại hoặc chọn sang danh mục active khác; không cho gán sang danh mục inactive mới. |
| EC-03 | Admin nhập `coverUrl = "javascript:alert(1)"` | Server từ chối — chỉ chấp nhận `http://`, `https://`, hoặc đường dẫn nội bộ `/assets/...` (hoặc rỗng). |
| EC-04 | Sách đã bán trong `OrderItems` bị ngừng kinh doanh (`is_active = 0`) | Ẩn khỏi Home/Catalogue/Detail public, nhưng `OrderItems` snapshot và trang chi tiết đơn cũ giữ nguyên. |

## 7. Acceptance Criteria (Given / When / Then)
> Mỗi FR xuất hiện ở cột "Kiểm chứng FR" của ≥ 1 AC. Test gắn thẻ `@spec admin-catalog-crud/AC-xx`.

| ID | Kịch bản | Kiểm chứng FR |
|---|---|---|
| AC-01 | **Given** Admin đăng nhập **When** mở `GET /admin/categories` và `POST /admin/categories` tạo danh mục mới `"Lịch sử"` / `"lich-su"` **Then** lưu thành công và hiển thị trong bảng kèm số sách `0`. | FR-01, FR-02 |
| AC-02 | **Given** danh mục `"Công nghệ"` đang có 5 sách **When** Admin ngừng hoạt động danh mục **Then** `is_active` chuyển `0`, không xóa vật lý sách, không lỗi FK, và danh mục không còn xuất hiện trong dropdown tạo sách mới (`TC-ADM-05`). | FR-03 |
| AC-03 | **Given** danh mục `"Công nghệ"` (`slug = "cong-nghe"`) đã tồn tại **When** Admin tạo danh mục trùng tên hoặc trùng slug **Then** từ chối lưu và hiện lỗi inline. | FR-04 |
| AC-04 | **Given** DB có cả sách active và inactive **When** Admin mở `GET /admin/books` với bộ lọc từ khóa/danh mục/trạng thái **Then** trả danh sách phân trang đúng bộ lọc (gồm cả sách inactive khi lọc tương ứng). | FR-05 |
| AC-05 | **Given** Admin điền hợp lệ form `SCR-16` (kể cả `isbn` trống được chuẩn hóa `NULL`) **When** `POST /admin/books` **Then** sách lưu vào DB với `BigDecimal` giá chính xác và xuất hiện ở public catalogue nếu `is_active = 1` (`TC-ADM-02`). | FR-06, EC-01 |
| AC-06 | **Given** Admin nhập `price = 0`, `stockQty = -1`, ISBN đã trùng, `publishedYear = 999`, hoặc `coverUrl = "javascript:alert(1)"` **When** `POST /admin/books` **Then** DB không đổi và form hiện lỗi inline đúng từng trường (`TC-ADM-03`). | FR-07, EC-02, EC-03 |
| AC-07 | **Given** sách đã có trong `OrderItems` của một đơn hàng **When** Admin ngừng kinh doanh sách đó **Then** `Books.is_active = 0`, sách biến mất khỏi storefront public, nhưng chi tiết đơn cũ vẫn giữ nguyên snapshot tên/giá (`TC-ADM-04`, `TC-DB-02`). | FR-08, EC-04 |

## 8. Out of Scope (🚫 TUYỆT ĐỐI KHÔNG LÀM)
- Không làm tính năng upload file ảnh nhị phân lên server/cloud (chỉ dùng URL hoặc đường dẫn assets có sẵn).
- Không làm nhập/xuất CSV/Excel hàng loạt hay quản lý đa kho/nhà cung cấp.
- Không cho xóa vật lý (hard-delete) sách hoặc danh mục đã từng có tham chiếu lịch sử.

---
## Phụ lục A: Ghi chú thiết kế
Tier Full — thiết kế chi tiết lập tại `PLAN.md` sau Gate 1.

## Phụ lục B: Câu hỏi mở & Phê duyệt
- [x] Q1: `SCR-16` (Book Form) không có input `book_code` (DB dùng `book_id` tự tăng + `isbn`); hiển thị mã `BK-<book_id>` dạng chỉ-đọc ở DTO khi xem/sửa — _người trả lời:_ Đã chốt (2026-11-23).

### Definition of Ready — chỉ khóa (LOCKED) khi đủ cả
- [x] Giải quyết xong Q1 ở Phụ lục B
- [x] Mỗi FR có ≥ 1 AC; `DESIGN.md` đồng duyệt cùng Gate 1
- [x] Out of Scope cụ thể; `scripts/spec-check` không báo lỗi

- **Duyệt (Gate 1):** Owner (BookHub) · 2026-11-23 · Version khóa: 1.0.0
