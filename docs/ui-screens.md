# Đặc tả Giao diện 25 Màn hình — BookHub (UI Screens Specification)

> **Nguồn thiết kế chính thức:** Figma Make Prototype `https://agenda-kale-62994833.figma.site`  
> **Design System & Token:** [.sdd/design/DESIGN.md](../.sdd/design/DESIGN.md) · [.sdd/design/tokens.css](../.sdd/design/tokens.css) · [.sdd/design/components.md](../.sdd/design/components.md)  
> **Ảnh baseline đối chiếu (1280px / 375px):** [.sdd/design/references/README.md](../.sdd/design/references/README.md)  
> **Cập nhật lần cuối:** 2026-11-23

---

## 1. Tổng quan 3 Khung bố cục (App Shells)

| Shell | Áp dụng cho | Cấu trúc chính | Responsive (375px) |
|---|---|---|---|
| **Storefront Shell** | `SCR-01`..`03b`, `SCR-08`..`13`, `SCR-23` | Sticky Header (`64px`, `bg-white/95 blur`, logo, nav, pill search, tài khoản, giỏ hàng + badge terracotta) + Container `max-w-1200px` trên nền `#faf8f5` + Footer `#164238` 3 cột | Header 2 hàng (hàng 1 logo + đăng nhập + giỏ + `☰`, hàng 2 search full-width); footer 1 cột |
| **Auth Shell** | `SCR-04`..`07` | Nền `#faf8f5`, không header/footer storefront, logo BookHub + card trắng `radius-2xl` căn giữa + liên kết `← Quay lại trang chủ` | Card full-width `px-4` |
| **Admin Shell** | `SCR-14`..`22` | Sidebar trái cố định `256px` nền `#164238` (logo `BookHub ADMIN`, 8 mục quản trị, mục active nền trắng chữ `#164238`) + Topbar trắng (`QT Quản trị viên`, `Đăng xuất`) + nội dung trên nền `#faf8f5` | Sidebar ẩn thành off-canvas drawer mở bằng nút `☰`; KPI xếp 1 cột; bảng cuộn ngang `.table-responsive` |

---

## 2. Danh mục đầy đủ 25 View (`SCR-01` → `SCR-23`)

### A. Public Storefront & Error Pages

| ID | Route Figma → Servlet | File JSP dự kiến | Thành phần & nội dung chính | Trạng thái & ghi chú |
|---|---|---|---|---|
| **SCR-01** | `#/` → `GET /` | `views/site/home.jsp` | Hero banner `#1e5649` (`rounded-3xl`, H1 Newsreader, CTA terracotta `Khám phá danh mục sách`, ghi chú `Phí giao cố định 30.000 ₫`) · Dải `Chọn theo thể loại` (6 thẻ desktop → 2 cột mobile) · 3 kệ sách 4×2 (tối đa 8 `BookCard`/kệ): `Sách nổi bật`, `Sách mới nhất`, `Sách bán chạy` + `Xem tất cả >` | Baseline: `scr-01__home__1280.png`, `375.png` |
| **SCR-02** | `#/books` → `GET /books` | `views/site/books.jsp` | Breadcrumb + H1 `Danh mục sách` · Bố cục 2 cột (`3/12` card `Bộ lọc`: từ khóa, danh mục, khoảng giá min–max VND, sắp xếp whitelist, nút `Áp dụng bộ lọc` · `9/12`: đếm kết quả, active filter chips có nút `×` + `Xóa bộ lọc`, lưới 3 cột 12 `BookCard`/trang, `Pagination`) | Có `EmptyState` khi 0 kết quả (`Không tìm thấy sách phù hợp` + `Xóa bộ lọc`). Baseline: `scr-02__books-catalogue__1280.png`, `375.png` |
| **SCR-03** | `#/books/detail` → `GET /books/detail?id={id}` | `views/site/book-detail.jsp` | Breadcrumb · Khối đầu 2 cột (bìa lớn + pill danh mục, H1, sao trung bình & số đánh giá, bảng meta Tác giả/NXB/Năm/ISBN, giá VND + `StatusBadge` còn hàng, `QuantityStepper`, nút `Thêm vào giỏ hàng`, mô tả ngắn) · Card `Mô tả chi tiết` · Khối 2 cột `Đánh giá của khách hàng` (điểm TB + thanh 5→1 sao + danh sách Review `APPROVED`) & form `Viết đánh giá` (1–5 sao, tiêu đề, nội dung, hộp info xanh giải thích điều kiện `DELIVERED` & chờ duyệt) · Khối 2 cột `Hỏi đáp về sách` (câu hỏi + hộp phản hồi `✓ BookHub phản hồi`) & form `Đặt câu hỏi` (đếm `0/150`, `0/1.500`) · `Sách tương tự` (4 `BookCard`) | Guest chưa login khi gửi review/câu hỏi/thêm giỏ → chuyển `/login` giữ return URL. Baseline: `scr-03__book-detail__1280.png` |
| **SCR-03b** | `#/books/detail-out` → `GET /books/detail?id={id}` | `views/site/book-detail.jsp` | Biến thể khi `stock_qty = 0`: `StatusBadge` xám `Hết hàng`, `QuantityStepper` (`0`) và nút `Thêm vào giỏ hàng` bị disabled, dòng ghi chú `Sách tạm hết hàng. Vui lòng quay lại sau.` | Trạng thái `Disabled` (FR-CAT-016) |
| **SCR-23** | `#/403` → `/403`, `/404`, `/500` | `views/error/{403,404,500}.jsp` | Card lỗi căn giữa trên Storefront Shell: `403` (`Không có quyền truy cập` + `Về trang chủ` / `Đăng nhập`), `404` (`Không tìm thấy trang` + `Về trang chủ` / `Xem danh mục sách`), `500` (`Đã có lỗi hệ thống` + `Về trang chủ`) | Tuyệt đối không hiển thị stack trace / SQL |

