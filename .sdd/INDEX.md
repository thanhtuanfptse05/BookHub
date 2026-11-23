# .sdd/INDEX.md — Bản đồ tài liệu (đọc gì, khi nào)

> Mục tiêu: nạp **đủ** ngữ cảnh, **không dư** token. Ngân sách: `AGENTS.md` ≲ 150 dòng · `CLAUDE.md` ≲ 300 dòng · mỗi skill ≲ 150 dòng (`scripts/spec-check` cảnh báo khi vượt).

| Tài liệu | Đọc khi | Tier |
|---|---|---|
| `AGENTS.md`, `CLAUDE.md`, `plan.md` | Mọi session | Quick/Std/Full |
| `constitution.md` | Trước khi thiết kế/sửa logic | Std/Full |
| `constraints/global.md` | Chọn thư viện, đặt tên | Std/Full |
| `constraints/business.md` | Đụng dữ liệu/domain/PII/phân quyền | Std/Full |
| `constraints/safety.md` | Trước mọi lệnh phá hủy, git, DB, mạng | Mọi tier |
| `constraints/ui.md` | Làm bất kỳ thứ gì có giao diện | Std/Full (Quick nếu chạm UI) |
| `skills/ui-craft-routing.md` | Chọn đúng skill UI (`impeccable` / `hallmark` / `taste-skill`) | Việc có UI |
| `skills/code-intelligence.md` | Dùng CodeGraph, Graphify & Spec Kit trước khi đọc mò | Mọi session có code |
| `constraints/seo.md` + `seo/SEO.md` | Website/trang công khai (SEO · AEO · GEO) | Std/Full có trang công khai |
| `skills/seo-aeo-geo.md` | Viết nội dung, meta, JSON-LD, robots/sitemap, chính sách AI crawler | Trang công khai |
| `docs/ci-cd.md` | Push/PR/auto-merge, deploy, release, rollback | Khi đụng pipeline hoặc ship |
| `docs/business-flows.md` · `docs/roadmap.md` | Xem luồng nghiệp vụ đang chạy (`QT-xx`) · đồng bộ tiến độ sản phẩm | Khi tra hiện trạng / xong feature |
| `design/{DESIGN.md,tokens.css,components.md}` | Dựng/sửa UI (màu, chữ, spacing, UX, component có sẵn) | Việc có UI |
| `specs/<NN>-feat-<slug>/DESIGN.md` | Feature có UI (`UI = Có` trong SPEC) | Std/Full có UI |
| `shared_context.md` (nếu có) | Đụng API/kiểu/event dùng chung | Std/Full |
| `specs/<NN>-feat-<slug>/CONTEXT→SPEC→PLAN→TASKS` | Làm feature đó | Std/Full |
| `specs/<NN>-feat-<slug>/PROGRESS.md` | Đầu & cuối session | Std/Full |
| `specs/_example-create-todo/` | Cần ví dụ văn phong chuẩn | Khi cần |
| `skills/INDEX.md` → skill khớp | Khi chạm domain tương ứng | Khi cần |
| `rfcs/ADR-*` (nếu có) | Hỏi "vì sao thiết kế thế này", hoặc đổi kiến trúc | Khi cần |
| `prompts/*` | Người dán để điều khiển từng pha | — |

## Cấu hình & công cụ
- `.sdd/project.conf` — nguồn duy nhất cho lệnh (`TEST_CMD`, `LINT_CMD`, …); `scripts/verify` đọc file này.
- `.sdd/presets/*.conf` — preset theo stack, dùng bởi `scripts/init-project`.
- Chạy kiểm tra: `scripts/verify` (đủ) · `scripts/verify -Fast` / `--fast` (lint+spec-check+secret, dùng trong pre-commit) · `scripts/spec-check` · `scripts/ui-check` (hardcode màu, inline style, `<img>` thiếu alt; chạy trong verify) · `scripts/seo-check` (SEO/AEO/GEO trên HTML build) · `scripts/ship` (push nhánh an toàn + PR auto-merge) · `scripts/setup-github` (branch protection, auto-merge).

## Profile
- **full** (mặc định) · **lite** cho solo/prototype: bỏ `rfcs/` và `shared_context.md` (chọn khi `init-project -Profile lite`). **Không** bỏ SPEC, safety, verify.
