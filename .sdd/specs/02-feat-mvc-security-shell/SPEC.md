# SPEC — Khung MVC, Bảo mật Filter & UI Shell dùng chung (mvc-security-shell)

> **Pha 1 · Hợp đồng đặc tả.** Agent soạn nháp từ `CONTEXT.md`, **người duyệt & khóa** (Gate 1).
> Sau khi `Status: LOCKED`, sửa đổi chỉ qua `CHANGELOG.md` + người duyệt. **Chưa LOCKED ⇒ cấm code.**

| Trường | Giá trị |
|---|---|
| Feature ID | `02-feat-mvc-security-shell` |
| Tier | Full |
| UI | Có |
| Status | `LOCKED` |
| Version | 1.0.0 |
| Owner | BookHub Team (PRJ301) |
| Liên quan | `constitution.md` (H-02, H-04, H-05, H-06, A-01, A-02, S-07, S-10), `constraints/ui.md`, `FR-VAL-001..008`, `ADR-004`, `SCR-23` |

---

## 1. Tổng quan (Overview)
Xây dựng lớp nền tảng Web MVC & bảo mật cho BookHub gồm cấu hình `web.xml` (Servlet 6.0, cưỡng chế không scriptlet), chuỗi 4 Servlet Filter (`EncodingFilter`, `AuthenticationFilter`, `AdminAuthorizationFilter`, `CsrfFilter`), các lớp ngoại lệ chuẩn, `PasswordUtil` (BCrypt), bộ tài nguyên tĩnh (`Bootstrap 5.3`, `tokens.css`, `bookhub.css`, JS tối giản), các JSP fragments dùng chung cho 3 App Shells và 3 trang lỗi thống nhất `/403`, `/404`, `/500` (`SCR-23`). Xem `CONTEXT.md`.

## 2. Actors & User Stories
| ID | Câu chuyện |
|---|---|
| US-01 | Là **Người dùng (Guest/Customer/Admin)**, tôi muốn mọi trang có giao diện tiếng Việt đồng nhất (Header/Footer/Sidebar) hiển thị đúng UTF-8 và responsive trên cả điện thoại lẫn máy tính. |
| US-02 | Là **Người dùng**, tôi muốn thấy trang thông báo thân thiện khi gặp lỗi 403/404/500 kèm nút quay về trang chủ thay vì màn hình lỗi kỹ thuật của Tomcat. |
| US-03 | Là **Quản trị/Hệ thống**, tôi muốn mọi truy cập trái quyền và mọi request POST giả mạo (CSRF) bị chặn tự động ngay tại tầng Filter trước khi chạm tới nghiệp vụ. |

## 3. Functional Requirements (EARS)
| ID | Loại | Mệnh đề | Liên kết (US / BR) |
|---|---|---|---|
| FR-01 | Ubiquitous | The system SHALL configure `web/WEB-INF/web.xml` with Jakarta Servlet 6.0 schema, `<scripting-invalid>true</scripting-invalid>` for `*.jsp`, `HttpOnly` session cookie flag, and unified error page mappings for HTTP `403`, `404`, and `500`. | US-02, US-03, ADR-004, NFR-SEC-006 |
| FR-02 | Ubiquitous | The system SHALL execute `EncodingFilter` on every request (`/*`) to enforce `UTF-8` character encoding for both request and response. | US-01, constraints/global.md §5 |
| FR-03 | Event | WHEN an unauthenticated Guest requests a protected customer route (`/cart`, `/checkout`, `/orders/*`, `/account/*`, `/books/review`, `/books/question`), the system SHALL redirect the browser via `AuthenticationFilter` to `/login` preserving a validated internal-only `returnUrl` (preventing open redirect). | US-03, FR-AUTH-013 |
| FR-04 | Unwanted | IF an unauthenticated user OR a logged-in `CUSTOMER` requests any `/admin` or `/admin/*` endpoint, THEN `AdminAuthorizationFilter` SHALL redirect guests to `/login` and respond with HTTP `403` (forwarding to `SCR-23` `/403`) for non-`ADMIN` users. | US-03, FR-AUTH-014, BR-ADM-001 |
| FR-05 | Event | WHEN any HTTP `POST` request arrives for a state-changing route, `CsrfFilter` SHALL verify that the `_csrf` form parameter matches the cryptographically random CSRF token stored in the current `HttpSession` using constant-time comparison. | US-03, NFR-SEC-005 |
| FR-06 | Unwanted | IF a `POST` request is missing the `_csrf` token or submits a mismatched token, THEN `CsrfFilter` SHALL reject the request with HTTP `403` without executing the target servlet. | US-03, NFR-SEC-005 |
| FR-07 | Ubiquitous | The system SHALL provide `PasswordUtil` using BCrypt (with random salt, cost ≥ 10) for password hashing/verification, plus SHA-256 hex hashing helper for opaque reset tokens, and a flash-message utility supporting PRG (`Post/Redirect/Get`). | US-03, H-02, FR-VAL-004 |
| FR-08 | Ubiquitous | The system SHALL provide shared static assets (`web/assets/vendor/bootstrap/`, `web/assets/css/tokens.css`, `web/assets/css/bookhub.css`, `web/assets/images/book-placeholder.svg`, minimal ES6 JS for stepper & confirm modal) synchronized with `.sdd/design/tokens.css` and reusable JSTL fragments in `web/WEB-INF/views/common/` (`header.jsp`, `nav.jsp`, `footer.jsp`, `messages.jsp`, `book-card.jsp`, `pagination.jsp`, `status-badge.jsp`, `empty-state.jsp`, `confirm-modal.jsp`) and `web/WEB-INF/views/admin/` (`layout-sidebar.jsp`, `layout-topbar.jsp`). | US-01, S-10, U-01..U-07 |
| FR-09 | Event | WHEN HTTP `403`, `404`, or `500` occurs, the system SHALL render the corresponding JSP under `web/WEB-INF/views/error/` (`403.jsp`, `404.jsp`, `500.jsp`) inside the Storefront Shell with friendly Vietnamese messaging and navigation CTAs, without exposing stack traces, SQL, or server internals. | US-02, FR-VAL-006, FR-VAL-007 |