### B. Authentication (Auth Shell — Không OAuth)

| ID | Route Figma → Servlet | File JSP dự kiến | Thành phần & nội dung chính | Trạng thái & ghi chú |
|---|---|---|---|---|
| **SCR-04** | `#/register` → `GET/POST /register` | `views/auth/register.jsp` | H1 `Đăng ký tài khoản`, form: `Họ và tên *`, `Email *`, `Số điện thoại` (tùy chọn), `Mật khẩu *` (toggle hiện/ẩn + gợi ý tối thiểu 8 ký tự có chữ và số), `Xác nhận mật khẩu *`, nút teal `Đăng ký`, link `Đăng nhập` | Lỗi inline dưới từng field (vd email trùng), giữ dữ liệu hợp lệ |
| **SCR-05** | `#/login` → `GET/POST /login` | `views/auth/login.jsp` | H1 `Đăng nhập`, `AlertBanner` lỗi chung (`Email hoặc mật khẩu không đúng.`), `Email *`, `Mật khẩu *` (toggle + link `Quên mật khẩu?`), nút `Đăng nhập`, link `Đăng ký ngay` | Không tiết lộ email tồn tại. Baseline: `scr-05__login__1280.png` |
| **SCR-06** | `#/forgot-password` → `GET/POST /forgot-password` | `views/auth/forgot-password.jsp` | H1 `Quên mật khẩu`, ô `Email *`, nút `Gửi liên kết đặt lại`, `AlertBanner` xanh phản hồi trung tính (15 phút, chống dò email), link `Quay lại đăng nhập` | Local demo ghi URL ra server log (ADR-009) |
| **SCR-07** | `#/reset-password` → `GET/POST /reset-password` | `views/auth/reset-password.jsp` | H1 `Đặt lại mật khẩu`, `Mật khẩu mới *`, `Xác nhận mật khẩu mới *`, nút `Cập nhật mật khẩu` | Biến thể token sai/hết hạn: card cảnh báo + nút `Yêu cầu liên kết mới` |

### C. Customer Shopping & Account (Storefront Shell — Yêu cầu đăng nhập)

