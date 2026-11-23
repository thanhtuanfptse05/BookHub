# PLAN — Nền tảng Database SQL Server & JPA/Hibernate (`01-feat-foundation-jpa`)

> **Pha 2 · Thiết kế kiến trúc** sau khi SPEC v1.0.0 đã `LOCKED`. Duyệt (Gate 2) cùng `TASKS.md`.

| Trường | Giá trị |
|---|---|
| SPEC version | 1.0.0 |
| Status | `APPROVED` |

## 1. Tóm tắt giải pháp
Coi `database/01_schema.sql` là nguồn sự thật duy nhất của 12 bảng SQL Server (`BookHubDB`), đi kèm `02_seed.sql` (BCrypt hash thực, không plaintext) và `03_demo_queries.sql`. Tầng Java dùng **Jakarta Persistence 3.1 + Hibernate ORM 6.6.59.Final** (`RESOURCE_LOCAL`, `hbm2ddl.auto=validate`), cấu hình kết nối đọc từ `bookhub-local.properties` ngoài Git thông qua `AppConfig` + `JpaUtil` singleton khởi tạo/đóng theo `ServletContextListener`. Mọi Repository đều dùng `EntityManager` do Service cung cấp trong ranh giới transaction tập trung (`ADR-001`, `ADR-002`, `ADR-003`).

## 2. Phương án đã cân nhắc
| Phương án | Ưu | Nhược | Chọn? |
|---|---|---|---|
| A: `01_schema.sql` làm chuẩn + Hibernate `hbm2ddl.auto=validate` + `AppConfig` đọc `bookhub-local.properties` | Khớp chặt schema/CHECK/filtered index SQL Server; không lộ password lên Git; phát hiện lệch mapping ngay khi startup | Cần đảm bảo đồng bộ tay giữa SQL script và Entity annotations | ✅ |
| B: Hibernate `hbm2ddl.auto=update` + ghi thẳng user/pass vào `persistence.xml` | Nhanh lúc thử nghiệm đầu | Không tạo được filtered index/CHECK đầy đủ của SQL Server; vi phạm `H-01` (lộ password) | ❌ |

## 3. Kiến trúc & luồng xử lý
```text
Tomcat Startup → AppLifecycleListener.contextInitialized()
  → AppConfig.load() [bookhub-local.properties / ENV — fail-fast nếu thiếu]
  → JpaUtil.init(overrides) → Persistence.createEntityManagerFactory("BookHubPU", overrides)
  → Hibernate SchemaValidator kiểm tra 12 Entities ↔ BookHubDB (12 tables)

Service Operation → JpaUtil.executeInTransaction(em -> { repo.save(em, entity); ... })
  → begin → thực thi → commit (hoặc rollback khi RuntimeException) → finally em.close()

Tomcat Shutdown → AppLifecycleListener.contextDestroyed() → JpaUtil.shutdown() → emf.close()
```

## 4. Thay đổi theo tầng
| Tầng | File tạo mới | File sửa | Ghi chú |
|---|---|---|---|
| `database/` | `01_schema.sql`, `02_seed.sql`, `03_demo_queries.sql`, `README_DATABASE.md`, `99_drop_all.sql` (down/reset local) | — | 12 bảng, constraints, indexes, seed BCrypt |
| `src/conf/` | `src/conf/META-INF/persistence.xml`, `bookhub-local.properties.example` | `.gitignore` (thêm `bookhub-local.properties`) | Không chứa password thật |
| `config/` | `com.bookhub.config.{AppConfig,AppConstants,JpaUtil,AppLifecycleListener}` | — | Singleton EMF + transaction helper |
| `entity/` | `Role`, `User`, `Category`, `Book`, `Cart`, `CartItem`, `Order`, `OrderItem`, `Review`, `Question`, `Answer`, `PasswordResetToken` + Enums (`RoleCode`, `UserStatus`, `OrderStatus`, `ReviewStatus`, `QuestionStatus`, `PaymentMethod`) | — | `jakarta.persistence.*`, `BigDecimal`, `LAZY`, không `CascadeType.REMOVE` |
| `repository/` | `BaseRepository`, `UserRepository`, `CategoryRepository`, `BookRepository` (smoke CRUD/read) | — | Nhận `EntityManager` từ Service |
| `web/WEB-INF/lib/` | Bộ JAR runtime (Hibernate 6.6.59.Final + transitives, JPA 3.1, `mssql-jdbc 13.6.2.jre11`, JSTL 3.0.2/3.0.1, jBCrypt, SLF4J) | `nbproject/project.properties` | Không trùng Servlet/JSP/EL API của Tomcat |
| `tests/` | `tests/unit/AppConfigTest.java`, `tests/integration/JpaFoundationIntegrationTest.java` | — | Gắn `@spec foundation-jpa/AC-01..08` |

