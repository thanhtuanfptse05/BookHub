# Roadmap — BookHub

> Nguồn theo dõi tiến độ sản phẩm ở cấp cao (đồng bộ từ `BookHub_PRJ301_Project_Plan_Requirements.md` §20).
> **Luật đồng bộ (`roadmap-sync`):** mỗi khi feature chuyển trạng thái (`implementing` → `done` hoặc `dropped`), cập nhật bảng này cùng lúc với `.sdd/specs/README.md` và `docs/business-flows.md`.

| Trường | Giá trị |
|---|---|
| Giai đoạn hiện tại | `MVP — Pha 2–3 (Đã khóa Gate 1 & lập xong 8 PLAN + 48 TASKS, sẵn sàng Gate 2–3)` |
| Thời lượng baseline | 4 tuần (triển khai theo vertical slice, 8 feature tương ứng EPIC 1 → EPIC 8) |
| Cập nhật lần cuối | 2026-11-23 |

## 1. Đang làm (Now)
| Feature / Phase | Tier | Spec | Tiến độ | Ghi chú |
|---|---|---|---|---|
| **Pha 2–3 — Thiết kế & Phân rã Task (8 tính năng)** | Full | `.sdd/specs/01-feat-*` .. `08-feat-*` | 8/8 `SPEC+DESIGN LOCKED`, 8/8 `PLAN+TASKS` xong | Chờ người duyệt Gate 2–3 để bắt đầu code từ `01-feat-foundation-jpa` (`T-001`) |

## 2. Sắp tới (Next — Lộ trình 8 Feature Vertical Slice)
| Tuần | Ưu tiên | Feature Spec | Mục tiêu ngắn & Exit Criteria | Phụ thuộc |
|---|---|---|---|---|
| Tuần 1 | P1 | **`01-feat-foundation-jpa`** (EPIC 1) | `01_schema.sql`, `02_seed.sql`, runtime JARs (`web/WEB-INF/lib`), `persistence.xml`, `JpaUtil`, 12 Entities → Hibernate `validate` & query được `Books` | — |
| Tuần 1 | P1 | **`02-feat-mvc-security-shell`** (EPIC 2) | `EncodingFilter`, `AuthenticationFilter`, `AdminAuthorizationFilter`, `CsrfFilter`, `/403`/`/404`/`/500`, common JSTL layout (`scripting-invalid=true`) | `01-feat-foundation-jpa` |
| Tuần 1 | P1 | **`03-feat-auth-account`** (EPIC 3) | Register, Login/Logout (session rotation), Profile, Change Password, Forgot/Reset token 15 phút (`SCR-04..07, 13`) → đủ 1.0đ PRJ301 | `02-feat-mvc-security-shell` |
| Tuần 2 | P1 | **`04-feat-public-catalog`** (EPIC 4) | Home (featured/newest/bestseller), `/books` search/filter/sort/paging (12/trang), `/books/detail`, sách tương tự (`SCR-01..03b`) → đủ 2.0đ PRJ301 | `02-feat-mvc-security-shell` |
| Tuần 2 | P1 | **`05-feat-admin-catalog-crud`** (EPIC 5) | CRUD + search/filter/paging + soft-deactivate cho `Categories` & `Books` (`SCR-15..17`) → phần lõi 2.0đ CRUD PRJ301 | `03-feat-auth-account`, `04-feat-public-catalog` |
| Tuần 3 | P1 | **`06-feat-cart-checkout-orders`** (EPIC 6) | Cart DB, Checkout COD transactional (30.000 ₫ ship, trừ kho atomic, snapshot), My Orders/Cancel (hoàn kho 1 lần), Admin Order state machine (`SCR-08..12b, 18`) → đủ 1.0đ Cart | `03-feat-auth-account`, `04-feat-public-catalog` |
| Tuần 4 | P1 | **`07-feat-reviews-qa`** (EPIC 7) | Customer Review (sau `DELIVERED`, 1/user/book, `PENDING`→`APPROVED`/`HIDDEN`); Customer Question (`OPEN`) & Admin Answer (`ANSWERED`/`HIDDEN`) (`SCR-03, 12b, 20, 21`) → đủ 2.0đ Q&A | `06-feat-cart-checkout-orders` |
| Tuần 4 | P1 | **`08-feat-admin-users-reports`** (EPIC 8) | Khóa/mở khóa Customer; Dashboard & Reports `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`, bestseller (`SCR-14, 19, 22`) → đủ 1.0đ Statistics | `05`, `06`, `07` |
| Tuần 4 | P2 | **EPIC 9** — Quality, Security & Submission | Responsive UI, full test cases (`TC-*`), quét không scriptlet/không `javax.*`/không JDBC DAO/không secret, clean DB test, screenshots & demo script | Cả 8 feature trên |

### Các mốc nghiệm thu (Milestones)
- **M1 — Environment ready:** NetBeans 17 build/deploy WAR lên Tomcat 10.1.x, kết nối SQL Server `localhost:1433`.
- **M2 — Persistence ready:** Hibernate 6.6 / JPA 3.1 `validate` khớp `BookHubDB`, đọc/ghi thành công.
- **M3 — Catalogue ready:** Home, Search, Filter, Sort, Pagination, Book Detail chạy bằng dữ liệu SQL Server.
- **M4 — Commerce ready:** Cart + Checkout COD transactional + tồn kho + lịch sử đơn & hủy đơn nhất quán.
- **M5 — Admin ready:** CRUD Books/Categories, quản lý Orders/Users và Reports `COUNT/SUM/AVG/MIN/MAX` có authZ đầy đủ.
- **M6 — Academic ready:** Review/Q&A, kiểm thử, tài liệu nộp bài và kịch bản demo hoàn tất.

## 3. Để sau (Later / Out of Scope MVP)
Chỉ xem xét sau khi hoàn thành toàn bộ Must-have MVP và pass mọi tiêu chí PRJ301:
- Cổng thanh toán trực tuyến (VNPay, MoMo, ZaloPay, Stripe, SePay) & luồng hoàn tiền/trả hàng.
- Voucher, mã giảm giá, điểm thưởng, membership, flash sale.
- Mô hình đa người bán (Marketplace / Seller Portal) & Shipper Portal / tracking vận chuyển real-time.
- Upload file ảnh bìa tùy ý (MVP dùng URL hoặc static assets).
- Đăng nhập OAuth (Google/Facebook), email SMTP thật & email marketing, đa ngôn ngữ.
- Chat real-time, recommendation AI/ML, mobile app, public REST API, đa kho/ERP/POS.

## 4. Đã hoàn thành (Done)
| Ngày | Feature | Mã quy trình (`business-flows.md`) | Spec / Ghi chú |
|---|---|---|---|
| 2026-11-23 | Khởi tạo GitHub repo & hoàn thiện tài liệu nền tảng SDD | — | Repo `thanhtuanfptse05/BookHub` + bộ docs/constraints/ADRs |
