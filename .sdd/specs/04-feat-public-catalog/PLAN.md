# PLAN — Trang chủ, Danh mục Sách & Chi tiết Sách công khai (`04-feat-public-catalog`)

> **Pha 2 · Thiết kế kiến trúc** sau khi SPEC & DESIGN v1.0.0 đã `LOCKED`. Duyệt (Gate 2) cùng `TASKS.md`.

| Trường | Giá trị |
|---|---|
| SPEC version | 1.0.0 |
| Status | `DRAFT` |

## 1. Tóm tắt giải pháp
Triển khai `CatalogService` kết hợp `BookRepository`, `CategoryRepository` (và truy vấn đọc-chỉ `Reviews` `APPROVED`, `Questions`/`Answers` công khai) dùng **JPA DTO projection** + `setFirstResult`/`setMaxResults` phân trang tại SQL Server (12 sách/trang) để triệt tiêu hoàn toàn `N+1` và `LazyInitializationException`. 5 kiểu sắp xếp (`newest`, `price_asc`, `price_desc`, `title_asc`, `bestseller`) được map qua enum `BookSortOption` ở server (whitelist tuyệt đối). 3 Servlet công khai (`HomeServlet`, `BookListServlet`, `BookDetailServlet`) render SSR bằng JSTL 3.0 khớp Figma `SCR-01`..`SCR-03b` và chuẩn SEO trong `.sdd/seo/SEO.md`.

## 2. Phương án đã cân nhắc
| Phương án | Ưu | Nhược | Chọn? |
|---|---|---|---|
| A: JPA Criteria/JPQL động với named parameters + DTO projection (`BookCardDTO`, `BookDetailDTO`) + `BookSortOption` enum | Chống SQL/JPQL injection 100%, không N+1, không lazy exception trên JSP, phân trang DB thật | Code query rõ ràng hơn cần 2 query (data + count) | ✅ |
| B: Trả thẳng Entity `List<Book>` sang JSP với `FetchType.EAGER` | Ít lớp DTO | Kéo dư dữ liệu, dễ `N+1`/`LazyInitializationException`, lộ cấu trúc bảng | ❌ |

## 3. Kiến trúc & luồng xử lý
```text
GET /             → HomeServlet → CatalogService.getHomeData() → [featured(8), newest(8), bestseller(8), categoryShortcuts] → views/site/home.jsp
GET /books        → BookListServlet (parse BookFilterCriteria + BookSortOption.from(sort))
                  → CatalogService.searchBooks(criteria, page, 12) → PageResult<BookCardDTO> → views/site/books.jsp
GET /books/detail → BookDetailServlet (parse id) → CatalogService.getBookDetail(id)
                  → [BookDetailDTO, similarBooks(4), approvedReviews + ratingSummary, answeredQaList]
                  → views/site/book-detail.jsp (hoặc 404 nếu inactive/not found)
```

## 4. Thay đổi theo tầng
| Tầng | File tạo mới | File sửa | Ghi chú |
|---|---|---|---|
| `dto/` | `BookCardDTO`, `BookDetailDTO`, `BookFilterCriteria`, `BookSortOption`, `CategoryShortcutDTO`, `PageResult<T>`, `PublicReviewDTO`, `PublicQaDTO`, `RatingSummaryDTO` | — | Không chứa Entity lazy |
| `repository/` | — | `BookRepository`, `CategoryRepository` (bổ sung query Home, search/filter/sort/paging, similar, public review/Q&A read) | 100% parameter binding, lọc `is_active = 1` |
| `service/` | `CatalogService` | — | Clamp page an toàn, validate khoảng giá |
| `controller/` | `site/{HomeServlet,BookListServlet,BookDetailServlet}` | `web.xml` (welcome/route `/` về `HomeServlet`) | SSR + SEO meta attributes |
| `web/views/` | `site/{home,books,book-detail}.jsp` | `common/header.jsp` (nối search bar về `/books`) | Khớp `SCR-01`..`03b`, dùng `book-card.jsp` & `pagination.jsp` |
| `tests/` | `tests/unit/BookSortOptionTest.java`, `tests/integration/CatalogServiceIntegrationTest.java` | — | Gắn `@spec public-catalog/AC-01..08` |

