# ADR-003: Dùng Hibernate ORM 6.6 + JPA 3.1, cấm DAO JDBC thuần

| Trường | Giá trị |
|---|---|
| Status | `Accepted` |
| Ngày | 2026-11-23 |
| Người quyết định | Nhóm phát triển BookHub (PRJ301) |
| Liên quan | `constitution H-04, A-03`, `.sdd/constraints/global.md` |

## 1. Bối cảnh
Đề bài PRJ301 yêu cầu truy cập Microsoft SQL Server qua **JPA** và **không dùng JDBC thuần** (`DriverManager`/`PreparedStatement` trong business DAO), đồng thời có mục chấm điểm riêng (1.0đ) cho Framework & ORM.

## 2. Các phương án
| # | Phương án | Ưu | Nhược | Chi phí/Rủi ro |
|---|---|---|---|---|
| 1 | Jakarta Persistence 3.1 + Hibernate ORM `6.6.59.Final` (`RESOURCE_LOCAL`) | Đáp ứng đúng yêu cầu PRJ301, ánh xạ 12 Entity, JPQL parameter binding, transaction rõ ràng | Cần quản lý kỹ `EntityManager` scope và tránh `LazyInitializationException` | Trung bình — giảm thiểu bằng DTO projection & `hbm2ddl.auto=validate` |
| 2 | DAO JDBC thuần | Ít dependency JAR | Vi phạm trực tiếp yêu cầu đề bài PRJ301 | Không đạt tiêu chí chấm điểm |

## 3. Quyết định
Chọn phương án **#1 (JPA 3.1 + Hibernate ORM 6.6.59.Final)** với `persistence-unit` tên `BookHubPU` (`RESOURCE_LOCAL`, `SQLServerDialect`, `hbm2ddl.auto=validate`).

## 4. Hệ quả & đánh đổi (Trade-offs)
- ➕ Chuẩn hóa tầng Persistence, tránh lặp code mapping, an toàn parameter binding, đáp ứng rubric Framework.
- ➖ Bắt buộc đóng gói đúng bộ transitive JAR của Hibernate 6.6 trong Ant và tuân thủ nguyên tắc `EntityManagerFactory` singleton + `EntityManager` theo từng request/transaction.

## 5. Cách kiểm chứng & điều kiện xem xét lại
- Thước đo thành công: Hibernate khởi động `validate` thành công với `BookHubDB`; 0 lớp DAO dùng `DriverManager`/`PreparedStatement` trực tiếp.
- Xem lại khi: Không thay đổi trong phạm vi MVP PRJ301.