| ID | Route Figma → Servlet | File JSP dự kiến | Thành phần & nội dung chính | Trạng thái & ghi chú |
|---|---|---|---|---|
| **SCR-08** | `#/cart` → `GET/POST /cart` | `views/customer/cart.jsp` | H1 `Giỏ hàng` · 2 cột: trái là danh sách card sản phẩm (bìa, tên, tác giả, đơn giá DB, `QuantityStepper` + gợi ý tồn kho, thành tiền, nút `Xóa`) + `Xóa toàn bộ giỏ`; phải là `Tóm tắt đơn hàng` (`Tạm tính`, ghi chú ship `30.000 ₫` ở bước thanh toán, nút `Tiến hành đặt hàng`, link `Tiếp tục mua sách`) | Có `EmptyState` (`Giỏ hàng của bạn đang trống` + `Khám phá sách`). Baseline: `scr-08__cart__1280.png` |
| **SCR-09** | `#/checkout` → `GET/POST /checkout` | `views/customer/checkout.jsp` | Breadcrumb `Giỏ hàng / Thanh toán` · Trái: card `Thông tin giao hàng` (`Họ tên người nhận *`, `Số điện thoại *`, `Địa chỉ nhận hàng *`, `Ghi chú đơn hàng`) + card `Phương thức thanh toán` khóa duy nhất **COD** · Phải: `Tóm tắt đơn hàng` (từng sách, `Tạm tính`, `Phí vận chuyển 30.000 ₫`, `Tổng cộng`, nút `Đặt hàng`) | Bổ sung `AlertBanner` vàng khi giá/tồn kho thay đổi trước lúc xác nhận (FR-ORD). Không ô voucher. Baseline: `scr-09__checkout-cod__1280.png` |
| **SCR-10** | `#/orders/success` → `GET /orders/success?id={id}` | `views/customer/order-success.jsp` | Card xác nhận căn giữa: dấu check xanh, H1 `Đặt hàng thành công!`, mã đơn monospace (`BH-20261123-00042`), badge `Chờ xác nhận`, tóm tắt người nhận & COD, 2 nút `Xem chi tiết đơn hàng` & `Tiếp tục mua sách` | PRG sau POST `/checkout`; badge giỏ về `0` |
| **SCR-11** | `#/account/orders` → `GET /account/orders` | `views/customer/orders.jsp` | Tabs tài khoản (`Hồ sơ cá nhân` / `Đơn hàng của tôi`) · Filter pills theo 5 trạng thái · Bảng phân trang: `MÃ ĐƠN` (mono), `NGÀY ĐẶT`, `SỐ LƯỢNG`, `TỔNG TIỀN`, `TRẠNG THÁI` (`StatusBadge`), `Xem chi tiết` | Chỉ tải đơn thuộc `currentUser.id`; có `EmptyState` khi chưa có đơn |
| **SCR-12** | `#/account/orders/detail` → `GET /account/orders/detail?id={id}` | `views/customer/order-detail.jsp` | Link `← Đơn hàng của tôi`, mã đơn + `StatusBadge` + nút đỏ `Hủy đơn hàng` (chỉ hiện khi `PENDING`/`CONFIRMED`) · `OrderStepper` 4 bước · 2 card `Thông tin nhận hàng` (snapshot) & `Thông tin thanh toán` (COD) · Bảng `OrderItems` snapshot + tổng tiền | Mở `ConfirmModal` trước khi POST `/account/orders/cancel` |
| **SCR-12b** | `#/account/orders/detail-delivered` → `GET /account/orders/detail?id={id}` | `views/customer/order-detail.jsp` | Biến thể trạng thái `DELIVERED`: 4 bước hoàn tất, ẩn nút hủy, mỗi dòng sách hiển thị link `Viết đánh giá` (khi chưa review sách đó) | Khớp BR-REV-001 |
| **SCR-13** | `#/account/profile` → `GET/POST /account/profile` | `views/customer/profile.jsp` | Tabs tài khoản · Card `Thông tin cá nhân` (`Họ và tên *`, `Email` disabled, `Số điện thoại`, nút `Lưu thay đổi` + `AlertBanner` thành công) · Card `Đổi mật khẩu` (`Mật khẩu hiện tại *`, `Mật khẩu mới *`, `Xác nhận mật khẩu mới *`, nút `Cập nhật mật khẩu`) | Xem mục 3 bên dưới về trường `Địa chỉ mặc định` trong Figma |

