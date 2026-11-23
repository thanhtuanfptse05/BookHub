# SPEC — Nền tảng Database SQL Server & JPA/Hibernate (foundation-jpa)

> **Pha 1 · Hợp đồng đặc tả.** Agent soạn nháp từ `CONTEXT.md`, **người duyệt & khóa** (Gate 1).
> Sau khi `Status: LOCKED`, sửa đổi chỉ qua `CHANGELOG.md` + người duyệt. **Chưa LOCKED ⇒ cấm code.**

| Trường | Giá trị |
|---|---|
| Feature ID | `01-feat-foundation-jpa` |
| Tier | Full |
| UI | Không |
| Status | `LOCKED` |
| Version | 1.0.0 |
| Owner | BookHub Team (PRJ301) |
| Liên quan | `constitution.md` (H-01, H-04, H-07, A-03, A-07, A-09), `constraints/global.md`, `ADR-001..003`, `ADR-007..008`, `TC-DB-01` |

---

## 1. Tổng quan (Overview)
Thiết lập tầng nền cơ sở dữ liệu Microsoft SQL Server (`BookHubDB`) gồm 12 bảng chuẩn hóa kèm script khởi tạo/seed/demo queries và cấu hình tầng Persistence **Jakarta Persistence 3.1 + Hibernate ORM 6.6** trong dự án NetBeans 17 Ant. Feature cung cấp bộ 12 JPA Entities, Enum trạng thái, cơ chế nạp cấu hình cục bộ không lưu mật khẩu vào Git (`AppConfig`/`JpaUtil`) và khung Repository/Transaction dùng chung cho toàn bộ các module nghiệp vụ phía sau. Xem `CONTEXT.md`.

## 2. Actors & User Stories
| ID | Câu chuyện |
|---|---|
| US-01 | Là **Developer**, tôi muốn dựng lại cơ sở dữ liệu `BookHubDB` sạch kèm dữ liệu mẫu nhất quán bằng script SQL để mọi máy dev/chấm bài chạy giống nhau. |
| US-02 | Là **Developer**, tôi muốn ứng dụng khởi tạo `EntityManagerFactory` đọc thông tin DB từ file cục bộ ngoài Git và tự động kiểm tra khớp schema (`validate`) để phát hiện sớm sai lệch mapping. |
| US-03 | Là **Developer**, tôi muốn có đầy đủ 12 JPA Entity, Enum trạng thái và tiện ích thực thi transaction ở Service để mọi tính năng nghiệp vụ truy cập DB 100% qua JPA/Hibernate. |

