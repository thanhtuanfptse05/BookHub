# SPEC — Trang chủ, Danh mục Sách & Chi tiết Sách công khai (public-catalog)

> **Pha 1 · Hợp đồng đặc tả.** Agent soạn nháp từ `CONTEXT.md`, **người duyệt & khóa** (Gate 1).
> Sau khi `Status: LOCKED`, sửa đổi chỉ qua `CHANGELOG.md` + người duyệt. **Chưa LOCKED ⇒ cấm code.**

| Trường | Giá trị |
|---|---|
| Feature ID | `04-feat-public-catalog` |
| Tier | Full |
| UI | Có |
| Status | `LOCKED` |
| Version | 1.0.0 |
| Owner | BookHub Team (PRJ301) |
| Liên quan | `FR-CAT-001..016`, `BR-CAT-001..008`, `shared_context.md` (`RT-001..003`), `.sdd/seo/SEO.md`, `TC-CAT-01..09` |

---

## 1. Tổng quan (Overview)
Xây dựng toàn bộ trải nghiệm xem và tìm kiếm sách công khai trên BookHub gồm Trang chủ (`/`), Danh mục sách (`/books` có search/filter/sort/phân trang DB), và Trang chi tiết sách (`/books/detail` kèm trạng thái hết hàng, sách tương tự, hiển thị chỉ-đọc Review `APPROVED` và Q&A công khai). Toàn bộ dữ liệu được truy vấn qua JPA/Hibernate thành DTO để đảm bảo hiệu năng và tránh `LazyInitializationException`. Xem `CONTEXT.md`.

## 2. Actors & User Stories
| ID | Câu chuyện |
|---|---|
| US-01 | Là **Guest/Customer**, tôi muốn xem trang chủ với sách nổi bật, sách mới, sách bán chạy và lối tắt thể loại để nhanh chóng khám phá sách. |
| US-02 | Là **Guest/Customer**, tôi muốn tìm kiếm theo tên/tác giả/ISBN, lọc theo danh mục & khoảng giá, sắp xếp và chuyển trang mà không mất tiêu chí đang chọn. |
| US-03 | Là **Guest/Customer**, tôi muốn xem đầy đủ thông tin chi tiết một cuốn sách, tình trạng còn/hết hàng, đánh giá đã duyệt, hỏi đáp đã trả lời và sách tương tự trước khi quyết định mua. |

## 3. Functional Requirements (EARS)
| ID | Loại | Mệnh đề | Liên kết (US / BR) |
|---|---|---|---|
| FR-01 | Event | WHEN a user requests `GET /`, the system SHALL load from SQL Server via JPA up to 8 active featured books (`is_featured = 1`), 8 newest active books (`created_at DESC`), up to 8 best-selling active books (ordered by total `OrderItems.quantity` from `DELIVERED` orders), and active category shortcuts with active book counts, then render `SCR-01`. | US-01, FR-CAT-001..004, BR-CAT-001 |
| FR-02 | Ubiquitous | The system SHALL exclude all books where `Books.is_active = 0` or whose parent `Categories.is_active = 0` from every public catalog query (Home, Catalogue, Search, Similar Books, Book Detail). | US-01, US-02, FR-CAT-005, BR-CAT-001 |
| FR-03 | Event | WHEN a user requests `GET /books` with optional `keyword`, `categoryId`, `minPrice`, `maxPrice`, `sort`, and `page`, the system SHALL perform a case-insensitive JPA parameterized search matching `title`, `author`, or `isbn`, apply active-category and price-range filters, and return a database-paginated `PageResult<BookCardDTO>` of 12 items per page. | US-02, FR-CAT-006..008, FR-CAT-010, NFR-PERF-002 |
| FR-04 | Unwanted | IF `minPrice < 0`, `maxPrice < minPrice`, `page < 1`, `page > totalPages`, or non-numeric filter params are submitted to `GET /books`, THEN the system SHALL handle them safely (return validation hint or clamp `page` safely without HTTP 500 or uncaught exception). | US-02, FR-CAT-008, FR-CAT-010 |
| FR-05 | Event | WHEN a `sort` parameter is supplied to `GET /books`, the system SHALL map it exclusively through a server-side allow-list (`newest`, `price_asc`, `price_desc`, `title_asc`, `bestseller`) and fall back to `newest` for any unrecognized sort key. | US-02, FR-CAT-009, NFR-SEC-002 |
| FR-06 | Ubiquitous | The system SHALL preserve all active filter/search/sort query parameters across pagination links on `SCR-02` and display active filter chips with a one-click clear-filters action. | US-02, US-G-02 |
| FR-07 | Event | WHEN a user requests `GET /books/detail?id={id}` for an active book in an active category, the system SHALL load `BookDetailDTO`, up to 4 active similar books from the same category (excluding current book), rating summary + `APPROVED` reviews only, and `ANSWERED` (non-`HIDDEN`) Q&A items, then render `SCR-03`. | US-03, FR-CAT-011..014 |
| FR-08 | State | WHILE a book has `stock_qty = 0`, the system SHALL still allow viewing `SCR-03` (`SCR-03b` variant), display the `"Hết hàng"` badge, and disable quantity selection and the `"Thêm vào giỏ hàng"` action. | US-03, FR-CAT-016, BR-CAT-006 |
| FR-09 | Unwanted | IF `GET /books/detail` receives a missing/non-numeric `id`, a non-existent book, or an inactive book/category, THEN the system SHALL respond with HTTP `404` (`SCR-23`). | US-03, FR-CAT-005, TC-CAT-06 |

