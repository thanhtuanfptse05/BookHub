# Quy Trình Nghiệp Vụ — BookHub (hiện trạng đang chạy)

> **Loại:** Mô tả hệ thống **đang chạy thật (As-Is)**, KHÔNG thay thế đặc tả yêu cầu (`BookHub_PRJ301_Project_Plan_Requirements.md` và `.sdd/specs/feat-<slug>/SPEC.md`).
> **Nguồn:** Trace trực tiếp từ `src/java/` và `web/WEB-INF/views/`. Cập nhật file này cùng PR mỗi khi hoàn thành một feature (đổi `⬜` → `✅` và điền đúng đường dẫn file thật).

| Mục | Giá trị |
|---|---|
| Cập nhật lần cuối | 2026-11-23 |
| Hiện trạng mã nguồn | Dự án mới khởi tạo skeleton NetBeans Ant (`web/index.html`, `web/META-INF/context.xml`); chưa có mã Java/JSP nghiệp vụ trong `src/java/` |
| Nguồn trace | `src/java/com/bookhub/` · `web/WEB-INF/views/` |

## Mục lục quy trình (Kế hoạch MVP → cập nhật khi implement)
| Mã | Quy trình | Ai thực hiện | Trạng thái (As-Is) | Module / Spec dự kiến |
|---|---|---|---|---|
| `QT-01` | Đăng ký, Đăng nhập, Đăng xuất, Quên/Đặt lại mật khẩu & Hồ sơ | Guest / Customer / Admin | ⬜ Chưa implement | `feat-auth` (EPIC 3) |
| `QT-02` | Xem Home, Tìm kiếm, Lọc, Sắp xếp, Phân trang & Chi tiết sách | Guest / All | ⬜ Chưa implement | `feat-catalog` (EPIC 4) |
| `QT-03` | Quản lý Giỏ hàng (Add / Update / Remove / Subtotal) | Customer | ⬜ Chưa implement | `feat-cart` (EPIC 6) |
| `QT-04` | Checkout COD nguyên tử & Trừ tồn kho | Customer | ⬜ Chưa implement | `feat-checkout-cod` (EPIC 6) |
| `QT-05` | Theo dõi & Hủy đơn hàng / Cập nhật trạng thái đơn | Customer / Admin | ⬜ Chưa implement | `feat-orders` (EPIC 6) |
| `QT-06` | Viết & Kiểm duyệt Đánh giá sách (Review) | Customer / Admin | ⬜ Chưa implement | `feat-reviews` (EPIC 7) |
| `QT-07` | Đặt câu hỏi & Trả lời / Ẩn Q&A | Customer / Admin | ⬜ Chưa implement | `feat-qa` (EPIC 7) |
| `QT-08` | Admin CRUD Sách/Danh mục, Quản lý User & Báo cáo Thống kê | Admin | ⬜ Chưa implement | `feat-admin-crud` / `feat-reports` (EPIC 5, 8) |

---

## QT-01 — Xác thực & Tài khoản _(⬜ Chưa implement — luồng mục tiêu)_
- **Ai:** Guest, Customer, Admin.
- **Đầu vào / Điều kiện:** Email hợp lệ (chuẩn hóa trim + lowercase), mật khẩu ≥ 8 ký tự có chữ và số; reset token hợp lệ trong 15 phút.
- **Luồng mục tiêu:**
  1. `POST /register` → validate server → kiểm tra email duy nhất → hash BCrypt → tạo `Users` (`CUSTOMER`, `ACTIVE`) → PRG redirect.
  2. `POST /login` → xác minh hash + trạng thái `ACTIVE` → đổi session ID → redirect trang đích an toàn.
  3. `POST /forgot-password` → trả phản hồi chung → lưu `token_hash` (TTL 15 phút) → ghi reset URL ra server log (ADR-009).
  4. `POST /reset-password` → xác minh token chưa dùng & còn hạn → lưu hash mới, đánh dấu `used_at`, vô hiệu token cũ.
- **Ngoại lệ chính:** Email trùng (409/validation error), sai thông tin (thông báo chung), tài khoản `LOCKED`/`DISABLED`, token sai/hết hạn/đã dùng.
- **File dự kiến:** `controller/site/{Register,Login,Logout,ForgotPassword,ResetPassword}Servlet.java`, `controller/customer/ProfileServlet.java`, `service/AuthService.java`, `repository/UserRepository.java`.

## QT-02 — Danh mục & Chi tiết sách _(⬜ Chưa implement — luồng mục tiêu)_
- **Ai:** Guest, Customer, Admin.
- **Đầu vào / Điều kiện:** `keyword`, `categoryId`, `minPrice`, `maxPrice`, `sort` (whitelist), `page` (mặc định 12 sách/trang).
- **Luồng mục tiêu:**
  1. `GET /` → tải 3 nhóm sách active từ DB: nổi bật (`is_featured=1`), mới nhất (`created_at DESC`), bán chạy (từ đơn `DELIVERED`).
  2. `GET /books` → truy vấn JPA parameter binding + DB pagination → forward `WEB-INF/views/site/books.jsp`.
  3. `GET /books/detail?id={id}` → tải `BookDetailDTO`, sách tương tự cùng danh mục, Review `APPROVED` và Q&A công khai.
