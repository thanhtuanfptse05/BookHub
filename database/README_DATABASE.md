# BookHub Database Package (`database/`)

Tài liệu dựng và kiểm thử cơ sở dữ liệu **BookHubDB** trên **Microsoft SQL Server 2019/2022** (đồng bộ với `01-feat-foundation-jpa` v1.0.0 `LOCKED`).

## 1. Danh sách Script
| File | Thứ tự chạy | Mục đích |
|---|---|---|
| `01_schema.sql` | 1 | Tạo database `BookHubDB` (nếu chưa có) và 12 bảng kèm PK, FK, UNIQUE, CHECK, Filtered Unique Index (`isbn`) và Indexes |
| `02_seed.sql` | 2 | Nạp dữ liệu mẫu chuẩn 100% logic nghiệp vụ (BCrypt `$2a$10$` thật, 26 sách, 8 đơn đủ 5 trạng thái, 6 review, 6 câu hỏi / 3 câu trả lời, 2 giỏ hàng) |
| `03_demo_queries.sql` | 3 (kiểm chứng) | Đối chiếu 5 hàm tổng hợp `COUNT`/`SUM`/`AVG`/`MIN`/`MAX` + 6 truy vấn kiểm toán nghiệp vụ (mọi mục phải trả `0` vi phạm) |
| `99_drop_all.sql` | Khi reset local | Xóa 12 bảng theo thứ tự ngược FK để dựng lại DB sạch trên máy dev/test |

## 2. Cách chạy nhanh trong SSMS hoặc `sqlcmd`
```powershell
# Đảm bảo SQL Server đã bật TCP/IP port 1433 & Mixed Mode
sqlcmd -S localhost,1433 -U sa -P <your-local-sql-password> -C -i database\01_schema.sql
sqlcmd -S localhost,1433 -U sa -P <your-local-sql-password> -C -i database\02_seed.sql
sqlcmd -S localhost,1433 -U sa -P <your-local-sql-password> -C -i database\03_demo_queries.sql
```
*(Nếu dùng SSMS: mở lần lượt `01_schema.sql` → Execute → `02_seed.sql` → Execute → `03_demo_queries.sql` → Execute.)*

## 3. Tài khoản demo có sẵn trong `02_seed.sql`
> Toàn bộ mật khẩu trong bảng `dbo.Users` được lưu dưới dạng **BCrypt `$2a$10$`** (mỗi tài khoản có salt riêng, tương thích `jBCrypt 0.4` và `Spring Security Crypto`).

| Email | Mật khẩu demo (local) | Vai trò | Trạng thái | Kịch bản kiểm thử gắn kèm |
|---|---|---|---|---|
| `admin@bookhub.vn` | `Admin@123` | `ADMIN` | `ACTIVE` | Quản trị viên chính (đã trả lời Q&A `q1`, `q2`) |
| `admin2@bookhub.vn` | `Admin@123` | `ADMIN` | `ACTIVE` | Quản trị viên phụ (kiểm thử khóa/chặn khóa Admin cuối cùng) |
| `minhanh@bookhub.vn` | `Customer@123` | `CUSTOMER` | `ACTIVE` | Có giỏ 3 dòng (4 cuốn), có đơn `DELIVERED`/`SHIPPING`/`CONFIRMED`/`PENDING`, còn sách `Designing Data-Intensive Applications` đã `DELIVERED` chưa review (`TC-REV-01`) |
| `hoangnam@bookhub.vn` | `Customer@123` | `CUSTOMER` | `ACTIVE` | Có giỏ 1 dòng (`TC-CART-06` IDOR), có đơn `DELIVERED` (gồm sách inactive `bk25`) & `CANCELLED`, còn sách đã giao chưa review |
| `thuhuong@bookhub.vn` | `Customer@123` | `CUSTOMER` | `ACTIVE` | Giỏ rỗng, chưa có đơn (test Empty Cart, Empty Orders, từ chối review `TC-REV-02`) |
| `locked.user@bookhub.vn` | `Customer@123` | `CUSTOMER` | `LOCKED` | Test từ chối đăng nhập tài khoản bị khóa (`TC-AUTH-07`) & mở khóa ở `SCR-19` |

## 4. Tóm tắt ma trận kịch bản đã cài sẵn trong dữ liệu Seed
- **Phân trang & Bộ lọc (`SCR-01`/`SCR-02`):** Đúng **24 sách active** thuộc **6 danh mục active** (4 sách/danh mục = đủ 2 trang × 12), **8 sách nổi bật** (`is_featured = 1`), giá active từ **45.000 ₫ (`MIN`)** đến **320.000 ₫ (`MAX`)**.
- **Edge cases danh mục & sách:**
  - `Head First Design Patterns`: `stock_qty = 0` → test `SCR-03b` & `TC-CAT-07`.
  - `Effective Java`: `stock_qty = 1` → test double-checkout concurrency `TC-ORD-04`/`TC-ORD-06`.
  - `Kinh tế học hài hước` & `Số đỏ`: `isbn = NULL` → test `UX_Books_isbn_not_null`.
  - `Lập trình Servlet 2.5 (Ấn bản cũ)`: `is_active = 0`, `is_featured = 1`, giá `15.000 ₫`, nằm trong đơn `DELIVERED` cũ → test `TC-CAT-06` (404), `TC-ADM-04`, `TC-REP-03` (không kéo `MIN` giá active).
  - `Sách lưu trữ cũ`: danh mục `is_active = 0` chứa 1 sách active → test ẩn sách thuộc danh mục đã tắt.
- **Đơn hàng & Snapshot (`SCR-11`/`SCR-12`/`SCR-18`):** 8 đơn phủ đủ 5 trạng thái (`4 DELIVERED`, `1 SHIPPING`, `1 CONFIRMED`, `1 PENDING`, `1 CANCELLED`), phí ship cố định `30.000 ₫`. Trong đơn `BH-20261110-00002`, sách `Atomic Habits` giữ snapshot `unit_price = 150.000 ₫` dù giá hiện tại đã đổi thành `165.000 ₫` (`TC-DB-02`).
- **Số liệu Báo cáo đối chiếu (`SCR-22`):** Doanh thu `DELIVERED` = **`2.446.000 ₫`** · AOV = **`611.500 ₫`** · Pending Reviews = **`2`** · Open Questions = **`2`**.
- **Review & Q&A:** 100% review gắn với đơn `DELIVERED` thật của đúng user (`3 APPROVED`, `2 PENDING`, `1 HIDDEN`); 3 câu hỏi `ANSWERED` có đúng 3 `Answers` do `ADMIN` trả lời, `2 OPEN`, `1 HIDDEN`.
- **Reset Token:** 2 bản ghi mẫu (1 đã dùng, 1 đã hết hạn — chỉ lưu SHA-256 hash) dùng test `TC-AUTH-10`.

## 5. Backup & Restore nhanh (SQL Server)
```sql
-- Backup
BACKUP DATABASE BookHubDB TO DISK = 'D:\backup\BookHubDB_backup.bak' WITH FORMAT;

-- Restore
RESTORE DATABASE BookHubDB FROM DISK = 'D:\backup\BookHubDB_backup.bak' WITH REPLACE;
```
