# REVIEW — admin-catalog-crud

> Review **độc lập** (agent/session khác, hoặc người) — prompt: `.sdd/prompts/06-review.md`. Reviewer **không sửa code**.
> Tier Full: bắt buộc `APPROVED` trước khi merge.

| Trường | Giá trị |
|---|---|
| Reviewer | Chờ Owner duyệt (bản nháp bằng chứng từ implementer) · Ngày: 2026-11-23 |
| Commit/nhánh | `feat/bookhub-mvp-01-08` (commit `89e788f` + fix JSP EL) |
| **Kết luận** | `PENDING` (chờ Owner xác nhận `APPROVED`) |

## 1. Bằng chứng chạy thật
```text
$ ant clean dist test-junit5
- AdminCatalogCrudIntegrationTest: 2/2 OK (SQL Server 2022 BookHubDB)
  - categoryCrudDuplicateRejectionAndSoftDeactivateWithoutDeletingBooks() [OK]
  - bookCrudValidationNullIsbnAndSoftDeactivatePreservingOrderSnapshots() [OK]
- Live Tomcat 10.1: /admin/books, /admin/books/form, /admin/categories → 200 OK (Admin), 403 (Customer)
```

## 2. Truy vết
| Yêu cầu | Code | Test (`@spec`) | OK? |
|---|---|---|---|
| FR-01..04 | `AdminCategoryService`, `AdminCategoryServlet`, `admin/categories.jsp` | `admin-catalog-crud/AC-01..03` | ✅ |
| FR-05..08 | `AdminBookService`, `AdminBookServlet`, `AdminBookFormServlet`, `admin/{books,book-form}.jsp` | `admin-catalog-crud/AC-04..07` | ✅ |

## 3. Kiểm tra theo hiến pháp
- [x] Không hard-delete sách/danh mục; dùng soft-deactivate (`is_active = 0`) bảo toàn `OrderItems` và FK lịch sử
- [x] Validate server-side đầy đủ (`price > 0`, `stock >= 0`, `publishedYear` hợp lệ, chuẩn hóa ISBN rỗng thành `NULL` khớp filtered unique index `UX_Books_isbn_not_null`, URL ảnh bìa an toàn)
- [x] Giữ quyết định Gate 1: không thêm cột `book_code`, chỉ format read-only `BK-00001` từ `book_id`
- [x] Bảo vệ toàn bộ route bởi `AdminAuthorizationFilter` + `CsrfFilter` + PRG

## 4. Phát hiện
| # | Mức | File:dòng | Vấn đề | Cách sửa đề xuất | Trạng thái |
|---|---|---|---|---|---|
| 1 | 🟡 | `web/WEB-INF/views/admin/books.jsp` | Biểu thức EL `${pageResult.empty}` xung đột từ khóa dành riêng `empty` | Đã thay bằng `${empty pageResult.items}` & xác minh live Tomcat 200 OK | Đã sửa |

## 5. Phê duyệt cuối
_(Chờ Owner xác nhận `APPROVED` để chốt Gate 4)_
