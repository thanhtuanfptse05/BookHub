# CLAUDE.md — Bộ nhớ & DNA dự án

> **Đọc `AGENTS.md` trước.** File này chứa *kiến thức* (dự án là gì, vì sao như vậy, đã sai ở đâu); `AGENTS.md` chứa *luật*.
> Giữ file ≤ ~300 dòng. Tài liệu yêu cầu đầy đủ: `BookHub_PRJ301_Project_Plan_Requirements.md`. Chi tiết kiến trúc/luồng: `docs/` và `.sdd/`.

## 1. Tổng quan
- **Tên:** BookHub — Website bán sách online single-store (MVP môn PRJ301)
- **Người dùng chính:** Guest (khách xem sách), Customer (mua sách, đơn hàng, review, Q&A), Admin (quản trị cửa hàng, kiểm duyệt, thống kê)
- **Giai đoạn:** `MVP`  ·  **Cập nhật lần cuối:** 2026-11-23

## 2. Tech stack & lệnh thường dùng
| Hạng mục | Giá trị |
|---|---|
| Ngôn ngữ / Runtime | Java 17 LTS (`jakarta.*` namespace, cấm trộn `javax.*`) |
| Web MVC / View | Jakarta Servlet 6.0 · JSP 3.1 + JSTL 3.0 (`jakarta.tags.*`) + EL 5.0 (cấm scriptlet `<% %>`) |
| Persistence / ORM | Jakarta Persistence (JPA) 3.1 · Hibernate ORM 6.6.59.Final (cấm DAO JDBC thuần) |
| Database | Microsoft SQL Server 2019/2022 (`BookHubDB`, `localhost:1433`, `mssql-jdbc 13.6.2.jre11`) |
| IDE / Build / Server | Apache NetBeans 17 · Apache Ant (`build.xml`) · Apache Tomcat 10.1.x |
| UI stack / Design system | Bootstrap 5.3.x + Brand CSS (`Newsreader` + `Be Vietnam Pro`, `#1e5649` / `#c85a32` / `#faf8f5`) · `.sdd/design/` (`LOCKED` từ Figma) · `docs/ui-screens.md` |
| Test | JUnit 5 (unit/service tests + JPA integration tests) |
| Build WAR | `ant dist` (hoặc NetBeans Clean & Build) |
| Test | `ant test` (khi đã cấu hình JUnit 5 test classpath) |
| Verify dự án | `./scripts/verify.ps1` |
| Chạy dev | Deploy WAR lên Apache Tomcat 10.1.x trong NetBeans 17 (`http://localhost:8080/BookHub/`) |

## 3. Kiến trúc (Layered MVC + JPA/Hibernate)
```text
src/
├── java/com/bookhub/
│   ├── config/        # JpaUtil (EMF singleton), AppConfig (bookhub-local.properties), PasswordUtil, AppConstants
│   ├── entity/        # 12 JPA Entities (Role, User, Category, Book, Cart, CartItem, Order, OrderItem, Review, Question, Answer, PasswordResetToken)
│   ├── dto/           # ViewModel/DTO (BookCardDTO, BookDetailDTO, CartItemDTO, CheckoutRequest, OrderSummaryDTO, DashboardDTO, PageResult<T>)
│   ├── repository/    # Truy vấn JPA/JPQL/Criteria (không tự commit transaction, không dùng JDBC thuần)
│   ├── service/       # Business rules, transaction boundary, ownership check, order state machine
│   ├── controller/    # Jakarta Servlets (site/, customer/, admin/) — chỉ parse input, gọi Service, forward/redirect (PRG)
│   ├── filter/        # EncodingFilter, AuthenticationFilter, AdminAuthorizationFilter, CsrfFilter
│   ├── exception/     # BusinessException, NotFoundException, AccessDeniedException, OutOfStockException
│   └── util/          # Validation, formatting, helper chung
└── conf/META-INF/persistence.xml   # BookHubPU (RESOURCE_LOCAL, SQLServerDialect, hbm2ddl.auto=validate)

web/
├── assets/            # css/, js/, images/, vendor/bootstrap/
└── WEB-INF/
    ├── web.xml        # Servlet 6.0, <scripting-invalid>true</scripting-invalid>, error pages 403/404/500
    ├── lib/           # Runtime JARs (không trùng API Tomcat cung cấp)
    └── views/         # JSP chỉ dùng JSTL 3.0 + EL (common/, site/, auth/, customer/, admin/, error/)
```
**Quy tắc phụ thuộc:** `Filter → Controller (Servlet) → Service → Repository (JPA) → Entity/DB` và `Controller → DTO/ViewModel → JSP/JSTL`. Không đảo chiều; không đặt SQL/JPA/business logic trong JSP; không truyền Entity có lazy association chưa load sang JSP.

