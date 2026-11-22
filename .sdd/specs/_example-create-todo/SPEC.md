# SPEC — Tạo công việc (create-todo)

| Trường | Giá trị |
|---|---|
| Feature ID | `feat-create-todo` |
| Tier | Full |
| UI | Có |
| Status | LOCKED |
| Version | 1.0.0 |
| Owner | Nguyễn A |
| Liên quan | BR-21, API-010 |

---

## 1. Tổng quan
Cho phép user đã đăng nhập tạo một công việc (todo) gồm tiêu đề và hạn chót tuỳ chọn. Là nền tảng cho tính năng nhắc việc sau này. Xem `CONTEXT.md`.

## 2. Actors & User Stories
| ID | Câu chuyện |
|---|---|
| US-01 | Là **user**, tôi muốn **tạo một việc kèm hạn chót** để **không quên**. |

## 3. Functional Requirements (EARS)
| ID | Loại | Mệnh đề | Liên kết |
|---|---|---|---|
| FR-01 | Event | WHEN user gửi `POST /api/v1/todos` với tiêu đề hợp lệ, the system SHALL tạo todo trạng thái `open` thuộc về user đó và trả `201` kèm todo. | US-01 |
| FR-02 | Unwanted | IF tiêu đề rỗng hoặc dài hơn 200 ký tự, THEN the system SHALL trả `422 VALIDATION_ERROR` và không tạo todo. | US-01 |
| FR-03 | Unwanted | IF `dueAt` ở quá khứ, THEN the system SHALL trả `422 VALIDATION_ERROR`. | US-01 |
| FR-04 | Unwanted | IF request không xác thực, THEN the system SHALL trả `401`. | US-01 |

## 4. Non-Functional Requirements
| ID | Hạng mục | Yêu cầu |
|---|---|---|
| NFR-01 | Hiệu năng | p95 < 150 ms @ 100 rps |
| NFR-02 | Bảo mật | Chỉ tạo cho chính mình; `ownerId` lấy từ token, không nhận từ body |
| NFR-03 | Quan sát | Log `requestId`, không log tiêu đề |

## 5. Data & API Contract
```text
todos: id uuid pk, owner_id uuid fk(users) not null, title varchar(200) not null,
       due_at timestamptz null, status text check in (open,done) default open,
       created_at, updated_at, deleted_at
index: (owner_id, created_at desc) where deleted_at is null
```
| Method + Path | Auth | Request | Response (2xx) | Lỗi |
|---|---|---|---|---|
| `POST /api/v1/todos` | user | `{ "title": string, "dueAt"?: ISO-8601 }` | `201 { id, title, dueAt, status }` | `401`, `422 VALIDATION_ERROR` |

## 6. Edge Cases & Xử lý lỗi
| ID | Tình huống | Hành vi |
|---|---|---|
| EC-01 | Tiêu đề chỉ gồm khoảng trắng | Coi là rỗng → `422` |
| EC-02 | Body có thêm trường `ownerId` | Bỏ qua trường lạ, dùng owner từ token |
| EC-03 | Client gửi lặp cùng `Idempotency-Key` | Trả todo đã tạo, không tạo bản thứ 2 |

## 7. Acceptance Criteria (Given / When / Then)
> Mỗi FR xuất hiện ở cột "Kiểm chứng FR" của ≥ 1 AC. Test gắn thẻ `@spec create-todo/AC-xx`.

| ID | Kịch bản | Kiểm chứng FR |
|---|---|---|
| AC-01 | **Given** user đã đăng nhập **When** POST tiêu đề "Mua sữa" **Then** `201`, `status=open`, `ownerId` = user | FR-01 |
| AC-02 | **Given** user đã đăng nhập **When** POST tiêu đề rỗng hoặc 201 ký tự **Then** `422` và DB không có bản ghi mới | FR-02, EC-01 |
| AC-03 | **Given** user đã đăng nhập **When** POST `dueAt` hôm qua **Then** `422` | FR-03 |
| AC-04 | **Given** không có token **When** POST **Then** `401` | FR-04 |

## 8. Out of Scope (🚫)
- Không làm sửa/xóa/hoàn thành todo (feature riêng).
- Không làm nhắc việc, thông báo, chia sẻ todo giữa user.
- Không đổi hệ thống đăng nhập hiện có.

---
## Phụ lục A: Ghi chú thiết kế
Tier Full — xem `PLAN.md`.

## Phụ lục B: Câu hỏi mở & Phê duyệt
- [x] Q1: Hạn chót có bắt buộc? → Không.

- **Duyệt (Gate 1):** Trần B · 2026-01-10 · Version khóa: 1.0.0
