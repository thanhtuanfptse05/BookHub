# constraints/global.md — Tech stack & quy ước toàn cục (BookHub)

> Đây là danh sách "đóng" (allow-list) chốt từ `BookHub_PRJ301_Project_Plan_Requirements.md`. Cái gì không có ở đây ⇒ **hỏi người trước khi dùng**.

## 1. Tech stack cố định
| Lớp | Công nghệ | Phiên bản baseline | Ghi chú |
|---|---|---|---|
| Ngôn ngữ / Runtime | Java JDK | 17 LTS | Namespace `jakarta.*` |
| IDE / Build | Apache NetBeans / Apache Ant | NetBeans 17 | Ant Web Application (`build.xml`), không dùng Maven (ADR-001) |
| Web Server | Apache Tomcat | 10.1.x (baseline `10.1.62`) | Jakarta EE 10 web profile subset (ADR-002) |
| Web MVC / View | Jakarta Servlet / JSP / JSTL / EL | Servlet 6.0 · JSP 3.1 · JSTL 3.0 · EL 5.0 | JSP chỉ dùng JSTL/EL, cấm scriptlet (ADR-004) |
| Persistence / ORM | Jakarta Persistence + Hibernate ORM | JPA 3.1 · Hibernate `6.6.59.Final` | `RESOURCE_LOCAL`, cấm DAO JDBC thuần (ADR-003) |
| Database / Driver | Microsoft SQL Server + mssql-jdbc | SQL Server 2019/2022 · `13.6.2.jre11` | `BookHubDB`, `localhost:1433` |
| UI / Frontend | Bootstrap + Brand CSS + Vanilla JS | Bootstrap 5.3.x · ES6+ | Responsive desktop/tablet/mobile |
| Test | JUnit | 5.x | Unit/Service + JPA integration tests |
| Version Control / CI | Git + GitHub Actions | Current stable | Conventional Commits, PR workflow |

## 2. Thư viện
- ✅ **Cho phép (đã duyệt — đặt tại `web/WEB-INF/lib` hoặc test classpath):**
  - `org.hibernate.orm:hibernate-core:6.6.59.Final` (và bộ transitive dependencies khớp đúng release)
  - `jakarta.persistence:jakarta.persistence-api:3.1.x`
  - `com.microsoft.sqlserver:mssql-jdbc:13.6.2.jre11`
  - `jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api:3.0.2`
  - `org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1`
  - Password hashing: BCrypt tương thích Java 17 (`jbcrypt 0.4` hoặc `spring-security-crypto` BCrypt — chọn 1 implementation duy nhất)
  - Logging: SLF4J + 1 provider tương thích (không trùng binding)
  - Testing (chỉ test classpath, không đóng gói vào WAR): JUnit 5.x
  - Frontend assets: Bootstrap 5.3.x (CSS/JS tĩnh trong `web/assets/vendor/bootstrap/`)
- ⛔ **Cấm:**
  - Mọi thư viện `javax.servlet.*`, `javax.persistence.*`, JSTL 1.2 URI cũ (`http://java.sun.com/jsp/jstl/...`).
  - Triển khai DAO bằng JDBC thuần (`DriverManager`, `PreparedStatement` trong business repository).
  - Java scriptlet `<% ... %>`, `<%= ... %>`, `<%! ... %>` và JSTL SQL tags (`<sql:query>`, `<sql:update>`) trong JSP.
  - Đóng gói trùng các API đã do Tomcat 10.1 cung cấp (`jakarta.servlet-api`, `jakarta.servlet.jsp-api`, `jakarta.el-api`).
  - Nâng cấp tùy tiện lên Tomcat 11 / JSTL 3.1 hoặc thư viện ngoài danh sách khi chưa được duyệt.
- **Quy tắc thêm dependency:** kiểm tra artifact tồn tại thật, tương thích JDK 17 + Jakarta EE 10, đúng license (MIT, Apache-2.0, BSD, EPL, LGPL tương thích Hibernate — cấm GPL/AGPL trừ khi ADR duyệt) và được người duyệt trước khi thêm JAR.

