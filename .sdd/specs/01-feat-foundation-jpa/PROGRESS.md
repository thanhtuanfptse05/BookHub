# PROGRESS — Nền tảng Database SQL Server & JPA/Hibernate (`01-feat-foundation-jpa`)

| Trường | Giá trị |
|---|---|
| Feature | `01-feat-foundation-jpa` · Tier: Full · Nhánh: `feat/bookhub-mvp-01-08` |
| Pha hiện tại | `done` (Owner đã duyệt `REVIEW.md` `APPROVED` — đóng đủ `T-001`..`T-007`) |
| Task đang làm | — (đã hoàn tất toàn bộ `T-001`..`T-007`) |
| Cập nhật | 2026-11-23 12:17 |

## 1. Plan của task hiện tại
- [x] `T-001`: 12 bảng SQL Server (`01_schema.sql`), `99_drop_all.sql`, `README_DATABASE.md`, filtered unique index `UX_Books_isbn_not_null`
- [x] `T-002`: Dữ liệu seed chuẩn logic nghiệp vụ (`02_seed.sql`, 6 users BCrypt `$2a$10$`, 7 categories, 26 books, 8 orders 5 trạng thái, 6 reviews, 6 questions / 3 answers, 2 reset tokens) + `03_demo_queries.sql` (0/6 vi phạm kiểm toán)
- [x] `T-003`: 23 runtime JARs Jakarta EE 10 / Hibernate `6.6.59.Final` / `mssql-jdbc 13.6.2.jre11` / JSTL `3.0.2`+`3.0.1` / `jbcrypt 0.4` / SLF4J trong `web/WEB-INF/lib/`, `junit-platform-console-standalone-1.11.4.jar` trong `lib-test/`, cập nhật `nbproject/project.{properties,xml}`, `build.xml`, `.gitignore`, `.agentignore`
- [x] `T-004`: `src/conf/META-INF/persistence.xml` (`BookHubPU`, `RESOURCE_LOCAL`, `validate`), `bookhub-local.properties.example`, `AppConstants`, `AppConfig`, `JpaUtil`, `AppLifecycleListener`
- [x] `T-005`: 6 Enum (`RoleCode`, `UserStatus`, `PaymentMethod`, `OrderStatus`, `ReviewStatus`, `QuestionStatus`) + 6 Core Entities (`Role`, `User`, `Category`, `Book`, `Cart`, `CartItem`)
- [x] `T-006`: 6 Commerce/Interaction Entities (`Order`, `OrderItem`, `Review`, `Question`, `Answer`, `PasswordResetToken`)
- [x] `T-007`: `BaseRepository` + smoke Repositories (`User`, `Category`, `Book`) & 13/13 JUnit 5 tests (`@spec foundation-jpa/AC-01..08`) — Owner đã duyệt `REVIEW.md` (`APPROVED`)

## 2. Nhật ký thực thi
| Giờ | Việc đã làm | Kết quả |
|---|---|---|
| 2026-11-23 11:40 | Khóa SPEC v1.0.0 (Gate 1); lập `PLAN.md` và `TASKS.md` (`T-001`..`T-007`) | Xong Pha 1–3 |
| 2026-11-23 11:55 | Triển khai `T-001` & `T-002` (`database/*.sql`), chạy thực tế trên SQL Server 2022 (`BookHubDB`) | 12/12 bảng OK, 0 lỗi, 0/6 vi phạm kiểm toán |
| 2026-11-23 12:10 | Hoàn tất `T-003` (23 runtime JARs + JUnit 5) & `T-004` (`persistence.xml`, `AppConfig`, `JpaUtil`, `AppLifecycleListener`) | Không còn JAR cũ `allowedlib`/`javax.*` |
| 2026-11-23 12:14 | Hoàn tất `T-005` & `T-006` (6 Enums + 12 Entities) và `T-007` (4 Repositories + `AppConfigTest` + `JpaFoundationIntegrationTest`) | `ant clean dist test-junit5`: 26 src + 2 tests, 0 warning, `dist/BookHub.war` OK, **13/13 tests PASS**, Hibernate `validate` xanh 12/12 bảng |

## 3. Kiểm tra
Lint ⬜ (static check trong `AppConfigTest` ✅) · Test ✅ (13/13 JUnit 5 + Hibernate `validate` trên SQL Server 2022) · Build ✅ (`ant clean dist` → `dist/BookHub.war`, 0 warning) · spec-check ✅ (0 lỗi) · Secret scan ✅

## 4. Blockers / câu hỏi cho người
- ⬜ Chờ Owner review & duyệt `.sdd/specs/01-feat-foundation-jpa/REVIEW.md` (Gate 4) để đánh `[x]` `T-007` và đóng Feature 01.

## 5. Quyết định & giả định
- `nbproject/project.properties` đặt `persistence.xml.dir=${conf.dir}/META-INF` để NetBeans Ant tự đóng `src/conf/META-INF/persistence.xml` vào `WEB-INF/classes/META-INF/persistence.xml`.
- Override `library-inclusion-in-archive` và `library-inclusion-in-manifest` trong `build.xml` để tránh copy trùng hoặc gọi các đường dẫn tuyệt đối cũ `allowedlib` trong `build-impl.xml` (vì `-copy-webdir` đã copy toàn bộ `web/WEB-INF/lib/*.jar`).
- Tạo SQL Server local dev login `bookhub_dev` (`db_owner` chỉ trên `BookHubDB`) và ghi `bookhub-local.properties` ngoài Git (đã được `.gitignore` & `.agentignore` bảo vệ) để chạy integration test thực tế.

## 6. Handoff
- **Đã xong:** Toàn bộ code, cấu hình, WAR build và 13/13 test JUnit 5 (`@spec foundation-jpa/AC-01..08`) của `01-feat-foundation-jpa` trên nhánh `feat/foundation-jpa`.
- **Làm tiếp:** Sau khi Owner duyệt `REVIEW.md` → chốt `APPROVED` + đánh `✅` `T-007` → merge `feat/foundation-jpa` vào `main` → chuyển sang `02-feat-mvc-security-shell`.
- **Cẩn thận:** Tuyệt đối không commit `bookhub-local.properties`; giữ `hibernate.hbm2ddl.auto=validate`.
