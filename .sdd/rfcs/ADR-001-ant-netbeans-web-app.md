# ADR-001: Dùng NetBeans 17 Ant Web Application thay vì Maven

| Trường | Giá trị |
|---|---|
| Status | `Accepted` |
| Ngày | 2026-11-23 |
| Người quyết định | Nhóm phát triển BookHub (PRJ301) |
| Liên quan | `CLAUDE.md §2`, `.sdd/constraints/global.md`, `constitution A-08` |

## 1. Bối cảnh
Dự án BookHub được triển khai cho môn PRJ301 trên môi trường chuẩn hóa **Apache NetBeans 17 + JDK 17 + Apache Tomcat 10.1.x**, sử dụng mẫu Java Web Application mặc định của NetBeans (Apache Ant với `build.xml` và `nbproject/`).

## 2. Các phương án
| # | Phương án | Ưu | Nhược | Chi phí/Rủi ro |
|---|---|---|---|---|
| 1 | NetBeans 17 Ant Web Application | Khớp trực tiếp dự án hiện có (`build.xml`, `nbproject/`), deploy nhanh lên Tomcat 10.1 | Phải quản lý JAR và transitive dependencies thủ công trong `web/WEB-INF/lib/` | Nguy cơ thiếu JAR hoặc lệch version khi deploy WAR |
| 2 | Chuyển sang Maven (`pom.xml`) | Tự quản lý transitive dependencies | Không khớp baseline Ant đã chốt của môn học & project hiện có | Tốn công cấu hình lại NetBeans project |

## 3. Quyết định
Chọn phương án **#1 (Ant Web Application)** vì khớp chính xác cấu trúc project hiện tại và yêu cầu môi trường NetBeans 17 của đồ án.

## 4. Hệ quả & đánh đổi (Trade-offs)
- ➕ Bám sát môi trường học phần PRJ301, chạy trực tiếp trong NetBeans 17.
- ➖ Phải tự kiểm soát đầy đủ bộ JAR runtime (Hibernate 6.6, JPA 3.1, `mssql-jdbc`, JSTL 3.0, BCrypt, SLF4J) và tránh đóng gói trùng API do Tomcat cung cấp.
- Việc phải làm theo: Ghi rõ ma trận phiên bản thư viện trong `.sdd/constraints/global.md` và `docs/runbook.md`.

## 5. Cách kiểm chứng & điều kiện xem xét lại
- Thước đo thành công: `ant dist` / Clean & Build tạo WAR sạch và deploy chạy ổn định trên Tomcat 10.1.x mà không lỗi `ClassNotFoundException`.
- Xem lại khi: Dự án mở rộng ngoài phạm vi đồ án PRJ301 hoặc chuyển sang CI build đa module cần quản lý dependency tự động.
