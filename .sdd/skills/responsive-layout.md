---
name: responsive-layout
description: Bố cục responsive mobile-first trung lập stack (flex/grid, breakpoint, container, ảnh, bảng). Kích hoạt khi dựng màn hình, sửa lỗi tràn/cuộn ngang trên mobile, hoặc review responsive.
---

# Skill: Responsive Layout

## Nguyên tắc
- **Mobile-first:** viết kiểu cho `base` (375px) trước, thêm `min-width` breakpoint (`sm 480 · md 768 · lg 1024 · xl 1280`) để mở rộng.
- Layout co giãn **theo nội dung/container**, không theo thiết bị cụ thể. Không `width` cố định bằng px cho khối chính.
- Kiểm bắt buộc ở **375 · 768 · 1280** và 320px (không cuộn ngang).

## Công cụ bố cục
- 1 chiều → **flex** (`gap` thay margin thủ công); 2 chiều/lưới thẻ → **grid** (`repeat(auto-fit, minmax(16rem, 1fr))`).
- Khoảng cách chỉ dùng `--space-*`. Container: `max-width: var(--container-max)`, căn giữa, padding ngang theo breakpoint.
- Đơn vị tương đối: `rem` cho chữ/khoảng cách, `%`/`fr` cho chiều rộng, `dvh/svh` cho chiều cao màn hình (tránh `100vh` trên mobile).
- `min-width: 0` trên phần tử flex/grid chứa nội dung dài để tránh tràn.

## Mẫu thích ứng
| Thành phần | Mobile | Rộng hơn |
|---|---|---|
| Điều hướng | Menu thu gọn/tab dưới | Thanh ngang/sidebar |
| Bảng | Thẻ xếp dọc hoặc cuộn ngang có gợi ý | Bảng đầy đủ |
| Form | 1 cột, nút chính full-width/dính đáy | 2 cột khi hợp lý, nút căn theo nhãn |
| Modal | Bottom sheet/toàn màn hình | Hộp giữa màn hình, rộng tối đa 480–640px |
| Ảnh | Co theo container, tỉ lệ cố định | Giữ tỉ lệ, giới hạn chiều rộng |

## Lỗi hay gặp
- Cuộn ngang do phần tử rộng cố định/ảnh/`pre` dài → `max-width: 100%`, `overflow-wrap: anywhere`, bọc `overflow-x: auto` có chủ đích.
- Vùng chạm < 44px trên mobile; input < 16px làm trình duyệt tự zoom.
- Chữ/nút cố định chiều cao → vỡ khi chuỗi dài hơn (i18n) hoặc zoom 200%.
- Layout nhảy khi ảnh/font tải xong → đặt `aspect-ratio`/kích thước trước.
- Phụ thuộc `:hover` cho chức năng quan trọng (mobile không có hover).

## Kiểm
Chụp 375/768/1280 cho từng trạng thái (đặc biệt error/empty — nội dung dài làm vỡ layout), zoom 200%, xoay ngang. Ghi ảnh vào REVIEW.md.

## Checklist
- [ ] Không cuộn ngang ở 320px · [ ] Vùng chạm ≥ 44px · [ ] Chỉ dùng token spacing
- [ ] Chuỗi dài/zoom 200% không vỡ · [ ] Không dựa vào hover · [ ] Ảnh có tỉ lệ cố định
