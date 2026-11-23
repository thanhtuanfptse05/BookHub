# SPEC — Quản trị Người dùng, Dashboard & Báo cáo Thống kê (admin-users-reports)

> **Pha 1 · Hợp đồng đặc tả.** Agent soạn nháp từ `CONTEXT.md`, **người duyệt & khóa** (Gate 1).
> Sau khi `Status: LOCKED`, sửa đổi chỉ qua `CHANGELOG.md` + người duyệt. **Chưa LOCKED ⇒ cấm code.**

| Trường | Giá trị |
|---|---|
| Feature ID | `08-feat-admin-users-reports` |
| Tier | Full |
| UI | Có |
| Status | `LOCKED` |
| Version | 1.0.0 |
| Owner | BookHub Team (PRJ301) |
| Liên quan | `FR-ADM-001`, `FR-ADM-008`, `FR-ADM-011..015`, `BR-ACC-005`, `BR-ADM-007`, `shared_context.md` (`RT-018`, `RT-022`, `RT-025`), `TC-REP-01..04` |

---

## 1. Tổng quan (Overview)
Xây dựng 3 màn hình quản trị & tổng hợp còn lại của BookHub: (1) Trang Quản lý tài khoản (`/admin/users`, `SCR-19`) cho phép tìm kiếm, lọc trạng thái và khóa/mở khóa Customer an toàn; (2) Admin Dashboard (`/admin`, `SCR-14`) hiển thị 4 KPI chính, 2 hàng chờ kiểm duyệt (`PENDING` reviews, `OPEN` questions), đơn hàng gần đây và Top 5 sách bán chạy; và (3) Báo cáo thống kê (`/admin/reports`, `SCR-22`) áp dụng đầy đủ 5 hàm tổng hợp SQL `COUNT`, `SUM`, `AVG`, `MIN`, `MAX` theo khoảng ngày. Xem `CONTEXT.md`.

## 2. Actors & User Stories
| ID | Câu chuyện |
|---|---|
| US-01 | Là **Admin**, tôi muốn xem nhanh các chỉ số chính và hàng chờ cần xử lý ngay khi mở `/admin`. |
| US-02 | Là **Admin**, tôi muốn tìm kiếm danh sách người dùng và khóa/mở khóa tài khoản Customer vi phạm mà không vô tình khóa mất quyền Admin cuối cùng. |
| US-03 | Là **Admin**, tôi muốn xem báo cáo doanh thu đã giao, số đơn theo trạng thái, thống kê giá sách, tổng tồn kho và sách bán chạy theo khoảng ngày để đánh giá hiệu quả kinh doanh. |

## 3. Functional Requirements (EARS)
| ID | Loại | Mệnh đề | Liên kết (US / BR) |
|---|---|---|---|
| FR-01 | Event | WHEN an `ADMIN` requests `GET /admin`, the system SHALL compute via `ReportRepository` and render `SCR-14` with: 4 KPIs (delivered revenue, total orders + pending order count, active books count, total customers count), 2 moderation alerts (pending reviews count, open questions count), 5 most recent orders, and top 5 best-selling books from `DELIVERED` orders. | US-01, FR-ADM-001, §19.1 |
| FR-02 | Event | WHEN an `ADMIN` requests `GET /admin/users` with optional `keyword` (name/email/phone), `status` (`ACTIVE`/`LOCKED`/`DISABLED`/all), and `page`, the system SHALL return a DB-paginated list of users without exposing `password_hash`, rendering `SCR-19`. | US-02, FR-ADM-008, US-A-04 |
| FR-03 | Event | WHEN an `ADMIN` submits `POST /admin/users` to lock (`LOCKED`) or unlock (`ACTIVE`) a `CUSTOMER` account, the system SHALL update `Users.status` and `updated_at` in a JPA transaction and PRG-redirect with a confirmation flash message; a locked user shall immediately be unable to log in. | US-02, FR-AUTH-012, FR-ADM-008 |
| FR-04 | Unwanted | IF an `ADMIN` attempts to lock/disable their own account OR lock/disable the last remaining `ACTIVE` account with role `ADMIN`, THEN the system SHALL reject the action with an explicit error and leave the account `ACTIVE`. | US-02, FR-ADM-008, BR-ACC-006, US-A-04 |
| FR-05 | Event | WHEN an `ADMIN` requests `GET /admin/reports` with optional `fromDate` and `toDate` (defaulting to current month or all-time clear range, inclusive of `toDate` end-of-day on `Orders.created_at`), the system SHALL compute via DB aggregate queries and render `SCR-22` with: `COUNT` of orders grouped by all 5 statuses, `SUM(total_amount)` delivered revenue, `AVG(total_amount)` delivered order value, `MIN`/`MAX`/`AVG` price of active books, `SUM(stock_qty)` of active books, and Top 10 best-selling books by `SUM(OrderItems.quantity)` from `DELIVERED` orders. | US-03, FR-ADM-011..015, BR-ADM-007, §19.1..19.3 |
| FR-06 | Unwanted | IF `fromDate > toDate` or an invalid date string is submitted to `GET /admin/reports`, THEN the system SHALL display a friendly validation error without HTTP 500 and fall back safely. | US-03, FR-VAL-001 |
| FR-07 | State | WHILE there are zero `DELIVERED` orders in the selected date range (or zero active books in DB), the system SHALL display `0 ₫` / `0` / `—` cleanly without throwing division-by-zero or null-pointer exceptions. | US-03, §19.2, TC-REP-02 |
| FR-08 | Ubiquitous | The system SHALL display explicit scope labels on `SCR-22` clarifying that revenue/AOV/bestseller include only `DELIVERED` orders filtered by `Orders.created_at`, and price/stock stats include only active books (`is_active = 1`). | US-03, FR-ADM-013, BR-ADM-007, §19.2 |

