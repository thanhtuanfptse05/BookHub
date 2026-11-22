# TASKS — Tạo công việc (create-todo)

| ID | Task | Phụ thuộc | Ước lượng | Truy vết | Trạng thái |
|---|---|---|---|---|---|
| T-001 | Migration `todos` + index (up/down) | — | 1h | FR-01 | ✅ |
| T-002 | Domain: `TodoTitle`, `Todo` + unit test | — | 2h | FR-02, FR-03, EC-01 | ✅ |
| T-003 | Usecase `CreateTodo` + port + unit test (fake repo) | T-002 | 2h | FR-01 | ✅ |
| T-004 | Repository SQL + integration test | T-001, T-003 | 3h | FR-01 | ✅ |
| T-005 | Controller + DTO + auth + integration test | T-003 | 3h | FR-01..04, EC-02 | ✅ |
| T-006 | Idempotency-Key + OpenAPI + `shared_context.md` | T-005 | 2h | EC-03 | 🔄 |

## Chi tiết task T-006
- **Mục tiêu:** Gửi lặp cùng `Idempotency-Key` không tạo todo thứ 2.
- **File dự kiến:** migration unique index, `create-todo.usecase`, `todos.controller`, `docs/openapi.yaml`.
- **Acceptance:**
  - [ ] Test: gửi 2 lần cùng key → cùng `id`, DB 1 bản ghi (`@spec create-todo/AC-01`)
  - [ ] OpenAPI & `shared_context.md` có `API-010`
- **Không làm:** idempotency cho endpoint khác.

## Đồ thị phụ thuộc
```text
T-001 ─┐
       ├─► T-004
T-002 ─► T-003 ─┬─► T-004
                └─► T-005 ─► T-006
```

## Phê duyệt (Gate 3)
Trần B · 2026-01-11