## 4. Non-Functional Requirements
| ID | Hạng mục | Yêu cầu (số cụ thể) |
|---|---|---|
| NFR-01 | Hiệu năng | Trang Home, `/books`, `/books/detail` mục tiêu tải `< 3s` với 1.000 sách & 20 user đồng thời; không N+1 query ở danh sách card; phân trang 100% ở DB. |
| NFR-02 | Bảo mật | 100% JPQL/Criteria dùng named parameters (không nối chuỗi `keyword` hay `sort`); toàn bộ dữ liệu động render bằng `<c:out>`. |
| NFR-03 | SEO / SSR | HTML trả về lần đầu chứa đầy đủ `<title>`, `<meta name="description">`, `<h1>`, ảnh bìa có `alt` và fallback ảnh khi URL lỗi. |

## 5. Data & API Contract
**Bảng truy vấn (chỉ đọc):** `Categories`, `Books`, `Orders`, `OrderItems`, `Reviews`, `Questions`, `Answers`, `Users`.

**Servlet Routes (`shared_context.md` `RT-001..003`):**
| Method + Path | Auth | Query Params | View Forward | Lỗi |
|---|---|---|---|---|
| `GET /` | Public | — | `WEB-INF/views/site/home.jsp` | `500` (nếu DB lỗi) |
| `GET /books` | Public | `keyword?`, `categoryId?`, `minPrice?`, `maxPrice?`, `sort?`, `page?` | `WEB-INF/views/site/books.jsp` | Lỗi khoảng giá hiển thị tại form lọc |
| `GET /books/detail` | Public | `id` (Long) | `WEB-INF/views/site/book-detail.jsp` | `404` nếu không tồn tại hoặc inactive |

**DTOs trả ra View:** `BookCardDTO`, `BookDetailDTO`, `PageResult<BookCardDTO>`, `CategoryShortcutDTO`, `PublicReviewDTO`, `PublicQaDTO`.

## 6. Edge Cases & Xử lý lỗi
| ID | Tình huống | Hành vi mong đợi |
|---|---|---|
| EC-01 | Chưa có đơn `DELIVERED` nào trong DB khi hiển thị nhóm `Bán chạy` / `sort=bestseller` | Nhóm bán chạy xử lý sạch (trả danh sách rỗng hoặc sắp xếp ổn định theo `soldQty DESC, createdAt DESC`), không lỗi SQL/null. |
| EC-02 | Tìm kiếm không khớp bất kỳ cuốn sách nào | Hiển thị `EmptyState` (`"Không tìm thấy sách phù hợp"`) kèm nút `"Xóa bộ lọc"` về `/books`. |
| EC-03 | `sort=price; DROP TABLE Books;--` được truyền qua URL | Không bao giờ nối vào JPQL; whitelist fallback về `newest`. |
| EC-04 | Sách chưa có review `APPROVED` nào | Hiển thị trạng thái chưa có đánh giá (0 đánh giá, không chia cho 0). |

## 7. Acceptance Criteria (Given / When / Then)
> Mỗi FR xuất hiện ở cột "Kiểm chứng FR" của ≥ 1 AC. Test gắn thẻ `@spec public-catalog/AC-xx`.