## 4. Non-Functional Requirements
| ID | Hạng mục | Yêu cầu (số cụ thể) |
|---|---|---|
| NFR-01 | Bảo mật | Thứ tự Filter cố định: `EncodingFilter` → `CsrfFilter` → `AuthenticationFilter` → `AdminAuthorizationFilter`; `returnUrl` chỉ chấp nhận đường dẫn tương đối bắt đầu bằng `/` (không bắt đầu bằng `//` hoặc `http`). |
| NFR-02 | Chính sách JSP | `0` scriptlet `<% %>`, `<%= %>`, `<%! %>` và `0` thẻ `<sql:*>` trong toàn bộ `web/WEB-INF/views/**/*.jsp`; mọi text động dùng `<c:out>`. |
| NFR-03 | UI / A11y | Đạt tương phản WCAG 2.2 AA (`≥ 4.5:1`), `:focus-visible` rõ nét (`2px solid #1e5649`), không cuộn ngang ở `375px`, nút `Đăng nhập` mobile đặt `whitespace-nowrap`. |
| NFR-04 | Đóng gói sạch | Toàn bộ JSP nghiệp vụ nằm bên trong `web/WEB-INF/views/` để trình duyệt không thể truy cập trực tiếp bỏ qua Servlet. |

## 5. Data & API Contract
**Filter & Error Routes:**
| Thành phần | URL Pattern | Thứ tự | Hành vi |
|---|---|---|---|
| `EncodingFilter` | `/*` | 1 | Set UTF-8 request & response |
| `CsrfFilter` | `/*` | 2 | Sinh token cho session; xác thực `_csrf` trên mọi `POST` (trả `403` nếu sai/thiếu) |
| `AuthenticationFilter` | `/cart`, `/checkout`, `/orders/*`, `/account/*`, `/books/review`, `/books/question` | 3 | Chưa login → redirect `/login?returnUrl=...` |
| `AdminAuthorizationFilter` | `/admin`, `/admin/*` | 4 | Chưa login → `/login`; không phải `ADMIN` → HTTP `403` |
| Error Servlets / JSPs | `/403`, `/404`, `/500` | — | Forward `WEB-INF/views/error/{403,404,500}.jsp` |

**Exception Hierarchy (`com.bookhub.exception`):**
- `BusinessException` (validation / domain rule violation → hiển thị lỗi tại form/flash)
- `NotFoundException` → HTTP `404`
- `AccessDeniedException` → HTTP `403`
- `OutOfStockException` (extends `BusinessException`, kèm danh sách sách thiếu tồn kho)

## 6. Edge Cases & Xử lý lỗi
| ID | Tình huống | Hành vi mong đợi |
|---|---|---|
| EC-01 | Guest truy cập `/cart` kèm `returnUrl=https://evil.com` hoặc `//evil.com` | Bộ chuẩn hóa URL bỏ qua URL ngoài domain và chuyển về `/` sau login. |
| EC-02 | Người dùng mở form trước khi session hết hạn rồi bấm POST | `CsrfFilter` từ chối với `403` hoặc chuyển về trang hiện tại kèm thông báo phiên đã hết hạn; không thực hiện thay đổi DB. |
| EC-03 | Truy cập trực tiếp `/WEB-INF/views/site/home.jsp` từ trình duyệt | Tomcat tự chặn bằng `404` (render trang `404.jsp` thân thiện). |
| EC-04 | Ảnh bìa `cover_url` rỗng hoặc URL chết | Fragment `book-card.jsp` tự động hiển thị ảnh dự phòng `web/assets/images/book-placeholder.svg` kèm `alt` hợp lệ. |

