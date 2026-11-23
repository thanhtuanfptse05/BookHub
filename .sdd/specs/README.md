# specs/ — Đặc tả theo từng tính năng

Mỗi tính năng = 1 thư mục **`<NN>-feat-<slug>/`** (bắt buộc tiền tố số thứ tự 2 chữ số `01`, `02`, … tăng dần theo thứ tự triển khai, không tái sử dụng số đã archive; tạo tự động bằng `scripts/new-feature` và được máy cưỡng chế bởi `scripts/spec-check`).

| File | Pha | Ai viết | Gate | Tier Standard | Tier Full |
|---|---|---|---|---|---|
| `CONTEXT.md` | 0 | Người | — | ✅ (ngắn) | ✅ |
| `SPEC.md` | 1 | Agent soạn, **người khóa** | 🔒 Gate 1 | ✅ rút gọn §1,3,7,8 | ✅ đủ 8 mục |
| `PLAN.md` | 2 | Agent | 🔒 Gate 2 | ❌ (thiết kế ở Phụ lục A SPEC) | ✅ |
| `TASKS.md` | 3 | Agent | 🔒 Gate 3 (Full) | ✅ | ✅ |
| `PROGRESS.md` | 4 | Agent | — | ✅ | ✅ |
| `REVIEW.md` | 5 | Reviewer độc lập | — | khuyến nghị | ✅ bắt buộc |
| `DESIGN.md` | 1b | Agent soạn, người duyệt cùng Gate 1 | 🔒 Gate 1 | chỉ khi `UI = Có` | chỉ khi `UI = Có` |
| `CHANGELOG.md` | mọi lúc | Agent/Người | — | ✅ | ✅ |

Tạo mới (tự động cấp số tiếp theo `09`, `10`, … hoặc chỉ định `-Number 09`):
```powershell
./scripts/new-feature.ps1 <slug> -Tier Full [-Ui] [-Number 09]
```
```bash
./scripts/new-feature.sh <slug> full [ui] [09]
```
Tier **Quick** không tạo thư mục feature (chỉ dùng `plan.md`).

Ví dụ điền sẵn chuẩn văn phong: [`_example-create-todo/`](_example-create-todo/) (bỏ qua bởi `spec-check`).
Feature đã `done` hoặc `dropped`: chuyển thư mục sang `_archive/<NN>-feat-<slug>/` (chống phình rác; `spec-check` tự bỏ qua thư mục bắt đầu bằng `_`).

## Chỉ mục feature
| Feature | Tier | UI | Trạng thái | Pha hiện tại | Phụ thuộc & Mục tiêu PRJ301 |
|---|---|---|---|---|---|
| [`01-feat-foundation-jpa`](01-feat-foundation-jpa/SPEC.md) | Full | Không | `done` | `APPROVED` (Gate 4 · 7/7 tasks · 13/13 tests) | Nền tảng — SQL Server 12 bảng, JPA 3.1 / Hibernate 6.6 (1.0đ Framework) |
| [`02-feat-mvc-security-shell`](02-feat-mvc-security-shell/SPEC.md) | Full | Có | `done` | `APPROVED` (Gate 4 · 6/6 tasks · 7/7 tests) | Sau `01` — `web.xml`, 4 Filters, 3 App Shells, `SCR-23` |
| [`03-feat-auth-account`](03-feat-auth-account/SPEC.md) | Full | Có | `done` | `APPROVED` (Gate 4 · 6/6 tasks · 4/4 tests) | Sau `02` — Register/Login/Reset/Profile (`SCR-04..07,13` · 1.0đ Auth) |
| [`04-feat-public-catalog`](04-feat-public-catalog/SPEC.md) | Full | Có | `done` | `APPROVED` (Gate 4 · 6/6 tasks · 3/3 tests) | Sau `02` — Home/Catalogue/Detail (`SCR-01..03b` · 2.0đ Home/Data) |
| [`05-feat-admin-catalog-crud`](05-feat-admin-catalog-crud/SPEC.md) | Full | Có | `done` | `APPROVED` (Gate 4 · 5/5 tasks · 2/2 tests) | Sau `03`, `04` — CRUD Books & Categories (`SCR-15..17` · 2.0đ CRUD) |
| [`06-feat-cart-checkout-orders`](06-feat-cart-checkout-orders/SPEC.md) | Full | Có | `done` | `APPROVED` (Gate 4 · 8/8 tasks · 3/3 tests) | Sau `03`, `04` — Cart DB, COD Checkout, Orders (`SCR-08..12b,18` · 1.0đ Cart + Order) |
| [`07-feat-reviews-qa`](07-feat-reviews-qa/SPEC.md) | Full | Có | `done` | `APPROVED` (Gate 4 · 5/5 tasks · 2/2 tests) | Sau `06` — Review & Q&A Moderation (`SCR-03,12b,20,21` · 2.0đ Q&A) |
| [`08-feat-admin-users-reports`](08-feat-admin-users-reports/SPEC.md) | Full | Có | `done` | `APPROVED` (Gate 4 · 5/5 tasks · 3/3 tests) | Sau `05`, `06`, `07` — Users, Dashboard & Reports (`SCR-14,19,22` · 1.0đ Stats) |

Trạng thái: `idea → specifying → planning → implementing → verifying → done | dropped`
