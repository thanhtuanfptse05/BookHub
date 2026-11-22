# skills/INDEX.md — Danh mục skill (nạp theo nhu cầu)

Quy tắc: **chỉ mở skill khớp domain của task**. Mỗi skill có frontmatter `name` + `description` (cách công cụ tự phát hiện). Khuyến nghị: nếu công cụ hỗ trợ thư mục skill riêng (vd `.claude/skills/<name>/SKILL.md`, `.agents/skills/`), liên kết hoặc sao chép skill vào đó; nguồn gốc vẫn là thư mục này.

| Skill | Kích hoạt khi (từ khóa) | File |
|---|---|---|
| sql-performance | query, index, migration, N+1, chậm, phân trang | `sql-performance.md` |
| api-security | endpoint, auth, validate, upload, webhook, CORS, rate limit | `api-security.md` |
| error-handling | exception, lỗi API, retry, timeout, log lỗi | `error-handling.md` |
| testing | viết test, TDD, mock, fixture, coverage, flaky | `testing.md` |
| architecture-guard | import ngược tầng, ranh giới module, dependency-cruiser | `architecture-guard.md` |
| ui-accessibility | a11y, focus, ARIA, form, modal, trình đọc màn hình | `ui-accessibility.md` |
| component-design | component mới, variant, token, ánh xạ thiết kế → code | `component-design.md` |
| responsive-layout | responsive, mobile, breakpoint, tràn/cuộn ngang, grid/flex | `responsive-layout.md` |
| frontend-performance | Core Web Vitals, ảnh/font/JS nặng, nhảy layout | `frontend-performance.md` |
| ui-craft-routing | điều phối 3 UI skill (`impeccable`, `hallmark`, `design-taste-frontend`) | `ui-craft-routing.md` |
| code-intelligence | CodeGraph, Graphify, Spec Kit (`specify`), blast radius, architecture graph | `code-intelligence.md` |
| seo-aeo-geo | SEO, AEO, GEO, meta, JSON-LD, schema, robots, sitemap, AI crawler, trích dẫn AI, answer-first | `seo-aeo-geo.md` |
| _(mẫu)_ | tạo skill mới | `_skill-template.md` |

## Khi nào tạo skill mới
- Cùng một lỗi/chỉ dẫn xuất hiện ≥ 2 lần ở `CLAUDE.md §5`, hoặc một kỹ thuật cần > 10 dòng giải thích.
- Skill ≲ 150 dòng, một chủ đề, có checklist review. Thêm dòng vào bảng trên.