## 5. Data model & Migration
- 12 bảng mới theo `BookHub_PRJ301_Project_Plan_Requirements.md §17.5` (`Roles`, `Users`, `Categories`, `Books`, `Carts`, `CartItems`, `Orders`, `OrderItems`, `Reviews`, `Questions`, `Answers`, `PasswordResetTokens`).
- Migration `up`: `database/01_schema.sql` + `02_seed.sql`. Script `down` (dựng lại DB sạch local): `database/99_drop_all.sql` drop theo thứ tự ngược FK.
- Quyết định Gate 1 đã khóa: giữ nguyên schema gốc (không thêm cột `book_code` hay `default_address`).

## 6. API / Contract & ảnh hưởng chéo
- Thiết lập contract nội bộ `JpaUtil`, `AppConfig`, 12 Entity và 6 Enum làm nền cho `02`..`08`. Không breaking change.

## 7. Truy vết SPEC → thiết kế
| Yêu cầu | Thành phần đáp ứng | Test dự kiến |
|---|---|---|
| FR-01, EC-03 | `01_schema.sql` (12 bảng, CHECK, `UX_Books_isbn_not_null`) | AC-01, AC-08 |
| FR-02 | `02_seed.sql` + `03_demo_queries.sql` | AC-02 |
| FR-03, FR-04, FR-07 | `persistence.xml`, `AppConfig`, `JpaUtil`, 12 Entities | AC-03 |
| FR-05 | `AppLifecycleListener.contextDestroyed` → `JpaUtil.shutdown()` | AC-04 |
| FR-06, EC-01 | `AppConfig` validation fail-fast + mask log | AC-05 |
| FR-08, EC-02 | `JpaUtil.executeInTransaction()` rollback + `finally close` | AC-06 |
| FR-09 | `web/WEB-INF/lib/` manifest + quét import không `javax.*`/JDBC | AC-07 |

## 8. Chiến lược kiểm thử
- **Unit (JUnit 5):** `AppConfig` (thiếu file/key, không log password), Entity invariants (`equals`/`hashCode`, enum).
- **Integration (SQL Server test DB `BookHubDB`):** Chạy `01_schema.sql` + `02_seed.sql`, boot `JpaUtil` (`validate`), persist/query `Category`/`Book`, test rollback khi ném exception, test filtered unique index `isbn`.
- **Static check:** Quét `0` import `javax.servlet`/`javax.persistence`, `0` `DriverManager`/`PreparedStatement`.

## 9. Rủi ro & giảm thiểu
| # | Rủi ro | Xác suất | Tác động | Giảm thiểu / Rollback |
|---|---|---|---|---|
| R1 | Thiếu transitive JAR của Hibernate 6.6 trong Ant project | Cao | Cao | Lập danh sách JAR đầy đủ theo release Hibernate 6.6.59.Final; test WAR sạch trên Tomcat 10.1 |
| R2 | Lệch kiểu cột SQL Server (`TINYINT`/`SMALLINT`/`DATETIME2(0)`) khiến `validate` fail | Trung bình | Cao | Khai báo rõ `columnDefinition`/kiểu Java tương ứng (`Byte`/`Short`/`LocalDateTime`) và test trên DB dựng mới |
| R3 | Lộ `bookhub-local.properties` lên Git | Thấp | Cao | Thêm vào `.gitignore` + `.agentignore` trước khi tạo file local; kiểm tra `scripts/verify.ps1` |

## 10. Rollout & Quan sát
- Khởi động Tomcat 10.1 ghi 1 dòng `INFO` xác nhận `BookHubPU` kết nối thành công tới `BookHubDB` (chỉ in host/db, không in user/password). Rollback bằng `99_drop_all.sql` và gỡ commit trên nhánh `feat/foundation-jpa`.

## 11. ❓ Câu hỏi cho con người
- Không còn câu hỏi mở (Q1, Q2 đã khóa ở Gate 1).

## 12. Phê duyệt (Gate 2)
Owner (BookHub) · 2026-11-23
