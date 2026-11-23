# REVIEW — foundation-jpa

> Review **độc lập** (agent/session khác, hoặc người) — prompt: `.sdd/prompts/06-review.md`. Reviewer **không sửa code**.
> Tier Full: bắt buộc `APPROVED` trước khi merge.

| Trường | Giá trị |
|---|---|
| Reviewer | Chờ Owner duyệt (bản nháp bằng chứng từ implementer) · Ngày: 2026-11-23 |
| Commit/nhánh | `feat/foundation-jpa` |
| **Kết luận** | `PENDING` (chờ Owner xác nhận `APPROVED`) |

## 1. Bằng chứng chạy thật
```text
$ ant clean dist test-junit5
- Compiling 26 source files (JDK 17, -Xlint:deprecation -Xlint:unchecked): 0 errors, 0 warnings
- Building jar: dist/BookHub.war
- Hibernate ORM 6.6.59.Final (BookHubPU, hbm2ddl.auto=validate) vs SQL Server 2022 (16.0 BookHubDB): PASS (12/12 tables)
- AppConfigTest: 7/7 OK
- JpaFoundationIntegrationTest: 6/6 OK
- Total: 13 tests found, 13 tests successful, 0 tests failed (4.2s)
```

## 2. Truy vết
| Yêu cầu | Code | Test (`@spec`) | OK? |
|---|---|---|---|
| FR-01, EC-03 | `database/01_schema.sql`, `99_drop_all.sql`, `README_DATABASE.md` | `foundation-jpa/AC-01`, `foundation-jpa/AC-08` | ✅ |
| FR-02 | `database/02_seed.sql`, `03_demo_queries.sql` | `foundation-jpa/AC-02` | ✅ |
| FR-03, FR-04 | `src/conf/META-INF/persistence.xml`, `AppConfig.java`, `JpaUtil.java`, `AppLifecycleListener.java` | `foundation-jpa/AC-03`, `foundation-jpa/AC-07` | ✅ |
| FR-05 | `AppLifecycleListener.contextDestroyed`, `JpaUtil.shutdown` | `foundation-jpa/AC-04` | ✅ |
| FR-06, EC-01 | `AppConfig.load/fromProperties/getSanitizedDbUrl/toString` | `foundation-jpa/AC-05` | ✅ |
| FR-07, EC-04 | 6 Enums + 12 Entities dưới `com.bookhub.entity` | `foundation-jpa/AC-03`, `foundation-jpa/AC-08` | ✅ |
| FR-08, EC-02 | `JpaUtil.executeInTransaction`, `BaseRepository` + 3 Repositories | `foundation-jpa/AC-06` | ✅ |
| FR-09 | `web/WEB-INF/lib/` (23 JARs), `lib-test/` (JUnit 5), `nbproject/project.{properties,xml}`, `build.xml` | `foundation-jpa/AC-07` | ✅ |

## 3. Kiểm tra theo hiến pháp
- [x] Không vi phạm Out of Scope (chưa tạo Servlet/JSP/Filter; giữ đúng schema gốc không thêm `book_code`/`default_address`; không dùng `hbm2ddl.auto=create/update`)
- [x] Không vi phạm constitution/constraints (`H-01..09`, `A-01..09`, `S-01..12` — 0 `javax.*`, 0 JDBC DAO, 0 secret trong Git)
- [x] Bảo mật (`H-01`, `H-05`): `bookhub-local.properties` trong `.gitignore` & `.agentignore`; `AppConfig.toString()` và log lỗi che mật khẩu; `User`/`PasswordResetToken` không in hash
- [x] Hiệu năng/SQL (`A-09`): `FetchType.LAZY` toàn bộ N:1/1:1; `UserRepository.findByEmail` và `BookRepository.findActivePage` dùng `JOIN FETCH` + phân trang DB
- [x] Xử lý lỗi (`EC-01`, `EC-02`): fail-fast khi thiếu config/key; rollback + `finally close` khi lỗi giữa transaction
- [x] Test chất lượng: 13/13 JUnit 5 test gồm cả đường lỗi (missing file, missing key, rollback, duplicate ISBN)
- [x] Docs/contract cập nhật; có migration `down` (`database/99_drop_all.sql`)

## 4. Phát hiện
| # | Mức | File:dòng | Vấn đề | Cách sửa đề xuất | Trạng thái |
|---|---|---|---|---|---|
| 1 | 🟢 | `src/conf/META-INF/persistence.xml:27` | Hibernate 6.6 log `WARN HHH90000025` vì `hibernate.dialect` được khai báo tường minh (khớp đúng yêu cầu `FR-03`) | Giữ nguyên để tuân `FR-03` hoặc bỏ nếu có `CHANGELOG` duyệt | Chấp nhận |

## 5. Phê duyệt cuối
_(Chờ Owner xác nhận `APPROVED` để chốt Gate 4)_