## 3. Quy ước đặt tên (Naming)
| Đối tượng | Quy ước | Ví dụ |
|---|---|---|
| Package Java | `lowercase`, root `com.bookhub.*` (không dùng keyword `public`) | `com.bookhub.controller.site` |
| Class / Interface / Enum | `PascalCase` (không tiền tố `I`) | `CheckoutService`, `OrderStatus` |
| Method / Field / Biến | `camelCase` | `calculateSubtotal`, `stockQty` |
| Hằng số | `UPPER_SNAKE_CASE` | `DEFAULT_SHIPPING_FEE` |
| Boolean | tiền tố `is/has/can/should` | `isActive`, `isFeatured` |
| File JSP / Asset | `kebab-case` | `book-detail.jsp`, `order-list.jsp` |
| Bảng DB (SQL Server) | `PascalCase`, số nhiều (theo `01_schema.sql`) | `Books`, `OrderItems`, `PasswordResetTokens` |
| Cột DB (SQL Server) | `snake_case`; PK/FK `<bảng_số_ít>_id` | `book_id`, `user_id`, `stock_qty` |
| Constraint / Index DB | `PK_`, `FK_`, `UQ_`, `CK_`, `DF_`, `IX_`, `UX_` | `UQ_Users_email`, `CK_Books_price` |
| Servlet Route | `kebab-case`, danh từ số nhiều cho tài nguyên | `/books/detail`, `/admin/orders` |
| Config key local | `dot.case` (`bookhub-local.properties`) | `db.url`, `db.username`, `db.password` |
| Test Java | `<ClassName>Test.java`, method mô tả hành vi | `CheckoutServiceTest` |
| Nhánh Git | `type/slug` | `feat/checkout-cod`, `fix/stock-race` |

## 4. Cấu trúc thư mục & Quy tắc đặt file (Cấm xả rác ở root)
Xem `CLAUDE.md §3` cho cấu trúc `src/` và `web/`. **Mọi file mới phải đặt đúng chỗ — cấm tự tạo file `.md`, ảnh, hay script nháp ở thư mục gốc:**
| Loại file | Nơi đặt duy nhất | Ghi chú |
|---|---|---|
| Source Java & `persistence.xml` | `src/java/com/bookhub/` · `src/conf/META-INF/` | Phân lớp đúng package |
| JSP Views & Web config | `web/WEB-INF/views/` · `web/WEB-INF/web.xml` | Không đặt JSP nghiệp vụ ở public root |
| Tài nguyên tĩnh của app | `web/assets/{css,js,images,vendor}/` | Bootstrap, CSS brand, fallback cover |
| SQL Schema, Seed & Demo queries | `database/` | `01_schema.sql`, `02_seed.sql`, `03_demo_queries.sql` |
| Test tự động | `tests/{unit,integration,e2e}/` | Gắn thẻ `// @spec <slug>/AC-xx` |
| Đặc tả feature (SPEC/PLAN/TASKS/DESIGN…) | `.sdd/specs/feat-<slug>/` | Xong/hủy → `.sdd/specs/_archive/` |
| Tài liệu người đọc | `docs/` | Không rải `.md` ở root (ngoại trừ file gốc chuẩn) |
| Ảnh tham chiếu thiết kế | `.sdd/design/references/` | Gói `.zip` đặt ở `references/incoming/` (gitignore), đọc xong xóa |
| File nháp / thử nghiệm tạm | `.tmp/` (đã gitignore) | **Xóa trước khi xong task** |

## 5. Định dạng, tiền tệ & thời gian
- **Ngôn ngữ giao diện:** Tiếng Việt; **Tiền tệ:** VND, hiển thị ví dụ `125.000 ₫`.
- **Lưu trữ tiền tệ:** `DECIMAL(18,2)` trong SQL Server và `java.math.BigDecimal` (scale 2) trong Java; **cấm** `float`/`double`.
- **Thời gian:** `DATETIME2(0)` theo một múi giờ thống nhất trong môi trường local demo, hiển thị theo `Asia/Ho_Chi_Minh` định dạng `dd/MM/yyyy HH:mm` (khi lên production chuyển chuẩn hóa UTC).
- **Khóa chính (ID):** `IDENTITY(1,1)` — `BIGINT` cho bảng giao dịch/tăng trưởng (`Users`, `Books`, `Carts`, `CartItems`, `Orders`, `OrderItems`, `Reviews`, `Questions`, `Answers`, `PasswordResetTokens`); `INT` cho bảng danh mục nhỏ (`Roles`, `Categories`).
- **Charset & Encoding:** UTF-8 toàn bộ (JSP, Servlet `EncodingFilter`, HTTP); cột tiếng Việt trong SQL Server dùng `NVARCHAR`, mã/email/ISBN/slug dùng `VARCHAR`.
