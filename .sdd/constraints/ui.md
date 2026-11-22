# constraints/ui.md — Luật cứng cho giao diện

> Chi tiết & giá trị mặc định nằm ở `.sdd/design/DESIGN.md` và `tokens.css`. Luật có dấu 🤖 được `scripts/ui-check` quét tự động.

| ID | Luật | Cưỡng chế |
|---|---|---|
| U-01 | **Không hardcode màu** (`#hex`, `rgb()`, `hsl()`) trong code UI; chỉ dùng token `--color-*`. | 🤖 `ui-check` |
| U-02 | **Không hardcode spacing/radius/font-size** ngoài thang token trong `DESIGN.md`. | Review · lint khi chốt stack |
| U-03 | **Không inline style** (trừ giá trị động lúc chạy, kèm comment `ui-check:ignore`). | 🤖 `ui-check` |
| U-04 | **Dùng lại component** trong `design/components.md` trước khi tạo mới; tạo mới phải ghi lại vào danh mục. | Review |
| U-05 | **Đủ trạng thái:** không chỉ code mỗi màn hình default — phải có Loading, Empty, Error, Disabled theo `DESIGN.md §3`. | Review · `VAC` |
| U-06 | **Truy cập được (WCAG AA):** bàn phím dùng hết được, focus rõ, tương phản ≥ 4.5:1, nhãn input đủ, `<img>` có `alt`. | 🤖 `ui-check` (`alt`) · `A11Y_CMD` |
| U-07 | **Mobile-first & Responsive:** kiểm ở **375 · 768 · 1280**, không cuộn ngang; vùng chạm ≥ 44px; ảnh có tỉ lệ cố định. | `VISUAL_CMD` · ảnh trong REVIEW |
| U-08 | **i18n & Copy:** mọi chữ hiển thị đi qua file ngôn ngữ (không nối chuỗi), đúng giọng văn trong `DESIGN.md §1`. | Review |

## Điều phối 3 UI Skills (`ui-craft-routing.md`)
- **App UI / Dashboard / Review & sửa UI có sẵn** ⇒ dùng **`impeccable`** (`Mode: Operate/Read`, lệnh `shape`, `critique`, `audit`, `polish`, `harden`) + chạy `npx impeccable detect <UI_SRC>`.
- **Landing Page / Website tạo mới** ⇒ chọn **1** trong **`hallmark`** (50 material theme, hoặc `/hallmark redesign`) hoặc **`design-taste-frontend`** (kiểu `figma.com`/`x.ai`), sau đó hậu kiểm bằng `impeccable`. Không chạy chồng 2 skill tạo layout lên cùng 1 màn hình; mọi skill phải tuân `tokens.css` nếu dự án đã khóa Design System.

## Điều kiện báo xong task UI
Chạy app, **chụp ảnh thật ở 375 và 1280** (so với ảnh Stitch/Figma trong `.sdd/design/references/` nếu có) + chạy `scripts/verify` xanh. Không có ảnh chụp ⇒ chưa xong.

> Cấu hình trong `.sdd/project.conf`: `UI_STACK`, `A11Y_CMD`, `VISUAL_CMD`, `UI_CHECK=warn|error|off`, `UI_SRC=src`.
