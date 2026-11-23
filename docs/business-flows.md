# Quy Trình Nghiệp Vụ — BookHub (hiện trạng đang chạy)

> **Loại:** Mô tả hệ thống **đang chạy thật (As-Is)**, KHÔNG thay thế đặc tả yêu cầu (`BookHub_PRJ301_Project_Plan_Requirements.md` và `.sdd/specs/<NN>-feat-<slug>/SPEC.md`).
> **Nguồn:** Trace trực tiếp từ `src/java/` và `web/WEB-INF/views/`.

| Mục | Giá trị |
|---|---|
| Cập nhật lần cuối | 2026-11-23 |
| Hiện trạng mã nguồn | Đã triển khai đủ 8 tính năng MVP (`01`..`08`) trên nhánh `feat/bookhub-mvp-01-08`: 116 file Java, 34 file JSP (JSTL 3.0 + EL 5.0, 0 scriptlet), 25 routes hoạt động trên Tomcat 10.1 + SQL Server 2022 (chờ duyệt Gate 4 `REVIEW.md`) |
| Nguồn trace | `src/java/com/bookhub/` · `web/WEB-INF/views/` |

## Mục lục quy trình (As-Is trên `feat/bookhub-mvp-01-08`)
| Mã | Quy trình | Ai thực hiện | Trạng thái (As-Is) | Module / Spec |
|---|---|---|---|---|
| `QT-01` | Đăng ký, Đăng nhập, Đăng xuất, Quên/Đặt lại mật khẩu & Hồ sơ | Guest / Customer / Admin | ✅ Đã triển khai & test (chờ Gate 4) | `03-feat-auth-account` |
| `QT-02` | Xem Home, Tìm kiếm, Lọc, Sắp xếp, Phân trang & Chi tiết sách | Guest / All | ✅ Đã triển khai & test (chờ Gate 4) | `04-feat-public-catalog` |
| `QT-03` | Quản lý Giỏ hàng (Add / Update / Remove / Subtotal) | Customer | ✅ Đã triển khai & test (chờ Gate 4) | `06-feat-cart-checkout-orders` |
| `QT-04` | Checkout COD nguyên tử & Trừ tồn kho | Customer | ✅ Đã triển khai & test (chờ Gate 4) | `06-feat-cart-checkout-orders` |
| `QT-05` | Theo dõi & Hủy đơn hàng / Cập nhật trạng thái đơn | Customer / Admin | ✅ Đã triển khai & test (chờ Gate 4) | `06-feat-cart-checkout-orders` |
| `QT-06` | Viết & Kiểm duyệt Đánh giá sách (Review) | Customer / Admin | ✅ Đã triển khai & test (chờ Gate 4) | `07-feat-reviews-qa` |
| `QT-07` | Đặt câu hỏi & Trả lời / Sửa phản hồi / Ẩn Q&A | Customer / Admin | ✅ Đã triển khai & test (chờ Gate 4) | `07-feat-reviews-qa` |
| `QT-08` | Admin CRUD Sách/Danh mục, Quản lý User & Báo cáo Thống kê | Admin | ✅ Đã triển khai & test (chờ Gate 4) | `05-feat-admin-catalog-crud` / `08-feat-admin-users-reports` |

---

## QT-01 — Xác thực & Tài khoản
- **Ai:** Guest, Customer, Admin.
- **Đầu vào / Điều kiện:** Email hợp lệ (trim + lowercase), mật khẩu ≥ 8 ký tự có chữ và số; reset token SHA-256 hash trong DB, TTL 15 phút, dùng 1 lần.
- **Luồng đang chạy:**
  1. `POST /register` → `RegisterServlet` → `AuthService.register()` kiểm tra email duy nhất, hash BCrypt (`PasswordUtil`), tạo `User` (`CUSTOMER`, `ACTIVE`) → PRG redirect `/login`.
  2. `POST /login` → `LoginServlet` → `AuthService.authenticate()` (chỉ báo `"Tài khoản của bạn đã bị khóa."` sau khi mật khẩu đúng) → `request.changeSessionId()`, lưu `SessionUser` & `cartCount` → redirect an toàn qua `UrlSafeUtil`.
  3. `POST /forgot-password` → `ForgotPasswordServlet` → `AuthService.requestPasswordReset()` lưu SHA-256 token hash (TTL 15 phút), ghi URL qua `LocalLogResetLinkNotifier` và trả thông báo chung.
  4. `POST /reset-password` → `ResetPasswordServlet` → `AuthService.resetPassword()` tiêu thụ token atomic (`markUsedAtomically`), đổi mật khẩu và vô hiệu các token còn mở.
  5. `GET/POST /account/profile` → `ProfileServlet` → `UserService.updateProfile()` (allow-list `fullName`, `phone`) & `changePassword()`.