### D. Admin Portal (Admin Shell — Yêu cầu role `ADMIN`)

| ID | Route Figma → Servlet | File JSP dự kiến | Thành phần & nội dung chính | Trạng thái & ghi chú |
|---|---|---|---|---|
| **SCR-14** | `#/admin` → `GET /admin` | `views/admin/dashboard.jsp` | 4 `KpiCard` (`Doanh thu đã giao DELIVERED`, `Tổng đơn hàng` + pill `PENDING`, `Sách đang kinh doanh`, `Tổng khách hàng`) · 2 thẻ cảnh báo viền vàng (`Đánh giá chờ duyệt`, `Câu hỏi chưa trả lời`) · 2 bảng tổng hợp (`Đơn hàng gần đây`, `Top 5 sách bán chạy`) | Baseline: `scr-14__admin-dashboard__1280.png`, `375.png` |
| **SCR-15** | `#/admin/books` → `GET/POST /admin/books` | `views/admin/books.jsp` | H1 `Quản lý sách` + nút `+ Thêm sách mới` · Thanh lọc (từ khóa, danh mục, trạng thái `Đang bán`/`Ngừng kinh doanh`) · Bảng phân trang (bìa nhỏ, tên/ISBN/tác giả, danh mục, giá, tồn kho, trạng thái, `Sửa` & `Ngừng bán`/`Mở bán`) | Dùng `ConfirmModal` cho soft-deactivate |
| **SCR-16** | `#/admin/books/form` → `GET/POST /admin/books/form` | `views/admin/book-form.jsp` | Link quay lại · Bố cục 2 cột: trái là form thông tin sách (tên, danh mục active, tác giả, NXB, năm XB, ISBN, giá > 0, tồn kho ≥ 0, mô tả ngắn/chi tiết, toggle nổi bật & đang bán, `Hủy`/`Lưu sách`); phải là ô URL `cover_url` + khung xem trước ảnh bìa | Không upload file. Xem mục 3 bên dưới về trường `book_code` trong Figma |
| **SCR-17** | `#/admin/categories` → `GET/POST /admin/categories` | `views/admin/categories.jsp` | H1 `Quản lý danh mục` + `+ Thêm danh mục` · Bảng (`TÊN`, `SLUG`, `SỐ SÁCH`, `TRẠNG THÁI`, `THAO TÁC`) + panel/modal Thêm/Sửa (`Tên *`, `Slug *`, `Mô tả`, toggle hiển thị) | Soft-deactivate thay vì hard-delete khi còn sách |
| **SCR-18** | `#/admin/orders` → `GET/POST /admin/orders` | `views/admin/orders.jsp` | Bộ lọc (`Mã đơn`, `Trạng thái`, `Từ ngày`, `Đến ngày`) · Bảng đơn phân trang · Panel `Chi tiết đơn hàng` 2 cột: trái hiển thị snapshot đơn hàng bất biến; phải là nút chuyển trạng thái **chỉ hiện bước hợp lệ theo state machine** + bảng chú thích quy tắc | Baseline: `scr-18__admin-orders__1280.png` |
| **SCR-19** | `#/admin/users` → `GET/POST /admin/users` | `views/admin/users.jsp` | Bộ lọc tìm kiếm (tên/email/SĐT) + trạng thái (`ACTIVE`/`LOCKED`/`DISABLED`) · Bảng user + `Khóa tài khoản`/`Mở khóa` + `ConfirmModal` | Khóa nút trên chính tài khoản Admin đang đăng nhập / Admin cuối cùng |
| **SCR-20** | `#/admin/reviews` → `GET/POST /admin/reviews` | `views/admin/reviews.jsp` | Filter tabs kèm bộ đếm (`Tất cả`, `Chờ duyệt`, `Đã duyệt`, `Đã ẩn`) · Bảng/thẻ đánh giá (sách, khách, sao, tiêu đề & nội dung `<c:out>`, ngày, trạng thái, nút `Duyệt` / `Ẩn`) | Chỉ `APPROVED` hiển thị public & tính sao TB |
| **SCR-21** | `#/admin/questions` → `GET/POST /admin/questions` | `views/admin/questions.jsp` | Filter tabs (`Tất cả`, `OPEN`, `ANSWERED`, `HIDDEN`) · Card câu hỏi (sách, khách, nội dung, ngày, badge) + ô textarea phản hồi Admin (1 câu trả lời/câu hỏi) + nút `Gửi phản hồi` & `Ẩn câu hỏi` | Lưu `Answers` + đổi `ANSWERED` trong 1 transaction |
| **SCR-22** | `#/admin/reports` → `GET /admin/reports` | `views/admin/reports.jsp` | Card lọc ngày (`Từ ngày`–`Đến ngày`, chú thích `Orders.created_at`) · 4 `KpiCard` có nhãn SQL (`SUM DELIVERED`, `AVG DELIVERED`, `SUM stock_qty`, `COUNT`) · Khối `Giá sách` (`MIN`/`MAX`/`AVG` chỉ sách đang bán) · Lưới 2 cột: thanh ngang `COUNT` theo 5 trạng thái (thuần HTML/CSS) + bảng `Top 10 sách bán chạy` (`SUM(quantity)` đơn `DELIVERED`) · Hộp info xanh quy ước hiển thị `0 ₫` khi trống | Baseline: `scr-22__admin-reports__1280.png` |