## 3. Functional Requirements (EARS)
| ID | Loại | Mệnh đề | Liên kết (US / BR) |
|---|---|---|---|
| FR-01 | Ubiquitous | The system SHALL provide SQL Server scripts `database/01_schema.sql`, `database/02_seed.sql`, `database/03_demo_queries.sql` and `database/README_DATABASE.md` defining all 12 tables (`Roles`, `Users`, `Categories`, `Books`, `Carts`, `CartItems`, `Orders`, `OrderItems`, `Reviews`, `Questions`, `Answers`, `PasswordResetTokens`) with PK, FK, UNIQUE, CHECK constraints and indexes. | US-01, NFR-DATA-001 |
| FR-02 | Event | WHEN `02_seed.sql` is executed on a clean `BookHubDB`, the system SHALL populate 2 roles (`ADMIN`, `CUSTOMER`), 6–8 active categories, 20–30 books, 1 admin and 2–3 customer accounts with pre-hashed BCrypt passwords (no plaintext), plus consistent sample orders (`PENDING`, `CONFIRMED`, `DELIVERED`), reviews (`PENDING`, `APPROVED`, `HIDDEN`) and Q&A (`OPEN`, `ANSWERED`). | US-01, BR-ACC-004 |
| FR-03 | Ubiquitous | The system SHALL declare `BookHubPU` (`transaction-type="RESOURCE_LOCAL"`, Hibernate `SQLServerDialect`, `hibernate.hbm2ddl.auto=validate`) in `src/conf/META-INF/persistence.xml` without hardcoded production/local passwords. | US-02, H-01, ADR-003 |
| FR-04 | Event | WHEN the web application starts via a `ServletContextListener`, the system SHALL load DB connection overrides (`db.url`, `db.username`, `db.password`) via `AppConfig` from an external/uncommitted `bookhub-local.properties` file (or environment variables) and initialize a single singleton `EntityManagerFactory` in `JpaUtil`. | US-02, A-03, A-07 |
| FR-05 | Event | WHEN the web application shuts down, the system SHALL close the singleton `EntityManagerFactory` cleanly to release all DB connections. | US-02, A-03 |
| FR-06 | Unwanted | IF `bookhub-local.properties` is missing required keys OR the database schema does not match JPA entity mappings during `validate`, THEN the system SHALL fail fast at startup with a clear diagnostic log without printing `db.password`. | US-02, H-01, H-05 |
| FR-07 | Ubiquitous | The system SHALL map all 12 tables as JPA entities under `com.bookhub.entity` using `jakarta.persistence.*`, `IDENTITY` primary keys, `BigDecimal` (`precision=18, scale=2`) for money columns, `@Enumerated(EnumType.STRING)` for status enums, `FetchType.LAZY` on N:1 relations, and no `CascadeType.REMOVE` across historical commerce entities. | US-03, ADR-007, ADR-008 |
| FR-08 | Ubiquitous | The system SHALL provide transaction helper / base repository infrastructure so that each service operation opens a dedicated `EntityManager`, commits on success, rolls back on any `RuntimeException`/error, and always closes the `EntityManager` in a `finally` block. | US-03, A-03 |
| FR-09 | Ubiquitous | The system SHALL package only Tomcat 10.1 / Java 17 / Jakarta EE 10 compatible runtime JARs in `web/WEB-INF/lib/` (Hibernate `6.6.59.Final`, JPA `3.1`, `mssql-jdbc 13.6.2.jre11`, JSTL `3.0`, BCrypt, SLF4J) and exclude legacy `javax.*` JARs and Tomcat-provided APIs. | US-02, H-09, ADR-001, ADR-002 |

## 4. Non-Functional Requirements
| ID | Hạng mục | Yêu cầu (số cụ thể) |
|---|---|---|
| NFR-01 | Tương thích | Biên dịch & chạy sạch trên JDK 17 LTS + NetBeans 17 Ant + Apache Tomcat 10.1.x + SQL Server 2019/2022 (`0` import `javax.servlet.*`/`javax.persistence.*`). |
| NFR-02 | Bảo mật | `0` mật khẩu thật trong `persistence.xml`, `02_seed.sql` (chỉ BCrypt hash) và Git repo; `bookhub-local.properties` nằm trong `.gitignore`. |
| NFR-03 | Toàn vẹn dữ liệu | Mọi cột tiền dùng `DECIMAL(18,2)` / `BigDecimal`; các ràng buộc `CK_Books_price`, `CK_Books_stock`, `CK_Orders_total`, `CK_OrderItems_line_total`, `UX_Books_isbn_not_null` thực thi ở cả DB lẫn Java. |
| NFR-04 | Hiệu năng & Thread-safety | `EntityManagerFactory` khởi tạo 1 lần duy nhất; không chia sẻ `EntityManager` giữa các luồng; các cột lọc chính có index theo `01_schema.sql`. |

