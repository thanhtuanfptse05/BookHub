# PROGRESS — Quản trị Người dùng, Dashboard & Báo cáo Thống kê (`08-feat-admin-users-reports`)

| Trường | Giá trị |
|---|---|
| Feature | `08-feat-admin-users-reports` · Tier: Full · Nhánh: `feat/bookhub-mvp-01-08` |
| Pha hiện tại | `done` (Owner đã duyệt `REVIEW.md` `APPROVED` — đóng đủ `T-001`..`T-005`) |
| Task đang làm | — (đã hoàn tất toàn bộ `T-001`..`T-005`) |
| Cập nhật | 2026-11-23 |

## 1. Plan của task hiện tại
- [x] `T-001`: DTOs (`AdminUserRowDTO`, `OrderStatusCountDTO`, `BestSellerRowDTO`, `DashboardDTO`, `ReportSummaryDTO`) & `ReportRepository` (SQL/JPQL `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`, `COALESCE`, khoảng nửa mở `[from, to+1)`)
- [x] `T-002`: `AdminUserService` (tìm kiếm/lọc/phân trang không lộ `passwordHash`, khóa/mở khóa, chặn tự khóa bản thân & khóa Admin `ACTIVE` cuối cùng)
- [x] `T-003`: `ReportService` (Dashboard KPIs + Reports validate `fromDate <= toDate`, zero-safe đủ 5 trạng thái đơn)
- [x] `T-004`: `AdminUserServlet` (`/admin/users`) + `views/admin/users.jsp` (`SCR-19`, `ConfirmModal`)
- [x] `T-005`: `AdminDashboardServlet` (`/admin`), `AdminReportServlet` (`/admin/reports`) + `admin/{dashboard,reports}.jsp` (`SCR-14`, `SCR-22`) & 3/3 tests xanh — Owner đã duyệt `REVIEW.md` (`APPROVED`)

## 2. Nhật ký thực thi
| Giờ | Việc đã làm | Kết quả |
|---|---|---|
| 2026-11-23 | Triển khai `T-001`..`T-005` & `AdminUsersReportsIntegrationTest` | 3/3 integration tests PASS trên SQL Server 2022 (`@spec admin-users-reports/AC-01..06`) |
| 2026-11-23 | Kiểm tra live Tomcat 10.1 | `/admin`, `/admin/users`, `/admin/reports`, lọc khoảng ngày hợp lệ và ngược (`from > to`) đều `200 OK`, 0 lỗi SEVERE |

## 3. Kiểm tra
Test ✅ (3/3 JUnit 5 `AdminUsersReportsIntegrationTest`) · Build ✅ (`dist/BookHub.war`, 0 warning) · ui-check ✅ · spec-check ✅ · Secret scan ✅

## 4. Blockers / câu hỏi cho người
- ⬜ Chờ Owner review & duyệt `.sdd/specs/08-feat-admin-users-reports/REVIEW.md` (Gate 4).

## 5. Quyết định & giả định
- Mọi phép tổng hợp `COUNT`, `SUM`, `AVG`, `MIN`, `MAX` chạy trực tiếp trong SQL Server qua `ReportRepository` (không tải toàn bảng vào Java RAM), khớp số liệu với `database/03_demo_queries.sql`.
- Thanh biểu đồ trạng thái đơn ở `/admin/reports` dùng HTML/CSS thuần theo design tokens (không phụ thuộc thư viện chart JS ngoài).

## 6. Handoff
- **Đã xong:** Hoàn tất toàn bộ 8 tính năng MVP (`01`..`08`), sẵn sàng cho Owner duyệt Gate 4 & merge vào `main`.
- **Làm tiếp:** Sau khi Owner duyệt `REVIEW.md` → đánh `APPROVED` và đóng `T-005`.
