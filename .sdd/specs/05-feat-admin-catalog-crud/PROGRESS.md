# PROGRESS — Quản trị Sách & Danh mục (`05-feat-admin-catalog-crud`)

| Trường | Giá trị |
|---|---|
| Feature | `05-feat-admin-catalog-crud` · Tier: Full · Nhánh: `feat/bookhub-mvp-01-08` |
| Pha hiện tại | `verifying` (code & 2/2 integration tests xanh + live Tomcat verify; chờ Owner duyệt `REVIEW.md`) |
| Task đang làm | `T-005` (đã hoàn tất code, test `AC-01..07`, live preview & fix EL `.empty`; chờ Gate 4 `REVIEW.md`) |
| Cập nhật | 2026-11-23 |

## 1. Plan của task hiện tại
- [x] `T-001`: DTOs (`AdminCategoryRowDTO`, `AdminBookRowDTO`, `BookFormDTO`) & truy vấn quản trị trong `CategoryRepository`, `BookRepository` (kèm check trùng `name`/`slug`/`isbn` loại trừ `id`, phân trang DB)
- [x] `T-002`: `AdminCategoryService` (CRUD, validate trùng, soft-deactivate không phá FK sách)
- [x] `T-003`: `AdminBookService` (CRUD, chuẩn hóa `isbn` rỗng → `NULL`, validate giá/tồn/năm/`coverUrl`, soft-deactivate bảo toàn `OrderItems`, hiển thị `BK-00001` read-only)
- [x] `T-004`: `AdminCategoryServlet` (`/admin/categories`) + `views/admin/categories.jsp` (`SCR-17`)
- [ ] `T-005`: `AdminBookServlet`, `AdminBookFormServlet` + `views/admin/{books,book-form}.jsp` (`SCR-15`, `SCR-16`) & 2/2 integration tests xanh — **chờ Owner duyệt `REVIEW.md` Gate 4**

## 2. Nhật ký thực thi
| Giờ | Việc đã làm | Kết quả |
|---|---|---|
| 2026-11-23 | Triển khai `T-001`..`T-005` & `AdminCatalogCrudIntegrationTest`, commit `89e788f` | 2/2 integration tests PASS trên SQL Server 2022 (`@spec admin-catalog-crud/AC-01..07`) |
| 2026-11-23 | Sửa `${pageResult.empty}` trong `admin/books.jsp` & verify live Tomcat 10.1 | `/admin/books`, `/admin/books/form`, `/admin/categories` đều `200 OK` |

## 3. Kiểm tra
Test ✅ (2/2 JUnit 5 `AdminCatalogCrudIntegrationTest`) · Build ✅ (`dist/BookHub.war`, 0 warning) · ui-check ✅ · spec-check ✅ · Secret scan ✅

## 4. Blockers / câu hỏi cho người
- ⬜ Chờ Owner review & duyệt `.sdd/specs/05-feat-admin-catalog-crud/REVIEW.md` (Gate 4).

## 5. Quyết định & giả định
- `book_code` hiển thị dạng `BK-00001` tính read-only từ `book_id` qua `FormatUtil.formatBookCode()` (không thêm cột vào schema theo Gate 1 Q1).

## 6. Handoff
- **Đã xong:** Toàn bộ CRUD & soft-deactivate Books/Categories (`RT-019`, `RT-020`, `SCR-15`..`SCR-17`).
- **Làm tiếp:** Sau khi Owner duyệt `REVIEW.md` → đánh `APPROVED` và đóng `T-005`.
