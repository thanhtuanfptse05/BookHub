# DESIGN — Quản trị Sách & Danh mục (admin-catalog-crud)

> **Bản thiết kế gọn cho feature có UI.** Tuân thủ Admin Shell và token tại `.sdd/design/DESIGN.md` & `tokens.css`.

| Trường | Giá trị |
|---|---|
| Feature | `05-feat-admin-catalog-crud` |
| Status | `LOCKED` |
| Tham chiếu Stitch/Figma | `https://agenda-kale-62994833.figma.site/#/admin/books` (`SCR-15`, `SCR-16`, `SCR-17`) · `docs/ui-screens.md` |

## 1. Màn hình & Luồng đi
| ID | Màn hình / View | Vào từ đâu | Mục đích |
|---|---|---|---|
| SCR-15 | Quản lý sách (`views/admin/books.jsp`) | Sidebar Admin `Sách` | Tìm kiếm, lọc, phân trang, mở form sửa và bật/tắt kinh doanh sách |
| SCR-16 | Form thêm/sửa sách (`views/admin/book-form.jsp`) | Nút `+ Thêm sách mới` / `Sửa` ở `SCR-15` | Tạo mới hoặc cập nhật chi tiết sách + xem trước ảnh bìa URL |
| SCR-17 | Quản lý danh mục (`views/admin/categories.jsp`) | Sidebar Admin `Danh mục` | Bảng danh mục kèm panel/form thêm/sửa & soft-deactivate |

```text
SCR-15 Books List ──► "+ Thêm sách mới" / "Sửa" ──► SCR-16 Book Form ──(PRG)──► SCR-15 (flash xanh)
                  └──► "Ngừng bán" ──► ConfirmModal ──(POST/PRG)──► SCR-15 (status "Ngừng bán")
SCR-17 Categories ──► Inline/Drawer Form (Thêm/Sửa) hoặc ConfirmModal (Ngừng hoạt động) ──(PRG)──► SCR-17
```

## 2. Component & Token dùng
- **Dùng lại từ `components.md`:** `AdminShell`, `Button` (`primary-teal`, `outline-neutral`), `FormControl`, `StatusBadge` (`Đang bán`, `Ngừng bán`), `AlertBanner`, `Pagination`, `ConfirmModal`, `EmptyState`.
- **Component mới:** Khung xem trước ảnh bìa live-preview theo `cover_url` trên `SCR-16` (JS tối giản ~15 dòng, fallback về `book-placeholder.svg` khi lỗi tải ảnh).
- **Token mới ngoài `tokens.css`?** Không.

## 3. Trạng thái, Responsive & Copy (chỉ ghi điểm riêng)
- **Trạng thái:**
  - **Empty:** Khi lọc sách/danh mục không ra kết quả → `EmptyState` + nút `"Xóa bộ lọc"`.
  - **Error:** Lỗi validation giá/tồn kho/ISBN/slug hiển thị ngay dưới ô tương ứng và giữ nguyên dữ liệu đã nhập.
  - **Confirm:** Ngừng kinh doanh sách hoặc danh mục bắt buộc mở `ConfirmModal` giải thích sách sẽ ẩn khỏi trang bán nhưng vẫn giữ lịch sử đơn hàng.
- **Responsive (375 vs 1280):** Bảng sách/danh mục bọc `.table-responsive`; `SCR-16` 2 cột (form trái + preview ảnh bìa phải) xếp dọc 1 cột trên mobile `375px`.
- **Copy / i18n chính:** `"+ Thêm sách mới"`, `"Lưu sách"`, `"Ngừng kinh doanh sách này? Sách sẽ ẩn khỏi trang bán nhưng vẫn giữ trong lịch sử đơn hàng."`, `"Mở bán lại"`.
- **a11y cần lưu ý:** Switch/checkbox `Sách nổi bật` & `Đang kinh doanh` có `<label for>` rõ ràng; cảnh báo tồn kho thấp dùng chữ/badge kèm màu.
- **SEO/AEO/GEO:** Toàn bộ `SCR-15..17` là khu vực Admin — bắt buộc `noindex, nofollow`.

## 4. Tiêu chí nghiệm thu bằng mắt (VAC) & Duyệt
- [ ] **VAC-01:** `SCR-15`, `SCR-16`, `SCR-17` đồng bộ Admin Shell (`#164238` sidebar + nền `#faf8f5`), không có vùng dropzone upload ảnh.
- [ ] **VAC-02:** Live preview ảnh bìa URL, lỗi validation inline và `ConfirmModal` soft-deactivate hoạt động rõ ràng ở 375px và 1280px.
- **Duyệt (cùng Gate 1):** Owner (BookHub) · 2026-11-23
