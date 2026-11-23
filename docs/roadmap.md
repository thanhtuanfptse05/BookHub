# Roadmap — BookHub

> Nguồn theo dõi tiến độ sản phẩm ở cấp cao (đồng bộ từ `BookHub_PRJ301_Project_Plan_Requirements.md` §20).
> **Luật đồng bộ (`roadmap-sync`):** mỗi khi feature chuyển trạng thái (`implementing` → `done` hoặc `dropped`), cập nhật bảng này cùng lúc với `.sdd/specs/README.md` và `docs/business-flows.md`.

| Trường | Giá trị |
|---|---|
| Giai đoạn hiện tại | `MVP — Pha 4–5 (Đã triển khai đủ 8/8 tính năng 01..08, 37/37 JUnit 5 + live Tomcat 10.1 pass, chờ Owner duyệt Gate 4 REVIEW.md)` |
| Thời lượng baseline | 4 tuần (triển khai theo vertical slice, 8 feature tương ứng EPIC 1 → EPIC 8) |
| Cập nhật lần cuối | 2026-11-23 |

## 1. Đang làm (Now)
| Feature / Phase | Tier | Spec | Tiến độ | Ghi chú |
|---|---|---|---|---|
| **Pha 5 — Review & Gate 4 (`01`..`08`)** | Full | `.sdd/specs/01-feat-*` .. `08-feat-*` | 8/8 code + 37/37 tests + Tomcat smoke test xanh | Chờ Owner duyệt `REVIEW.md` (`APPROVED`) và merge `feat/bookhub-mvp-01-08` vào `main` |

## 2. Lộ trình 8 Feature Vertical Slice + EPIC 9
| Tuần | Ưu tiên | Feature Spec | Mục tiêu ngắn & Hiện trạng | Phụ thuộc |
|---|---|---|---|---|
| Tuần 1 | P1 | **`01-feat-foundation-jpa`** (EPIC 1) | `01_schema.sql`, `02_seed.sql`, 23 runtime JARs, `persistence.xml`, `JpaUtil`, 12 Entities → **Code + 13/13 tests xanh** (chờ duyệt Gate 4) | — |
| Tuần 1 | P1 | **`02-feat-mvc-security-shell`** (EPIC 2) | `web.xml`, 4 Filters, `/403`/`/404`/`/500`, Bootstrap 5.3 + design tokens + common JSTL layout → **Code + 7/7 tests xanh** (chờ duyệt Gate 4) | `01-feat-foundation-jpa` |
| Tuần 1 | P1 | **`03-feat-auth-account`** (EPIC 3) | Register, Login/Logout (session rotation), Profile, Change Password, Forgot/Reset token 15 phút (`SCR-04..07, 13`) → **Code + 4/4 tests xanh** (chờ duyệt Gate 4) | `02-feat-mvc-security-shell` |
| Tuần 2 | P1 | **`04-feat-public-catalog`** (EPIC 4) | Home (featured/newest/bestseller), `/books` search/filter/sort/paging DB, `/books/detail`, sách tương tự (`SCR-01..03b`) → **Code + 3/3 tests xanh** (chờ duyệt Gate 4) | `02-feat-mvc-security-shell` |
| Tuần 2 | P1 | **`05-feat-admin-catalog-crud`** (EPIC 5) | CRUD + search/filter/paging + soft-deactivate cho `Categories` & `Books` (`SCR-15..17`) → **Code + 2/2 tests xanh** (chờ duyệt Gate 4) | `03-feat-auth-account`, `04-feat-public-catalog` |
| Tuần 3 | P1 | **`06-feat-cart-checkout-orders`** (EPIC 6) | Cart DB, Checkout COD transactional (30.000 ₫ ship, trừ kho atomic), My Orders/Cancel (hoàn kho 1 lần), Admin state machine (`SCR-08..12b, 18`) → **Code + 3/3 tests (kèm 2-thread concurrency) xanh** (chờ duyệt Gate 4) | `03-feat-auth-account`, `04-feat-public-catalog` |
| Tuần 4 | P1 | **`07-feat-reviews-qa`** (EPIC 7) | Customer Review (sau `DELIVERED`, 1/user/book, `PENDING`→`APPROVED`/`HIDDEN`); Q&A Customer hỏi & Admin trả lời/sửa/ẩn (`SCR-03, 12b, 20, 21`) → **Code + 2/2 tests xanh** (chờ duyệt Gate 4) | `06-feat-cart-checkout-orders` |
| Tuần 4 | P1 | **`08-feat-admin-users-reports`** (EPIC 8) | Khóa/mở khóa Customer (bảo vệ Admin cuối); Dashboard & Reports `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`, bestseller (`SCR-14, 19, 22`) → **Code + 3/3 tests xanh** (chờ duyệt Gate 4) | `05`, `06`, `07` |
| Tuần 4 | P2 | **EPIC 9** — Quality, Security & Submission | Duyệt Gate 4 `01`..`08`, chụp ảnh kiểm UI 375/768/1280 trên trình duyệt thực, hoàn thiện kịch bản demo & gói nộp PRJ301 | Cả 8 feature trên |

### Các mốc nghiệm thu (Milestones)
- **M1 — Environment ready:** ✅ NetBeans 17 Ant build `dist/BookHub.war`, chạy thực tế trên Tomcat 10.1.x & SQL Server 2022 `localhost:1433`.
- **M2 — Persistence ready:** ✅ Hibernate 6.6.59.Final / JPA 3.1 `validate` khớp 12/12 bảng `BookHubDB`.
- **M3 — Catalogue ready:** ✅ Home, Search, Filter, Sort, Pagination, Book Detail chạy bằng dữ liệu SQL Server.
- **M4 — Commerce ready:** ✅ Cart + Checkout COD transactional + tồn kho concurrency-safe + lịch sử đơn & hủy đơn nhất quán.
- **M5 — Admin ready:** ✅ CRUD Books/Categories, quản lý Orders/Users và Reports `COUNT/SUM/AVG/MIN/MAX` có authZ đầy đủ.
- **M6 — Academic ready:** 🔄 Code & test Review/Q&A hoàn tất; chờ Owner duyệt Gate 4, kiểm trực quan trình duyệt và merge `main`.

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
| 2026-11-23 | Khởi tạo GitHub repo, Spec Kit & bộ 8 Spec khóa Gate 1 + Database SQL Server | — | Đã merge `main` (`8b4d3ee`) |
| 2026-11-23 | Triển khai full MVP `01`..`08` (trên nhánh `feat/bookhub-mvp-01-08`) | `QT-01`..`QT-08` | Chờ Owner duyệt `REVIEW.md` Gate 4 để chốt `done` & merge `main` |
