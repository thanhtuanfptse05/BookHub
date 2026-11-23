# PROGRESS — Trang chủ, Danh mục Sách & Chi tiết Sách công khai (`04-feat-public-catalog`)

| Trường | Giá trị |
|---|---|
| Feature | `04-feat-public-catalog` · Tier: Full · Nhánh: `feat/bookhub-mvp-01-08` |
| Pha hiện tại | `done` (Owner đã duyệt `REVIEW.md` `APPROVED` — đóng đủ `T-001`..`T-006`) |
| Task đang làm | — (đã hoàn tất toàn bộ `T-001`..`T-006`) |
| Cập nhật | 2026-11-23 |

## 1. Plan của task hiện tại
- [x] `T-001`: Bộ DTO công khai (`PageResult<T>`, `BookSortOption`, `BookFilterCriteria`, `BookCardDTO`, `CategoryShortcutDTO`, `PublicReviewDTO`, `PublicQaDTO`, `BookDetailDTO`, `HomePageDTO`)
- [x] `T-002`: Mở rộng `BookRepository` & `CategoryRepository` (Home 3 kệ active tối đa 8/nhóm, search/filter/sort whitelist + phân trang DB, sách tương tự, Review `APPROVED` & Q&A `ANSWERED`)
- [x] `T-003`: `CatalogService` + `PublicCatalogIntegrationTest` (`@spec public-catalog/AC-01..08`)
- [x] `T-004`: `HomeServlet` (`urlPatterns = {""}`) + `views/site/home.jsp` (`SCR-01`)
- [x] `T-005`: `BookListServlet` (`/books`) + `views/site/books.jsp` (`SCR-02`, filter chips, pagination, empty state)
- [x] `T-006`: `BookDetailServlet` (`/books/detail`) + `views/site/book-detail.jsp` (`SCR-03`, `SCR-03b`) & kiểm tra live Tomcat — Owner đã duyệt `REVIEW.md` (`APPROVED`)

## 2. Nhật ký thực thi
| Giờ | Việc đã làm | Kết quả |
|---|---|---|
| 2026-11-23 | Triển khai `T-001`..`T-006` & `PublicCatalogIntegrationTest`, commit `5a0ee73` | 3/3 integration tests PASS trên SQL Server 2022 (`@spec public-catalog/AC-01..08`) |
| 2026-11-23 | Sửa biểu thức EL `${pageResult.empty}` → `${empty pageResult.items}` & verify live Tomcat 10.1 | `/`, `/books`, `/books/detail?id=1` trả `200 OK`; ID sai/inactive trả `404` |

## 3. Kiểm tra
Test ✅ (3/3 JUnit 5 `PublicCatalogIntegrationTest`) · Build ✅ (`dist/BookHub.war`, 0 warning) · ui-check ✅ · spec-check ✅ · Secret scan ✅

## 4. Blockers / câu hỏi cho người
- ⬜ Chờ Owner review & duyệt `.sdd/specs/04-feat-public-catalog/REVIEW.md` (Gate 4).

## 5. Quyết định & giả định
- `HomeServlet` ánh xạ `urlPatterns = {""}` (chuỗi rỗng, không dùng `"/"`) để `DefaultServlet` của Tomcat vẫn phục vụ `/assets/*` và `/robots.txt` bình thường.

## 6. Handoff
- **Đã xong:** Toàn bộ 3 trang công khai (`RT-001`..`RT-003`, `SCR-01`..`SCR-03b`).
- **Làm tiếp:** Sau khi Owner duyệt `REVIEW.md` → đánh `APPROVED` và đóng `T-006`.
