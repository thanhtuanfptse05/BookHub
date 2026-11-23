---
name: code-intelligence
description: Quy tắc dùng CodeGraph, Graphify và Spec Kit (specify) để đọc hiểu codebase, đánh giá blast radius, lọc test và lập spec chuẩn. Kích hoạt trước khi tìm kiếm codebase rộng, refactor, hoặc chạy Pha 1–3.
---

# Skill: Code Intelligence & Spec Kit

Bộ ba công cụ chuẩn hóa ngữ cảnh giúp Agent **không đọc mò tốn token**, **không bỏ sót ảnh hưởng khi sửa**, và **viết spec đúng chuẩn**.

## 1. Phân biệt CodeGraph vs Graphify (dùng đúng việc)
| Công cụ | Cách hoạt động | Dùng khi nào | Lệnh / MCP chính |
|---|---|---|---|
| **`codegraph`** (`colbymchenry/codegraph`) | Tree-sitter index AST vào SQLite cục bộ (`.codegraph/codegraph.db`, FTS5) — **chuyên mã nguồn** | Trước khi `grep`/`read_file` mò symbol; truy vết **call graph**, tìm `callers`/`callees`, đo **blast radius (`impact`)**, tìm **test bị ảnh hưởng (`affected`)** trước khi sửa | MCP: `codegraph_explore`<br>CLI: `codegraph explore`, `callers`, `callees`, `impact`, `affected` |
| **`graphify`** (`Graphify-Labs/graphify`) | Knowledge Graph hợp nhất **Code + Docs + SPEC + ADR + Ảnh/Video** (`graphify-out/`) | Tìm hiểu **kiến trúc tổng thể**, God Nodes, ranh giới community, câu hỏi liên kết giữa tài liệu/spec và code, khi cần sơ đồ `GRAPH_REPORT.md` | MCP: `query_graph`, `get_neighbors`, `god_nodes`<br>CLI: `graphify query`, `path`, `explain`, `graphify . --update` |

## 2. Quy tắc bắt buộc khi đọc & sửa code
1. **CodeGraph trước Grep (khi `.codegraph/` tồn tại):** Muốn tìm caller, callee, hoặc hiểu 1 luồng code ⇒ gọi `codegraph_explore` / `codegraph impact <symbol>` **trước** khi quét file thủ công.
2. **Kiểm tra Blast Radius & Test ảnh hưởng:** Trước khi sửa interface/hàm dùng chung ⇒ chạy `codegraph impact <symbol> --depth 3` và `git diff --name-only | codegraph affected --stdin`.
3. **Graphify trước khi đọc hết docs (khi `graphify-out/graph.json` tồn tại):** Câu hỏi về kiến trúc hệ thống, ADR, liên kết giữa các module ⇒ gọi `query_graph` / đọc `graphify-out/GRAPH_REPORT.md` trước.
4. **Cập nhật đồ thị sau thay đổi lớn:** Xong feature hoặc thêm/đổi cấu trúc file ⇒ chạy `codegraph sync` (và `graphify . --update --no-viz` nếu dùng Graphify).

## 3. Spec Kit (`github/spec-kit` / `specify`)
- **Bắt buộc setup trước khi viết Spec (tier Standard/Full):** Chạy `scripts/setup-tools` (gồm `specify init . --integration agy --force --ignore-agent-tools`). Nếu `REQUIRE_SPECKIT=true` trong `.sdd/project.conf`, `scripts/spec-check` sẽ chặn nếu thiếu `.specify/`.
- **Phối hợp `.sdd/` ↔ Spec Kit:**
  - Dùng quy trình `/speckit-specify` (EARS, Out of Scope, AC) → `/speckit-clarify` → `/speckit-plan` → `/speckit-tasks` → `/speckit-analyze` → `/speckit-implement`.
  - Hợp đồng feature chính thức của repo lưu tại `.sdd/specs/<NN>-feat-<slug>/` (tiền tố số 2 chữ số `01`, `02`, …); khi mâu thuẫn luật, `.sdd/constitution.md` và `.sdd/constraints/safety.md` thắng.

## 4. Multi-Agent & Tiết kiệm Token
- **Khi chia nhiều Agent / Subagent song song:**
  - Mỗi agent chỉ sở hữu một tập file tách biệt — **không để 2 agent cùng sửa 1 file**.
  - Ghi tài nguyên đang sửa vào `.sdd/shared_context.md §5` trước khi bắt đầu và gỡ sau khi xong.
  - Dùng nhánh hoặc workspace/worktree tách biệt cho các task độc lập; mọi contract chung phải đồng bộ qua `shared_context.md`.
- **Tiết kiệm Token:**
  - Phân rõ vùng *luôn đọc* (`AGENTS.md`, `CLAUDE.md`, `plan.md`) và *mở khi cần* (`.sdd/INDEX.md`).
  - Dùng `codegraph_explore` / `query_graph` lấy đúng đoạn code/symbol cần thay vì đọc toàn bộ file lớn; chỉ trích dẫn đường dẫn + dòng, không chép nguyên văn file dài vào chat.
