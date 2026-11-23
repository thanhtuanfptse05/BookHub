# CONTEXT — Quản trị Người dùng, Dashboard & Báo cáo Thống kê (admin-users-reports)

> **Pha 0 · Bối cảnh tính năng.** Nguồn chuẩn: `BookHub_PRJ301_Project_Plan_Requirements.md` (§8.7, §13.6, §19, PRJ301 mục 3.5 — 1.0 điểm Statistics & Reports).
> Trạng thái: `ready`

## 1. Bối cảnh & Pain point
- Quản trị viên cần một trung tâm điều hành (`/admin`) nắm nhanh sức khỏe cửa hàng (doanh thu đã giao, đơn chờ xử lý, hàng chờ duyệt review/Q&A), quản trị tài khoản khách hàng (`/admin/users` — khóa/mở khóa tài khoản vi phạm), và màn hình báo cáo tổng hợp (`/admin/reports`) đáp ứng mục **3.5 (1.0 điểm)** của đề PRJ301.
- Các chỉ số thống kê phải minh bạch định nghĩa nghiệp vụ (doanh thu & bestseller chỉ tính đơn `DELIVERED`, giá sách MIN/MAX/AVG chỉ tính sách `is_active = 1`, xử lý an toàn trường hợp 0 đơn không lỗi chia 0).

## 2. Mục tiêu nghiệp vụ & thước đo thành công
| Mục tiêu | Chỉ số (đo được) | Hiện tại → Mục tiêu |
|---|---|---|
| Đáp ứng đủ 5 hàm tổng hợp PRJ301 | `COUNT`, `SUM`, `AVG`, `MIN`, `MAX` chạy qua JPA/SQL thật và khớp `03_demo_queries.sql` | Pass `TC-REP-01`..`TC-REP-04` |
| Quản lý tài khoản an toàn | Tìm kiếm, lọc, phân trang, khóa/mở khóa Customer; 0 nguy cơ tự khóa Admin cuối cùng | Tuân thủ `FR-ADM-008`, `BR-ACC-005` |
| Báo cáo minh bạch & không chia 0 | Bộ lọc ngày `Orders.created_at`, trả `0 ₫` khi không có đơn `DELIVERED` | Khớp §19.2 và Figma `SCR-14`, `SCR-19`, `SCR-22` |

## 3. Người dùng / Tác nhân
| Actor | Muốn gì | Ghi chú |
|---|---|---|
| Admin (`ADMIN`) | Xem KPI tổng quan mỗi khi đăng nhập, xử lý tài khoản vi phạm và theo dõi doanh thu/tồn kho/bán chạy theo khoảng ngày | Chỉ Admin truy cập `/admin`, `/admin/users`, `/admin/reports` |

## 4. Thuật ngữ nghiệp vụ riêng của feature
| Thuật ngữ | Định nghĩa |
|---|---|
| `Delivered Revenue` | `SUM(Orders.total_amount)` chỉ tính đơn `status = 'DELIVERED'` lọc theo `Orders.created_at` trong khoảng ngày chọn |
| `Average Order Value (AOV)` | `AVG(Orders.total_amount)` của đơn `DELIVERED` trong kỳ; bằng `0` khi số đơn `DELIVERED = 0` |
| `Active Book Price Stats` | `MIN(price)`, `MAX(price)`, `AVG(price)` và `SUM(stock_qty)` tính trên các sách `is_active = 1` |

## 5. Ràng buộc cố định (không thương lượng)
- Toàn bộ truy vấn tổng hợp đi qua `ReportRepository` (JPA JPQL / Named Native Query), tuyệt đối không viết SQL trong JSP hay tải toàn bộ bảng lên Java để tính vòng lặp.
- Admin không bao giờ được khóa tài khoản của chính mình hoặc khóa tài khoản `ADMIN` hoạt động cuối cùng trong hệ thống.
- Không dùng thư viện biểu đồ JS bên ngoài trong MVP — biểu đồ phân bố đơn theo trạng thái dùng thanh ngang HTML/CSS thuần khớp Figma `SCR-22`.

## 6. Tham chiếu
- Figma: `SCR-14`, `SCR-19`, `SCR-22` · Baseline: `scr-14__admin-dashboard__1280.png`, `scr-14__admin-dashboard__375.png`, `scr-22__admin-reports__1280.png` · UI Spec: `docs/ui-screens.md` · Reporting Spec: `BookHub_PRJ301_Project_Plan_Requirements.md` (§19).

## 7. Giả định & câu hỏi mở
- Khoảng ngày báo cáo lọc theo `Orders.created_at` (theo §19.2, vì schema MVP chưa tách cột `delivered_at`) và ghi chú rõ ngay trên UI `SCR-22`. Không có câu hỏi mở chặn.
