# Runbook — BookHub (Cài đặt, Vận hành & Xử lý sự cố)

> Nguồn kỹ thuật: `BookHub_PRJ301_Project_Plan_Requirements.md` (§14, §16, §17, §22.1).

## 1. Yêu cầu môi trường local
- **JDK:** Java 17 LTS (`JAVA_HOME` và NetBeans Java Platform cùng trỏ JDK 17).
- **IDE / Build:** Apache NetBeans 17 (Java Web Application — Apache Ant).
- **Web Server:** Apache Tomcat 10.1.x (Jakarta EE 10 — không dùng Tomcat 9 hay Tomcat 11).
- **Database:** Microsoft SQL Server 2019/2022 Express/Developer + SSMS.

## 2. Thiết lập SQL Server (`BookHubDB`)
1. Mở **SQL Server Configuration Manager** → **SQL Server Network Configuration** → bật **TCP/IP**.
2. Vào **TCP/IP Properties → IP Addresses → IPAll**: xóa `TCP Dynamic Ports`, đặt `TCP Port = 1433`.
3. Khởi động lại dịch vụ **SQL Server**.
4. Bật chế độ **SQL Server and Windows Authentication Mode (Mixed Mode)** và đảm bảo SQL login cục bộ hoạt động trong SSMS.
5. Tạo database `BookHubDB`, sau đó chạy lần lượt:
   - `database/01_schema.sql` (tạo 12 bảng, PK/FK, UNIQUE, CHECK, Indexes).
   - `database/02_seed.sql` (seed `Roles`, `Categories`, 20–30 `Books`, tài khoản demo có password hash BCrypt, đơn/review/Q&A mẫu).
   - `database/03_demo_queries.sql` (tùy chọn — đối chiếu số liệu báo cáo).

## 3. Cấu hình kết nối cục bộ (KHÔNG commit lên Git)
Tạo file `bookhub-local.properties` ở đường dẫn cục bộ ngoài Git (được `JpaUtil` nạp lúc khởi động `EntityManagerFactory`):
```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=BookHubDB;encrypt=true;trustServerCertificate=true
db.username=sa
db.password=<your-local-sql-password>
```
> **Cảnh báo bảo mật:** Không commit mật khẩu SQL Server vào `persistence.xml` hay Git; không mở port `1433` ra Internet; `trustServerCertificate=true` chỉ dùng ở máy local/demo.

## 4. Build, Deploy & Kiểm tra trước khi merge
1. Kiểm tra `web/WEB-INF/lib/` có đủ bộ JAR tương thích Java 17 + Jakarta EE 10 (Hibernate `6.6.59.Final`, `jakarta.persistence-api 3.1.x`, `mssql-jdbc 13.6.2.jre11`, JSTL `3.0.2`/`3.0.1`, BCrypt, SLF4J) và **không chứa** JAR `javax.*` hay API Tomcat đã cung cấp.
2. Trong NetBeans 17: **Clean and Build** (hoặc `ant dist`) → **Run** trên Tomcat 10.1.x → mở `http://localhost:8080/BookHub/`.
3. Kiểm tra log Tomcat: Hibernate khởi tạo `BookHubPU` với `hibernate.hbm2ddl.auto=validate` thành công, không có exception.
4. Chạy kiểm tra dự án trước khi commit/PR:
   ```powershell
   ./scripts/verify.ps1
   ```
5. Quét nhanh tuân thủ kỹ thuật PRJ301:
   - Không có `javax.servlet` hoặc `javax.persistence` trong `src/`.
   - Không có `<%` scriptlet trong `web/WEB-INF/views/**/*.jsp` (`<scripting-invalid>true</scripting-invalid>` đã bật trong `web.xml`).
   - Không có `DriverManager`/`PreparedStatement` trong business repository.

## 5. Backup, Restore & Rollback Database
- **Backup nhanh (SSMS / T-SQL):** `BACKUP DATABASE BookHubDB TO DISK = 'D:\backup\BookHubDB_YYYYMMDD.bak' WITH FORMAT;`
- **Dựng lại DB sạch:** Drop/tạo lại `BookHubDB` cục bộ → chạy lại `database/01_schema.sql` → `database/02_seed.sql`.
- **Rollback ứng dụng:** Checkout commit/tag ổn định trước đó (hoặc revert PR) → Clean & Build WAR → redeploy lên Tomcat 10.1.x.

## 6. Sự cố thường gặp (Troubleshooting)
| Triệu chứng | Nguyên nhân | Cách xử lý |
|---|---|---|
| `ClassNotFoundException` / `NoClassDefFoundError` cho `javax.servlet.*` hoặc `javax.persistence.*` | Trộn thư viện `javax.*` cũ trên Tomcat 10.1 | Gỡ toàn bộ JAR `javax.*`, chỉ dùng bộ `jakarta.*` đúng version trong `.sdd/constraints/global.md` |
| JSTL tag không chạy / lỗi URI | Dùng URI cũ `http://java.sun.com/jsp/jstl/core` hoặc thiếu JAR JSTL 3.0 | Đổi sang `jakarta.tags.core` / `jakarta.tags.fmt` và kiểm tra 2 JAR JSTL 3.0 trong `web/WEB-INF/lib/` |
| `Connection refused` tới `localhost:1433` | SQL Server chưa bật TCP/IP hoặc đang dùng dynamic port | Bật TCP/IP, set `TCP Port = 1433`, xóa dynamic port, restart SQL Server service |
| Login SQL Server thất bại với mật khẩu yếu | Password policy của Windows/SQL Server từ chối mật khẩu đơn giản | Đặt mật khẩu mạnh hơn cho login local và cập nhật `bookhub-local.properties` |
| Hibernate `SchemaManagementException` khi startup | Entity mapping lệch với `01_schema.sql` (`hbm2ddl.auto=validate`) | Coi `01_schema.sql` là chuẩn, sửa annotation Entity cho khớp kiểu/tên cột |
| `LazyInitializationException` khi render JSP | Truyền JPA Entity có quan hệ LAZY ra ngoài Service transaction | Chuyển sang map DTO/ViewModel bên trong Service transaction trước khi forward JSP |
| Lỗi biên dịch JSP với `<% ... %>` | Vi phạm chính sách JSTL-First (`scripting-invalid=true`) | Chuyển logic sang Servlet/Service và render bằng JSTL/EL (`<c:forEach>`, `<c:if>`, `<c:out>`) |
| Nghi mất đồng bộ tồn kho hoặc hoàn kho 2 lần | Thiếu lock/conditional update trong checkout hoặc cancel | Dùng pessimistic lock hoặc conditional update `stock_qty >= qty` và kiểm tra trạng thái đơn trong cùng transaction |
