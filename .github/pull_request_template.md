## Tóm tắt
<!-- 1–3 câu: làm gì, vì sao -->

## Truy vết
- Feature: `.sdd/specs/feat-<slug>/` · Tier: Quick / Standard / Full
- Task: `T-xxx` · SPEC version: x.y.z
- FR/AC đáp ứng: <!-- FR-01, AC-01 -->

## Cách kiểm chứng
- [ ] `scripts/verify` xanh (dán output nếu cần)
- Test thêm: <!-- tên test có thẻ @spec <slug>/AC-xx -->

## Checklist (Definition of Done)
- [ ] Không làm gì thuộc **Out of Scope**; không vi phạm constitution/constraints
- [ ] Test mới + test hồi quy; không sửa/skip test cũ
- [ ] Không secret/PII trong code, log, ảnh chụp; `AUDIT_CMD` không báo CVE nghiêm trọng
- [ ] Không dead code, code comment-out, `console.log`/file nháp, trùng lặp helper sẵn có (`S-11`)
- [ ] DB: không `SELECT *` / N+1, có `LIMIT` + index cho FK, migration có `down` (`A-09`, `H-07`)
- [ ] Contract/schema đổi? → OpenAPI + `shared_context.md` + migration có `down`
- [ ] Dependency mới? → đã được duyệt (nêu lý do, license)
- [ ] Trang công khai: `seo-check` sạch, tuân SE-01…SE-11 (`constraints/seo.md`)
- [ ] Tier Full: `REVIEW.md` = APPROVED

## Rủi ro & rollback
<!-- ảnh hưởng, cách rollback -->
