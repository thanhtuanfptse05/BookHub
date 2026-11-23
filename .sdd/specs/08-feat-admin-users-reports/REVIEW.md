# REVIEW — admin-users-reports

> Review **độc lập** (agent/session khác, hoặc người) — prompt: `.sdd/prompts/06-review.md`. Reviewer **không sửa code**.
> Tier Full: bắt buộc `APPROVED` trước khi merge.

| Trường | Giá trị |
|---|---|
| Reviewer | Owner (BookHub) · Ngày: 2026-11-23 |
| Commit/nhánh | `feat/bookhub-mvp-01-08` |
| **Kết luận** | `APPROVED` (Owner đã duyệt Gate 4 ngày 2026-11-23) |

## 1. Bằng chứng chạy thật
```text
$ ant clean dist test-junit5
- AdminUsersReportsIntegrationTest: 3/3 OK (SQL Server 2022 BookHubDB)
  - dashboardComputesAccurateKpisModerationQueuesRecentOrdersAndBestsellers() [OK]
  - userManagementLockUnlockEnforcesLoginBlockAndProtectsSelfAndLastAdmin() [OK]
  - sqlAggregateReportMatchesSeededValuesValidatesDateRangeAndHandlesZeroDataSafely() [OK]
- Live Tomcat 10.1: /admin, /admin/users, /admin/reports, /admin/reports?fromDate=2026-11-01&toDate=2026-11-30, /admin/reports?fromDate=2026-12-01&toDate=2026-11-01 → 200 OK, 0 SEVERE
```

## 2. Truy vết
| Yêu cầu | Code | Test (`@spec`) | OK? |
|---|---|---|---|
| FR-01 | `ReportRepository`, `ReportService.loadDashboard`, `AdminDashboardServlet`, `admin/dashboard.jsp` | `admin-users-reports/AC-01` | ✅ |
| FR-02..04, EC-03 | `UserRepository`, `AdminUserService`, `AdminUserServlet`, `AdminUserRowDTO`, `admin/users.jsp` | `admin-users-reports/AC-02..03` | ✅ |
| FR-05..08, EC-01..02, EC-04 | `ReportRepository` (`COUNT/SUM/AVG/MIN/MAX` + `COALESCE` + `[from, to+1)`), `ReportService.loadReport`, `AdminReportServlet`, `admin/reports.jsp` | `admin-users-reports/AC-04..06` | ✅ |

## 3. Kiểm tra theo hiến pháp
- [x] `AdminUserRowDTO` không chứa `passwordHash`; chặn tuyệt đối tự khóa bản thân và khóa Admin `ACTIVE` cuối cùng; tài khoản bị khóa lập tức bị chặn đăng nhập
- [x] Toàn bộ thống kê `COUNT`, `SUM`, `AVG`, `MIN`, `MAX` chạy trực tiếp trong SQL Server qua JPA aggregate (không tải toàn bộ đơn/sách về RAM), zero-safe không chia cho 0, luôn trả đủ 5 trạng thái đơn
- [x] Validate `fromDate <= toDate` rõ ràng; lọc theo biên nửa mở `[fromDate 00:00, toDate + 1 ngày 00:00)` không bỏ sót đơn cuối ngày
- [x] Thanh phân bố trạng thái dùng HTML/CSS thuần theo design tokens (không cài thư viện chart JS ngoài)

## 4. Phát hiện
| # | Mức | File:dòng | Vấn đề | Cách sửa đề xuất | Trạng thái |
|---|---|---|---|---|---|
| 1 | 🟡 | `web/WEB-INF/views/admin/users.jsp` | Biểu thức EL `${pageResult.empty}` | Đã thay bằng `${empty pageResult.items}` & verify live Tomcat 200 OK | Đã sửa |

## 5. Phê duyệt cuối
**`APPROVED`** — Owner (BookHub) · 2026-11-23
