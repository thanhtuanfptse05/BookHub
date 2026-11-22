# 🧭 {{PROJECT_NAME}} — AI-Native Project Template (Spec-Driven Development)

Khung dự án cho **vibe coding có kiểm soát**: Người viết *ý định*, AI viết *code*, mọi thứ đi qua **tier → spec → plan → tasks → code → verify** và được **máy cưỡng chế**, không chỉ ghi chữ.

## 1. Bắt đầu nhanh (5 phút)
```powershell
# 1) Copy template thành repo mới, rồi:
git init
./scripts/init-project.ps1 -Name "Acme Shop" -OneLiner "Nền tảng B2B" -Preset web-ts -Profile full
./scripts/setup-tools.ps1    # cài/khởi tạo Spec Kit (.specify/), CodeGraph (.codegraph/), Graphify & 3 UI Skills
./scripts/setup-github.ps1 -Apply   # (sau khi có remote GitHub) branch protection + auto-merge + environment production
#    (macOS/Linux: ./scripts/init-project.sh ... && ./scripts/setup-tools.sh)
# 2) Điền tay: CLAUDE.md §1-2, .sdd/constraints/global.md, .sdd/constraints/business.md
# 3) Kiểm tra nền: ./scripts/verify.ps1
# 4) Dán .sdd/prompts/00-start-session.md cho Agent → bắt đầu
```
Preset: `generic` · `web-ts` · `python-api` · `frontend-spa` · `web-nextjs` (SSR/SSG, hợp SEO/AEO/GEO) (thêm preset = thêm file `.sdd/presets/<tên>.conf`).
Profile: `full` · `lite` (solo/prototype: bỏ `rfcs/` và `shared_context.md`).

## 2. Chọn tier trước khi làm (AGENTS.md §2)
| Tier | Khi nào | Làm gì |
|---|---|---|
| **Quick** | Sửa nhỏ ≲ 30 dòng, ≤ 2 file | `plan.md` → test hồi quy → `verify` (prompt `09`) |
| **Standard** | Tính năng nhỏ, 1 module, không đổi schema/contract | SPEC rút gọn → TASKS → code (`new-feature -Tier Standard`) |
| **Full** | Chạm DB schema / auth / tiền / PII / contract / CI / > 3 module / > 1 ngày | CONTEXT → SPEC → PLAN → TASKS → code → REVIEW |

```
Full:  CONTEXT ─► SPEC 🔒G1 ─► PLAN 🔒G2 ─► TASKS 🔒G3 ─► IMPLEMENT ─► REVIEW(độc lập) ─► DONE
Std :  CONTEXT ─► SPEC 🔒G1 ─► TASKS ─► IMPLEMENT ─► DONE
Quick: plan.md ─► test đỏ ─► fix ─► verify
```

## 3. Bản đồ file
| File | Ai viết | Vai trò |
|---|---|---|
| `AGENTS.md` | Người | **Nguồn luật duy nhất** (persona, tier, quyền, DoD) |
| `CLAUDE.md` · `GEMINI.md` · `.cursor/rules/` · `.github/copilot-instructions.md` | Người | Điểm vào theo công cụ — chỉ trỏ về `AGENTS.md` (CLAUDE.md còn giữ bộ nhớ dự án) |
| `plan.md` | Agent | Con trỏ session + sổ nháp tier Quick |
| `.agentignore` | Người | Chặn Agent đọc file rác/nhạy cảm (một số công cụ mới tôn trọng — đừng chỉ dựa vào đây) |
| `.sdd/INDEX.md` | Người | Bản đồ "đọc gì khi nào", ngân sách token |
| `.sdd/project.conf` + `presets/` | init | Nguồn duy nhất cho lệnh lint/test/build |
| `.sdd/constitution.md` | Người | Hiến pháp 3 tầng, chỉ đổi qua ADR |
| `.sdd/constraints/*` | Người | Tech stack · nghiệp vụ · an toàn · **UI** (`ui.md`) |
| `.sdd/design/` | Người + Agent | Gọn 3 file: `DESIGN.md` (cẩm nang), `tokens.css` (giá trị), `components.md` (danh mục) + `references/` (ảnh Stitch/Figma) |
| `.sdd/shared_context.md` | Agent (người duyệt) | API contract & breaking changes (bỏ ở profile lite) |
| `.sdd/specs/feat-*/` | Người + Agent | `CONTEXT, SPEC, PLAN, TASKS, PROGRESS, REVIEW, CHANGELOG` |
| `.sdd/specs/_example-create-todo/` | — | Ví dụ điền sẵn chuẩn văn phong |
| `.sdd/prompts/` | Người dùng | 15 prompt dán-và-chạy cho từng pha (gồm 4 prompt UI) (có map `speckit-*`) |
| `.sdd/skills/` | Người (senior) | Playbook; `INDEX.md` để nạp theo nhu cầu |
| `.sdd/rfcs/ADR-*` | Người + Agent | Quyết định kiến trúc |
| `.sdd/seo/SEO.md` + `templates/` | Người + Agent | SEO·AEO·GEO của website: định danh thực thể, chính sách AI crawler, danh mục trang; mẫu `robots.txt`, `llms.txt` |
| `docs/ci-cd.md` | Người | CI/CD tự động: luồng, secrets, branch protection, deploy, rollback |
| `docs/business-flows.md` · `docs/roadmap.md` | Agent + Người | Luồng nghiệp vụ **hiện trạng đang chạy** (`QT-xx` trace từ `src/`) & lộ trình sản phẩm tự đồng bộ |

