# TASKS — Nền tảng Database SQL Server & JPA/Hibernate (`01-feat-foundation-jpa`)

> **Pha 3 · Phân rã từ `PLAN.md`.** Task atomic ≤ 4h · 1 mục tiêu · có test gắn `@spec foundation-jpa/AC-xx`.

**Trạng thái:** `⬜ todo` · `🔄 doing` · `✅ done` · `⛔ blocked`

| ID | Task | Phụ thuộc | Ước lượng | Truy vết | Trạng thái |
|---|---|---|---|---|---|
| T-001 | Tạo `database/01_schema.sql`, `99_drop_all.sql` và `README_DATABASE.md` | — | 2h | FR-01, EC-03, AC-01 | ✅ |
| T-002 | Tạo `database/02_seed.sql` (BCrypt hash thực) & `03_demo_queries.sql` | T-001 | 2h | FR-02, AC-02 | ✅ |
| T-003 | Chuẩn hóa runtime JARs trong `web/WEB-INF/lib/` & JUnit 5 test classpath | — | 2h | FR-09, AC-07 | ✅ |
| T-004 | Tạo `persistence.xml`, `bookhub-local.properties.example`, `AppConfig`, `JpaUtil`, `AppLifecycleListener` | T-003 | 3h | FR-03..06, FR-08, EC-01..02, AC-04..06 | ✅ |
| T-005 | Tạo 6 Enum & 6 Entity lõi (`Role`, `User`, `Category`, `Book`, `Cart`, `CartItem`) | T-004 | 3h | FR-07, AC-03, AC-08 | ✅ |
| T-006 | Tạo 6 Entity giao dịch & tương tác (`Order`, `OrderItem`, `Review`, `Question`, `Answer`, `PasswordResetToken`) | T-005 | 3h | FR-07, AC-03 | ✅ |
| T-007 | Tạo `BaseRepository` + smoke Repositories (`User`, `Category`, `Book`) & bộ integration test `AC-01..08` | T-002, T-006 | 3h | FR-01..09, AC-01..08 | ✅ |

## Chi tiết task

### T-001 — Script Schema SQL Server 12 bảng (`01_schema.sql`)
- **Mục tiêu:** Tạo đầy đủ 12 bảng, PK, FK, UNIQUE, CHECK, filtered unique index ISBN và script `99_drop_all.sql`.
- **File dự kiến:** `database/01_schema.sql`, `database/99_drop_all.sql`, `database/README_DATABASE.md`.
- **Acceptance:**
  - [ ] Chạy sạch trên `BookHubDB` trống không lỗi (`@spec foundation-jpa/AC-01`).
  - [ ] `UX_Books_isbn_not_null` cho phép nhiều `NULL`, chặn 2 ISBN non-null trùng (`@spec foundation-jpa/AC-08`).
- **Không làm:** Không chèn dữ liệu mẫu trong `01_schema.sql`.

### T-002 — Seed Data nhất quán & Demo Queries
- **Mục tiêu:** Cung cấp dữ liệu demo đủ 2 roles, 6–8 categories, 20–30 books, 1 admin + 2–3 customers (BCrypt hash), đơn/review/Q&A mẫu nhất quán.
- **File dự kiến:** `database/02_seed.sql`, `database/03_demo_queries.sql`.
- **Acceptance:**
  - [ ] Chạy nối tiếp `01_schema.sql` thành công, 0 mật khẩu plaintext (`@spec foundation-jpa/AC-02`).
  - [ ] `03_demo_queries.sql` trả đủ kết quả `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`.
- **Không làm:** Không hardcode dữ liệu này trong code Java.

### T-003 — Chuẩn hóa Runtime JARs & Ant Classpath
- **Mục tiêu:** Nạp đúng bộ JAR Jakarta EE 10 / Hibernate 6.6.59.Final / `mssql-jdbc 13.6.2.jre11` / JSTL 3.0 / jBCrypt / SLF4J và JUnit 5.
- **File dự kiến:** `web/WEB-INF/lib/*.jar`, `nbproject/project.properties`, `.gitignore`.
- **Acceptance:**
  - [ ] Build Ant WAR sạch; 0 JAR `javax.*`, không đóng gói trùng API Servlet/JSP/EL của Tomcat (`@spec foundation-jpa/AC-07`).
- **Không làm:** Không chuyển project sang Maven.

### T-004 — `persistence.xml`, `AppConfig`, `JpaUtil` & Lifecycle
- **Mục tiêu:** Bootstrap `BookHubPU` (`validate`) từ `bookhub-local.properties` ngoài Git, quản lý EMF singleton & transaction helper.
- **File dự kiến:** `src/conf/META-INF/persistence.xml`, `bookhub-local.properties.example`, `src/java/com/bookhub/config/{AppConfig,AppConstants,JpaUtil,AppLifecycleListener}.java`, `tests/unit/AppConfigTest.java`.
- **Acceptance:**
  - [ ] Thiếu file/key ném lỗi fail-fast không in password (`@spec foundation-jpa/AC-05`).
  - [ ] `shutdown()` đóng sạch EMF (`@spec foundation-jpa/AC-04`); lỗi giữa transaction rollback toàn bộ (`@spec foundation-jpa/AC-06`).
- **Không làm:** Không ghi mật khẩu thật vào `persistence.xml`.

### T-005 & T-006 — 12 JPA Entities & 6 Enums
- **Mục tiêu:** Ánh xạ chính xác 12 bảng SQL Server sang `com.bookhub.entity` tuân chuẩn `BigDecimal`, `FetchType.LAZY`, không `CascadeType.REMOVE`.
- **File dự kiến:** `src/java/com/bookhub/entity/*.java`.
- **Acceptance:**
  - [ ] Hibernate `hbm2ddl.auto=validate` pass 100% với `BookHubDB` (`@spec foundation-jpa/AC-03`).
- **Không làm:** Không viết business logic tính tiền/state transition trong Entity.

### T-007 — BaseRepository & Bộ Integration Test nền tảng
- **Mục tiêu:** Hoàn thiện `BaseRepository`, smoke Repositories và chạy full integration test + `scripts/verify.ps1`.
- **File dự kiến:** `src/java/com/bookhub/repository/{BaseRepository,UserRepository,CategoryRepository,BookRepository}.java`, `tests/integration/JpaFoundationIntegrationTest.java`.
- **Acceptance:**
  - [ ] Toàn bộ test gắn `@spec foundation-jpa/AC-01..08` xanh; `scripts/verify.ps1` xanh.
- **Không làm:** Không dùng `DriverManager`/`PreparedStatement` (JDBC thuần).

## Đồ thị phụ thuộc
```text
T-001 ──► T-002 ────────────────────────┐
T-003 ──► T-004 ──► T-005 ──► T-006 ────┴──► T-007
```

## Phê duyệt (Gate 3)
Owner (BookHub) · 2026-11-23