## 4. Non-Functional Requirements
| ID | Hạng mục | Yêu cầu (số cụ thể) |
|---|---|---|
| NFR-01 | Tính chính xác & Hiệu năng | Toàn bộ `COUNT`/`SUM`/`AVG`/`MIN`/`MAX` thực thi trực tiếp tại SQL Server qua JPA `ReportRepository` (`COALESCE` chống null); không load toàn bộ `Orders`/`Books` lên Java RAM để cộng tay. |
| NFR-02 | Bảo mật | Bảo vệ bởi `AdminAuthorizationFilter` + `CsrfFilter`; DTO User tuyệt đối không chứa `password_hash`. |
| NFR-03 | UI Polish | Số tiền VND trên KPI dùng `whitespace-nowrap`; 2 bảng tổng hợp ở `SCR-14`/`SCR-22` bọc `.table-responsive` để không cắt chữ ở viewport `1280px` và `375px`. |

## 5. Data & API Contract
**Bảng truy vấn:** `Users`, `Roles`, `Books`, `Orders`, `OrderItems`, `Reviews`, `Questions`.

**Servlet Routes (`shared_context.md` `RT-018`, `RT-022`, `RT-025`):**
| Method + Path | Auth | Request Params | Response / Redirect | Lỗi |
|---|---|---|---|---|
| `GET /admin` | Admin | — | Render `views/admin/dashboard.jsp` | `403` |
| `GET /admin/users` | Admin | `keyword?`, `status?`, `page?` | Render `views/admin/users.jsp` | `403` |
| `POST /admin/users` | Admin + CSRF | `userId`, `action=lock\|unlock` | PRG `302 /admin/users` | Flash lỗi tự khóa / `404` |
| `GET /admin/reports` | Admin | `fromDate?` (`yyyy-MM-dd`), `toDate?` (`yyyy-MM-dd`) | Render `views/admin/reports.jsp` | Lỗi ngày hiện ở bộ lọc |

**DTOs:** `DashboardDTO`, `ReportSummaryDTO`, `OrderStatusCountDTO`, `BestSellerRowDTO`, `AdminUserRowDTO`.

## 6. Edge Cases & Xử lý lỗi
| ID | Tình huống | Hành vi mong đợi |
|---|---|---|
| EC-01 | Khoảng lọc ngày không có đơn nào, hoặc chỉ có đơn `PENDING`/`CANCELLED` | `deliveredRevenue = 0`, `avgOrderValue = 0`, 5 trạng thái vẫn hiển thị đủ với giá trị `0`, không lỗi chia 0 (`TC-REP-01`, `TC-REP-02`). |
| EC-02 | Có 1 sách giá `10.000 ₫` đã bị `is_active = 0` và các sách active có giá `60.000 – 200.000 ₫` | `MIN(price)` trả về `60.000 ₫` (không bị sách inactive kéo lệch) (`TC-REP-03`). |
| EC-03 | Admin gửi POST `/admin/users` trực tiếp với `userId = currentAdmin.id` | Service chặn và trả thông báo lỗi, trạng thái giữ nguyên `ACTIVE`. |
| EC-04 | Bộ lọc `toDate = 2026-11-23` có đơn tạo lúc `2026-11-23 23:45:00` | Truy vấn dùng biên trên nửa mở `created_at < toDate + 1 day` (`2026-11-24 00:00:00`) để không bỏ sót đơn cuối ngày. |

