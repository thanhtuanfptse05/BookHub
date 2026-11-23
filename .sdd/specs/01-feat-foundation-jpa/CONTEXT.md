# CONTEXT — Nền tảng Database SQL Server & JPA/Hibernate (foundation-jpa)

> **Pha 0 · Bối cảnh tính năng.** Nguồn chuẩn: `BookHub_PRJ301_Project_Plan_Requirements.md` (§14, §16, §17).
> Trạng thái: `ready`

## 1. Bối cảnh & Pain point
- BookHub cần nguồn dữ liệu quan hệ duy nhất là Microsoft SQL Server (`BookHubDB`) và toàn bộ truy cập DB phải đi qua **Jakarta Persistence 3.1 + Hibernate ORM 6.6** (đề PRJ301 cấm viết DAO bằng JDBC thuần và chấm riêng 1.0đ mục 3.7 Framework).
- Môi trường dự án là NetBeans 17 Apache Ant Web Application trên Tomcat 10.1.x (Jakarta EE 10): nếu trộn JAR `javax.*`, thiếu transitive dependency hoặc hardcode password DB vào `persistence.xml` sẽ gây lỗi deploy hoặc lộ bí mật lên Git công khai.

## 2. Mục tiêu nghiệp vụ & thước đo thành công
| Mục tiêu | Chỉ số (đo được) | Hiện tại → Mục tiêu |
|---|---|---|
| Dựng lại DB sạch từ script | Chạy `01_schema.sql` + `02_seed.sql` không lỗi | 0 bảng → 12 bảng + đầy đủ constraint/index + dữ liệu demo nhất quán |
| Kết nối JPA/Hibernate ổn định | `hibernate.hbm2ddl.auto=validate` pass 100% | Chưa có JPA → 12 Entity khớp schema, đọc/ghi giao dịch thành công |
| Tách biệt bí mật cấu hình | 0 mật khẩu DB trong Git | Đọc `bookhub-local.properties` ngoài Git lúc khởi tạo `EntityManagerFactory` |

## 3. Người dùng / Tác nhân
| Actor | Muốn gì | Ghi chú |
|---|---|---|
| Developer / Tester | Dựng nhanh `BookHubDB` sạch, kiểm chứng Entity/Repository độc lập trước khi code nghiệp vụ | Chạy trên JDK 17 + Tomcat 10.1 + SQL Server 1433 |
| Hệ thống BookHub | Quản lý `EntityManagerFactory` singleton & `EntityManager` an toàn theo phạm vi thao tác | Không leak connection, không share `EntityManager` giữa thread |

## 4. Thuật ngữ nghiệp vụ riêng của feature
| Thuật ngữ | Định nghĩa |
|---|---|
| `BookHubPU` | Tên Persistence Unit trong `src/conf/META-INF/persistence.xml` (`RESOURCE_LOCAL`) |
| `bookhub-local.properties` | File cấu hình kết nối DB cục bộ nằm ngoài Git (ghi đè URL/user/password lúc bootstrap `JpaUtil`) |
| `hbm2ddl.auto=validate` | Chế độ Hibernate chỉ kiểm tra ánh xạ Entity khớp bảng thật, không tự ý sửa/xóa bảng |

## 5. Ràng buộc cố định (không thương lượng)
- Bắt buộc dùng namespace `jakarta.persistence.*`, Hibernate `6.6.59.Final`, `mssql-jdbc 13.6.2.jre11` trên JDK 17 + Tomcat 10.1.x (ADR-001, ADR-002, ADR-003).
- Cấm tuyệt đối `javax.persistence.*`, cấm viết DAO JDBC thuần (`DriverManager`/`PreparedStatement`).
- Tiền tệ dùng `DECIMAL(18,2)` / `BigDecimal`, không dùng `FLOAT`/`REAL`; không hardcode password DB trong file commit lên Git.

## 6. Tham chiếu
- Đặc tả gốc: `BookHub_PRJ301_Project_Plan_Requirements.md` (§14, §16, §17) · Kiến trúc: `docs/architecture.md` · Runbook: `docs/runbook.md` · ADRs: `ADR-001`, `ADR-002`, `ADR-003`, `ADR-007`, `ADR-008`.

## 7. Giả định & câu hỏi mở
- Giả định: SQL Server đã bật TCP/IP port `1433` và Mixed Mode Authentication trên máy local.
- Câu hỏi mở đã đưa vào Phụ lục B của `SPEC.md`.
