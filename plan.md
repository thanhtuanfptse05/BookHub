# plan.md — Con trỏ session + sổ nháp tier Quick

> Đọc file này **sau** `AGENTS.md`/`CLAUDE.md`. Agent sở hữu. Tiến độ chi tiết của Standard/Full nằm ở `.sdd/specs/<NN>-feat-<slug>/PROGRESS.md`.
> Hết việc: xóa nội dung, trả file về template.

## 1. Đang làm
| Trường | Giá trị |
|---|---|
| Feature | 8 feature MVP `01-feat-foundation-jpa` .. `08-feat-admin-users-reports` (`SPEC+DESIGN LOCKED`, `PLAN+TASKS` hoàn tất) |
| Tier | `Full` |
| Tiến độ chi tiết | `.sdd/specs/01-feat-foundation-jpa/PROGRESS.md` · `.sdd/specs/README.md` |
| Nhánh Git | `main` (tạo nhánh `feat/foundation-jpa` khi bắt đầu triển khai `01`) |
| Cập nhật lần cuối | 2026-11-23 |

## 2. Sổ nháp tier Quick (bỏ trống nếu không phải Quick)
- **Mục tiêu (1 câu, đo được):** —
- **Phạm vi file:** —
- [ ] Tái hiện lỗi bằng test đỏ
- [ ] Sửa tối thiểu → test xanh
- [ ] `scripts/verify` xanh
- [ ] Ghi bài học vào `CLAUDE.md §5` nếu lỗi có thể lặp lại

## 3. Blockers / câu hỏi cho người
- ⬜ Duyệt Gate 2–3 (`PLAN.md` + `TASKS.md`) để bắt đầu triển khai `01-feat-foundation-jpa` (`T-001`..`T-007`).

## 4. Handoff cho session kế tiếp
- **Đã xong:** Khóa Gate 1 (`Status: LOCKED` v1.0.0) cho cả 8 `SPEC.md` + 7 `DESIGN.md`; viết đầy đủ `PLAN.md`, `TASKS.md` (48 task atomic), `PROGRESS.md`, `CHANGELOG.md` cho `01` → `08`.
- **Làm tiếp:** Sau khi người xác nhận bắt đầu implement → tạo nhánh `feat/foundation-jpa`, triển khai lần lượt `T-001` (`01_schema.sql`) → `T-007` (integration tests) của `01-feat-foundation-jpa`.
- **Cẩn thận:** Giữ kỷ luật `jakarta.*` (0 `javax.*`), 0 scriptlet JSP, 0 DAO JDBC thuần, 0 mật khẩu DB lên Git; `REVIEW.md` giữ sạch cho Pha 5 sau khi code xong.