- **Ngoại lệ chính:** Book không tồn tại hoặc `is_active = 0` → `404`; sách `stock_qty = 0` → hiển thị "Hết hàng", khóa nút thêm giỏ.
- **File dự kiến:** `controller/site/{Home,BookList,BookDetail}Servlet.java`, `service/CatalogService.java`, `repository/BookRepository.java`.

## QT-03 & QT-04 — Giỏ hàng & Checkout COD _(⬜ Chưa implement — luồng mục tiêu)_
- **Ai:** Customer (`ACTIVE`).
- **Đầu vào / Điều kiện:** Cart không rỗng, mọi sách active & `stock_qty >= quantity`, đủ tên người nhận / SĐT / địa chỉ, có CSRF token.
- **Luồng mục tiêu:**
  1. `POST /cart` → tạo/lấy Cart duy nhất của `currentUser.id` → cộng dồn/cập nhật số lượng `1..stock_qty` → tính tạm tính từ giá DB.
  2. `POST /checkout` → mở 1 JPA transaction: tải lại Cart & khóa/kiểm tra `Books.stock_qty` → tính `subtotal` & `total_amount = subtotal + 30.000` từ giá DB → tạo `Orders` (`PENDING`, `COD`) + `OrderItems` (snapshot tên & giá) → trừ `stock_qty` atomic → xóa `CartItems` → commit → PRG redirect `/orders/success`.
- **Ngoại lệ chính:** Cart rỗng, sách ngừng bán, hết tồn kho (rollback toàn bộ), double submit (PRG + guard).
- **File dự kiến:** `controller/customer/{Cart,Checkout}Servlet.java`, `service/{Cart,Checkout}Service.java`, `repository/{Cart,Order,Book}Repository.java`.

## QT-05 — Theo dõi, Hủy đơn & Cập nhật trạng thái _(⬜ Chưa implement — luồng mục tiêu)_
- **Ai:** Customer (đơn của mình), Admin (mọi đơn).
- **Luồng mục tiêu:**
  1. `GET /account/orders`, `/account/orders/detail` → lọc theo `currentUser.id` (chống IDOR).
  2. `POST /account/orders/cancel` → trong transaction, xác minh chủ đơn & trạng thái `PENDING`/`CONFIRMED` → đổi `CANCELLED`, ghi `cancelled_at`, hoàn `stock_qty` đúng 1 lần.
  3. `POST /admin/orders` → Admin chuyển trạng thái hợp lệ `PENDING → CONFIRMED → SHIPPING → DELIVERED` (hoặc hủy trước `SHIPPING`).
- **Ngoại lệ chính:** Truy cập đơn người khác (403/404), hủy đơn `SHIPPING`/`DELIVERED`/`CANCELLED`, nhảy trạng thái trái state machine.
- **File dự kiến:** `controller/customer/{MyOrders,OrderDetail}Servlet.java`, `controller/admin/AdminOrderServlet.java`, `service/OrderService.java`.

## QT-06 & QT-07 — Đánh giá (Review) & Hỏi đáp (Q&A) _(⬜ Chưa implement — luồng mục tiêu)_
- **Ai:** Customer (gửi), Admin (duyệt/trả lời/ẩn).
- **Luồng mục tiêu:**
  1. `POST /books/review` → kiểm tra Customer có đơn `DELIVERED` chứa sách & chưa review sách này → lưu `Reviews` (`PENDING`, rating 1–5). Admin duyệt `APPROVED` hoặc ẩn `HIDDEN`.
  2. `POST /books/question` → kiểm tra sách active → lưu `Questions` (`OPEN`).
  3. `POST /admin/questions` → Admin tạo `Answers` (tối đa 1/Question) và cập nhật `Questions.status = ANSWERED` trong cùng transaction, hoặc chuyển `HIDDEN`.
- **File dự kiến:** `controller/customer/{Review,Question}Servlet.java`, `controller/admin/{AdminReview,AdminQuestion}Servlet.java`, `service/{Review,Question}Service.java`.

## QT-08 — Admin CRUD & Báo cáo Thống kê _(⬜ Chưa implement — luồng mục tiêu)_
- **Ai:** Admin (`/admin/*`, bảo vệ bởi `AdminAuthorizationFilter`).
- **Luồng mục tiêu:**
  1. CRUD `Categories` & `Books` (validate giá > 0, tồn kho ≥ 0, ISBN duy nhất; dùng soft-deactivate `is_active = 0` nếu có lịch sử).
  2. Quản lý `Users` (tìm kiếm, phân trang, khóa/mở khóa Customer — cấm tự khóa Admin cuối cùng).
  3. `GET /admin/reports` → truy vấn aggregate JPA: `COUNT` đơn theo trạng thái, `SUM` doanh thu `DELIVERED`, `AVG` giá trị đơn `DELIVERED`, `MIN`/`MAX`/`AVG` giá sách active, `SUM(stock_qty)`, top bestseller (`DELIVERED`).
- **File dự kiến:** `controller/admin/{AdminDashboard,AdminBook,AdminCategory,AdminUser,AdminReport}Servlet.java`, `service/{Admin,Report}Service.java`, `repository/ReportRepository.java`.
