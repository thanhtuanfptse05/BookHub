# DESIGN.md — Cẩm nang UI & Design System (BookHub)

> **File duy nhất cho quy chuẩn giao diện BookHub.** Giá trị token thực nằm trong [.sdd/design/tokens.css](tokens.css), danh mục component ở [.sdd/design/components.md](components.md), đặc tả chi tiết 25 view ở [docs/ui-screens.md](../../docs/ui-screens.md), và ảnh chụp chuẩn ở [.sdd/design/references/baseline/](references/README.md).
> **Trạng thái:** `LOCKED` (đồng bộ trực tiếp từ Figma Make prototype `https://agenda-kale-62994833.figma.site` ngày `2026-11-23`). Đổi token/màu chủ đạo sau khi LOCKED = tier **Full**.

---

## 1. Thương hiệu, Phong cách & Giọng văn *(Đã chốt từ Figma)*
- **Cá tính (3 từ):** hiệu sách hiện đại · ấm áp · tin cậy (editorial flat, ưu tiên viền giấy tinh gọn thay vì bóng đổ nặng hay gradient tím).
- **Tham khảo chính thức:** `https://agenda-kale-62994833.figma.site` + 12 ảnh chụp baseline 1280px/375px tại `.sdd/design/references/baseline/`.
- **UI Stack triển khai thật:** Bootstrap 5.3.x + `web/assets/css/tokens.css` (đồng bộ từ `.sdd/design/tokens.css`) + `web/assets/css/bookhub.css` + Vanilla JS (ES6+) + JSP 3.1 / JSTL 3.0.
- **Bảng màu thương hiệu đã chốt:**
  - Nền giấy ấm (`--color-bg`): `#faf8f5` · Bề mặt card (`--color-surface`): `#ffffff` · Viền tinh (`--color-border`): `#e6e1d8` · Viền input (`--color-border-strong`): `#c9c2b6`.
  - Màu chính **Deep Bookstore Teal** (`--color-primary`): `#1e5649` (hover / Admin sidebar / Footer: `#164238`).
  - Màu nhấn **Warm Terracotta** (`--color-accent`): `#c85a32` (hover `#a8431f`) — dùng cho nút CTA Hero, badge đếm giỏ hàng, badge `ADMIN`, sao đánh giá.
  - Chữ chính **Deep Ink** (`--color-text`): `#1a222c` · Chữ phụ (`--color-text-muted`): `#5a6472`.
  - Màu ngữ nghĩa: Success `#15803d` · Warning `#b45309` · Danger `#b91c1c` · Info `#1d4ed8`.
- **Kiểm tra tương phản WCAG 2.2 AA:**
  - `#1a222c` trên `#faf8f5` (**15.4:1** ✅) · `#5a6472` trên `#faf8f5` (**5.7:1** ✅) · `#ffffff` trên `#1e5649` (**8.6:1** ✅) · `#ffffff` trên `#164238` (**11.6:1** ✅) · 4 màu ngữ nghĩa trên nền sáng đều **≥ 5.0:1** ✅.
  - *Lưu ý kỹ thuật nhỏ:* Chữ `#ffffff` trên nền `#c85a32` đạt **4.23:1** (đạt AA cho chữ lớn/bold, sát ngưỡng 4.5:1 ở cỡ 15px); khi dùng nút nền terracotta nhỏ hãy giữ `font-weight: 600` hoặc dùng `#a8431f` (**5.9:1** ✅) nếu kiểm audit AA nghiêm ngặt ở nút chữ thường.
- **Cặp chữ hỗ trợ tiếng Việt (Google Fonts `subset=vietnamese`):**
  - Tiêu đề `h1, h2, h3`: **`Newsreader`** (500/600/700, `letter-spacing: -0.01em`, `line-height: 1.2`).
  - Thân bài & UI: **`Be Vietnam Pro`** (400/500/600/700, thân bài `16px`, `line-height: 1.5`).
  - Mã đơn (`BH-YYYYMMDD-xxxxx`), ISBN, slug: `ui-monospace`.
