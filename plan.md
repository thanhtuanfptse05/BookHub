# plan.md — Con trỏ session + sổ nháp tier Quick

> Đọc file này **sau** `AGENTS.md`/`CLAUDE.md`. Agent sở hữu. Tiến độ chi tiết của Standard/Full nằm ở `.sdd/specs/<NN>-feat-<slug>/PROGRESS.md`.
> Hết việc: xóa nội dung, trả file về template.

## 1. Đang làm
| Trường | Giá trị |
|---|---|
| Feature | `01-feat-foundation-jpa` (code & 13/13 tests xong, chờ duyệt `REVIEW.md`) → tiếp theo `02-feat-mvc-security-shell` |
| Tier | `Full` |
| Tiến độ chi tiết | `.sdd/specs/01-feat-foundation-jpa/PROGRESS.md` · `.sdd/specs/01-feat-foundation-jpa/REVIEW.md` |
| Nhánh Git | `feat/foundation-jpa` |
| Cập nhật lần cuối | 2026-11-23 |

## 2. Sổ nháp tier Quick (bỏ trống nếu không phải Quick)
- **Mục tiêu (1 câu, đo được):** —
- **Phạm vi file:** —
- [ ] Tái hiện lỗi bằng test đỏ
- [ ] Sửa tối thiểu → test xanh
- [ ] `scripts/verify` xanh
- [ ] Ghi bài học vào `CLAUDE.md §5` nếu lỗi có thể lặp lại

## 3. Blockers / câu hỏi cho người
- ⬜ Duyệt Gate 4 (`.sdd/specs/01-feat-foundation-jpa/REVIEW.md`) và merge `feat/foundation-jpa` vào `main`.
- ⬜ Duyệt Gate 2–3 (`PLAN.md` + `TASKS.md`) của `02-feat-mvc-security-shell` để triển khai tiếp.

## 4. Handoff cho session kế tiếp
- **Đã xong:** Toàn bộ `T-001`..`T-007` của `01-feat-foundation-jpa` (DB 12 bảng + seed sạch, 23 runtime JARs Jakarta EE 10/Hibernate `6.6.59.Final`, `persistence.xml`, `AppConfig`, `JpaUtil`, `AppLifecycleListener`, 6 Enums, 12 Entities, 4 Repositories, 13/13 JUnit 5 tests xanh + Hibernate `validate` pass trên SQL Server 2022, WAR `dist/BookHub.war` build sạch).
- **Làm tiếp:** Sau khi Owner duyệt `REVIEW.md` → đánh `APPROVED` + `T-007 ✅` → merge vào `main` → tạo nhánh `feat/mvc-security-shell` triển khai Feature 02.
- **Cẩn thận:** Không commit `bookhub-local.properties`; luôn chạy Ant với JDK 17 (`C:\Program Files\Java\jdk-17`).
