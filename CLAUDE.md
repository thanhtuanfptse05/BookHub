# CLAUDE.md — Bộ nhớ & DNA dự án

> **Đọc `AGENTS.md` trước.** File này chứa *kiến thức* (dự án là gì, vì sao như vậy, đã sai ở đâu); `AGENTS.md` chứa *luật*.
> Giữ file ≤ ~300 dòng. Cái gì chi tiết → link sang `docs/` hoặc `.sdd/`.

## 1. Tổng quan
- **Tên:** {{PROJECT_NAME}} — {{PROJECT_ONE_LINER}}
- **Người dùng chính:** {{USERS}}
- **Giai đoạn:** `prototype | MVP | production`  ·  **Cập nhật lần cuối:** {{YYYY-MM-DD}}

## 2. Tech stack & lệnh thường dùng
| Hạng mục | Giá trị |
|---|---|
| Ngôn ngữ / Framework | {{LANGUAGE}} / {{FRAMEWORK}} |
| Database | {{DB}} |
| Package manager | {{PKG_MANAGER}} |
| Cài đặt | `{{INSTALL_CMD}}` |
| Chạy dev | `{{DEV_CMD}}` |
| Test | `{{TEST_CMD}}` |
| Lint / Format | `{{LINT_CMD}}` |
| Build | `{{BUILD_CMD}}` |
| UI stack / Design system | {{UI_STACK}} (chưa chốt = giữ trung lập) · `.sdd/design/` |

## 3. Kiến trúc (Clean Architecture)
```
src/
├── domain/      # Entity, value object, domain service — KHÔNG import tầng ngoài
├── usecase/     # Application logic, port (interface) — chỉ phụ thuộc domain
├── interface/   # Controller/handler/CLI/UI, DTO, validation — gọi usecase
└── infra/       # DB, HTTP client, queue, config — implement port của usecase
```
**Quy tắc phụ thuộc:** `interface → usecase → domain` ; `infra → usecase(port)`. Không đảo chiều.

### Luồng dữ liệu chính
```
Request → interface (validate DTO) → usecase (nghiệp vụ) → port → infra (DB/API) → domain entity → response DTO
```
> TODO: vẽ thêm sơ đồ chi tiết ở `docs/architecture.md` và link vào đây.

## 4. Thuật ngữ nghiệp vụ (Glossary)
| Thuật ngữ | Nghĩa | Lưu ý |
|---|---|---|
| {{TERM}} | {{MEANING}} | |

## 5. Lessons Learned (🩸 bài học xương máu)
> Agent **phải** thêm dòng mới khi: bug lặp lại, sự cố prod, bị người sửa cùng một lỗi ≥ 2 lần (prompt: `.sdd/prompts/10-retro.md`).
> Dòng cũ không xóa — chỉ đánh dấu `(obsolete)`.
> **Nâng cấp bài học:** nếu một quy tắc bị vi phạm lần thứ 2 ⇒ biến nó thành thứ máy kiểm được (lint rule, test kiến trúc, hook) và ghi `→ enforced: <đường dẫn>`. Quá ~20 dòng ⇒ gộp các dòng cùng chủ đề, hoặc chuyển thành skill.

| Ngày | Sự cố / Lỗi | Nguyên nhân gốc | Quy tắc rút ra |
|---|---|---|---|
| {{YYYY-MM-DD}} | _ví dụ: N+1 query làm API timeout_ | _lặp query trong vòng for_ | _Luôn batch/join; xem `skills/sql-performance.md`_ |

## 6. Vùng nguy hiểm (Known Hazards)
- {{ví dụ: `infra/payment/` — đụng vào là phải có người review}}
- {{ví dụ: migration bảng `orders` > 10M dòng — chạy ngoài giờ cao điểm}}

## 7. Quyết định kiến trúc đã chốt
Xem `.sdd/rfcs/` — mỗi quyết định = 1 ADR. Danh sách nhanh:
- ADR-001 — {{tiêu đề}} _(status)_

## 8. Việc đang dang dở / Nợ kỹ thuật
- [ ] {{mô tả}} — _ưu tiên: thấp/vừa/cao_
