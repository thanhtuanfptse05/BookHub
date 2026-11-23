# shared_context.md — Nguồn sự thật chung (BookHub)

> Dùng khi có **nhiều Agent / nhiều session / nhiều người** cùng làm. Ai đổi route/DTO/contract ⇒ **cập nhật file này trong cùng PR** và thêm dòng vào mục 4.
> Nguồn đặc tả đầy đủ: `BookHub_PRJ301_Project_Plan_Requirements.md` (§13, §15.4–15.5, §17).

## 1. Servlet Routes đã chốt (Planned MVP — chưa implement trong `src/java/`)
| ID | Method + Path | Access | Controller dự kiến | Chức năng tóm tắt | Trạng thái |
|---|---|---|---|---|---|
| RT-001 | `GET /` | Public | `HomeServlet` | Home: sách nổi bật, mới nhất, bán chạy (tối đa 8/nhóm) | planned |
| RT-002 | `GET /books` | Public | `BookListServlet` | Danh mục sách: search, filter category/giá, sort whitelist, phân trang 12/trang | planned |
| RT-003 | `GET /books/detail?id={id}` | Public | `BookDetailServlet` | Chi tiết sách, sách tương tự, Review `APPROVED`, Q&A công khai | planned |
| RT-004 | `GET/POST /register` | Guest | `RegisterServlet` | Đăng ký Customer (`ACTIVE`, BCrypt hash) | planned |
| RT-005 | `GET/POST /login` | Guest | `LoginServlet` | Đăng nhập + đổi session ID sau login | planned |
| RT-006 | `POST /logout` | Auth | `LogoutServlet` | Hủy session phía server (PRG redirect) | planned |
| RT-007 | `GET/POST /forgot-password` | Guest | `ForgotPasswordServlet` | Sinh reset token (hash DB, TTL 15 phút, phản hồi chung) | planned |
| RT-008 | `GET/POST /reset-password` | Guest | `ResetPasswordServlet` | Đặt mật khẩu mới qua token hợp lệ dùng 1 lần | planned |
| RT-009 | `GET/POST /cart` | Customer | `CartServlet` | Xem/thêm/sửa/xóa dòng giỏ hàng (tính giá từ DB) | planned |
| RT-010 | `GET/POST /checkout` | Customer | `CheckoutServlet` | Xác nhận & tạo đơn COD (phí ship 30.000 VND, trừ kho atomic) | planned |
| RT-011 | `GET /orders/success?id={id}` | Customer | `OrderDetailServlet` | Trang xác nhận đặt hàng thành công (PRG) | planned |
| RT-012 | `GET /account/orders` | Customer | `MyOrdersServlet` | Lịch sử đơn của chính Customer (phân trang, mới nhất) | planned |
| RT-013 | `GET /account/orders/detail?id={id}` | Customer | `OrderDetailServlet` | Chi tiết đơn của chính Customer (ownership check) | planned |
| RT-014 | `POST /account/orders/cancel` | Customer | `OrderDetailServlet` | Hủy đơn `PENDING`/`CONFIRMED` + hoàn kho đúng 1 lần | planned |
| RT-015 | `GET/POST /account/profile` | Customer | `ProfileServlet` | Xem/sửa `full_name`, `phone` & đổi mật khẩu | planned |
| RT-016 | `POST /books/review` | Customer | `ReviewServlet` | Gửi review (`PENDING`, đã mua & `DELIVERED`, 1/user/book) | planned |
| RT-017 | `POST /books/question` | Customer | `QuestionServlet` | Đặt câu hỏi (`OPEN`) cho sách active | planned |
| RT-018 | `GET /admin` | Admin | `AdminDashboardServlet` | Dashboard KPI tổng quan | planned |
| RT-019 | `GET/POST /admin/books` | Admin | `AdminBookServlet` | Danh sách/tìm kiếm/CRUD/soft-deactivate sách | planned |
| RT-020 | `GET/POST /admin/categories` | Admin | `AdminCategoryServlet` | Danh sách/CRUD/soft-deactivate danh mục | planned |
| RT-021 | `GET/POST /admin/orders` | Admin | `AdminOrderServlet` | Danh sách/chi tiết/cập nhật trạng thái đơn theo state machine | planned |
| RT-022 | `GET/POST /admin/users` | Admin | `AdminUserServlet` | Danh sách/tìm kiếm/khóa/mở khóa Customer | planned |
| RT-023 | `GET/POST /admin/reviews` | Admin | `AdminReviewServlet` | Duyệt (`APPROVED`) / ẩn (`HIDDEN`) Review | planned |
| RT-024 | `GET/POST /admin/questions` | Admin | `AdminQuestionServlet` | Lọc (`OPEN`/`ANSWERED`/`HIDDEN`), trả lời (`ANSWERED`), ẩn (`HIDDEN`) | planned |
| RT-025 | `GET /admin/reports` | Admin | `AdminReportServlet` | Báo cáo `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`, bestseller theo khoảng ngày | planned |

