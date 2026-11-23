# plan.md — Con trỏ session + sổ nháp tier Quick

> Đọc file này **sau** `AGENTS.md`/`CLAUDE.md`. Agent sở hữu. Tiến độ chi tiết của Standard/Full nằm ở `.sdd/specs/feat-<slug>/PROGRESS.md` (tránh đè nhau khi làm song song).
> Hết việc: xóa nội dung, trả file về template.

## 1. Đang làm
| Trường | Giá trị |
|---|---|
| Feature | `none` (vừa hoàn thiện tài liệu nền tảng BookHub MVP theo `BookHub_PRJ301_Project_Plan_Requirements.md`) |
| Tier | `Full` (chuẩn bị bước vào EPIC 1 — Database & JPA) |
| Tiến độ chi tiết | `docs/roadmap.md` |
| Nhánh Git | `main` (tạo nhánh `feat/*` / `docs/*` trước khi code hoặc ship) |
| Cập nhật lần cuối | 2026-11-23 |

## 2. Sổ nháp tier Quick (bỏ trống nếu không phải Quick)
- **Mục tiêu (1 câu, đo được):** —
- **Phạm vi file:** —
- [ ] Tái hiện lỗi bằng test đỏ
- [ ] Sửa tối thiểu → test xanh
- [ ] `scripts/verify` xanh
- [ ] Ghi bài học vào `CLAUDE.md §5` nếu lỗi có thể lặp lại

## 3. Blockers / câu hỏi cho người
- ⬜ Cần cấu hình `.sdd/project.conf` và chạy `./scripts/setup-tools.ps1` (khởi tạo Spec Kit `.specify/`) trước khi bắt đầu tạo SPEC cho EPIC 1.

## 4. Handoff cho session kế tiếp
- **Đã xong:** Đẩy repo lên GitHub; hoàn thiện toàn bộ tài liệu `.md` nền tảng và đồng bộ Design System + 12 ảnh baseline + đặc tả 25 màn hình UI từ Figma (`https://agenda-kale-62994833.figma.site` → `.sdd/design/`, `docs/ui-screens.md`).
- **Làm tiếp:** Chạy `./scripts/setup-tools.ps1`, tạo feature đầu tiên (EPIC 1 — Database & JPA: `01_schema.sql`, `02_seed.sql`, `persistence.xml`, 12 Entities).
- **Cẩn thận:** Giữ kỷ luật `jakarta.*` (cấm `javax.*`), cấm scriptlet trong JSP, cấm commit `bookhub-local.properties` hoặc mật khẩu SQL Server.
