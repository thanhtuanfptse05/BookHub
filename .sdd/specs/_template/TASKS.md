# TASKS — {{Tên feature}}

> **Pha 3 · Agent phân rã `PLAN.md`** thành task atomic. Người duyệt (Gate 3) rồi mới `implement`.
> **Quy tắc task:** ≤ 4 giờ · 1 mục tiêu · 1 commit/PR nhỏ · có Acceptance rõ · có test · không chồng lấn file với task song song.

**Trạng thái:** `⬜ todo` · `🔄 doing` · `✅ done` · `⛔ blocked`

| ID | Task | Phụ thuộc | Ước lượng | Truy vết | Trạng thái |
|---|---|---|---|---|---|
| T-001 | Migration + model `{{...}}` | — | 2h | FR-01 | ⬜ |
| T-002 | Domain entity + rule `{{...}}` | — | 2h | FR-01 | ⬜ |
| T-003 | Usecase `{{...}}` + unit test | T-001, T-002 | 3h | FR-01, AC-01 | ⬜ |
| T-004 | Endpoint + validation + integration test | T-003 | 3h | FR-01, FR-02, EC-01 | ⬜ |
| T-005 | Cập nhật OpenAPI + `shared_context.md` | T-004 | 1h | — | ⬜ |
| T-006 | E2E + kiểm tra NFR | T-004 | 3h | AC-01, NFR-01 | ⬜ |

## Chi tiết task (nhân bản cho mỗi task)
### T-001 — {{tiêu đề}}
- **Mục tiêu:** {{1 câu}}
- **File dự kiến:** {{...}}
- **Việc cần làm:** {{...}}
- **Acceptance (kiểm chứng được):**
  - [ ] {{...}}
  - [ ] Test: {{tên test}} xanh
- **Không làm (scope guard):** {{...}}
- **Definition of Done:** theo `AGENTS.md §6`

## Đồ thị phụ thuộc
```text
T-001 ─┐
       ├─► T-003 ─► T-004 ─┬─► T-005
T-002 ─┘                   └─► T-006
```

## Phê duyệt (Gate 3)
{{tên}} · {{YYYY-MM-DD}}
