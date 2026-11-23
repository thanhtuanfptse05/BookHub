# PLAN — Quản trị Người dùng, Dashboard & Báo cáo Thống kê (`08-feat-admin-users-reports`)

> **Pha 2 · Thiết kế kiến trúc** sau khi SPEC & DESIGN v1.0.0 đã `LOCKED`. Duyệt (Gate 2) cùng `TASKS.md`.

| Trường | Giá trị |
|---|---|
| SPEC version | 1.0.0 |
| Status | `DRAFT` |

## 1. Tóm tắt giải pháp
Xây dựng `AdminUserService` (tìm kiếm/lọc/phân trang tài khoản không lộ `password_hash`, khóa/mở khóa Customer và khóa chặn tự khóa Admin hiện tại hoặc Admin `ACTIVE` cuối cùng) và `ReportService` + `ReportRepository` thực thi trực tiếp toàn bộ 5 hàm tổng hợp SQL (`COUNT`, `SUM`, `AVG`, `MIN`, `MAX`) tại SQL Server qua JPA JPQL/Native Query với `COALESCE` chống null và biên thời gian nửa mở `[fromDate 00:00:00, toDate+1 00:00:00)` trên `Orders.created_at`. Giao diện `SCR-14`, `SCR-19`, `SCR-22` dùng HTML/CSS thanh ngang thuần (không thư viện chart JS), bổ sung `whitespace-nowrap` cho số tiền KPI VND và `.table-responsive` cho cặp bảng 1280px.

## 2. Phương án đã cân nhắc
| Phương án | Ưu | Nhược | Chọn? |
|---|---|---|---|
| A: Aggregate trực tiếp tại SQL Server trong `ReportRepository` (`COUNT`, `SUM`, `AVG`, `MIN`, `MAX` với `COALESCE`) + thanh ngang CSS thuần | Nhanh, chuẩn PRJ301 mục 3.5, không tốn RAM Java, không lỗi chia 0, khớp 100% `03_demo_queries.sql` | Cần map cẩn thận kiểu trả về `BigDecimal`/`Long` từ aggregate query | ✅ |
| B: Load toàn bộ `Orders` và `Books` vào `List` rồi tính `stream().mapToDouble()` trong Java | Viết nhanh | Vi phạm `A-09`/hiệu năng, dễ sai số `double` và không chứng minh được SQL aggregate | ❌ |

## 3. Kiến trúc & luồng xử lý
```text
GET  /admin         → AdminDashboardServlet → ReportService.getDashboard()
                    → [4 KPIs, pendingReviews, openQuestions, recentOrders(5), topBestsellers(5)] → views/admin/dashboard.jsp (SCR-14)

GET  /admin/users   → AdminUserServlet → AdminUserService.searchUsers(keyword, status, page) → views/admin/users.jsp (SCR-19)
POST /admin/users   → [AdminFilter + CsrfFilter] → AdminUserService.toggleLock(currentAdminId, targetUserId, action)
                    → [1 TX] verify target != currentAdminId & nếu target là ADMIN thì countActiveAdmins() > 1 → update status → PRG /admin/users

GET  /admin/reports → AdminReportServlet (parse fromDate, toDate; nếu fromDate > toDate → lỗi thân thiện)
                    → ReportService.getReport(fromDateTime, toDateExclusive)
                    → [COUNT by 5 statuses, SUM revenue DELIVERED, AVG AOV DELIVERED, MIN/MAX/AVG price active Books, SUM stock_qty active Books, Top 10 bestsellers]
                    → views/admin/reports.jsp (SCR-22)
```

