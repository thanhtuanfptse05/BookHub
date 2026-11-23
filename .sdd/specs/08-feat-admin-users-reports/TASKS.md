# TASKS — Quản trị Người dùng, Dashboard & Báo cáo Thống kê (`08-feat-admin-users-reports`)

> **Pha 3 · Phân rã từ `PLAN.md`.** Task atomic ≤ 4h · gắn thẻ `@spec admin-users-reports/AC-xx`.

**Trạng thái:** `⬜ todo` · `🔄 doing` · `✅ done` · `⛔ blocked`

| ID | Task | Phụ thuộc | Ước lượng | Truy vết | Trạng thái |
|---|---|---|---|---|---|
| T-001 | Tạo DTOs (`DashboardDTO`, `ReportSummaryDTO`, `OrderStatusCountDTO`, `BestSellerRowDTO`, `AdminUserRowDTO`) & `ReportRepository` (`COUNT/SUM/AVG/MIN/MAX`) | — | 3.5h | FR-01, FR-05, FR-07, EC-01..02, EC-04 | ⬜ |
| T-002 | Tạo `AdminUserService` (search/filter/paging, lock/unlock, guard Admin cuối) + integration tests (`AC-02..03`) | — | 2.5h | FR-02..04, EC-03, AC-02..03 | ⬜ |
| T-003 | Tạo `ReportService` (Dashboard + Reports date validation + zero-safe) + unit/integration tests (`AC-01`, `AC-04..06`) | T-001 | 3h | FR-01, FR-05..08, AC-01, AC-04..06 | ⬜ |
| T-004 | Tạo `AdminUserServlet` + `views/admin/users.jsp` (`SCR-19` + `ConfirmModal`) | T-002 | 2.5h | FR-02..04, AC-02..03 | ⬜ |
| T-005 | Tạo `AdminDashboardServlet`, `AdminReportServlet` + `views/admin/{dashboard,reports,kpi-card}.jsp` (`SCR-14`, `SCR-22`) + kiểm trực quan 375/1280 & `verify` | T-003 | 3.5h | FR-01, FR-05..08, AC-01, AC-04..06, VAC-01..02 | ⬜ |

## Chi tiết task

### T-001 — `ReportRepository` (SQL/JPA Aggregates) & DTOs
- **Mục tiêu:** Triển khai toàn bộ 6 nhóm truy vấn tổng hợp (`COUNT` theo 5 trạng thái, `SUM` doanh thu `DELIVERED`, `AVG` đơn `DELIVERED`, `MIN`/`MAX`/`AVG` giá sách active, `SUM(stock_qty)` sách active, Top 5 & Top 10 bestseller `DELIVERED`) với `COALESCE` và biên nửa mở `created_at < toDate + 1`.
- **File dự kiến:** `repository/ReportRepository.java`, `dto/{Dashboard,ReportSummary,OrderStatusCount,BestSellerRow,AdminUserRow,BookPriceStats}DTO.java`.
- **Acceptance:**
  - [ ] Số liệu khớp 100% với `database/03_demo_queries.sql` trên cùng tập dữ liệu.

### T-002 — `AdminUserService` & Tests
- **Mục tiêu:** Tìm kiếm/lọc/phân trang user (không trả `password_hash`), khóa/mở khóa Customer, và chặn tuyệt đối tự khóa chính mình hoặc Admin `ACTIVE` cuối cùng.
- **File dự kiến:** `service/AdminUserService.java`, `repository/UserRepository.java`, `tests/integration/AdminUserServiceTest.java`.
- **Acceptance:**
  - [ ] Pass `@spec admin-users-reports/AC-02`, `AC-03`.

### T-003 — `ReportService` & Tests
- **Mục tiêu:** Đóng gói logic Dashboard, validate khoảng ngày `fromDate <= toDate`, đảm bảo đủ 5 trạng thái đơn kể cả khi bằng `0` và không bao giờ chia cho 0.
- **File dự kiến:** `service/ReportService.java`, `tests/unit/ReportDateRangeTest.java`, `tests/integration/ReportServiceIntegrationTest.java`.
- **Acceptance:**
  - [ ] Pass `@spec admin-users-reports/AC-01`, `AC-04`, `AC-05`, `AC-06` (`TC-REP-01..04`).

### T-004 & T-005 — Admin Servlets & JSP Views (`SCR-14`, `SCR-19`, `SCR-22`)
- **Mục tiêu:** Dựng 3 màn hình quản trị cuối cùng trong Admin Shell (thanh ngang HTML/CSS thuần cho `COUNT`, nhãn phạm vi SQL rõ ràng, fix polish không rớt ký hiệu `₫` và `.table-responsive` cho bảng 1280px), đối chiếu ảnh baseline và chạy `scripts/verify.ps1`.
- **File dự kiến:** `controller/admin/{AdminDashboard,AdminUser,AdminReport}Servlet.java`, `web/WEB-INF/views/admin/{dashboard,users,reports,kpi-card}.jsp`.
- **Acceptance:**
  - [ ] Đạt `VAC-01..02`; không dùng chart JS ngoài; `scripts/verify.ps1` xanh.

## Đồ thị phụ thuộc
```text
T-001 ──► T-003 ──► T-005
T-002 ──► T-004
```

## Phê duyệt (Gate 3)
(chờ duyệt) · —
