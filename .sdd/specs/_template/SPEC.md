# SPEC — {{Tên feature}}

> **Pha 1 · Hợp đồng đặc tả.** Agent soạn nháp từ `CONTEXT.md`, **người duyệt & khóa** (Gate 1).
> Sau khi `Status: LOCKED`, sửa đổi chỉ qua `CHANGELOG.md` + người duyệt. **Chưa LOCKED ⇒ cấm code.**

| Trường | Giá trị |
|---|---|
| Feature ID | `{{num}}-feat-{{slug}}` |
| Tier | Full |
| UI | Không |
| Status | `DRAFT → REVIEW → LOCKED` |
| Version | 0.1.0 |
| Owner | {{người}} |
| Liên quan | `constraints/business.md` (BR-…), `shared_context.md` (API-…) |

---

## 1. Tổng quan (Overview)
Một đoạn ≤ 5 câu: feature làm gì, cho ai, giá trị gì. Link `CONTEXT.md`.

## 2. Actors & User Stories
| ID | Câu chuyện |
|---|---|
| US-01 | Là **{{actor}}**, tôi muốn **{{hành động}}** để **{{lợi ích}}**. |

## 3. Functional Requirements (EARS)
> Mẫu EARS — dùng đúng một mẫu cho mỗi mệnh đề; mỗi mệnh đề **kiểm thử được** & có ID.
> - **Ubiquitous:** The system shall `<response>`.
> - **Event-driven:** WHEN `<trigger>`, the system shall `<response>`.
> - **State-driven:** WHILE `<state>`, the system shall `<response>`.
> - **Optional feature:** WHERE `<feature included>`, the system shall `<response>`.
> - **Unwanted behaviour:** IF `<unwanted condition>`, THEN the system shall `<response>`.
> - (Tiếng Việt: **KHI** … **HỆ THỐNG PHẢI** …)

| ID | Loại | Mệnh đề | Liên kết (US / BR) |
|---|---|---|---|
| FR-01 | Event | WHEN {{trigger}}, the system SHALL {{response}}. | US-01 |
| FR-02 | Unwanted | IF {{điều kiện lỗi}}, THEN the system SHALL {{xử lý}}. | US-01 |

## 4. Non-Functional Requirements
| ID | Hạng mục | Yêu cầu (số cụ thể) |
|---|---|---|
| NFR-01 | Hiệu năng | p95 < {{300 ms}} @ {{N rps}} |
| NFR-02 | Bảo mật | {{authN/authZ, rate limit, mã hóa PII…}} |
| NFR-03 | Độ tin cậy | {{retry, idempotency, timeout}} |
| NFR-04 | Quan sát | {{log, metric, trace cần có}} |
| NFR-05 | Khả dụng/A11y/i18n | {{...}} |

## 5. Data & API Contract
**Mô hình dữ liệu** (bảng/trường mới, quan hệ, index, ràng buộc):
```text
{{table: field type constraints}}
```
**API / Event** (khớp `docs/openapi.yaml`, ghi vào `shared_context.md` khi đổi):
| Method + Path | Auth | Request | Response (2xx) | Lỗi (4xx/5xx) |
|---|---|---|---|---|
| `POST /api/v1/{{...}}` | {{role}} | `{...}` | `201 {...}` | `400 VALIDATION_ERROR`, `409 CONFLICT` |

## 6. Edge Cases & Xử lý lỗi
| ID | Tình huống | Hành vi mong đợi |
|---|---|---|
| EC-01 | Input rỗng / sai kiểu / quá dài | {{...}} |
| EC-02 | Gọi trùng (idempotency) / race condition | {{...}} |
| EC-03 | Dịch vụ ngoài timeout / lỗi | {{...}} |
| EC-04 | Không đủ quyền / hết phiên | {{...}} |

## 7. Acceptance Criteria (Given / When / Then)
> Feature có giao diện: đặt `UI = Có` ở đầu file, dùng `DESIGN.md` 4 mục (duyệt cùng Gate 1) và thêm AC cho các trạng thái UI quan trọng (loading/empty/error).
> Mỗi FR phải xuất hiện ở cột "Kiểm chứng FR" của ít nhất 1 AC. Test gắn thẻ truy vết `@spec <slug>/AC-xx` (tên test hoặc comment) — `scripts/spec-check` dò thẻ này.
| ID | Kịch bản | Kiểm chứng FR |
|---|---|---|
| AC-01 | **Given** {{bối cảnh}} **When** {{hành động}} **Then** {{kết quả quan sát được}} | FR-01 |
| AC-02 | **Given** … **When** … **Then** … | FR-02, EC-01 |

## 8. Out of Scope (🚫 TUYỆT ĐỐI KHÔNG LÀM)
- {{vd: Không làm đăng nhập bằng mạng xã hội}}
- {{vd: Không đụng tới module thanh toán}}
- {{vd: Không refactor code cũ ngoài file bắt buộc}}
> Agent thấy cần làm một mục ở đây ⇒ **dừng & hỏi**, không tự mở rộng.

---
## Phụ lục A: Ghi chú thiết kế (tier Standard — thay cho PLAN.md)
{{Hướng tiếp cận, file/tầng sẽ đụng, rủi ro chính — ≤ 15 dòng. Tier Full: bỏ trống, dùng PLAN.md.}}

## Phụ lục B: Câu hỏi mở & Phê duyệt
- [ ] Q1: {{...}} — _người trả lời:_ {{...}}  _(đánh `[x]` và xóa khi đã giải quyết)_

### Definition of Ready — chỉ khóa (LOCKED) khi đủ cả
- [ ] Không còn placeholder chưa điền và không còn câu hỏi mở `- [ ] Q…`
- [ ] Mỗi FR có ≥ 1 AC; mỗi AC đo được (Given/When/Then)
- [ ] Out of Scope có ≥ 1 mục cụ thể; tier khớp bảng ở `AGENTS.md §2`
- [ ] Không mâu thuẫn `constraints/*`; `shared_context.md` đã ghi nếu đổi contract
- [ ] `scripts/spec-check` không báo lỗi

- **Duyệt (Gate 1):** {{tên}} · {{YYYY-MM-DD}} · Version khóa: {{x.y.z}}