> Mọi thao tác thay đổi dữ liệu dùng `POST` + CSRF token + PRG. Trang lỗi chung: `/403`, `/404`, `/500`.

## 2. Kiểu dữ liệu & DTO dùng chung (`com.bookhub.dto` & Enums)
```text
Money           = java.math.BigDecimal (scale = 2, VND; SQL DECIMAL(18,2))
DateTime        = java.time.LocalDateTime (hiển thị dd/MM/yyyy HH:mm, Asia/Ho_Chi_Minh)
PageResult<T>   = { items: List<T>, page: int, pageSize: int (default 12), totalItems: long, totalPages: int }

RoleCode        = ADMIN | CUSTOMER
UserStatus      = ACTIVE | LOCKED | DISABLED
OrderStatus     = PENDING | CONFIRMED | SHIPPING | DELIVERED | CANCELLED
ReviewStatus    = PENDING | APPROVED | HIDDEN
QuestionStatus  = OPEN | ANSWERED | HIDDEN
PaymentMethod   = COD

BookCardDTO     = { bookId, title, author, price, stockQty, coverUrl, categoryName, isFeatured }
BookDetailDTO   = { bookId, isbn, title, author, publisher, publishedYear, price, stockQty, shortDescription, description, coverUrl, categoryId, categoryName, averageRating, reviewCount }
CartItemDTO     = { cartItemId, bookId, title, coverUrl, unitPrice, stockQty, quantity, lineTotal }
CheckoutRequest = { recipientName, recipientPhone, shippingAddress, note }
OrderSummaryDTO = { orderId, orderCode, createdAt, status, subtotal, shippingFee, totalAmount, itemCount }
DashboardDTO    = { activeBooks, totalCustomers, totalOrders, pendingOrders, deliveredOrders, deliveredRevenue, avgOrderValue, minBookPrice, maxBookPrice, avgBookPrice, totalStockUnits, pendingReviews, openQuestions }
```

## 3. Sự kiện / Message dùng chung
- Không sử dụng message broker/queue trong MVP (ứng dụng web đồng bộ trên Tomcat 10.1 + SQL Server).

## 4. 🔴 Breaking Changes Log (mới nhất ở trên)
| Ngày | Thay đổi | Ảnh hưởng tới | Cách migrate | Hạn chót | Người/Agent |
|---|---|---|---|---|---|
| 2026-11-23 | Chốt danh mục routes & DTO nền tảng MVP | Toàn bộ module BookHub | Theo `BookHub_PRJ301_Project_Plan_Requirements.md` | — | Init |

## 5. Tài nguyên đang bị "khóa" (tránh đụng độ giữa Agent)
| File/Module | Ai đang sửa | Feature/Task | Từ lúc |
|---|---|---|---|
| _(trống)_ | | | |

## 6. Biến cấu hình dùng chung (chỉ TÊN, không giá trị)
Đọc bởi `AppConfig` / `JpaUtil` từ file cục bộ `bookhub-local.properties` (nằm ngoài Git):
- `db.url` — JDBC URL SQL Server (`BookHubDB`, port `1433`)
- `db.username` — tài khoản SQL Server local
- `db.password` — mật khẩu SQL Server local (**cấm commit lên Git**)
- `app.shipping.fee` (tùy chọn, mặc định `30000`)
- `app.reset.token.ttl.minutes` (tùy chọn, mặc định `15`)