## 5. Data model & Migration
- Chỉ đọc trên các bảng đã có (`Categories`, `Books`, `Orders`, `OrderItems`, `Reviews`, `Questions`, `Answers`, `Users`) tận dụng 3 index `IX_Books_category_active`, `IX_Books_active_created`, `IX_Books_active_price`.

## 6. API / Contract & ảnh hưởng chéo
- Hiện thực hóa `RT-001`, `RT-002`, `RT-003` trong `.sdd/shared_context.md`.
- Trên `SCR-03`, khu nút `"Thêm vào giỏ hàng"` trỏ sẵn `POST /cart` (kích hoạt ở `06`) và form Review/Q&A trỏ `POST /books/review`, `/books/question` (kích hoạt ở `07`).

## 7. Truy vết SPEC → thiết kế
| Yêu cầu | Thành phần đáp ứng | Test dự kiến |
|---|---|---|
| FR-01, FR-02, EC-01 | `HomeServlet` + `CatalogService.getHomeData()` | AC-01 |
| FR-03, FR-04 | `BookListServlet` + `BookRepository.searchActiveBooks()` | AC-02, AC-03 |
| FR-05, EC-03 | `BookSortOption` enum whitelist (fallback `newest`) | AC-04 |
| FR-06, EC-02 | `books.jsp` filter chips + `pagination.jsp` (`<c:url>`/`<c:param>`) + `empty-state.jsp` | AC-05 |
| FR-07, FR-08, FR-09, EC-04 | `BookDetailServlet` + `CatalogService.getBookDetail()` + `book-detail.jsp` (`SCR-03`/`03b`) | AC-06, AC-07, AC-08 |

## 8. Chiến lược kiểm thử
- **Unit:** `BookSortOption.from()` với payload injection, validate `minPrice`/`maxPrice`/`page`.
- **Integration (SQL Server test DB):** Đủ 9 kịch bản `TC-CAT-01`..`TC-CAT-09` (kèm dữ liệu sách inactive, sách hết hàng, đơn `DELIVERED` vs chưa giao, review `APPROVED` vs `PENDING`/`HIDDEN`).
- **Visual & SEO:** Chụp ảnh 375/1280 cho `SCR-01`, `SCR-02`, `SCR-03`, `SCR-03b` đối chiếu 5 ảnh baseline; kiểm view-source có `<h1>`, `<title>`, `<meta description>`, `alt`.

## 9. Rủi ro & giảm thiểu
| # | Rủi ro | Xác suất | Tác động | Giảm thiểu / Rollback |
|---|---|---|---|---|
| R1 | Query `bestseller` bỏ mất sách chưa từng có đơn `DELIVERED` trên `/books?sort=bestseller` | Trung bình | Trung bình | Dùng `LEFT JOIN` + `COALESCE(SUM(CASE WHEN o.status = 'DELIVERED' THEN oi.quantity ELSE 0 END), 0)` |
| R2 | URL tìm kiếm tiếng Việt bị lỗi ký tự khi bấm sang trang 2 | Thấp | Trung bình | Dùng `<c:url>` + `<c:param>` của JSTL 3.0 để tự động URL-encode UTF-8 |

## 10. Rollout & Quan sát
- Kiểm SQL log trong dev mode để xác nhận số query cố định trên Home và Catalogue (0 N+1). Rollback qua nhánh `feat/public-catalog`.

## 11. ❓ Câu hỏi cho con người
- Không có câu hỏi mở.

## 12. Phê duyệt (Gate 2)
(chờ duyệt) · —
