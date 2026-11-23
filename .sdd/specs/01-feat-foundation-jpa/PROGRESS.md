# PROGRESS — Nền tảng Database SQL Server & JPA/Hibernate (`01-feat-foundation-jpa`)

| Trường | Giá trị |
|---|---|
| Feature | `01-feat-foundation-jpa` · Tier: Full · Nhánh: `feat/foundation-jpa` |
| Pha hiện tại | `implementing` (đã xong `T-001`, `T-002` — Database SQL Server `BookHubDB` sẵn sàng) |
| Task đang làm | Tiếp theo: `T-003` (Runtime JARs & Ant Classpath) |
| Cập nhật | 2026-11-23 11:56 |

## 1. Plan của task hiện tại
- **Đã hoàn thành:** `T-001` (`01_schema.sql`, `99_drop_all.sql`, `README_DATABASE.md`) & `T-002` (`02_seed.sql`, `03_demo_queries.sql`) — đã chạy thực tế & vượt 6/6 truy vấn kiểm toán trên SQL Server 2022 (`localhost:1433`).
- [x] `T-001`: 12 bảng SQL Server + constraints + filtered unique index `UX_Books_isbn_not_null`
- [x] `T-002`: Dữ liệu seed chuẩn logic nghiệp vụ (6 users BCrypt `$2a$10$`, 7 categories, 26 books, 8 orders 5 trạng thái, 6 reviews, 6 questions / 3 answers, 2 reset tokens) + 0 vi phạm kiểm toán
- [ ] `T-003`: Chuẩn hóa runtime JARs trong `web/WEB-INF/lib/` & JUnit 5 test classpath
- [ ] `T-004` → `T-007`: `persistence.xml`, `AppConfig`/`JpaUtil`, 12 Entities, BaseRepository & integration test (`@spec foundation-jpa/AC-01..08`)

## 2. Nhật ký thực thi
| Giờ | Việc đã làm | Kết quả |
|---|---|---|
| 2026-11-23 11:40 | Khóa SPEC v1.0.0 (Gate 1); lập `PLAN.md` và `TASKS.md` (`T-001`..`T-007`) | Xong Pha 1–3 |
| 2026-11-23 11:55 | Triển khai `T-001` & `T-002` (`database/*.sql`), chạy thực tế trên SQL Server 2022 (`BookHubDB`) | 12/12 bảng OK, 0 lỗi, 0/6 vi phạm kiểm toán |

## 3. Kiểm tra
Lint ⬜ · Test ⬜ (DB SQL audit ✅) · Build ⬜ · spec-check ✅ · Secret scan ✅ · Coverage mới ⬜ %

## 4. Blockers / câu hỏi cho người
- _(Không có)_

## 5. Quyết định & giả định
- Dùng `SET ANSI_NULLS ON; SET QUOTED_IDENTIFIER ON;` ở đầu `01_schema.sql`, `02_seed.sql`, `03_demo_queries.sql` và lưu UTF-8 BOM để chạy sạch trên cả SSMS lẫn `sqlcmd`.
- BCrypt dùng prefix `$2a$10$` để tương thích cả `jBCrypt 0.4` lẫn `Spring Security Crypto`.

## 6. Handoff
- **Đã xong:** `T-001`, `T-002` (DB `BookHubDB` đã dựng và kiểm toán xong trên SQL Server 2022). · **Làm tiếp:** `T-003` (add JARs) → `T-004` (`persistence.xml`, `AppConfig`, `JpaUtil`) → `T-005..007` (12 Entities + integration tests). · **Cẩn thận:** Không commit `bookhub-local.properties` hoặc JAR `javax.*`.