### Luồng dữ liệu chính
```text
Browser → Filter (UTF-8, Auth, Admin, CSRF) → Servlet Controller (parse & validate)
       → Service (business rules + transaction) → Repository (JPA/Hibernate) → SQL Server (BookHubDB)
       → DTO/ViewModel → JSP (JSTL/EL) hoặc PRG Redirect → Browser
```
> Chi tiết sơ đồ bối cảnh, luồng request và ERD 12 bảng: xem [docs/architecture.md](docs/architecture.md).

## 4. Thuật ngữ nghiệp vụ (Glossary)
| Thuật ngữ | Nghĩa | Lưu ý |
|---|---|---|
| Guest | Người chưa đăng nhập | Không có bản ghi trong `Roles`/`Users`; chỉ xem catalog, review/Q&A công khai |
| Customer | Khách hàng có tài khoản (`CUSTOMER`) | Có 1 `Cart` duy nhất, đặt đơn COD, xem/hủy đơn đủ điều kiện, review, hỏi Q&A |
| Admin | Quản trị viên cửa hàng (`ADMIN`) | Quản trị `/admin/*`, trả lời Q&A, duyệt review, xem báo cáo; không tự đăng ký public |
| Order Snapshot | Bản sao `book_title_snapshot` + `unit_price` trong `OrderItems` | Bất biến; sửa/ngừng bán sách không làm sai lịch sử đơn |
| Soft Deactivate | Đặt `is_active = 0` hoặc `status = LOCKED/HIDDEN` | Cấm hard-delete Book/Category/User đã có dữ liệu lịch sử |
| COD | Thanh toán khi nhận hàng | Phương thức thanh toán duy nhất trong MVP; phí ship cố định `30.000 VND/đơn` |
| PRG | Post / Redirect / Get | Tránh submit lặp form khi F5 sau thao tác POST |

## 5. Lessons Learned (🩸 bài học xương máu)
> Agent **phải** thêm dòng mới khi: bug lặp lại, sự cố, bị người sửa cùng một lỗi ≥ 2 lần (prompt: `.sdd/prompts/10-retro.md`).
> Dòng cũ không xóa — chỉ đánh dấu `(obsolete)`.

| Ngày | Sự cố / Lỗi | Nguyên nhân gốc | Quy tắc rút ra |
|---|---|---|---|
| 2026-11-23 | `sqlcmd` báo lỗi `Msg 1934: QUOTED_IDENTIFIER` khi INSERT vào bảng có filtered index (`UX_Books_isbn_not_null`) | `sqlcmd` mặc định `QUOTED_IDENTIFIER OFF` khác với SSMS | Mọi file `.sql` SQL Server phải đặt `SET ANSI_NULLS ON; SET QUOTED_IDENTIFIER ON;` ở đầu và lưu UTF-8 BOM (`database/*.sql`) |