| ID | Kịch bản | Kiểm chứng FR |
|---|---|---|
| AC-01 | **Given** DB có sách active (`is_featured=1`, mới nhất, và đơn `DELIVERED`) cùng 1 sách `is_active=0` **When** truy cập `GET /` **Then** Home hiển thị đúng 3 nhóm từ DB (tối đa 8 sách/nhóm), có lối tắt danh mục và không chứa sách `is_active=0` (`TC-CAT-01`). | FR-01, FR-02, EC-01 |
| AC-02 | **Given** DB có 25 sách active thuộc nhiều danh mục/giá **When** `GET /books?keyword=java&categoryId=1&minPrice=50000&maxPrice=200000&page=1` **Then** chỉ trả sách active khớp không phân biệt hoa/thường theo tên/tác giả/ISBN, đúng danh mục & giá, tối đa 12 item/trang (`TC-CAT-02`, `TC-CAT-03`). | FR-02, FR-03 |
| AC-03 | **Given** `GET /books` nhận `minPrice=-10`, `maxPrice < minPrice`, `page=999` hoặc `page=abc` **When** server xử lý **Then** không phát sinh `500`, khoảng giá lỗi có thông báo rõ ràng và `page` ngoài phạm vi được xử lý an toàn. | FR-04 |
| AC-04 | **Given** `GET /books` nhận `sort=price_asc`, `sort=price_desc`, `sort=bestseller`, và `sort=hacked_col` **When** truy vấn **Then** 3 kiểu hợp lệ sắp đúng thứ tự (`bestseller` tính từ đơn `DELIVERED`) và `hacked_col` fallback an toàn về `newest` (`TC-CAT-04`, `TC-CAT-09`). | FR-05, EC-03 |
| AC-05 | **Given** kết quả lọc có nhiều trang hoặc 0 kết quả **When** chuyển sang `page=2` hoặc lọc không khớp **Then** URL trang 2 giữ đủ `keyword/categoryId/minPrice/maxPrice/sort` (`TC-CAT-05`), và khi 0 kết quả hiển thị `EmptyState` + nút `"Xóa bộ lọc"`. | FR-06, EC-02 |
| AC-06 | **Given** sách active có sách cùng danh mục, review (`APPROVED`, `PENDING`, `HIDDEN`) và Q&A (`ANSWERED`, `OPEN`, `HIDDEN`) **When** `GET /books/detail?id={id}` **Then** hiển thị đủ thông tin sách, tối đa 4 sách tương tự active (không gồm chính nó), chỉ hiển thị + tính sao TB từ review `APPROVED`, và chỉ hiển thị Q&A `ANSWERED` không `HIDDEN` (`TC-CAT-08`). | FR-07, EC-04 |
| AC-07 | **Given** sách active có `stock_qty = 0` **When** truy cập `GET /books/detail?id={id}` **Then** trang vẫn mở được, hiển thị badge `"Hết hàng"` và khóa nút thêm vào giỏ (`TC-CAT-07`). | FR-08 |
| AC-08 | **Given** sách có `is_active = 0` hoặc `id` không tồn tại **When** truy cập `GET /books/detail?id={id}` **Then** trả về HTTP `404` (`TC-CAT-06`). | FR-02, FR-09 |

## 8. Out of Scope (🚫 TUYỆT ĐỐI KHÔNG LÀM)
- Không xử lý submit thêm vào giỏ hàng (nút gửi về `06-feat-cart-checkout-orders`).
- Không xử lý submit tạo Review mới hoặc Question mới (thuộc `07-feat-reviews-qa`).
- Không làm gợi ý sách bằng AI/ML, wishlist yêu thích, hay so sánh sách.

---
## Phụ lục A: Ghi chú thiết kế
Tier Full — thiết kế chi tiết lập tại `PLAN.md` sau Gate 1.

## Phụ lục B: Câu hỏi mở & Phê duyệt
- Không có câu hỏi mở — toàn bộ quy tắc lọc/sắp xếp/phân trang và giao diện `SCR-01`..`03b` đã rõ từ tài liệu gốc & Figma.

### Definition of Ready — chỉ khóa (LOCKED) khi đủ cả
- [x] Người duyệt xác nhận `SPEC.md` và `DESIGN.md` (Gate 1)
- [x] Mỗi FR có ≥ 1 AC; mỗi AC đo được (Given/When/Then)
- [x] Out of Scope cụ thể; `scripts/spec-check` không báo lỗi

- **Duyệt (Gate 1):** Owner (BookHub) · 2026-11-23 · Version khóa: 1.0.0