- **File thật:** `controller/site/{Register,Login,Logout,ForgotPassword,ResetPassword}Servlet.java`, `controller/customer/ProfileServlet.java`, `service/{AuthService,UserService,LocalLogResetLinkNotifier}.java`, `repository/{UserRepository,RoleRepository,PasswordResetTokenRepository}.java`, `web/WEB-INF/views/auth/*.jsp`, `web/WEB-INF/views/customer/profile.jsp`.

## QT-02 — Danh mục & Chi tiết sách
- **Ai:** Guest, Customer, Admin.
- **Đầu vào / Điều kiện:** `keyword`, `categoryId`, `minPrice`, `maxPrice`, `sort` (whitelist `BookSortOption`), `page` (12 sách/trang).
- **Luồng đang chạy:**
  1. `GET /` (`HomeServlet` mapped `""`) → `CatalogService.loadHomePage()` tải 3 nhóm sách active (nổi bật, mới nhất, bán chạy từ đơn `DELIVERED`, tối đa 8/nhóm) + danh mục shortcut → `site/home.jsp`.
  2. `GET /books` (`BookListServlet`) → `CatalogService.searchCatalog()` lọc `Books.is_active = 1 AND Categories.is_active = 1`, phân trang DB → `site/books.jsp`.
  3. `GET /books/detail?id={id}` (`BookDetailServlet`) → `CatalogService.loadBookDetail()` trả `BookDetailDTO`, sách tương tự, Review `APPROVED`, Q&A `ANSWERED` (hoặc `404` nếu inactive/không tồn tại) → `site/book-detail.jsp`.
- **File thật:** `controller/site/{Home,BookList,BookDetail}Servlet.java`, `service/CatalogService.java`, `repository/{BookRepository,CategoryRepository}.java`, `web/WEB-INF/views/site/{home,books,book-detail}.jsp`.

## QT-03 & QT-04 — Giỏ hàng & Checkout COD
- **Ai:** Customer (`ACTIVE`).
- **Luồng đang chạy:**
  1. `GET/POST /cart` (`CartServlet`) → `CartService` quản lý 1 `Cart`/User, validate sách active & `1..stockQty`, tính tạm tính bằng `BigDecimal` từ DB và cập nhật `cartCount` session.
  2. `GET/POST /checkout` (`CheckoutServlet`) → `CheckoutService.placeOrder()` trong 1 JPA transaction: khóa `Books` theo `bookId ASC` với `em.refresh(..., PESSIMISTIC_WRITE)`, trừ kho bằng conditional `UPDATE` (`decrementStockAtomically`, xác nhận `updated == 1`), tạo `Orders` (`PENDING`, `COD`, phí ship 30.000 VND) + `OrderItems` snapshot, xóa `CartItems`, retry mã đơn `BH-YYYYMMDD-xxxxx` nếu trùng → PRG redirect `/orders/success?id={id}`.
- **File thật:** `controller/customer/{Cart,Checkout,OrderSuccess}Servlet.java`, `service/{CartService,CheckoutService}.java`, `repository/{CartRepository,OrderRepository,BookRepository}.java`, `web/WEB-INF/views/customer/{cart,checkout,order-success}.jsp`.

