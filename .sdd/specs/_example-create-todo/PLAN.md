# PLAN — Tạo công việc (create-todo)

| Trường | Giá trị |
|---|---|
| SPEC version | 1.0.0 |
| Status | APPROVED |

## 1. Tóm tắt giải pháp
Thêm usecase `CreateTodo` thuần domain, cổng `TodoRepository` (port), controller validate bằng schema, repository SQL. Không đụng module auth ngoài việc đọc `userId` từ token.

## 2. Phương án đã cân nhắc
| Phương án | Ưu | Nhược | Chọn? |
|---|---|---|---|
| A: usecase + port + repo SQL | Đúng kiến trúc, dễ test | Nhiều file | ✅ |
| B: controller ghi thẳng DB | Nhanh | Vi phạm A-01, khó test | ❌ |

## 3. Luồng xử lý
```text
POST /todos → interface(validate DTO, lấy userId từ token) → CreateTodo.execute → TodoRepository.save → 201 DTO
```

## 4. Thay đổi theo tầng
| Tầng | Tạo mới | Sửa | Ghi chú |
|---|---|---|---|
| domain | `todo.entity`, `todo-title.vo` | — | Rule tiêu đề & dueAt |
| usecase | `create-todo.usecase`, `todo.repository.port` | — | |
| interface | `todos.controller`, `create-todo.dto` | router | Validate, map lỗi→422 |
| infra | `sql-todo.repository`, migration `0007_create_todos` | — | up/down |
| tests | unit domain+usecase, integration endpoint | — | |
| docs | — | `openapi.yaml` | |

## 5. Data model & Migration
Bảng `todos` như SPEC §5. Migration `up`: create table + partial index. `down`: drop index, drop table (bảng mới, chưa có dữ liệu).

## 6. Contract & ảnh hưởng chéo
`POST /api/v1/todos` mới → thêm `API-010` vào `shared_context.md`. Không breaking.

## 7. Truy vết
| Yêu cầu | Thành phần | Test |
|---|---|---|
| FR-01 | CreateTodo + controller | AC-01 |
| FR-02, EC-01 | TodoTitle VO | AC-02 |
| FR-03 | Todo entity rule | AC-03 |
| FR-04 | auth guard | AC-04 |
| NFR-02, EC-02 | DTO whitelist | AC-01 |

## 8. Chiến lược kiểm thử
Unit: VO/entity/usecase (fake repo). Integration: endpoint + DB container. Không e2e.

## 9. Rủi ro
| # | Rủi ro | Tác động | Giảm thiểu |
|---|---|---|---|
| R1 | Race khi gửi lặp | Todo trùng | Idempotency-Key (EC-03), unique index (owner_id, key) |

## 10. Rollout
Không cần feature flag (endpoint mới). Rollback: gỡ route; migration `down`.

## 11. Câu hỏi cho người
- [x] Q1: Có cần idempotency? → Có (đã vào SPEC EC-03).

## 12. Phê duyệt (Gate 2)
Trần B · 2026-01-11
