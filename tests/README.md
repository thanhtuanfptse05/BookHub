# tests/

Bộ kiểm thử tự động **BookHub** (JUnit 5):
- `unit/` — Unit tests nhanh cho Service, validation, tính tiền `BigDecimal`, chuyển trạng thái đơn hàng, hết hạn reset token.
- `integration/` — JPA/Hibernate Repository & Service transaction tests trên SQL Server test DB (checkout atomic, rollback, hủy đơn hoàn kho 1 lần, review eligibility, Q&A).
- `e2e/` — Kịch bản kiểm thử luồng đầu-cuối (Servlet routes, authZ, CSRF, XSS encoding, PRG).

Gắn thẻ truy vết trong test: `// @spec <slug>/AC-xx`. Xem `.sdd/skills/testing.md` và danh sách test cases (`TC-*`) tại `BookHub_PRJ301_Project_Plan_Requirements.md` (§21).
