# DESIGN.md — Cẩm nang UI & Design System ({{PROJECT_NAME}})

> **File duy nhất cho quy chuẩn giao diện.** Chưa chốt stack UI vẫn dùng được: giá trị nằm trong `tokens.css` (CSS custom properties), danh mục component ở `components.md`.
> **Cách dùng nhanh cho Vibe Code:** Bạn chỉ cần chốt **Mục 1** (cá tính, màu, giọng văn) 1 lần đầu dự án (dùng prompt `11-design-system`). Các mục **2–6 là luật mặc định cho AI tự đọc** — không cần sửa nếu không có yêu cầu đặc biệt.
> Trạng thái: `DRAFT → LOCKED`. Đổi token/màu chủ đạo sau khi LOCKED = tier **Full**.

---

## 1. Thương hiệu & Giọng văn *(Phần người chốt — 1 lần)*
- **Cá tính (3 từ):** {{vd: rõ ràng · tin cậy · nhẹ nhàng}} · **Tham khảo:** {{link Stitch/Figma hoặc ảnh trong references/}}
- **Màu chủ đạo:** chỉnh `--color-primary` trong `tokens.css` (mặc định đang đạt tương phản AA sáng/tối).
- **Ngôn ngữ & giọng văn:** mặc định `{{vi}}` (hỗ trợ `{{vi, en}}`) · giọng {{vd: thân thiện, xưng "bạn", câu ngắn}}.
- **Công thức microcopy mặc định:**
  - Nút = **Động từ + đối tượng** ("Tạo việc", không "OK/Submit").
  - Lỗi = **Chuyện gì + cách sửa** ("Tiêu đề quá 200 ký tự. Hãy rút gọn lại.").
  - Xóa/nguy hiểm = **Modal nêu đối tượng + hậu quả** ("Xóa 'Mua sữa'? Không thể khôi phục.").
  - **Mọi chữ đưa vào file i18n**, không hardcode trong component.

---

## 2. Token & Quy tắc hiển thị *(AI đọc từ `tokens.css`)*
| Nhóm | Token chuẩn | Quy tắc |
|---|---|---|
| **Màu (theo vai trò)** | `--color-bg`, `--color-surface`, `--color-surface-raised`, `--color-border`, `--color-text`, `--color-text-muted`, `--color-primary`, `--color-primary-hover`, `--color-primary-contrast`, `--color-success`, `--color-warning`, `--color-danger`, `--color-info`, `--color-focus-ring` | **Cấm hardcode màu** (`#hex`, `rgb()`). Chỉ dùng token vai trò. Hỗ trợ `[data-theme="dark"]`. |
| **Chữ** | `--font-sans`, `--font-mono`, `--text-xs`(12) → `--text-4xl`(36) | Tối đa 2 font. Thân bài `16px` (`line-height: 1.5`), input mobile ≥ `16px` để không bị zoom. |
| **Khoảng cách** | Thang 4px: `--space-1`(4) `2`(8) `3`(12) `4`(16) `6`(24) `8`(32) `12`(48) `16`(64) | Không dùng số lẻ ngoài thang. Vùng chạm tối thiểu **44×44px** (`--touch-target-min`). |
| **Hình khối & Motion** | `--radius-sm/md/lg/full`, `--shadow-sm/md/lg`, `--z-dropdown/sticky/modal/toast`, `--motion-fast/base/slow` | Chuyển động tắt khi `prefers-reduced-motion`. |
| **Breakpoint** | `base`(375) · `sm`(480) · `md`(768) · `lg`(1024) · `xl`(1280) | Mobile-first. Mọi màn hình kiểm ở **375 · 768 · 1280** (không cuộn ngang ở 320px). |

---

## 3. Quy tắc UX & Trạng thái mặc định *(AI tự áp dụng nếu feature không ghi khác)*
- **5 trạng thái cơ bản:** Mọi màn hình/danh sách/form phải xử lý đủ:
  1. **Default** (có dữ liệu)
  2. **Loading** (dùng Skeleton giữ bố cục; nút đang gửi hiện loading + chặn bấm lặp)
  3. **Empty** (nêu lý do trống + **1 nút hành động chính** để bắt đầu)
  4. **Error** (nói rõ cách sửa + nút **Thử lại**; không lộ mã lỗi nội bộ)
  5. **Disabled** (kèm gợi ý lý do khi cần)
- **Form:** Nhãn luôn hiện (không dùng placeholder thay nhãn). Lỗi hiển thị ngay dưới ô khi blur/submit và focus vào ô lỗi đầu tiên. Giữ nguyên dữ liệu user đã nhập khi lỗi.
- **Phản hồi:** Inline cho lỗi ở ô · Toast tự ẩn cho xác nhận nhanh · Modal chỉ dùng cho thao tác cần xác nhận chặn (xóa/hủy dữ liệu). Không chỉ dùng màu để báo trạng thái (phải kèm icon/chữ).

---

## 4. Truy cập (a11y tối thiểu WCAG 2.2 AA)
- Dùng **HTML ngữ nghĩa** (`button` cho hành động, `a` cho link, `label` gắn input, `h1–h6` đúng cấp). Cấm `div onclick`.
- Mọi thao tác dùng được bằng **bàn phím**; `:focus-visible` rõ nét (`--color-focus-ring`). Modal phải nhốt focus, Esc để đóng, trả focus khi đóng.
- Tương phản chữ ≥ **4.5:1**, thành phần UI ≥ **3:1** (cả sáng/tối). Ảnh nội dung có `alt` (trang trí `alt=""`). Zoom 200% không vỡ.

---

## 5. Quy trình khi vẽ bằng Stitch / Figma ở ngoài
> **Nguyên tắc: Đồng bộ Token — Lấy ảnh làm tham chiếu — KHÔNG copy thẳng code export vào `src/`.**
1. **Chốt thiết kế ngoài:** Nạp file `DESIGN.md` này vào Stitch (hoặc đồng bộ biến từ Figma vào `tokens.css`) để giữ đúng bảng màu/font.
2. **Lưu tham chiếu:** Chụp ảnh màn hình (Desktop/Mobile) vào `.sdd/design/references/`.
3. **Dịch sang Feature:** Dùng prompt `12-design-feature.md` để AI dịch ảnh/HTML export sang `.sdd/specs/feat-<slug>/DESIGN.md` (ánh xạ về component trong `components.md` + điền nốt trạng thái Loading/Empty/Error còn thiếu).
4. **Code & đối chiếu:** AI viết code sạch bằng component + token, chạy app chụp lại ảnh 375/1280 so trực tiếp với ảnh gốc.

## 6. Khi chốt UI stack (làm sau, không vội)
Tạo ADR trong `.sdd/rfcs/`, điền `UI_STACK`, `A11Y_CMD`, `VISUAL_CMD` vào `.sdd/project.conf`, ánh xạ Tailwind/theme về các biến `--*` trong `tokens.css`. Không phải sửa lại SPEC hay DESIGN.