## 4. Thay đổi theo tầng
| Tầng | File tạo mới | File sửa | Ghi chú |
|---|---|---|---|
| `dto/` | `DashboardDTO`, `ReportSummaryDTO`, `OrderStatusCountDTO`, `BestSellerRowDTO`, `AdminUserRowDTO`, `BookPriceStatsDTO` | — | Không chứa `password_hash`; mặc định `BigDecimal.ZERO` |
| `repository/` | `ReportRepository` | `UserRepository` (thêm `searchAdminUsers`, `countActiveAdmins`) | 100% SQL/JPQL aggregate với `COALESCE` |
| `service/` | `AdminUserService`, `ReportService` | — | Parse/validate ngày biên nửa mở `[from, to+1)` |
| `controller/` | `admin/{AdminDashboardServlet,AdminUserServlet,AdminReportServlet}` | — | PRG sau POST lock/unlock |
| `web/views/` | `admin/{dashboard,users,reports,kpi-card}.jsp` | `web/assets/css/bookhub.css` (polish KPI VND `whitespace-nowrap`, thanh COUNT, `.table-responsive`) | Khớp `SCR-14`, `SCR-19`, `SCR-22`, `noindex` |
| `tests/` | `tests/unit/ReportDateRangeTest.java`, `tests/integration/{AdminUserServiceTest,ReportServiceIntegrationTest}.java` | — | Gắn `@spec admin-users-reports/AC-01..06` |

## 5. Data model & Migration
- Chỉ đọc/cập nhật trên các bảng đã có (`Users`, `Roles`, `Books`, `Orders`, `OrderItems`, `Reviews`, `Questions`); tận dụng index `IX_Orders_status_created`.

## 6. API / Contract & ảnh hưởng chéo
- Hiện thực hóa `RT-018`, `RT-022`, `RT-025` trong `.sdd/shared_context.md`. Hoàn thiện 100% 25/25 Servlet routes và 25/25 màn hình BookHub MVP!

## 7. Truy vết SPEC → thiết kế
| Yêu cầu | Thành phần đáp ứng | Test dự kiến |
|---|---|---|
| FR-01 | `AdminDashboardServlet` + `ReportService.getDashboard()` | AC-01 |
| FR-02, FR-03 | `AdminUserServlet` + `AdminUserService` | AC-02 |
| FR-04, EC-03 | Guard `targetUserId != currentAdminId` & `countActiveAdmins() > 1` | AC-03 |
| FR-05, FR-08, EC-02, EC-04 | `AdminReportServlet` + `ReportRepository` (`COUNT`/`SUM`/`AVG`/`MIN`/`MAX`) + scope labels trên `reports.jsp` | AC-04 |
| FR-06 | `ReportDateRange` validator (`fromDate <= toDate`) | AC-05 |
| FR-07, EC-01 | `COALESCE(..., 0)` + điền đủ 5 trạng thái đơn = `0` khi trống | AC-06 |

## 8. Chiến lược kiểm thử
- **Unit:** Parse ngày hợp lệ/sai định dạng/`fromDate > toDate`, tính phần trăm thanh ngang `COUNT` khi tổng = `0`.
- **Integration (SQL Server test DB):** Đủ `TC-REP-01..04` + kiểm tra đối chiếu 1-1 với kết quả chạy tay `database/03_demo_queries.sql`; test tự khóa Admin / Admin cuối cùng bị chặn; test biên cuối ngày `23:45:00`.
- **Visual:** Chụp ảnh `SCR-14` (1280 & 375), `SCR-19`, `SCR-22` (1280) đối chiếu baseline, xác nhận không rớt ký hiệu `₫` và không cắt cột `TRẠNG THÁI`.

## 9. Rủi ro & giảm thiểu
| # | Rủi ro | Xác suất | Tác động | Giảm thiểu / Rollback |
|---|---|---|---|---|
| R1 | Hibernate trả kiểu `Number`/`Double` thay vì `BigDecimal` cho `AVG(price)` | Trung bình | Trung bình | Ép kiểu `CAST(... AS DECIMAL(18,2))` trong query và chuyển đổi an toàn sang `BigDecimal` tại `ReportRepository` |
| R2 | Bỏ sót đơn tạo trong ngày `toDate` vì dùng `created_at <= '2026-11-23 00:00:00'` | Cao | Cao | Bắt buộc dùng điều kiện nửa mở `created_at >= fromDate.atStartOfDay() AND created_at < toDate.plusDays(1).atStartOfDay()` (`EC-04`) |

## 10. Rollout & Quan sát
- Log `INFO` thao tác khóa/mở khóa tài khoản (`adminUserId`, `targetUserId`, `newStatus`). Rollback qua nhánh `feat/admin-users-reports`.

## 11. ❓ Câu hỏi cho con người
- Không có câu hỏi mở.

## 12. Phê duyệt (Gate 2)
(chờ duyệt) · —
