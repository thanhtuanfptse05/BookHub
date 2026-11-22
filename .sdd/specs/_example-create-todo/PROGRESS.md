# PROGRESS — Tạo công việc (create-todo)

| Trường | Giá trị |
|---|---|
| Feature | `feat-create-todo` · Tier: Full · Nhánh: `feat/create-todo` |
| Pha hiện tại | implementing |
| Task đang làm | `T-006` |
| Cập nhật | 2026-01-14 16:20 |

## 1. Plan của task hiện tại
- **Mục tiêu:** Idempotency-Key cho `POST /todos` · **Phạm vi:** usecase, controller, migration, openapi
- [x] Đọc SPEC EC-03, PLAN §9
- [x] Test đỏ: gửi 2 lần cùng key (`@spec create-todo/AC-01`)
- [ ] Code tối thiểu → xanh
- [ ] Refactor + lint
- [ ] `scripts/verify` xanh
- [ ] Cập nhật TASKS.md, OpenAPI, shared_context.md

## 2. Nhật ký thực thi
| Giờ | Việc | Kết quả |
|---|---|---|
| 15:40 | Viết integration test 2 lần cùng key | Đỏ như mong đợi |
| 16:10 | Thêm cột `idempotency_key` + unique index | Migration chạy up/down OK |

## 3. Kiểm tra
Lint ✅ · Test ⬜ (1 test đỏ có chủ đích) · Build ✅ · spec-check ✅

## 4. Blockers
- ⬜ Không.

## 5. Quyết định & giả định
- Giả định: key hết hạn sau 24h (không có trong SPEC) → **cần người xác nhận** trước khi code phần TTL.

## 6. Handoff
- **Đã xong:** T-001..T-005 · **Làm tiếp:** hoàn tất T-006 sau khi chốt TTL · **Cẩn thận:** unique index trên `(owner_id, idempotency_key)`, không phải toàn bảng.
