# 03 — Plan (Pha 2, tier Full)

```text
Điều kiện: SPEC của <NN>-feat-<slug> đang LOCKED (nếu không → dừng và báo).
Đọc: AGENTS.md, constitution.md, constraints/*, SPEC.md, CLAUDE.md §3 + §5 (kiến trúc & bài học), shared_context.md, skills liên quan (tra .sdd/skills/INDEX.md).
Việc: viết .sdd/specs/<NN>-feat-<slug>/PLAN.md theo template: ≥ 2 phương án, thay đổi theo tầng (domain/usecase/interface/infra/tests/docs), data model + migration up/down, contract, bảng truy vết SPEC→thiết kế→test, rủi ro + rollback.
- Quyết định lớn ⇒ đề xuất ADR trong .sdd/rfcs/.
- KHÔNG viết code implement. Ghi câu hỏi cho người ở §11.
Sau đó DỪNG chờ "duyệt" (Gate 2).
```
