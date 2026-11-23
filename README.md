# BookHub — Website Bán Sách Online (PRJ301)

BookHub là ứng dụng web bán sách trực tuyến theo mô hình **single-store e-commerce** (quy mô MVP cho môn **PRJ301**), xây dựng bằng **Java 17 + Jakarta Servlet/JSP/JSTL + JPA/Hibernate + Microsoft SQL Server** trên **NetBeans 17 (Apache Ant)** và **Apache Tomcat 10.1.x**.

> Tài liệu đặc tả đầy đủ: [BookHub_PRJ301_Project_Plan_Requirements.md](BookHub_PRJ301_Project_Plan_Requirements.md)  
> Hiến pháp & quy trình AI/SDD: [AGENTS.md](AGENTS.md) · [CLAUDE.md](CLAUDE.md) · [.sdd/INDEX.md](.sdd/INDEX.md)

---

## 1. Tính năng chính (MVP)

| Nhóm | Vai trò | Tính năng |
|---|---|---|
| **Public Catalog** | Guest / All | Trang chủ (sách nổi bật, mới nhất, bán chạy), tìm kiếm (tên/tác giả/ISBN), lọc danh mục & khoảng giá, sắp xếp (whitelist), phân trang (12 sách/trang), chi tiết sách, sách tương tự |
| **Authentication & Account** | Guest / Customer / Admin | Đăng ký Customer, đăng nhập/đăng xuất (BCrypt/Argon2, đổi session ID), quên/đặt lại mật khẩu (token 15 phút dùng 1 lần), cập nhật hồ sơ, đổi mật khẩu, phân quyền server-side (`ADMIN`/`CUSTOMER`) |
| **Cart & Checkout COD** | Customer | Giỏ hàng bền vững trong DB (1 cart/user), thêm/sửa/xóa, tính tạm tính từ giá DB, checkout COD (phí ship cố định `30.000 VND`), trừ tồn kho nguyên tử trong transaction, lưu snapshot đơn hàng, xem lịch sử & hủy đơn (`PENDING`/`CONFIRMED`) hoàn kho đúng 1 lần |
| **Review & Q&A** | Customer / Admin | Đánh giá 1–5 sao (chỉ sau khi đơn `DELIVERED`, 1 review/user/book, Admin duyệt `APPROVED`/`HIDDEN`); đặt câu hỏi theo sách (`OPEN`), Admin trả lời (`ANSWERED`) hoặc ẩn (`HIDDEN`) |
| **Admin Console & Reports** | Admin | Dashboard & báo cáo tổng hợp (`COUNT`, `SUM`, `AVG`, `MIN`, `MAX`), CRUD Sách & Danh mục (soft-deactivate), quản lý trạng thái đơn hàng theo state machine, khóa/mở khóa Customer, kiểm duyệt Review & Q&A |

**Out of Scope (MVP):** Payment gateway online (VNPay/MoMo/Stripe), đa người bán (marketplace), tracking vận chuyển real-time, voucher/coupon, OAuth social login, upload file ảnh tùy ý, chat real-time, đa ngôn ngữ.

---

## 2. Tech Stack

| Lớp | Công nghệ & phiên bản baseline |
|---|---|
| Ngôn ngữ / Runtime | Java JDK 17 LTS |
| IDE / Build | Apache NetBeans 17 · Apache Ant Web Application (`build.xml`) |
| Web Server | Apache Tomcat 10.1.x |
| Web MVC / View | Jakarta Servlet 6.0 · JSP 3.1 · JSTL 3.0 (`jakarta.tags.*`) · EL 5.0 |
| Persistence / ORM | Jakarta Persistence (JPA) 3.1 · Hibernate ORM `6.6.59.Final` |
| Database / Driver | Microsoft SQL Server 2019/2022 (`BookHubDB`) · `mssql-jdbc 13.6.2.jre11` |
| UI | Bootstrap 5.3.x + Brand CSS + Vanilla JS (ES6+) |
| Testing | JUnit 5.x |

> **Quy tắc cứng:** Chỉ dùng namespace `jakarta.*` (không trộn `javax.*`); 100% truy cập DB đi qua JPA/Hibernate (không DAO JDBC thuần); 100% JSP chỉ dùng JSTL/EL (không scriptlet `<% %>`, bật `<scripting-invalid>true</scripting-invalid>`).

---

## 3. Cài đặt & chạy local

### 3.1. Yêu cầu môi trường
- **JDK 17 LTS** (`JAVA_HOME` trỏ đúng JDK 17)
- **Apache NetBeans 17**
- **Apache Tomcat 10.1.x** (đã đăng ký trong NetBeans Services → Servers)
- **Microsoft SQL Server 2019/2022** (bật TCP/IP, static port `1433`, Mixed Mode Authentication)