## 7. Acceptance Criteria (Given / When / Then)
> Mỗi FR xuất hiện ở cột "Kiểm chứng FR" của ≥ 1 AC. Test gắn thẻ `@spec mvc-security-shell/AC-xx`.

| ID | Kịch bản | Kiểm chứng FR |
|---|---|---|
| AC-01 | **Given** một file JSP thử chèn `<% int x = 1; %>` **When** biên dịch/truy cập trên Tomcat 10.1 **Then** JSP compiler từ chối do `<scripting-invalid>true</scripting-invalid>` và quét toàn bộ JSP trong repo trả về `0` scriptlet (`TC-UI-01`). | FR-01 |
| AC-02 | **Given** request gửi dữ liệu tiếng Việt có dấu (vd `"Đắc Nhân Tâm"`) qua GET/POST **When** đi qua `EncodingFilter` **Then** Servlet đọc đúng chuỗi UTF-8 không lỗi font và response có `charset=UTF-8`. | FR-02 |
| AC-03 | **Given** Guest chưa đăng nhập **When** truy cập `GET /cart` hoặc `GET /account/orders` **Then** bị redirect sang `/login` với `returnUrl` nội bộ hợp lệ; nếu `returnUrl` trỏ domain ngoài sẽ bị loại bỏ. | FR-03, EC-01 |
| AC-04 | **Given** tài khoản `CUSTOMER` đã đăng nhập **When** gọi trực tiếp `GET /admin` hoặc `GET /admin/books` **Then** nhận HTTP `403` hiển thị trang lỗi `403.jsp` (`TC-ADM-01`). | FR-04 |
| AC-05 | **Given** form POST bất kỳ **When** gửi request thiếu `_csrf` hoặc sai `_csrf` **Then** `CsrfFilter` chặn với HTTP `403`; khi gửi đúng `_csrf` của session thì request đi tiếp tới Servlet. | FR-05, FR-06, EC-02 |
| AC-06 | **Given** mật khẩu `"BookHub123"` **When** băm qua `PasswordUtil.hash()` **Then** chuỗi hash bắt đầu bằng `$2a$`/`$2b$`, hai lần băm tạo 2 hash khác nhau (có salt), và `verify()` trả `true` cho mật khẩu đúng / `false` cho mật khẩu sai. | FR-07 |
| AC-07 | **Given** hệ thống chạy trên Tomcat 10.1 **When** kiểm tra 3 App Shells và trang lỗi `/403`, `/404`, `/500` ở viewport `375px` và `1280px` **Then** hiển thị đúng token `.sdd/design/tokens.css`, không cuộn ngang, không lộ stack trace, và `./scripts/ui-check.ps1` sạch cảnh báo hardcode màu. | FR-08, FR-09, EC-03, EC-04 |

## 8. Out of Scope (🚫 TUYỆT ĐỐI KHÔNG LÀM)
- Không triển khai nghiệp vụ đăng ký/đăng nhập/hồ sơ (thuộc `03-feat-auth-account`).
- Không triển khai truy vấn sách/danh mục thật (thuộc `04-feat-public-catalog`).
- Không đưa nút nổi `"Danh sách màn hình"` của prototype Figma vào giao diện thật.
- Không nhúng thư viện JS nặng (React/Vue/Chart.js/jQuery).

---
## Phụ lục A: Ghi chú thiết kế
Tier Full — thiết kế chi tiết sẽ lập tại `PLAN.md` sau Gate 1.

## Phụ lục B: Câu hỏi mở & Phê duyệt
- Không có câu hỏi mở — toàn bộ token và cấu trúc 3 App Shells đã chốt từ Figma (`DESIGN.md` `LOCKED`).

### Definition of Ready — chỉ khóa (LOCKED) khi đủ cả
- [x] Người duyệt xác nhận `SPEC.md` và `DESIGN.md` (Gate 1)
- [x] Mỗi FR có ≥ 1 AC; mỗi AC đo được (Given/When/Then)
- [x] Out of Scope cụ thể; `scripts/spec-check` không báo lỗi

- **Duyệt (Gate 1):** Owner (BookHub) · 2026-11-23 · Version khóa: 1.0.0
