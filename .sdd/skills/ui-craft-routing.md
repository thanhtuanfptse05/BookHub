---
name: ui-craft-routing
description: Điều phối 3 skill UI (impeccable, hallmark, design-taste-frontend) để chống giao diện AI rập khuôn mà không xung đột lẫn nhau. Kích hoạt mọi khi thiết kế, tạo mới, redesign, audit hoặc tinh chỉnh UI.
---

# Skill: UI Craft Routing (Impeccable · Hallmark · Taste Skill)

Dùng đúng skill cho đúng việc. **Không chạy chồng 2 skill tạo layout lên cùng một màn hình.**
Nếu `.sdd/design/DESIGN.md` & `tokens.css` đã `LOCKED`, **hiến pháp thị giác dự án luôn thắng** — mọi skill phải tuân theo bảng màu/token/giọng văn đã chốt.

## 1. Công dụng & Phân vai 3 skill
| Skill | Mạnh nhất ở | Khi nào kích hoạt | Điểm đặc biệt |
|---|---|---|---|
| **`impeccable`** (`pbakaus/impeccable`) | **App UI / SaaS bên trong (`Operate`, `Read`)** & **tinh chỉnh/review UI có sẵn** | Tạo dashboard/form/admin/settings; chỉnh sửa UI đang có; review/audit mọi UI | 24 lệnh (`shape`, `critique`, `audit`, `polish`, `bolder`, `quieter`, `distill`, `harden`, `clarify`, `adapt`, `optimize`, `extract`, `live`) + bộ quét **62 lỗi slop cơ học** `npx impeccable detect` |
| **`hallmark`** (`nutlope/hallmark`) | **Trang/Website tạo mới** có phong cách vật liệu rõ rệt hoặc `/hallmark redesign` | Dựng landing page/site mới từ đầu; hoặc khi user gọi `/hallmark redesign` / `audit` | 50 material themes, dựng theo job-to-be-done, hero trước, "The Floor" (cấm purple gradient, eyebrow thừa, 1 từ italic), `sloplint.mjs --render` + subagent review. *Không tự chạy trên UI đã có.* |
| **`design-taste-frontend`** (`leonxlnx/taste-skill`) | **Website thương mại / sản phẩm** cấp độ `figma.com` / `x.ai` | Dựng site công ty/sản phẩm mới (`software`, `gaming`, `tech`, `product`, `service`, `place`) | 1 section/màn hình, lề trái-phải cân, ảnh thật, micro-animation; shader/scroll scene chỉ dùng cho `tech/software/gaming`; `gate.mjs` kiểm trên Chrome/Edge (Node 22+) |

## 2. Quy tắc chọn & phối hợp bắt buộc
1. **App UI / Dashboard / Form / Admin / Sửa UI hiện có ⇒ dùng `impeccable`** (Mode `Operate` hoặc `Read`). Không gọi `hallmark` hay `taste-skill` làm xáo trộn layout app.
2. **Landing Page / Website marketing (`Persuade` / `Experience`) tạo mới ⇒ chọn 1 trong 2:**
   - Chọn **`hallmark`** khi muốn chọn từ 50 material theme hoặc đang `/hallmark redesign` trang cũ.
   - Chọn **`design-taste-frontend`** khi muốn style website sản phẩm/công ty hiện đại (`figma.com`/`x.ai`), 1 section/screen, nhiều motion/ảnh thật.
3. **Hậu kiểm bắt buộc cho MỌI UI (kể cả sau khi dùng Hallmark hay Taste Skill):**
   - Dùng **`impeccable`** (`audit` / `harden` / `polish`) hoặc chạy `npx impeccable detect <UI_SRC>` để quét 62 lỗi slop cơ học trước khi báo xong.
   - Đủ trạng thái Loading / Empty / Error (`constraints/ui.md` U-05) và chụp ảnh thật ở 375 / 1280.

## 3. Cài đặt (nếu máy chưa có)
Chạy `./scripts/setup-tools.ps1` (Windows) hoặc `./scripts/setup-tools.sh` (macOS/Linux).