## 5. Data & API Contract
**Mô hình dữ liệu (12 bảng trong `dbo` thuộc `BookHubDB` — chi tiết DDL tại `BookHub_PRJ301_Project_Plan_Requirements.md` §17.5):**
```text
Roles(role_id INT PK, role_code VARCHAR(20) UQ, role_name NVARCHAR(50))
Users(user_id BIGINT PK, role_id INT FK, full_name NVARCHAR(100), email VARCHAR(255) UQ, phone VARCHAR(20), password_hash NVARCHAR(255), status VARCHAR(20) CK, created_at, updated_at)
Categories(category_id INT PK, name NVARCHAR(80) UQ, slug VARCHAR(100) UQ, description NVARCHAR(300), is_active BIT, created_at, updated_at)
Books(book_id BIGINT PK, category_id INT FK, isbn VARCHAR(20) UX-filtered, title NVARCHAR(200), author NVARCHAR(150), publisher, published_year SMALLINT CK, price DECIMAL(18,2) CK>0, stock_qty INT CK>=0, short_description, description, cover_url, is_featured BIT, is_active BIT, created_at, updated_at)
Carts(cart_id BIGINT PK, user_id BIGINT FK UQ, created_at, updated_at)
CartItems(cart_item_id BIGINT PK, cart_id BIGINT FK, book_id BIGINT FK, quantity INT CK>0, UQ(cart_id, book_id), created_at, updated_at)
Orders(order_id BIGINT PK, order_code VARCHAR(30) UQ, user_id BIGINT FK, recipient_name, recipient_phone, shipping_address, note, subtotal, shipping_fee, total_amount CK=subtotal+shipping_fee, payment_method='COD', status CK(5), created_at, updated_at, cancelled_at)
OrderItems(order_item_id BIGINT PK, order_id BIGINT FK, book_id BIGINT FK, book_title_snapshot NVARCHAR(200), unit_price DECIMAL(18,2) CK>0, quantity INT CK>0, line_total CK=unit_price*quantity)
Reviews(review_id BIGINT PK, book_id BIGINT FK, user_id BIGINT FK, rating TINYINT CK 1..5, title, content, status CK(3), UQ(user_id, book_id), created_at, updated_at)
Questions(question_id BIGINT PK, book_id BIGINT FK, user_id BIGINT FK, subject NVARCHAR(150), content NVARCHAR(1500), status CK(3), created_at, updated_at)
Answers(answer_id BIGINT PK, question_id BIGINT FK UQ, responder_user_id BIGINT FK, content NVARCHAR(2000), created_at, updated_at)
PasswordResetTokens(reset_id BIGINT PK, user_id BIGINT FK, token_hash VARCHAR(128) UQ, expires_at, used_at, created_at)
```
**Giao diện nội bộ (Internal Java API — `com.bookhub.config`):**
- `AppConfig`: đọc cấu hình từ `bookhub-local.properties` / env (`db.url`, `db.username`, `db.password`, `app.shipping.fee`, `app.reset.token.ttl.minutes`) + cung cấp mẫu `bookhub-local.properties.example`.
- `JpaUtil`: `init()`, `getEntityManager()`, `executeInTransaction(Function<EntityManager, T>)`, `shutdown()`.
- `AppLifecycleListener` (`@WebListener`): gọi `JpaUtil.init()` và `JpaUtil.shutdown()`.

## 6. Edge Cases & Xử lý lỗi
| ID | Tình huống | Hành vi mong đợi |
|---|---|---|
| EC-01 | File `bookhub-local.properties` không tồn tại hoặc thiếu `db.password` | Ném `IllegalStateException` rõ nguyên nhân khi startup (fail-fast), không in giá trị password ra log. |
| EC-02 | Exception xảy ra giữa chừng trong transaction JPA | `JpaUtil` tự động rollback nếu transaction đang active, đóng `EntityManager` trong `finally`, và ném tiếp exception để Service xử lý. |
| EC-03 | Cột `isbn` trong `Books` là `NULL` ở nhiều sách | Chỉ mục lọc `UX_Books_isbn_not_null` cho phép nhiều giá trị `NULL` nhưng từ chối hai giá trị ISBN non-null trùng nhau. |
| EC-04 | Đổi tên/giá `Book` hoặc so sánh `equals`/`hashCode` trên Entity proxy Hibernate | `equals`/`hashCode` dùng ID ổn định (tránh chạm lazy graph); không kích hoạt `LazyInitializationException`. |

## 7. Acceptance Criteria (Given / When / Then)
> Mỗi FR xuất hiện ở cột "Kiểm chứng FR" của ≥ 1 AC. Test gắn thẻ `@spec foundation-jpa/AC-xx`.

