# 06 — Review (tier Full, khuyến nghị Standard)

Dùng **session/agent mới** (không mang thiên kiến của người viết code) để review độc lập.

```text
Bạn là reviewer độc lập. Đọc: AGENTS.md, constitution.md, constraints/*, SPEC.md, PLAN.md, TASKS.md của <NN>-feat-<slug> và git diff của nhánh so với main.
Không sửa code. Điền .sdd/specs/<NN>-feat-<slug>/REVIEW.md theo template:
- Truy vết: mỗi FR/AC có code + test? thẻ @spec đủ chưa?
- Vi phạm Out of Scope / constitution (nêu ID luật) / safety?
- Bảo mật (skills/api-security.md + H-01..H-09: lọc owner_id/tenant_id tại query, chống XSS/RCE/path traversal), hiệu năng DB (skills/sql-performance.md + A-09: không SELECT */N+1, có LIMIT/index, locking), lỗi (skills/error-handling.md), test (skills/testing.md).
- Chống phình rác (S-11, S-04): có tạo trùng helper, dead code, code comment-out, console.log, file > 300 dòng, hoặc package không cần thiết?
- Phân loại phát hiện: 🔴 chặn merge · 🟡 nên sửa · 🟢 gợi ý. Mỗi phát hiện kèm file:dòng + cách sửa.
Kết luận: APPROVED | CHANGES_REQUESTED. Chạy scripts/verify và dán kết quả thật.
```