## 4. Cưỡng chế bằng máy (không chỉ là lời khuyên)
| Cơ chế | Chặn gì |
|---|---|
| `scripts/verify` | Gộp: spec-check + lint + ui-check + test + build + audit (`AUDIT_CMD`) + dead-code (`UNUSED_CMD`) + secret scan (gitleaks hoặc regex dự phòng) |
| `scripts/spec-check` | SPEC LOCKED phải đủ: không placeholder, không câu hỏi mở, mỗi FR có AC, Out of Scope không trống, Full có PLAN; task xong thì mọi AC phải có test thẻ `@spec <slug>/AC-xx`, Full phải có REVIEW `APPROVED`; cảnh báo file `src/` > 300 dòng & vượt ngân sách dòng |
| `scripts/ui-check` | Phát hiện hardcode màu, inline style, `<img>` thiếu alt (`UI_CHECK=off\|warn\|error` trong `project.conf`) |
| `scripts/seo-check` | Quét HTML đã build: title/description/h1/canonical/OG/JSON-LD/alt, nội dung có trong HTML thô (SE-01), robots.txt + sitemap, chính sách AI crawler (`SEO_CHECK`, `SEO_BUILD_DIR`, `AI_CRAWLERS`) |
| `.githooks/pre-push` | Chặn push lên main/prod/release và chặn force-push |
| `scripts/ship` | **Tự động push an toàn:** verify đầy đủ → commit → push nhánh feature (không force) → PR + auto-merge |
| `ci.yml` · `deploy.yml` · `release.yml` | CI (verify, secrets, Lighthouse) → deploy tự động sau CI xanh (environment `production`, smoke test) → release tự động (release-please) |
| `.githooks/pre-commit` | Cấm commit lên nhánh chính; chạy `verify --fast` |
| `.githooks/commit-msg` | Bắt buộc Conventional Commits |
| `.github/workflows/ci.yml` | `verify --strict` + gitleaks trên mọi PR |
| `skills/architecture-guard.md` | Hướng dẫn biến luật phân tầng thành rule máy (dependency-cruiser, import-linter, ArchUnit…) |

Hook được bật bởi `init-project`, hoặc chạy `./scripts/setup-hooks.ps1|.sh`. Thêm **branch protection** trên `{{MAIN_BRANCH}}` ở GitHub/GitLab (cần cấu hình thủ công).

## 4b. Giao diện / Design (Gọn cho Vibe Code — trung lập stack)
Bạn **không phải tự điền tay** các file thiết kế — chỉ làm đúng 3 bước:
1. **Chốt tông màu/vibe (1 lần):** dán `prompts/11-design-system` → AI tự cập nhật `.sdd/design/DESIGN.md` & `tokens.css` (nếu dùng Stitch, nạp `DESIGN.md` vào Stitch để giữ đúng tông).
2. **Vẽ màn hình ngoài (Stitch/Figma):** lưu ảnh chụp vào `.sdd/design/references/`.
3. **Dịch sang spec & code sạch:** chạy `new-feature -Ui` → dán `prompts/12-design-feature` để AI dịch ảnh sang `DESIGN.md` 4 mục (liếc duyệt 30 giây) → `prompts/13-ui-implement` để AI code bằng token/component + chụp ảnh 375/1280 đối chiếu. *Tuyệt đối không copy code rác xuất từ Stitch/Figma vào `src/`.*

