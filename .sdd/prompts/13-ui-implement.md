# 13 — UI implement (code sạch + chụp ảnh đối chiếu)

```text
Làm task <T-00x> có UI của feat-<slug>. Tuân AGENTS.md §5 và .sdd/constraints/ui.md (U-01…U-08).
Đọc: TASKS.md, SPEC.md, specs/feat-<slug>/DESIGN.md, .sdd/design/{DESIGN.md,tokens.css,components.md}, .sdd/skills/ui-craft-routing.md, và các skill UI liên quan (`impeccable` / `hallmark` / `design-taste-frontend`, `component-design`, `responsive-layout`, `ui-accessibility`).
Quy trình:
1) Cập nhật PROGRESS.md. Kiểm tra SPEC + DESIGN.md phải ở trạng thái LOCKED.
2) Dùng đúng skill UI đã phân vai trong `ui-craft-routing.md` (không trộn layout của `hallmark` và `taste-skill` trên cùng màn hình). Tái sử dụng component trong `components.md` trước; tuyệt đối không copy code export tĩnh từ Figma/Stitch.
3) Dùng token từ tokens.css; không hardcode màu/inline style; mọi chữ qua i18n; làm đủ Loading/Empty/Error/Disabled.
4) Viết test hành vi + gắn thẻ `@spec <slug>/AC-xx`.
5) CHỤP ẢNH THẬT & ĐỐI CHIẾU TỰ ĐỘNG: chạy app trên `localhost`, dùng **Browser / Playwright MCP** (nếu có) mở URL chụp ảnh thật ở 375px và 1280px, so trực tiếp với ảnh gốc trong `.sdd/design/references/` — thấy lệch layout/spacing/màu thì tự căn chỉnh và chụp lại đến khi khớp; chạy `npx impeccable detect <UI_SRC>` (hoặc `sloplint`/`gate.mjs` nếu dùng hallmark/taste-skill) và sửa lỗi trước khi hoàn tất.
6) Chạy `scripts/verify` xanh (gồm ui-check). Tab thử bằng bàn phím.
Báo cáo ngắn: file đổi · ảnh chụp & chênh lệch (nếu có) · VAC đạt chưa. Không có ảnh chụp thật → chưa được báo xong.
```
