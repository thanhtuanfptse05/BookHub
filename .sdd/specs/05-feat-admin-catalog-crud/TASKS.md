# TASKS — Quản trị Sách & Danh mục (`05-feat-admin-catalog-crud`)

> **Pha 3 · Phân rã từ `PLAN.md`.** Task atomic ≤ 4h · gắn thẻ `@spec admin-catalog-crud/AC-xx`.

**Trạng thái:** `⬜ todo` · `🔄 doing` · `✅ done` · `⛔ blocked`

| ID | Task | Phụ thuộc | Ước lượng | Truy vết | Trạng thái |
|---|---|---|---|---|---|
| T-001 | Tạo DTOs (`AdminCategoryRowDTO`, `CategoryFormDTO`, `AdminBookRowDTO`, `BookFormDTO`) & mở rộng `CategoryRepository`, `BookRepository` | — | 2.5h | FR-01, FR-05 | ✅ |
| T-002 | Tạo `AdminCategoryService` + integration tests (`AC-01..03`) | T-001 | 2.5h | FR-01..04, AC-01..03 | ✅ |
| T-003 | Tạo `AdminBookService` + unit/integration tests (`AC-04..07`) | T-001 | 3.5h | FR-05..08, EC-01..04, AC-04..07 | ✅ |
| T-004 | Tạo `AdminCategoryServlet` + `views/admin/categories.jsp` (`SCR-17`) | T-002 | 2.5h | FR-01..04, AC-01..03 | ✅ |
| T-005 | Tạo `AdminBookServlet`, `AdminBookFormServlet` + `views/admin/{books,book-form}.jsp` (`SCR-15`, `SCR-16`) + live preview (Gate 4 `APPROVED`) | T-003 | 3.5h | FR-05..08, AC-04..07, VAC-01..02 | ✅ |

## Chi tiết task

### T-001 — DTOs & Admin Repository Queries
- **Mục tiêu:** Cung cấp query kiểm tra trùng `name`/`slug`/`isbn` (loại trừ `id` hiện tại khi sửa), đếm số sách theo danh mục và phân trang danh sách sách Admin (gồm cả `is_active = 0`).
- **File dự kiến:** `src/java/com/bookhub/dto/Admin*.java`, `BookFormDTO.java`, `CategoryFormDTO.java`, `repository/{CategoryRepository,BookRepository}.java`.
- **Acceptance:**
  - [ ] Truy vấn Admin Books có `JOIN FETCH` category (0 N+1) và phân trang DB.

### T-002 — `AdminCategoryService` & Tests
- **Mục tiêu:** CRUD danh mục với validate trùng tên/slug và soft-deactivate không phá FK.
- **File dự kiến:** `src/java/com/bookhub/service/AdminCategoryService.java`, `tests/integration/AdminCategoryServiceTest.java`.
- **Acceptance:**
  - [ ] Pass `@spec admin-catalog-crud/AC-01`, `AC-02`, `AC-03` (`TC-ADM-05`).

### T-003 — `AdminBookService` & Tests
- **Mục tiêu:** CRUD sách với chuẩn hóa `isbn` rỗng → `NULL`, validate `price > 0`, `stock >= 0`, `publishedYear 1000..2200`, `coverUrl` an toàn, giữ danh mục cũ khi edit và soft-deactivate bảo toàn `OrderItems`.
- **File dự kiến:** `src/java/com/bookhub/service/AdminBookService.java`, `tests/unit/BookFormValidatorTest.java`, `tests/integration/AdminBookServiceTest.java`.
- **Acceptance:**
  - [ ] Pass `@spec admin-catalog-crud/AC-04`..`AC-07` (`TC-ADM-02..04`, `TC-DB-02`).

### T-004 & T-005 — Admin Servlets & JSP Views (`SCR-15`, `SCR-16`, `SCR-17`)
- **Mục tiêu:** Dựng 3 màn hình quản trị trong Admin Shell dùng JSTL `<c:out>`, `ConfirmModal` cho soft-deactivate, live preview ảnh bìa URL (`cover-preview.js`), kiểm trực quan 375/1280 và chạy `scripts/verify.ps1`.
- **File dự kiến:** `controller/admin/{AdminCategoryServlet,AdminBookServlet,AdminBookFormServlet}.java`, `web/WEB-INF/views/admin/{categories,books,book-form}.jsp`, `web/assets/js/cover-preview.js`.
- **Acceptance:**
  - [ ] Đạt `VAC-01..02`; không có dropzone upload file; `scripts/verify.ps1` xanh.

## Đồ thị phụ thuộc
```text
T-001 ─┬─► T-002 ──► T-004
       └─► T-003 ──► T-005
```

## Phê duyệt (Gate 3)
Owner (BookHub) · 2026-11-23