## QT-05 — Theo dõi, Hủy đơn & Cập nhật trạng thái
- **Ai:** Customer (chỉ đơn của mình), Admin (mọi đơn).
- **Luồng đang chạy:**
  1. `GET /account/orders`, `/account/orders/detail?id={id}` (`MyOrdersServlet`, `OrderDetailServlet`) → `OrderService` kiểm tra ownership `order.user.userId == currentUserId`.
  2. `POST /account/orders/cancel` (`OrderCancelServlet`) → `OrderService.cancelCustomerOrder()` khóa `Order` (`PESSIMISTIC_WRITE`), kiểm tra `PENDING`/`CONFIRMED`, đổi `CANCELLED` và hoàn kho bằng `incrementStockAtomically` đúng 1 lần.
  3. `GET/POST /admin/orders` (`AdminOrderServlet`) → `OrderService.updateOrderStatusByAdmin()` tuân thủ `OrderStateMachine` (hoàn kho đúng 1 lần nếu chuyển sang `CANCELLED` từ `PENDING`/`CONFIRMED`).
- **File thật:** `controller/customer/{MyOrders,OrderDetail,OrderCancel}Servlet.java`, `controller/admin/AdminOrderServlet.java`, `service/OrderService.java`, `util/OrderStateMachine.java`, `web/WEB-INF/views/customer/{orders,order-detail}.jsp`, `web/WEB-INF/views/admin/orders.jsp`.

## QT-06 & QT-07 — Đánh giá (Review) & Hỏi đáp (Q&A)
- **Ai:** Customer (gửi), Admin (duyệt/trả lời/sửa phản hồi/ẩn).
- **Luồng đang chạy:**
  1. `POST /books/review` (`ReviewServlet`) → `ReviewService.submitReview()` kiểm tra `OrderRepository.hasDeliveredPurchase()`, chống duplicate `(user_id, book_id)`, lưu `PENDING` (rating `1..5`). Admin duyệt `APPROVED`/`HIDDEN` qua `AdminReviewServlet`.
  2. `POST /books/question` (`QuestionServlet`) → `QuestionService.askQuestion()` kiểm tra sách active, lưu `OPEN`.
  3. `POST /admin/questions` (`AdminQuestionServlet`) → `QuestionService.answerQuestion()` (tạo 1 `Answer` + đổi `ANSWERED` trong 1 TX), `updateAnswer()` (cập nhật phản hồi duy nhất theo Gate 1 Q1), hoặc `hideQuestion()` (`HIDDEN`).
- **File thật:** `controller/customer/{Review,Question}Servlet.java`, `controller/admin/{AdminReview,AdminQuestion}Servlet.java`, `service/{ReviewService,QuestionService}.java`, `repository/{ReviewRepository,QuestionRepository}.java`, `web/WEB-INF/views/admin/{reviews,questions}.jsp`.

## QT-08 — Admin CRUD, Quản lý User & Báo cáo Thống kê
- **Ai:** Admin (`/admin/*`, bảo vệ bởi `AdminAuthorizationFilter`).
- **Luồng đang chạy:**
  1. `AdminCategoryServlet` & `AdminBookServlet`/`AdminBookFormServlet` → `AdminCategoryService` & `AdminBookService` thực hiện CRUD, chuẩn hóa ISBN rỗng → `NULL`, soft-deactivate bảo toàn lịch sử đơn.
  2. `AdminUserServlet` → `AdminUserService` tìm kiếm/lọc/phân trang (không trả `passwordHash`), khóa/mở khóa và chặn tự khóa chính mình hoặc khóa Admin `ACTIVE` cuối cùng.
  3. `AdminDashboardServlet` (`/admin`) & `AdminReportServlet` (`/admin/reports`) → `ReportService` + `ReportRepository` chạy trực tiếp SQL/JPQL `COUNT`, `SUM`, `AVG`, `MIN`, `MAX` với `COALESCE` và biên ngày nửa mở `[fromDate, toDate + 1)`.
- **File thật:** `controller/admin/{AdminCategory,AdminBook,AdminBookForm,AdminUser,AdminDashboard,AdminReport}Servlet.java`, `service/{AdminCategoryService,AdminBookService,AdminUserService,ReportService}.java`, `repository/ReportRepository.java`, `web/WEB-INF/views/admin/{categories,books,book-form,users,dashboard,reports}.jsp`.
