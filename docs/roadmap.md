# Roadmap — BookHub

> Nguồn theo dõi tiến độ sản phẩm ở cấp cao (đồng bộ từ `BookHub_PRJ301_Project_Plan_Requirements.md` §20).
> **Luật đồng bộ (`roadmap-sync`):** mỗi khi feature chuyển trạng thái (`implementing` → `done` hoặc `dropped`), cập nhật bảng này cùng lúc với `.sdd/specs/README.md` và `docs/business-flows.md`.

| Trường | Giá trị |
|---|---|
| Giai đoạn hiện tại | `MVP — P0 Scope & Setup` |
| Thời lượng baseline | 4 tuần (triển khai theo vertical slice, EPIC 0 → EPIC 9) |
| Cập nhật lần cuối | 2026-11-23 |

## 1. Đang làm (Now)
| Feature / Phase | Tier | Spec | Tiến độ | Ghi chú |
|---|---|---|---|---|
| **P0 / EPIC 0** — Scope, Git & SDD Foundation | Full | Tài liệu nền tảng + GitHub repo | Đang hoàn thiện | Đã đẩy repo lên GitHub và hoàn thiện bộ tài liệu `.md` theo requirement |

## 2. Sắp tới (Next — Lộ trình 4 tuần MVP)
| Tuần | Ưu tiên | Phase / EPIC | Mục tiêu ngắn & Exit Criteria | Phụ thuộc |
|---|---|---|---|---|
| Tuần 1 | P1 | **EPIC 1** — Database & JPA | `01_schema.sql`, `02_seed.sql`, runtime JARs (`web/WEB-INF/lib`), `persistence.xml`, `JpaUtil`, 12 Entities → Hibernate `validate` & query được `Books` | EPIC 0 |
| Tuần 1 | P1 | **EPIC 2** — MVC & Security Filters | `EncodingFilter`, `AuthenticationFilter`, `AdminAuthorizationFilter`, `CsrfFilter`, `/403`/`/404`/`/500`, common JSTL layout (`scripting-invalid=true`) | EPIC 1 |
| Tuần 1 | P1 | **EPIC 3** — Authentication & Account | Register, Login/Logout (session rotation), Profile, Change Password, Forgot/Reset token 15 phút → đủ 1.0đ PRJ301 | EPIC 2 |
| Tuần 2 | P1 | **EPIC 4** — Public Catalog | Home (featured/newest/bestseller), `/books` search/filter/sort/paging (12/trang), `/books/detail`, sách tương tự → đủ 2.0đ PRJ301 | EPIC 1, 2 |
| Tuần 2 | P1 | **EPIC 5** — Admin Books & Categories CRUD | CRUD + search/filter/paging + soft-deactivate cho `Categories` & `Books` → phần lõi 2.0đ CRUD PRJ301 | EPIC 2, 4 |
| Tuần 3 | P1 | **EPIC 6** — Cart, Checkout COD & Orders | Cart DB, Checkout COD transactional (30.000 ₫ ship, trừ kho atomic, snapshot), My Orders/Cancel (hoàn kho 1 lần), Admin Order state machine → đủ 1.0đ Cart | EPIC 3, 4 |
| Tuần 4 | P1 | **EPIC 7** — Reviews & Q&A | Customer Review (sau `DELIVERED`, 1/user/book, `PENDING`→`APPROVED`/`HIDDEN`); Customer Question (`OPEN`) & Admin Answer (`ANSWERED`/`HIDDEN`) → đủ 2.0đ Q&A | EPIC 3, 4, 6 |
| Tuần 4 | P1 | **EPIC 8** — Admin Users, Dashboard & Reports | Khóa/mở khóa Customer; Dashboard & Reports `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`, bestseller → đủ 1.0đ Statistics | EPIC 5, 6, 7 |
| Tuần 4 | P2 | **EPIC 9** — Quality, Security & Submission | Responsive UI, full test cases (`TC-*`), quét không scriptlet/không `javax.*`/không JDBC DAO/không secret, clean DB test, screenshots & demo script | EPIC 3–8 |

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
