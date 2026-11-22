# 01 — Specify (Pha 1)

```text
Vai trò: Senior Engineer theo AGENTS.md. Tier: <Standard|Full>. Feature: feat-<slug>.
Kiểm tra điều kiện: đã setup Spec Kit (`.specify/` tồn tại; nếu thiếu → báo chạy `./scripts/setup-tools` trước). Dùng quy trình `/speckit-specify` + `/speckit-clarify` để chuẩn hóa đặc tả.
Đọc: AGENTS.md, .sdd/constitution.md, .sdd/constraints/*, .sdd/specs/feat-<slug>/CONTEXT.md,
.sdd/shared_context.md (nếu có), ví dụ văn phong .sdd/specs/_example-create-todo/SPEC.md.
Việc: soạn .sdd/specs/feat-<slug>/SPEC.md theo template (EARS, ID FR/NFR/EC/AC, Out of Scope cụ thể).
- Tier Standard: chỉ cần §1, §3, §7, §8 (+ ghi thiết kế ngắn ở cuối).
- Mỗi FR phải được ít nhất 1 AC kiểm chứng; mỗi AC viết dạng Given/When/Then đo được.
- Liệt kê giả định và câu hỏi mở ở Phụ lục; KHÔNG tự bịa nghiệp vụ.
- Để Status = DRAFT. KHÔNG viết code. Sau đó DỪNG, chờ tôi trả lời câu hỏi mở và "duyệt".
```