## 7. Acceptance Criteria (Given / When / Then)
> Mỗi FR xuất hiện ở cột "Kiểm chứng FR" của ≥ 1 AC. Test gắn thẻ `@spec admin-users-reports/AC-xx`.

| ID | Kịch bản | Kiểm chứng FR |
|---|---|---|
| AC-01 | **Given** DB có sách active, khách hàng, đơn đa trạng thái, review `PENDING` và câu hỏi `OPEN` **When** Admin mở `GET /admin` **Then** `SCR-14` hiển thị đúng 4 KPI, đúng số lượng hàng chờ review/Q&A, 5 đơn mới nhất và Top 5 bán chạy (`DELIVERED`). | FR-01 |
| AC-02 | **Given** DB có nhiều user Customer & Admin **When** Admin mở `GET /admin/users` với bộ lọc tìm kiếm/trạng thái và khóa/mở khóa một Customer **Then** danh sách phân trang đúng (không lộ `password_hash`), Customer bị khóa chuyển `LOCKED` và không đăng nhập được nữa; mở khóa thì đăng nhập lại bình thường. | FR-02, FR-03 |
| AC-03 | **Given** hệ thống chỉ còn 1 Admin `ACTIVE` (hoặc Admin cố gửi `userId` của chính mình) **When** `POST /admin/users` với `action=lock` **Then** server từ chối và giữ nguyên `ACTIVE`. | FR-04, EC-03 |
| AC-04 | **Given** DB có đơn `PENDING`, `CONFIRMED`, `SHIPPING`, `DELIVERED`, `CANCELLED` và sách active/inactive **When** Admin mở `GET /admin/reports` trong khoảng ngày **Then** doanh thu (`SUM`), AOV (`AVG`) và Top 10 bán chạy (`SUM(quantity)`) chỉ cộng đơn `DELIVERED` đúng biên ngày; `COUNT` tách đủ 5 trạng thái; `MIN`/`MAX`/`AVG` giá & `SUM(stock_qty)` khớp sách `is_active = 1` (`TC-REP-01`, `TC-REP-03`, `TC-REP-04`). | FR-05, FR-08, EC-02, EC-04 |
| AC-05 | **Given** Admin nhập `fromDate > toDate` hoặc chuỗi ngày sai định dạng **When** mở `GET /admin/reports` **Then** không lỗi `500` và hiển thị thông báo lỗi hợp lệ. | FR-06 |
| AC-06 | **Given** khoảng ngày không có đơn `DELIVERED` nào **When** mở `GET /admin/reports` **Then** doanh thu & AOV hiển thị `0 ₫`, bảng bestseller hiện trạng thái trống gọn và không phát sinh lỗi chia 0 (`TC-REP-02`). | FR-07, EC-01 |

## 8. Out of Scope (🚫 TUYỆT ĐỐI KHÔNG LÀM)
- Không xuất file Excel/PDF/CSV báo cáo trong MVP.
- Không nhúng thư viện chart JS nặng (Chart.js/ApexCharts) — dùng thanh ngang HTML/CSS thuần như Figma `SCR-22`.
- Không cho Admin đổi vai trò (`ADMIN` ↔ `CUSTOMER`) hay xóa cứng user khỏi DB trong MVP.

---
## Phụ lục A: Ghi chú thiết kế
Tier Full — thiết kế chi tiết lập tại `PLAN.md` sau Gate 1.

## Phụ lục B: Câu hỏi mở & Phê duyệt
- Không có câu hỏi mở — toàn bộ công thức `COUNT`/`SUM`/`AVG`/`MIN`/`MAX` và bố cục `SCR-14`, `SCR-19`, `SCR-22` đã chốt rõ.

### Definition of Ready — chỉ khóa (LOCKED) khi đủ cả
- [x] Người duyệt xác nhận `SPEC.md` và `DESIGN.md` (Gate 1)
- [x] Mỗi FR có ≥ 1 AC; mỗi AC đo được (Given/When/Then)
- [x] Out of Scope cụ thể; `scripts/spec-check` không báo lỗi

- **Duyệt (Gate 1):** Owner (BookHub) · 2026-11-23 · Version khóa: 1.0.0
