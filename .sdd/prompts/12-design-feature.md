# 12 — Thiết kế feature / Dịch thiết kế từ Stitch & Figma

```text
Feature: feat-<slug> (đã tạo bằng new-feature -Ui).
Đọc: AGENTS.md, .sdd/constraints/ui.md, .sdd/skills/ui-craft-routing.md, .sdd/design/{DESIGN.md,tokens.css,components.md}, CONTEXT.md, SPEC.md, ví dụ .sdd/specs/_example-create-todo/DESIGN.md.
Tham chiếu từ Stitch/Figma (nếu có): <đường dẫn ảnh trong .sdd/design/references/, hoặc gói .zip trong .sdd/design/references/incoming/, hoặc link Stitch/Figma>.
(Nếu có gói `.zip`: giải nén tạm vào `.tmp/` để đọc ảnh & cấu trúc, xóa `.tmp/` + `.zip` sau khi dịch xong — tuyệt đối KHÔNG copy code export vào `src/`.)

Việc: soạn .sdd/specs/feat-<slug>/DESIGN.md (chỉ 4 mục gọn) — chọn skill UI phù hợp theo `ui-craft-routing.md` (`impeccable` cho app UI/dashboard; `hallmark` hoặc `design-taste-frontend` cho landing/website mới):
1) Lập bảng màn hình (SCR-xx) + luồng đi.
2) Ánh xạ giao diện sang component CÓ SẴN trong .sdd/design/components.md (tuyệt đối KHÔNG copy code HTML/CSS rác từ Stitch/Figma vào src/). Nếu bắt buộc tạo component/token mới → ghi rõ để tôi duyệt.
3) Điền các trạng thái thiếu mà ảnh Stitch/Figma thường bỏ qua (Loading, Empty, Error, Disabled), responsive 375 vs 1280, câu copy/i18n chính, lưu ý a11y.
4) Viết 2–3 tiêu chí VAC (Visual Acceptance) kiểm bằng ảnh thật. Nếu SPEC §7 thiếu AC cho trạng thái UI, bổ sung luôn.
Để Status = DRAFT, KHÔNG viết code. DỪNG chờ tôi liếc qua và "duyệt" (Gate 1).
```
