# DESIGN — Quản trị Người dùng, Dashboard & Báo cáo Thống kê (admin-users-reports)

> **Bản thiết kế gọn cho feature có UI.** Tuân thủ Admin Shell và token tại `.sdd/design/DESIGN.md` & `tokens.css`.

| Trường | Giá trị |
|---|---|
| Feature | `08-feat-admin-users-reports` |
| Status | `LOCKED` |
| Tham chiếu Stitch/Figma | `https://agenda-kale-62994833.figma.site/#/admin`, `#/admin/users`, `#/admin/reports` · `.sdd/design/references/baseline/scr-14__admin-dashboard__1280.png`, `scr-14__admin-dashboard__375.png`, `scr-22__admin-reports__1280.png` |

## 1. Màn hình & Luồng đi
| ID | Màn hình / View | Vào từ đâu | Mục đích |
|---|---|---|---|
| SCR-14 | Admin Dashboard (`views/admin/dashboard.jsp`) | Sau đăng nhập Admin / Sidebar `Tổng quan` | 4 KPI chính, 2 thẻ chờ duyệt review/Q&A, đơn gần đây & Top 5 bán chạy |
| SCR-19 | Quản lý khách hàng (`views/admin/users.jsp`) | Sidebar Admin `Khách hàng` | Tìm kiếm/lọc user, khóa/mở khóa tài khoản Customer |
| SCR-22 | Báo cáo & thống kê (`views/admin/reports.jsp`) | Sidebar Admin `Báo cáo` | Bộ lọc ngày + đủ 5 chỉ số SQL (`COUNT`, `SUM`, `AVG`, `MIN`, `MAX`) & Top 10 bestseller |

```text
SCR-14 Dashboard ──► Thẻ "Duyệt đánh giá >" → SCR-20 · "Trả lời câu hỏi >" → SCR-21
SCR-19 Users     ──► "Khóa tài khoản" → ConfirmModal → POST/PRG → Badge "Đã khóa"
SCR-22 Reports   ──► Chọn Từ ngày / Đến ngày → "Áp dụng" → Cập nhật KPI, thanh COUNT & Top 10
```

## 2. Component & Token dùng
- **Dùng lại từ `components.md`:** `AdminShell`, `KpiCard & ReportBars`, `FormControl`, `Button` (`primary-teal`, `danger-outline`), `StatusBadge`, `AlertBanner` (`info`, `warning`, `error`), `ConfirmModal`, `Pagination`, `EmptyState`.
- **Component mới:** Không có.
- **Token mới ngoài `tokens.css`?** Không.

## 3. Trạng thái, Responsive & Copy (chỉ ghi điểm riêng)
- **Trạng thái:**
  - **Empty / Zero-data:** Khi không có đơn `DELIVERED` trong kỳ, hiển thị `0 ₫` kèm hộp `AlertBanner` xanh `"Khi không có đơn đã giao — Doanh thu và giá trị đơn trung bình hiển thị 0 ₫."`; bảng Top 10 hiển thị `EmptyState` gọn.
  - **Disabled:** Trên `SCR-19`, dòng của chính Admin đang đăng nhập ẩn/khóa nút `"Khóa tài khoản"` kèm ghi chú.
  - **Error:** Khoảng ngày `fromDate > toDate` hiện `AlertBanner` đỏ ngay cạnh thanh lọc ngày.
- **Responsive (375 vs 1280) & Polish bắt buộc:**
  - Số tiền VND trên `KpiCard` dùng `whitespace-nowrap` + `clamp(1.25rem, 1.6vw, 1.75rem)` để `48.620.000 ₫` không rớt ký hiệu `₫`.
  - Hai bảng song song ở `SCR-14` (`Đơn hàng gần đây` & `Top 5 sách bán chạy`) và `SCR-22` bọc `.table-responsive` và chia tỉ lệ/xếp dọc hợp lý để không cắt chữ cột `TRẠNG THÁI` ở `1280px`.
  - Trên 375px: 4 KPI xếp 1 cột, 2 thẻ chờ duyệt xếp 1 cột, bộ lọc ngày xếp dọc.
- **Copy / i18n chính:** `"Lọc theo ngày tạo đơn Orders.created_at"`, `"SUM · chỉ đơn DELIVERED"`, `"AVG · DELIVERED"`, `"Chỉ tính sách đang bán"`, `"Khóa tài khoản khách hàng?"`.
- **a11y cần lưu ý:** Các thanh ngang `COUNT` theo trạng thái hiển thị đầy đủ nhãn chữ + con số (không chỉ dùng màu/độ dài thanh).
- **SEO/AEO/GEO:** Toàn bộ `SCR-14`, `SCR-19`, `SCR-22` đặt `noindex, nofollow`.

## 4. Tiêu chí nghiệm thu bằng mắt (VAC) & Duyệt
- [ ] **VAC-01:** `SCR-14` và `SCR-22` khớp `scr-14__admin-dashboard__1280.png`, `375.png` và `scr-22__admin-reports__1280.png`, khắc phục triệt để lỗi rớt ký hiệu `₫` và cắt cột bảng ở 1280px.
- [ ] **VAC-02:** Hiển thị rõ 5 loại aggregate (`COUNT`, `SUM`, `AVG`, `MIN`, `MAX`), chú thích phạm vi lọc và trạng thái `0 ₫` khi không có đơn.
- **Duyệt (cùng Gate 1):** Owner (BookHub) · 2026-11-23