## 6. Vùng nguy hiểm (Known Hazards)
- **Trộn `javax.*` và `jakarta.*`:** Gây lỗi compile/deploy trên Tomcat 10.1.x. Luôn kiểm tra import và JAR trước build.
- **Checkout & tồn kho đồng thời (`CheckoutService` / `OrderService`):** Phải trừ kho atomic (`stock_qty >= qty` hoặc pessimistic lock) trong cùng transaction tạo `Orders`/`OrderItems`/xóa `CartItems`; không để stock âm.
- **Hủy đơn lặp (`OrderService.cancel`):** Phải kiểm tra và khóa trạng thái `PENDING`/`CONFIRMED` trong transaction để hoàn kho **đúng một lần**.
- **IDOR & tin giá từ client:** Mọi thao tác Cart/Order/Review phải lọc theo `currentUser.id`; checkout luôn tính lại giá và tổng tiền bằng `BigDecimal` từ DB.
- **LazyInitializationException:** Luôn build DTO/ViewModel bên trong Service transaction trước khi forward sang JSP.
- **Credential local SQL Server:** Không bao giờ commit mật khẩu DB hoặc `bookhub-local.properties` lên Git public; không expose port `1433`.

## 7. Quyết định kiến trúc đã chốt
Xem `.sdd/rfcs/` — mỗi quyết định = 1 ADR:
- [ADR-001](.sdd/rfcs/ADR-001-ant-netbeans-web-app.md) — NetBeans 17 Ant Web Application thay vì Maven _(Accepted)_
- [ADR-002](.sdd/rfcs/ADR-002-tomcat-10-jakarta-ee-10.md) — Apache Tomcat 10.1.x + Jakarta EE 10 namespace _(Accepted)_
- [ADR-003](.sdd/rfcs/ADR-003-hibernate-jpa-persistence.md) — Hibernate ORM 6.6 + JPA 3.1, cấm DAO JDBC thuần _(Accepted)_
- [ADR-004](.sdd/rfcs/ADR-004-jstl-first-no-scriptlets.md) — JSP 3.1 + JSTL 3.0/EL only, cấm Java scriptlet _(Accepted)_
- [ADR-005](.sdd/rfcs/ADR-005-cod-only-checkout.md) — Thanh toán COD duy nhất, phí ship cố định 30.000 VND _(Accepted)_
- [ADR-006](.sdd/rfcs/ADR-006-admin-only-qa-answers.md) — Chỉ Admin trả lời Q&A (1 câu trả lời/câu hỏi) _(Accepted)_
- [ADR-007](.sdd/rfcs/ADR-007-soft-deactivate-entities.md) — Soft-deactivate cho Book, Category, User _(Accepted)_
- [ADR-008](.sdd/rfcs/ADR-008-order-item-snapshots.md) — Lưu snapshot tên sách và đơn giá trong `OrderItems` _(Accepted)_
- [ADR-009](.sdd/rfcs/ADR-009-local-reset-link-output.md) — Ghi reset password URL ra server log cho local demo _(Accepted)_

## 8. Việc đang dang dở / Nợ kỹ thuật
- [ ] EPIC 0–1: Tạo `database/01_schema.sql`, `02_seed.sql`, thêm runtime JARs vào `web/WEB-INF/lib`, tạo `persistence.xml` + `JpaUtil` + 12 Entities — _ưu tiên: cao_
- [ ] EPIC 2–3: Dựng Filters (UTF-8, Auth, Admin, CSRF), error pages, common JSTL layout và module Authentication — _ưu tiên: cao_
- [ ] EPIC 4–5: Public Catalog (Home, search/filter/sort/paging, detail, similar) và Admin CRUD Books/Categories — _ưu tiên: cao_
- [ ] EPIC 6–8: Cart, Checkout COD transactional, Order lifecycle, Review moderation, Q&A và Dashboard Reports (`COUNT/SUM/AVG/MIN/MAX`) — _ưu tiên: cao_
- [ ] Cấu hình `TEST_CMD`/`BUILD_CMD` trong `.sdd/project.conf` và khởi tạo Spec Kit (`.specify/`) trước khi code feature — _ưu tiên: vừa_