- **Ngôn ngữ & định dạng hiển thị:**
  - 100% tiếng Việt, giọng rõ ràng, lịch sự (xưng **"bạn"**).
  - Tiền tệ VND: `125.000 ₫` (`<fmt:formatNumber>` + `whitespace-nowrap` để không rớt ký hiệu `₫` sang dòng mới).
  - Thời gian (`Asia/Ho_Chi_Minh`): `dd/MM/yyyy HH:mm` (`<fmt:formatDate>`).
- **Công thức microcopy:**
  - Nút hành động: **Động từ + đối tượng** ("Khám phá danh mục sách", "Thêm vào giỏ hàng", "Tiến hành đặt hàng", "Xác nhận hủy đơn", "Gửi đánh giá", "Gửi câu hỏi").
  - Lỗi: **Chuyện gì + cách sửa** ("Số lượng vượt quá tồn kho hiện còn (14 cuốn). Hãy giảm số lượng.").
  - Hành động nhạy cảm: **Modal xác nhận nêu rõ đối tượng + hậu quả** ("Bạn chắc chắn muốn hủy đơn BH-20261123-00042? Tồn kho sẽ được hoàn lại.").
- **Bảo mật render view:** Mọi text động xuất ra JSP dùng `<c:out value="${...}" />`; mọi form `POST` có input ẩn CSRF token; cấm scriptlet `<% %>`.

---

## 2. Token & Quy tắc hiển thị *(AI đọc từ `tokens.css`)*
| Nhóm | Token chuẩn | Quy tắc |
|---|---|---|
| **Màu (theo vai trò)** | `--color-bg`, `--color-surface`, `--color-border`, `--color-border-strong`, `--color-text`, `--color-text-muted`, `--color-primary`, `--color-primary-hover`, `--color-accent`, `--color-shell-dark`, `--color-success/warning/danger/info` (+ `-bg`, `-border`) | **Cấm hardcode màu** (`#hex`, `rgb()`) trong JSP/CSS component (U-01). Chỉ dùng token vai trò. |
| **Chữ** | `--font-serif` (Newsreader), `--font-sans` (Be Vietnam Pro), `--font-mono`; thang `--text-xs`(12) → `--text-hero`(60) | H1 trang chuẩn `30px`, H2 `24px`, H3 `18px`, thân bài `16px`, input/CTA chính `15px`, nav/table `14px`, badge/header bảng `12px`. |
| **Khoảng cách & Khung** | Thang 4px `--space-1`..`--space-20`; `--container-max`(1200px), `--header-height`(64px), `--sidebar-width`(256px) | Không dùng số lẻ ngoài thang. Vùng chạm tối thiểu **44×44px** (`--touch-target-min`). |
| **Hình khối & Chuyển động** | `--radius-lg`(8px nút/input), `--radius-xl`(12px alert), `--radius-2xl`(16px card/modal/table), `--radius-3xl`(24px hero), `--radius-full` | Phong cách phẳng (flat-editorial): ưu tiên viền `1px solid var(--color-border)`, chỉ đổ bóng nhẹ `--shadow-sm` khi hover. Tắt motion khi `prefers-reduced-motion`. |
| **Breakpoint** | `375` (mobile) · `768` (tablet) · `1024/1280` (desktop) | Storefront grid: Home 4 cột → 2 cột mobile; Category 6 cột → 2 cột; Catalogue sidebar 3/12 + grid 3 cột → 1 cột mobile; Admin sidebar 256px sticky → off-canvas drawer mobile. |

---

## 3. Quy tắc UX, 3 Khung Bố Cục & 5 Trạng Thái Bắt Buộc
### 3 khung bố cục chính (App Shells)
1. **Public / Customer Shell:** Sticky Header (`64px`, nền trắng mờ `blur(8px)`, viền đáy `--color-border`) gồm Logo BookHub, nav `Trang chủ` / `Danh mục sách`, ô tìm kiếm dạng pill, nút `Đăng nhập` (hoặc pill viết tắt `MA Minh Anh`), icon Giỏ hàng kèm badge terracotta; Footer nền `--color-shell-dark` (`#164238`) 3 cột (`Brand`, `CHÍNH SÁCH`, `LIÊN HỆ`).
2. **Auth Shell:** Nền `--color-bg` (`#faf8f5`), không header/footer storefront, logo BookHub + card trắng `rounded-2xl` căn giữa (`max-width: 440px`) kèm liên kết `← Quay lại trang chủ`.
3. **Admin Shell:** Sidebar trái cố định `256px` nền `--color-shell-dark` (`#164238`, 8 mục + logo `BookHub ADMIN`, mục active nền trắng chữ `#164238`) + Topbar trắng (nút mở drawer mobile, avatar `QT Quản trị viên`, nút `Đăng xuất`) + vùng nội dung trên nền `--color-bg`.

