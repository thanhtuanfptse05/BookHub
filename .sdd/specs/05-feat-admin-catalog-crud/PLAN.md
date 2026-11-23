# PLAN — Quản trị Sách & Danh mục (`05-feat-admin-catalog-crud`)

> **Pha 2 · Thiết kế kiến trúc** sau khi SPEC & DESIGN v1.0.0 đã `LOCKED`. Duyệt (Gate 2) cùng `TASKS.md`.

| Trường | Giá trị |
|---|---|
| SPEC version | 1.0.0 |
| Status | `DRAFT` |

## 1. Tóm tắt giải pháp
Xây dựng `AdminCategoryService` và `AdminBookService` dùng JPA transaction quản lý trọn vòng đời CRUD + soft-deactivate (`is_active = 0/1`, `ADR-007`) cho `Categories` và `Books`. Tách `AdminBookFormServlet` (`GET /admin/books/form`) với `AdminBookServlet` (`GET/POST /admin/books`) để giữ mỗi Servlet gọn gàng (< 150 dòng). Mọi input được chuẩn hóa ở Service (chuyển `isbn` rỗng thành `NULL`, kiểm tra `coverUrl` allow-list `http(s)://` hoặc `/assets/`, validate `BigDecimal price > 0`, `stock >= 0`, `publishedYear 1000..2200`).

## 2. Phương án đã cân nhắc
| Phương án | Ưu | Nhược | Chọn? |
|---|---|---|---|
| A: Tách `AdminCategoryService` + `AdminBookService`, validate server + bắt `PersistenceException` unique DB, soft-deactivate duy nhất | Rõ trách nhiệm, an toàn lịch sử FK `OrderItems`, dễ test độc lập | Cần đồng bộ hiển thị sách/danh mục inactive trên bảng Admin | ✅ |
| B: Cho phép hard-delete sách nếu kiểm tra chưa có `OrderItems` | Xóa bớt dòng | Dễ va chạm khóa ngoại với `CartItems`/`Reviews`/`Questions` và lệch chính sách `ADR-007` | ❌ |

## 3. Kiến trúc & luồng xử lý
```text
GET  /admin/categories  → AdminCategoryServlet → AdminCategoryService.listWithBookCount() → views/admin/categories.jsp (SCR-17)
POST /admin/categories  → [AdminFilter + CsrfFilter] → save (unique name/slug) | toggleActive → PRG /admin/categories
GET  /admin/books       → AdminBookServlet → AdminBookService.searchAdminBooks(filter, page) → views/admin/books.jsp (SCR-15)
GET  /admin/books/form  → AdminBookFormServlet (id? → load edit DTO + active categories) → views/admin/book-form.jsp (SCR-16)
POST /admin/books       → [AdminFilter + CsrfFilter] → save (normalize blank ISBN→NULL, check coverUrl/category/price/stock) | toggleActive → PRG /admin/books
```

## 4. Thay đổi theo tầng
| Tầng | File tạo mới | File sửa | Ghi chú |
|---|---|---|---|
| `dto/` | `AdminCategoryRowDTO`, `CategoryFormDTO`, `AdminBookRowDTO`, `BookFormDTO` | — | Format chỉ-đọc `BK-<book_id>` ở DTO (Gate 1 Q1) |
| `repository/` | — | `CategoryRepository`, `BookRepository` (admin list/count, `existsByName`, `existsBySlug`, `existsByIsbnExcludingId`) | 100% parameter binding, phân trang DB |
| `service/` | `AdminCategoryService`, `AdminBookService` | — | Validation + normalize `isbn` + `coverUrl` check |
| `controller/` | `admin/{AdminCategoryServlet,AdminBookServlet,AdminBookFormServlet}` | — | PRG sau mọi POST |
| `web/views/` | `admin/{categories,books,book-form}.jsp` | `web/assets/js/cover-preview.js` (~15 dòng) | Khớp `SCR-15..17`, `noindex`, `ConfirmModal` |
| `tests/` | `tests/unit/BookFormValidatorTest.java`, `tests/integration/AdminCatalogCrudIntegrationTest.java` | — | Gắn `@spec admin-catalog-crud/AC-01..07` |

## 5. Data model & Migration
- Dùng nguyên vẹn `Categories` và `Books` trong `01_schema.sql` (không thêm cột `book_code`).

## 6. API / Contract & ảnh hưởng chéo
- Hiện thực hóa `RT-019` và `RT-020` trong `.sdd/shared_context.md`.
- Khi ngừng bán sách (`is_active = 0`) sẽ lập tức ẩn khỏi `04-feat-public-catalog` và bị chặn thêm mới ở `06-feat-cart-checkout-orders`, nhưng giữ nguyên snapshot `OrderItems`.

## 7. Truy vết SPEC → thiết kế
| Yêu cầu | Thành phần đáp ứng | Test dự kiến |
|---|---|---|
| FR-01, FR-02, FR-04 | `AdminCategoryServlet` + `AdminCategoryService.save()` | AC-01, AC-03 |
| FR-03 | `AdminCategoryService.toggleActive()` (no hard-delete) | AC-02 |
| FR-05 | `AdminBookServlet` + `BookRepository.searchAdminBooks()` | AC-04 |
| FR-06, FR-07, EC-01..03 | `AdminBookFormServlet` + `AdminBookService.save()` | AC-05, AC-06 |
| FR-08, EC-04 | `AdminBookService.toggleActive()` + kiểm tra `OrderItems` snapshot | AC-07 |

## 8. Chiến lược kiểm thử
- **Unit:** Validate giá (`<=0`), tồn kho (`<0`), năm XB (`999`, `2201`), `isbn` rỗng → `NULL`, `coverUrl` chặn `javascript:`.
- **Integration (SQL Server test DB):** Đủ `TC-ADM-01`..`TC-ADM-05` + `TC-DB-02` (sửa/ngừng bán sách không làm đổi snapshot `OrderItems`).
- **Visual:** Kiểm `SCR-15`, `SCR-16`, `SCR-17` ở 375/1280, live preview ảnh bìa, `ConfirmModal`.

## 9. Rủi ro & giảm thiểu
| # | Rủi ro | Xác suất | Tác động | Giảm thiểu / Rollback |
|---|---|---|---|---|
| R1 | Trùng ISBN hoặc slug do 2 tab Admin submit song song | Thấp | Trung bình | Check trước ở Service + bắt `PersistenceException` unique constraint trả lỗi thân thiện tại form |
| R2 | Sửa sách cũ đang gắn danh mục đã ngừng hoạt động bị lỗi dropdown | Trung bình | Trung bình | Khi edit, thêm danh mục hiện tại của sách vào options nếu nó đang inactive, nhưng chặn đổi sang danh mục inactive khác (`EC-02`) |

## 10. Rollout & Quan sát
- Log `INFO` thao tác tạo/sửa/toggle Book & Category kèm `adminUserId` và `entityId`. Rollback qua nhánh `feat/admin-catalog-crud`.

## 11. ❓ Câu hỏi cho con người
- Không còn câu hỏi mở (Q1 đã khóa ở Gate 1).

## 12. Phê duyệt (Gate 2)
(chờ duyệt) · —