### 3.2. Thiết lập Database & cấu hình local
1. Tạo database `BookHubDB` trên SQL Server (`localhost:1433`).
2. Chạy script khởi tạo (khi đã tạo ở EPIC 1): `database/01_schema.sql` → `database/02_seed.sql`.
3. Tạo file cấu hình cục bộ `bookhub-local.properties` (nằm **ngoài Git**, không commit credential):
   ```properties
   db.url=jdbc:sqlserver://localhost:1433;databaseName=BookHubDB;encrypt=true;trustServerCertificate=true
   db.username=sa
   db.password=<your-local-sql-password>
   ```
   > Không commit mật khẩu DB vào repository công khai và không mở port `1433` ra Internet.

### 3.3. Build & Deploy
1. Thêm các JAR runtime (Hibernate 6.6.x, Jakarta Persistence 3.1, `mssql-jdbc`, JSTL 3.0 API/Impl, BCrypt, SLF4J) vào `web/WEB-INF/lib` (không đóng gói trùng API Servlet/JSP/EL do Tomcat cung cấp).
2. Mở project trong NetBeans 17 → **Clean and Build** (hoặc `ant dist`).
3. **Run** để deploy WAR lên Tomcat 10.1.x và mở `http://localhost:8080/BookHub/`.

Chi tiết xử lý sự cố và backup/restore DB xem [docs/runbook.md](docs/runbook.md).

---

## 4. Cấu trúc dự án

```text
BookHub/
├── AGENTS.md                  # Luật làm việc & phân tier tác vụ (Quick / Standard / Full)
├── CLAUDE.md                  # Bộ nhớ dự án, kiến trúc, hazards & danh mục ADR
├── BookHub_PRJ301_Project_Plan_Requirements.md  # Đặc tả tổng thể & PRJ301 traceability
├── build.xml & nbproject/     # Cấu hình NetBeans 17 Ant Web Application
├── src/
│   ├── java/com/bookhub/      # config, entity, dto, repository, service, controller, filter, exception, util
│   └── conf/META-INF/         # persistence.xml (BookHubPU)
├── web/
│   ├── assets/                # css, js, images, vendor/bootstrap
│   └── WEB-INF/               # web.xml, lib/, views/ (common, site, auth, customer, admin, error)
├── database/                  # 01_schema.sql, 02_seed.sql, 03_demo_queries.sql (EPIC 1)
├── tests/                     # unit/, integration/, e2e/ (JUnit 5)
├── docs/                      # architecture.md, business-flows.md, roadmap.md, runbook.md, ci-cd.md
├── .sdd/                      # constitution, constraints, design, seo, rfcs (ADR-001..009), specs, skills, prompts
└── scripts/                   # verify, spec-check, ui-check, seo-check, new-feature, ship, setup-hooks
```

---

## 5. Quy trình phát triển (Spec-Driven Development)

Dự án áp dụng quy trình **Spec-First** (`AGENTS.md`):
- **Kiểm tra nền:** `./scripts/verify.ps1` (`spec-check` + `ui-check` + `seo-check` + `secret-scan` + build/test khi cấu hình).
- **Khởi tạo feature mới:** `./scripts/new-feature.ps1 <slug> -Tier Full -Ui` (tự động tạo thư mục `.sdd/specs/<NN>-feat-<slug>/` với số thứ tự `01`, `02`, …).
- **Luồng làm việc:** `CONTEXT → SPEC (Gate 1) → PLAN (Gate 2) → TASKS (Gate 3) → IMPLEMENT → REVIEW → DONE`.
- **Git & PR:** Làm việc trên nhánh `feat/*`, `fix/*`, `chore/*`; commit theo Conventional Commits; đẩy lên GitHub qua PR và `scripts/ship`.

---

## 6. Đối chiếu tiêu chí PRJ301

| Nhóm tiêu chí | Điểm | Phạm vi BookHub |
|---|---:|---|
| 3.1 Authentication & Role | 1.0 | Register, Login, Logout, Forgot/Reset, Profile, `ADMIN`/`CUSTOMER` server-side authZ |
| 3.2 Home & Data Display | 2.0 | Featured/New/Bestseller, Search, Filter, Sort, Pagination (12/trang), Detail, Similar |
| 3.3 Full CRUD | 2.0 | Admin CRUD Books & Categories (soft-deactivate); quản lý Orders, Users, Reviews, Q&A |
| 3.5 Statistics & Reports | 1.0 | `COUNT`, `SUM`, `AVG`, `MIN`, `MAX` doanh thu đã giao, giá sách, tồn kho, bestseller |
| 3.6 Shopping Cart | 1.0 | Cart lưu DB theo Customer, add/update/remove/subtotal, checkout COD |
| 3.7 Framework | 1.0 | Jakarta Persistence 3.1 + Hibernate ORM 6.6 + Layered MVC + Transaction |
| 3.8 Q&A Module | 2.0 | Customer đặt câu hỏi, Admin trả lời/ẩn, hiển thị công khai theo trạng thái |