---

## 3. Đối soát Figma ↔ Schema DB (`01_schema.sql`) & Tinh chỉnh khi Code thật

Kiểm tra trực tiếp bản Figma `https://agenda-kale-62994833.figma.site` cho thấy **100% đúng phạm vi MVP** (không lọt tính năng Out-of-Scope như VNPay/MoMo, voucher, OAuth, upload ảnh, chart JS). Khi triển khai JSP/Servlet thật, lưu ý **6 điểm đối soát** sau:

| # | Vị trí | Quan sát ở Figma Prototype | Quyết định khi triển khai thật |
|---|---|---|---|
| 1 | `SCR-16` Book Form & `SCR-15`/`SCR-22` | Hiển thị trường `Mã sách (book_code)` dạng `BK-00101` | Bảng `dbo.Books` trong `01_schema.sql` dùng `book_id` (`BIGINT IDENTITY`) + `isbn` (không có cột `book_code`). Khi code MVP giữ đúng schema (`book_id` hiển thị dạng `#101` hoặc `BK-00101` format ở ViewModel, không thêm cột ngoài schema trừ khi có ADR/migration). |
| 2 | `SCR-13` My Profile | Có thêm ô `Địa chỉ mặc định` | Bảng `dbo.Users` trong `01_schema.sql` gồm `full_name`, `email`, `phone` (không có cột `default_address`; địa chỉ giao hàng nhập tại `Checkout` và snapshot vào `Orders.shipping_address`). Khi code MVP bỏ ô `Địa chỉ mặc định` khỏi form Profile (hoặc tạo migration bổ sung nếu người dùng quyết định thêm sau). |
| 3 | Mobile 375px Header | Nút `Đăng nhập` bị rớt 2 dòng ở màn 375px | Thêm `whitespace-nowrap` trong `bookhub.css`. |
| 4 | `SCR-14` Admin KPI | Số tiền `48.620.000 ₫` rớt ký hiệu `₫` xuống dòng mới ở 1280px | Dùng `whitespace-nowrap` và cỡ chữ responsive (`clamp(1.25rem, 1.6vw, 1.75rem)`). |
| 5 | `SCR-14` & `SCR-22` Bảng 2 cột | Hai bảng đặt cạnh nhau tại 1280px (vùng nội dung 1024px) hơi hẹp, dễ cắt cột `TRẠNG THÁI` | Bọc `.table-responsive`, tối ưu padding bảng hoặc xếp 1 cột dưới `1400px` / chia tỉ lệ `7/5`. |
| 6 | Nút nổi góc dưới-trái | Figma có nút nổi `"Danh sách màn hình"` để duyệt prototype | **Không đưa vào** code JSP thật. |
