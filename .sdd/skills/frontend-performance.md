---
name: frontend-performance
description: Hiệu năng giao diện (Core Web Vitals, tải ảnh/font/JS, tránh nhảy layout, render). Kích hoạt khi thêm thư viện UI, ảnh/font/video, trang chậm, hoặc review hiệu năng UI.
---

# Skill: Frontend Performance

## Ngân sách khuyến nghị
LCP < 2.5s · CLS < 0.1 · INP < 200ms · trọng lượng JS ban đầu theo ADR. **Đo trước, tối ưu sau** (Lighthouse/DevTools/RUM).

## Quy tắc
- **Thêm thư viện UI/animation nặng ⇒ phải qua ADR** (kích thước, thay thế nhẹ hơn). Ưu tiên CSS/HTML thuần khi đủ dùng.
- **Ảnh:** định dạng hiện đại (WebP/AVIF), nhiều kích thước (`srcset/sizes`), `width/height` hoặc `aspect-ratio` (chống CLS), `loading="lazy"` dưới màn hình đầu, ảnh LCP **không** lazy và ưu tiên tải.
- **Font:** tối đa 2 họ/3 độ đậm; `font-display: swap`; subset; preload font chính; font hệ thống khi có thể.
- **JS:** tách theo route/trang (code-splitting); tải lười thành phần nặng (biểu đồ, editor, bản đồ); không chặn render; bỏ code chết.
- **CSS:** tránh CSS khổng lồ không dùng; CSS tới hạn cho màn hình đầu nếu cần.
- **Render:** tránh thao tác layout lặp; danh sách dài dùng ảo hóa/phân trang; không tính toán nặng trong lúc render; debounce/throttle sự kiện dày.
- **Mạng:** cache tài nguyên tĩnh (hash tên file), nén (gzip/brotli), giảm số request, dùng CDN.
- **Cảm nhận nhanh:** skeleton thay spinner, phản hồi tức thì (optimistic UI cho thao tác an toàn), không chặn UI khi tải.
- **Chống nhảy layout (CLS):** đặt kích thước trước cho ảnh/quảng cáo/embed; không chèn nội dung phía trên nội dung đang xem.

## Kiểm
`VISUAL_CMD`/Lighthouse CI khi đã chốt stack; so với ngân sách; dán số đo vào REVIEW.md.

## Checklist
- [ ] Ảnh có kích thước + lazy đúng chỗ · [ ] Font ≤ 2 họ, swap · [ ] Không thêm thư viện nặng chưa duyệt
- [ ] Code-split màn hình nặng · [ ] Có số đo so với ngân sách
