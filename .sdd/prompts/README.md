# .sdd/prompts/ — Thư viện prompt cho từng pha

Dán nguyên văn vào chat (thay `<slug>`). Mỗi prompt tự nêu file phải đọc, đầu ra, và điểm dừng chờ người duyệt.

| File | Dùng khi | Tier | Tương đương Spec Kit |
|---|---|---|---|
| `00-start-session.md` | Mở session mới | Mọi tier | — |
| `01-specify.md` | Viết SPEC từ CONTEXT | Std/Full | `/speckit-specify` |
| `02-clarify.md` | Gỡ mơ hồ trước khi khóa | Std/Full | `/speckit-clarify` |
| `03-plan.md` | Lập PLAN kiến trúc | Full | `/speckit-plan` |
| `04-tasks.md` | Phân rã TASKS | Std/Full | `/speckit-tasks` |
| `05-implement.md` | Làm từng task | Mọi tier | `/speckit-implement` |
| `06-review.md` | Tự review + review độc lập | Full (khuyến nghị Std) | — |
| `07-debug.md` | Điều tra lỗi | Mọi tier | — |
| `08-refactor.md` | Dọn code an toàn | Mọi tier | — |
| `09-quick-fix.md` | Sửa nhỏ tier Quick | Quick | — |
| `10-retro.md` | Chốt bài học sau feature/sự cố | Mọi tier | — |
| `11-design-system.md` | Chốt màu/phong cách lần đầu (`design/DESIGN.md`) | Full | — |
| `12-design-feature.md` | Soạn/dịch thiết kế từ Stitch·Figma cho 1 feature | Std/Full có UI | — |
| `13-ui-implement.md` | Code UI sạch + chụp ảnh 375/1280 đối chiếu | Std/Full có UI | — |
| `14-ui-review.md` | Review UI độc lập (ảnh, token, trạng thái, a11y) | Full có UI | — |
| `15-seo-page.md` | SEO · AEO · GEO cho trang công khai (nội dung, schema, robots, seo-check) | Std/Full có trang công khai | — |
| `16-ship.md` | Ship an toàn: verify → push nhánh feature → PR + auto-merge | Mọi tier | — |

## Dùng chung với Spec Kit
Nếu công cụ có lệnh `/speckit-*`, dùng song song được, nhưng **ghi đầu ra vào đường dẫn của template này** (`.sdd/specs/<NN>-feat-<slug>/…`) và tuân `AGENTS.md`/`constraints/*`. Khi mâu thuẫn, **`AGENTS.md` thắng**.

## Quy ước
- Prompt ngắn, nhắc "đọc `AGENTS.md`" — luật không sao chép vào prompt.
- Agent **dừng** ở mỗi Gate và chờ người trả lời `duyệt` / yêu cầu sửa.