- **Luật:** `constraints/ui.md` (U-01…U-08) + `scripts/ui-check` tự quét hardcode màu, inline style, `<img>` thiếu `alt`.
- **3 UI Skills điều phối (`skills/ui-craft-routing.md`):** `impeccable` (App UI/dashboard & audit/polish + `npx impeccable detect`), `hallmark` (website mới 50 themes / redesign), `design-taste-frontend` (landing/site công ty mới). Không trộn `hallmark` và `taste-skill` trên cùng màn hình.
- **Code Intelligence & Spec Kit (`skills/code-intelligence.md`):** Bắt buộc setup Spec Kit (`setup-tools` → `.specify/`). Dùng `codegraph` (`codegraph_explore`/`impact`/`affected`) trước `grep`, và `graphify` (`query_graph`/`GRAPH_REPORT.md`) cho câu hỏi kiến trúc & Code↔Docs↔Spec.
- **Khi chốt stack UI:** tạo ADR, điền `UI_STACK`, `A11Y_CMD`, `VISUAL_CMD` trong `project.conf`. Không phải sửa lại spec/design.

## 4c. SEO · AEO · GEO (website công khai)
- **SEO** (xếp hạng) · **AEO** (được chọn làm câu trả lời trực tiếp) · **GEO** (AI trích dẫn). Luật `constraints/seo.md` (SE-01…SE-15), playbook `skills/seo-aeo-geo.md`, nguồn sự thật `.sdd/seo/SEO.md`, prompt `15-seo-page`.
- **Điểm sống còn:** nội dung phải có trong **HTML trả về lần đầu** (SSR/SSG) vì AI crawler thường không chạy JS — dùng preset `web-nextjs` hoặc framework SSR/SSG khác; SPA thuần client-render sẽ bị `seo-check` báo SE-01.
- **Chính sách AI crawler** (`AI_CRAWLERS=allow|block-training|block-all`) là quyết định của bạn: chặn *training* ≠ chặn *search*; `seo-check` kiểm `robots.txt` khớp chính sách.
- **Trung thực:** `llms.txt` chỉ là tùy chọn (chưa có bằng chứng hiệu quả); FAQ rich result của Google đã ngừng — nhưng nội dung hỏi–đáp rõ ràng vẫn có ích cho AEO/GEO. Không có công cụ nào *đảm bảo* thứ hạng/trích dẫn; template chỉ bảo đảm nền tảng kỹ thuật + quy trình nội dung đúng.

## 4d. CI/CD tự động & push an toàn (`docs/ci-cd.md`)
- **Agent chỉ push nhánh `feat|fix|chore/*` bằng `scripts/ship`** (verify đầy đủ → push không force → PR + auto-merge); không bao giờ push `main` hay deploy tay.
- **GitHub làm phần còn lại:** CI xanh → tự merge → `deploy.yml` tự deploy (smoke test, rollback bằng `ref`) → `release.yml` tự tạo Release PR/tag/CHANGELOG. Dependabot giữ dependency mới.
- **Thiết lập một lần:** có remote → `./scripts/setup-github.ps1 -Apply` (branch protection, auto-merge, environment `production`) → điền `DEPLOY_CMD`/`SITE_URL` + Secrets.

## 5. Vòng đời một feature
```powershell
./scripts/new-feature.ps1 user-login -Tier Full     # tạo .sdd/specs/feat-user-login/
# điền CONTEXT.md → dán prompts/01-specify → 02-clarify (khóa SPEC) → 03-plan → 04-tasks
# → 05-implement (từng task) → 06-review (agent/session KHÁC) → 10-retro
./scripts/verify.ps1
```
Gắn thẻ truy vết trong test: `// @spec user-login/AC-01`.

## 6. Tùy biến theo loại dự án
| Loại | Việc cần chỉnh |
|---|---|
| Backend/API | Giữ nguyên; điền `business.md` (PII, soft-delete) |
| Frontend/SPA | Preset `frontend-spa`; thêm skill a11y/state; bớt mục SQL |
| Mobile | Thêm skill offline-sync, quyền thiết bị |
| Data/ML | Thêm data contract vào `shared_context.md`, skill kiểm thử dữ liệu |
| Monorepo | `AGENTS.md` con cho mỗi package; `.sdd/` giữ ở gốc |
| Solo/prototype | `-Profile lite`; **không** bỏ SPEC + safety + verify |

## 7. Giới hạn cần biết
- Các luật `.md` chỉ hiệu lực với công cụ AI thực sự đọc chúng; phần **máy cưỡng chế** (hook, CI) mới là hàng rào thật. Hook có thể bị bỏ qua cục bộ bằng `--no-verify`, nên CI + branch protection là lớp chốt.
- `spec-check` chỉ kiểm cấu trúc và truy vết, không đánh giá chất lượng nội dung spec — người vẫn là Gate.
- `.agentignore` không phải công cụ nào cũng tôn trọng; đừng để secret trong repo.