| ID | Kịch bản | Kiểm chứng FR |
|---|---|---|
| AC-01 | **Given** database `BookHubDB` trống trên SQL Server **When** chạy `database/01_schema.sql` **Then** tạo đủ 12 bảng kèm toàn bộ PK, FK, UNIQUE, CHECK và index không phát sinh lỗi SQL. | FR-01 |
| AC-02 | **Given** `BookHubDB` đã chạy `01_schema.sql` **When** chạy `database/02_seed.sql` và `03_demo_queries.sql` **Then** có đủ 2 roles, ≥ 6 categories, ≥ 20 books, tài khoản demo có `password_hash` chuẩn BCrypt (không plaintext), cùng đơn/review/Q&A mẫu nhất quán. | FR-02 |
| AC-03 | **Given** `BookHubDB` đã dựng đúng schema và có `bookhub-local.properties` hợp lệ **When** deploy WAR lên Tomcat 10.1 hoặc khởi tạo `JpaUtil` **Then** `BookHubPU` bootstrap thành công với `hibernate.hbm2ddl.auto=validate` và đọc được danh sách `Book`/`Category` qua JPA (`TC-DB-01`). | FR-03, FR-04, FR-07 |
| AC-04 | **Given** `JpaUtil` đã khởi tạo **When** gọi `shutdown()` khi Tomcat dừng ứng dụng **Then** `EntityManagerFactory.isOpen()` trả về `false` và không còn leak connection. | FR-05 |
| AC-05 | **Given** thiếu `bookhub-local.properties` hoặc thiếu khóa `db.url` **When** khởi tạo `AppConfig`/`JpaUtil` **Then** ném lỗi khởi động tường minh và log không chứa giá trị mật khẩu. | FR-06, EC-01 |
| AC-06 | **Given** một thao tác ghi JPA ném ngoại lệ sau khi đã persist bản ghi thứ nhất **When** chạy qua transaction helper **Then** transaction rollback toàn bộ (0 bản ghi dở dang trong DB) và `EntityManager` được đóng sạch. | FR-08, EC-02 |
| AC-07 | **Given** mã nguồn `src/` và thư mục `web/WEB-INF/lib/` **When** quét import & build WAR **Then** có `0` import `javax.servlet`/`javax.persistence`, `0` DAO dùng `DriverManager`/`PreparedStatement`, và không đóng gói trùng API do Tomcat 10.1 cung cấp. | FR-09 |
| AC-08 | **Given** Entity `Book` với hai bản ghi cùng `isbn = NULL` và hai bản ghi trùng `isbn = "978-0132350884"` **When** persist qua JPA **Then** hai bản ghi `NULL` lưu thành công và cặp trùng ISBN bị từ chối bởi `UX_Books_isbn_not_null`. | FR-01, FR-07, EC-03 |

## 8. Out of Scope (🚫 TUYỆT ĐỐI KHÔNG LÀM)
- Không tạo Servlet nghiệp vụ, JSP view hay bộ lọc bảo mật (thuộc `02-feat-mvc-security-shell` và các feature sau).
- Không dùng `hibernate.hbm2ddl.auto=create` hoặc `update` thay thế `01_schema.sql`.
- Không thêm cột `book_code` vào `Books` hay `default_address` vào `Users` khi chưa chốt ở Q1/Q2 bên dưới.
- Không commit file `bookhub-local.properties` hoặc mật khẩu SQL Server thật lên Git.

---
## Phụ lục A: Ghi chú thiết kế
Tier Full — thiết kế chi tiết sẽ lập tại `PLAN.md` sau khi người dùng duyệt Gate 1.

## Phụ lục B: Câu hỏi mở & Phê duyệt
- [x] Q1: Giữ đúng schema MVP `01_schema.sql` (`book_id` tự tăng + `isbn`), không thêm cột `book_code`; chỉ format hiển thị `BK-00101` ở DTO nếu cần — _người trả lời:_ Đã chốt (2026-11-23).
- [x] Q2: Giữ đúng bảng `Users` gốc (`full_name`, `email`, `phone`), không thêm cột `default_address`; địa chỉ giao hàng nhập tại Checkout — _người trả lời:_ Đã chốt (2026-11-23).

### Definition of Ready — chỉ khóa (LOCKED) khi đủ cả
- [x] Giải quyết xong Q1, Q2 ở Phụ lục B
- [x] Mỗi FR có ≥ 1 AC; mỗi AC đo được (Given/When/Then)
- [x] Out of Scope cụ thể; không mâu thuẫn `constraints/*`
- [x] `scripts/spec-check` không báo lỗi

- **Duyệt (Gate 1):** Owner (BookHub) · 2026-11-23 · Version khóa: 1.0.0