### 5 trạng thái bắt buộc (U-05)
1. **Default:** Dữ liệu thật từ SQL Server qua JPA/DTO.
2. **Loading / Submitting:** Nút submit form hiện trạng thái đang xử lý + chặn bấm đúp (kết hợp PRG ở server).
3. **Empty:** Card viền nét đứt (`dashed`), icon tròn, tiêu đề Newsreader, giải thích ngắn + **1 nút CTA chính** (vd: "Không tìm thấy sách phù hợp" → "Xóa bộ lọc"; "Giỏ hàng trống" → "Khám phá sách").
4. **Error:** Lỗi từng field hiện ngay dưới ô nhập (giữ nguyên dữ liệu đã nhập); cảnh báo chung bằng `AlertBanner` đỏ; trang `/403`, `/404`, `/500` đồng nhất trên Storefront Shell.
5. **Disabled / Out of Stock:** Sách `stock_qty = 0` hiện badge `Hết hàng` trung tính, khóa quantity stepper & nút `Thêm vào giỏ hàng`; ô `Email`/`Vai trò` trong Profile bị disabled nền `--color-surface-subtle`; trạng thái đơn kết thúc (`DELIVERED`/`CANCELLED`) khóa nút chuyển trạng thái.

---

## 4. Truy cập (WCAG 2.2 AA) & Tinh chỉnh so với Prototype Figma
- **HTML ngữ nghĩa & Bàn phím:** `<header>`, `<nav>`, `<main>`, `<footer>`, `<button>` cho hành động, `<a>` cho điều hướng, `<label for>` gắn input; `:focus-visible` dùng `2px solid var(--color-focus-ring)` offset `2px`. Ảnh bìa luôn có `alt="Bìa sách {Tên sách}"` + fallback khi lỗi URL.
- **4 điểm tinh chỉnh bắt buộc khi dịch từ Figma sang JSP/Bootstrap thật:**
  1. Thêm `whitespace-nowrap` cho nút `Đăng nhập` trên mobile (`375px`) để không bị xuống 2 dòng.
  2. Thêm `whitespace-nowrap` và chỉnh cỡ chữ linh hoạt cho số tiền VND trong 4 thẻ KPI Admin (`48.620.000 ₫`) để không rớt ký hiệu `₫`.
  3. Tại `SCR-14` (Admin Dashboard) và `SCR-22` (Admin Reports), bọc bảng trong `.table-responsive` và phân tỉ lệ cột hợp lý (hoặc xếp dọc dưới `1280px`) để không bị cắt cột `TRẠNG THÁI`.
  4. **Loại bỏ** nút nổi `"Danh sách màn hình"` góc dưới-trái (chỉ là công cụ xem thử của prototype Figma).

---

## 5. Quy trình dịch từ Figma sang JSP/Bootstrap thật
> **Nguyên tắc: Đồng bộ Token — Lấy ảnh baseline làm chuẩn — KHÔNG copy nguyên JSX/Tailwind từ Figma vào `web/`.**
1. **Token chuẩn:** Đồng bộ `.sdd/design/tokens.css` → `web/assets/css/tokens.css` và ánh xạ sang biến Bootstrap 5.3 (`--bs-primary`, `--bs-body-bg`, `--bs-body-color`, `--bs-border-color`, `--bs-font-sans-serif`) trong `web/assets/css/bookhub.css`.
2. **Dịch theo Feature:** Khi triển khai từng feature (`.sdd/specs/feat-<slug>/DESIGN.md`), đối chiếu trực tiếp màn hình tương ứng trong [docs/ui-screens.md](../../docs/ui-screens.md) và ảnh chụp trong `.sdd/design/references/baseline/`.
3. **Kiểm chứng khi code xong:** Chụp ảnh thật ứng dụng chạy trên Tomcat ở **375px / 768px / 1280px**, đối chiếu với ảnh baseline và chạy `./scripts/verify.ps1` xanh.
