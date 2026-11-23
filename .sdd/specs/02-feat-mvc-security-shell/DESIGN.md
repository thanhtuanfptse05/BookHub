# DESIGN — Khung MVC, Bảo mật Filter & UI Shell dùng chung (mvc-security-shell)

> **Bản thiết kế gọn cho feature có UI.** Giá trị mặc định (màu, chữ, spacing, a11y) đã khóa tại `.sdd/design/DESIGN.md` & `tokens.css`.

| Trường | Giá trị |
|---|---|
| Feature | `02-feat-mvc-security-shell` |
| Status | `LOCKED` |
| Tham chiếu Stitch/Figma | `https://agenda-kale-62994833.figma.site/#/403` · `.sdd/design/references/baseline/scr-01__home__1280.png`, `scr-01__home__375.png`, `scr-14__admin-dashboard__1280.png`, `scr-14__admin-dashboard__375.png` |

## 1. Màn hình & Luồng đi
| ID | Màn hình / View | Vào từ đâu | Mục đích |
|---|---|---|---|
| SHELL-01 | Storefront Shell (`header.jsp`, `nav.jsp`, `footer.jsp`) | Mọi trang public & customer | Khung điều hướng, tìm kiếm, trạng thái tài khoản, badge giỏ hàng và chân trang |
| SHELL-02 | Auth Shell | `/login`, `/register`, `/forgot-password`, `/reset-password` | Khung card trắng căn giữa trên nền giấy `#faf8f5` |
| SHELL-03 | Admin Shell (`layout-sidebar.jsp`, `layout-topbar.jsp`) | Mọi trang `/admin/*` | Sidebar 256px `#164238` (8 mục) + Topbar trắng + drawer mobile |
| SCR-23 | Error Pages (`403.jsp`, `404.jsp`, `500.jsp`) | Tomcat error mapping / Filter / Exception | Thông báo lỗi thân thiện kèm nút điều hướng an toàn |

```text
Request lỗi quyền → 403.jsp ("Không có quyền truy cập" → "Về trang chủ" / "Đăng nhập")
URL không tồn tại → 404.jsp ("Không tìm thấy trang" → "Về trang chủ" / "Xem danh mục sách")
Lỗi máy chủ       → 500.jsp ("Đã có lỗi hệ thống" → "Về trang chủ")
```

## 2. Component & Token dùng
- **Dùng lại từ `components.md`:** Toàn bộ 14 component nền (`StorefrontHeader`, `StorefrontFooter`, `AdminShell`, `BookCard`, `Button`, `FormControl`, `StatusBadge`, `AlertBanner`, `QuantityStepper`, `Pagination`, `OrderStepper`, `EmptyState`, `ConfirmModal`, `KpiCard`).
- **Component mới (nếu bắt buộc):** Không có — feature này hiện thực hóa chính danh mục `components.md`.
- **Token mới ngoài `tokens.css`?** Không — ánh xạ trực tiếp từ `.sdd/design/tokens.css` sang `web/assets/css/tokens.css`.

## 3. Trạng thái, Responsive & Copy (chỉ ghi điểm riêng)
- **Trạng thái:**
  - **Loading:** Nút submit form thêm class `.is-loading` & `disabled` khi gửi form; phản hồi sau redirect bằng `messages.jsp`.
  - **Empty:** Fragment `empty-state.jsp` dùng viền nét đứt, icon tròn, tiêu đề Newsreader và 1 nút CTA chính.
  - **Error:** 3 trang `403`/`404`/`500` độc lập với dữ liệu DB (vẫn render sạch ngay cả khi DB gặp sự cố).
- **Responsive (375 vs 1280):**
  - Storefront Header 375px: 2 hàng (logo + nút `Đăng nhập` có `whitespace-nowrap` + giỏ + `☰`; hàng 2 là pill search full-width).
  - Admin Shell 375px: ẩn sidebar thành Bootstrap Offcanvas drawer, hiện nút `☰` ở Topbar.
- **Copy / i18n chính:** `"Không có quyền truy cập"`, `"Không tìm thấy trang"`, `"Đã có lỗi hệ thống"`, `"Về trang chủ"`, `"Xem danh mục sách"`.
- **a11y cần lưu ý:** Skip-to-content link, `<nav aria-label>`, `:focus-visible` `2px solid #1e5649`, `ConfirmModal` đóng bằng `Esc` và trả focus về nút mở.
- **SEO/AEO/GEO:** Các trang lỗi `403`/`404`/`500` và toàn bộ Admin Shell đặt `<meta name="robots" content="noindex, nofollow">`.

## 4. Tiêu chí nghiệm thu bằng mắt (VAC) & Duyệt
- [ ] **VAC-01:** Ở 375px và 1280px, Storefront Header/Footer và Admin Shell khớp ảnh baseline trong `.sdd/design/references/baseline/`, không cuộn ngang, nút `Đăng nhập` không rớt 2 dòng.
- [ ] **VAC-02:** Các trang `/403`, `/404`, `/500` hiển thị đúng bố cục, đủ nút quay về và không có thông tin kỹ thuật.
- **Duyệt (cùng Gate 1):** Owner (BookHub) · 2026-11-23
