# SPEC — Xác thực, Phân quyền & Quản lý Tài khoản (auth-account)

> **Pha 1 · Hợp đồng đặc tả.** Agent soạn nháp từ `CONTEXT.md`, **người duyệt & khóa** (Gate 1).
> Sau khi `Status: LOCKED`, sửa đổi chỉ qua `CHANGELOG.md` + người duyệt. **Chưa LOCKED ⇒ cấm code.**

| Trường | Giá trị |
|---|---|
| Feature ID | `03-feat-auth-account` |
| Tier | Full |
| UI | Có |
| Status | `LOCKED` |
| Version | 1.0.0 |
| Owner | BookHub Team (PRJ301) |
| Liên quan | `FR-AUTH-001..016`, `BR-ACC-001..008`, `shared_context.md` (`RT-004..008`, `RT-015`), `ADR-009`, `TC-AUTH-01..11` |

---

## 1. Tổng quan (Overview)
Cung cấp đầy đủ luồng đăng ký Customer, đăng nhập/đăng xuất với phân quyền `ADMIN`/`CUSTOMER`, quên và đặt lại mật khẩu bằng token dùng 1 lần có thời hạn 15 phút, xem/cập nhật hồ sơ cá nhân và đổi mật khẩu khi đã đăng nhập. Mọi quy tắc bảo mật (chuẩn hóa email, băm BCrypt, đổi session ID, thông báo không dò email, kiểm tra trạng thái `LOCKED`/`DISABLED`) được cưỡng chế tại tầng Service. Xem `CONTEXT.md`.

## 2. Actors & User Stories
| ID | Câu chuyện |
|---|---|
| US-01 | Là **Guest**, tôi muốn đăng ký tài khoản bằng họ tên, email, mật khẩu để sử dụng giỏ hàng và đặt mua sách. |
| US-02 | Là **Customer/Admin**, tôi muốn đăng nhập và đăng xuất an toàn để truy cập đúng khu vực quyền hạn của mình. |
| US-03 | Là **Guest quên mật khẩu**, tôi muốn yêu cầu liên kết đặt lại mật khẩu có thời hạn và đặt mật khẩu mới an toàn. |
| US-04 | Là **Customer**, tôi muốn cập nhật họ tên, số điện thoại và đổi mật khẩu cá nhân mà không ảnh hưởng quyền tài khoản. |

## 3. Functional Requirements (EARS)
| ID | Loại | Mệnh đề | Liên kết (US / BR) |
|---|---|---|---|
| FR-01 | Event | WHEN a Guest submits `POST /register` with valid `fullName`, unique normalized `email` (`trim` + `lowercase`), optional `phone`, and matching `password`/`confirmPassword` (≥ 8 chars, containing at least 1 letter and 1 digit), the system SHALL hash the password with BCrypt, create a `Users` record with role `CUSTOMER` and status `ACTIVE`, and redirect via PRG to `/login` with a success flash message. | US-01, FR-AUTH-001..004, BR-ACC-001..004 |
| FR-02 | Unwanted | IF `POST /register` receives an already registered email (case/whitespace-insensitive), weak password, mismatched confirmation, or blank `fullName`, THEN the system SHALL reject registration, preserve valid non-sensitive fields, and display inline field validation errors without creating a user record. | US-01, FR-AUTH-002..003 |
| FR-03 | Event | WHEN a user submits `POST /login` with matching normalized email, valid BCrypt password, and account status `ACTIVE`, the system SHALL rotate the HTTP session ID (`changeSessionId`), store authenticated user identity (`userId`, `fullName`, `email`, `roleCode` — never the JPA entity or `passwordHash`) in session, and redirect Admins to `/admin` or Customers to a validated internal `returnUrl` (default `/`). | US-02, FR-AUTH-005, FR-AUTH-011, FR-AUTH-015 |
| FR-04 | Unwanted | IF `POST /login` receives a non-existent email or wrong password, THEN the system SHALL refuse authentication with the generic message `"Email hoặc mật khẩu không đúng."`; IF credentials match an account whose status is `LOCKED` or `DISABLED`, THEN the system SHALL refuse login with `"Tài khoản của bạn đã bị khóa."`. | US-02, FR-AUTH-005, FR-AUTH-012, BR-ACC-006 |
| FR-05 | Event | WHEN an authenticated user submits `POST /logout` with a valid CSRF token, the system SHALL invalidate the server-side `HttpSession` and redirect via PRG to `/`. | US-02, FR-AUTH-006 |
| FR-06 | Event | WHEN a Guest submits `POST /forgot-password` with a valid email format, the system SHALL always display the same neutral confirmation message; if an `ACTIVE` user exists for that email, the system SHALL generate a cryptographically random opaque token, store only its SHA-256 `token_hash` in `PasswordResetTokens` with `expires_at = now + 15 minutes`, and output the local demo reset URL via the pluggable reset-notification handler (ADR-009). | US-03, FR-AUTH-007..008, BR-ACC-007 |
| FR-07 | Event | WHEN a user submits `POST /reset-password` with an unconsumed, non-expired token and valid matching new password, the system SHALL update `Users.password_hash` with a new BCrypt hash, mark `used_at = now`, invalidate all other open reset tokens for that user within a single JPA transaction, and redirect to `/login`. | US-03, FR-AUTH-008..009, BR-ACC-008 |
| FR-08 | Unwanted | IF `GET/POST /reset-password` receives a missing, invalid, expired (`> 15 min`), or already used (`used_at != NULL`) token, THEN the system SHALL refuse to change the password and render the invalid/expired token state with a CTA to `/forgot-password`. | US-03, FR-AUTH-008 |
| FR-09 | Event | WHEN an authenticated user views or submits `POST /account/profile` to update profile info, the system SHALL update ONLY allow-listed fields (`full_name`, `phone`) for `currentUser.id`, ignoring any tampered `email`, `role_id`, `status`, or `user_id` parameters. | US-04, FR-AUTH-010, BR-ACC-005 |
| FR-10 | Event | WHEN an authenticated user submits `POST /account/profile` (change-password action) with the correct current password and valid matching new password, the system SHALL verify the current BCrypt hash, persist the new BCrypt hash, invalidate any pending `PasswordResetTokens` for that user in one transaction, and display a confirmation message via PRG. | US-04, FR-AUTH-016 |
| FR-11 | Unwanted | IF a logged-in user submits an incorrect current password or invalid new password when changing password, THEN the system SHALL reject the change, keep the existing password hash unchanged, and display an inline error. | US-04, FR-AUTH-016 |

## 4. Non-Functional Requirements
| ID | Hạng mục | Yêu cầu (số cụ thể) |
|---|---|---|
| NFR-01 | Bảo mật mật khẩu | BCrypt salt ngẫu nhiên; không bao giờ in password, password hash hay session ID ra log. |
| NFR-02 | Chống leo thang & IDOR | `userId` cho Profile/Change Password luôn lấy từ `HttpSession`, không đọc từ request param; không tồn tại endpoint public tạo role `ADMIN`. |
| NFR-03 | Phiên & Cookie | Đổi session ID ngay sau login; cookie phiên đặt `HttpOnly`; mọi `POST` đều đi qua `CsrfFilter`. |
| NFR-04 | Trải nghiệm & UI | Khớp Figma `SCR-04`..`07`, `SCR-13`; có nút hiện/ẩn mật khẩu, nhãn `<label for>` tường minh, giữ trường hợp lệ khi lỗi validation. |

## 5. Data & API Contract
**Bảng sử dụng:** `Roles`, `Users`, `PasswordResetTokens` (định nghĩa ở `01-feat-foundation-jpa`).

**Servlet Routes (`shared_context.md` `RT-004..008`, `RT-015`):**
| Method + Path | Auth | Request Params | Response / Redirect | Lỗi |
|---|---|---|---|---|
| `GET /register` | Guest | — | Render `WEB-INF/views/auth/register.jsp` | Đã login → redirect `/` |
| `POST /register` | Guest + CSRF | `fullName`, `email`, `phone?`, `password`, `confirmPassword` | `302 /login` + flash success | Forward `register.jsp` + `errors` map |
| `GET /login` | Guest | `returnUrl?` | Render `WEB-INF/views/auth/login.jsp` | Đã login → redirect `/` hoặc `/admin` |
| `POST /login` | Guest + CSRF | `email`, `password`, `returnUrl?` | `302` tới `returnUrl` / `/` / `/admin` | Forward `login.jsp` + lỗi chung |
| `POST /logout` | Auth + CSRF | — | Invalidate session → `302 /` | `403` nếu thiếu CSRF |
| `GET/POST /forgot-password` | Guest + CSRF | `email` | Render/PRG thông báo trung tính | Forward kèm lỗi format email |
| `GET/POST /reset-password` | Guest + CSRF | `token`, `newPassword`, `confirmPassword` | `302 /login` + flash success | State token hết hạn / lỗi validation |
| `GET/POST /account/profile` | Auth + CSRF | `action=updateProfile\|changePassword`, fields tương ứng | PRG `302 /account/profile` + flash | Forward `profile.jsp` + lỗi inline |

## 6. Edge Cases & Xử lý lỗi
| ID | Tình huống | Hành vi mong đợi |
|---|---|---|
| EC-01 | Email đăng ký có khoảng trắng đầu/cuối và chữ hoa (`"  An.Nguyen@Email.COM "`) | Chuẩn hóa thành `"an.nguyen@email.com"` trước khi kiểm tra trùng và lưu DB (`TC-AUTH-03`). |
| EC-02 | Customer gửi POST `/account/profile` chèn thêm `roleId=1&status=ACTIVE&email=hack@x.com` | Server bỏ qua hoàn toàn mọi field ngoài allow-list (`fullName`, `phone`). |
| EC-03 | Hai request sử dụng cùng một reset token gần như đồng thời | Transaction kiểm tra `used_at IS NULL` và cập nhật nguyên tử; request thứ hai bị từ chối. |
| EC-04 | Người dùng đang login bấm mở `/login` hoặc `/register` | Tự động chuyển hướng về `/` (Customer) hoặc `/admin` (Admin). |

## 7. Acceptance Criteria (Given / When / Then)
> Mỗi FR xuất hiện ở cột "Kiểm chứng FR" của ≥ 1 AC. Test gắn thẻ `@spec auth-account/AC-xx`.

| ID | Kịch bản | Kiểm chứng FR |
|---|---|---|
| AC-01 | **Given** Guest nhập dữ liệu đăng ký hợp lệ với email `"  User01@Test.com "` **When** `POST /register` **Then** DB tạo user `CUSTOMER`/`ACTIVE` với email `"user01@test.com"` và `password_hash` chuẩn BCrypt (`TC-AUTH-01`, `TC-AUTH-03`). | FR-01, EC-01 |
| AC-02 | **Given** email đã tồn tại hoặc mật khẩu `"1234567"` (< 8 ký tự / thiếu chữ) **When** `POST /register` **Then** không tạo bản ghi mới và trả lỗi inline đúng field (`TC-AUTH-02`, `TC-AUTH-04`). | FR-02 |
| AC-03 | **Given** user `ACTIVE` tồn tại **When** `POST /login` đúng email/mật khẩu **Then** session ID thay đổi, session lưu thông tin user (không lưu `passwordHash`), Customer về `/` (hoặc `returnUrl` nội bộ), Admin về `/admin` (`TC-AUTH-05`). | FR-03 |
| AC-04 | **Given** sai mật khẩu, email không tồn tại, hoặc user ở trạng thái `LOCKED`/`DISABLED` **When** `POST /login` **Then** từ chối đăng nhập, không tạo phiên xác thực và không tiết lộ tài khoản tồn tại (`TC-AUTH-06`, `TC-AUTH-07`). | FR-04 |
| AC-05 | **Given** user đã đăng nhập **When** `POST /logout` hợp lệ **Then** session bị hủy và truy cập tiếp `/account/profile` bị chuyển về `/login` (`TC-AUTH-08`). | FR-05 |
| AC-06 | **Given** Guest gửi `POST /forgot-password` với email đã đăng ký hoặc chưa đăng ký **When** xử lý xong **Then** UI luôn trả thông báo trung tính giống nhau; với email `ACTIVE`, DB tạo bản ghi `PasswordResetTokens` chỉ lưu `token_hash` có `expires_at = now + 15 phút`. | FR-06 |
| AC-07 | **Given** token reset hợp lệ trong 15 phút **When** `POST /reset-password` với mật khẩu mới hợp lệ **Then** cập nhật BCrypt hash mới, ghi `used_at`, vô hiệu token chờ khác và đăng nhập được bằng mật khẩu mới (`TC-AUTH-09`). | FR-07 |
| AC-08 | **Given** token sai, đã quá 15 phút, hoặc đã sử dụng **When** truy cập/submit `/reset-password` **Then** mật khẩu không đổi và hiển thị trạng thái token không hợp lệ kèm nút yêu cầu mới (`TC-AUTH-10`). | FR-08, EC-03 |
| AC-09 | **Given** Customer đã đăng nhập **When** `POST /account/profile` sửa `fullName`/`phone` đồng thời chèn thêm `roleId=1` & `email=other@x.com` **Then** chỉ `full_name` & `phone` thay đổi, `role_id` và `email` giữ nguyên. | FR-09, EC-02 |
| AC-10 | **Given** Customer đã đăng nhập **When** đổi mật khẩu với mật khẩu hiện tại đúng và mật khẩu mới hợp lệ **Then** lưu hash mới và vô hiệu các reset token đang chờ; nếu nhập sai mật khẩu hiện tại thì từ chối (`TC-AUTH-11`). | FR-10, FR-11 |

## 8. Out of Scope (🚫 TUYỆT ĐỐI KHÔNG LÀM)
- Không tích hợp đăng nhập Google/Facebook (OAuth2/SSO).
- Không triển khai chức năng "Remember Me" persistent token ngoài session chuẩn.
- Không triển khai khóa/mở khóa tài khoản từ trang Admin (thuộc `08-feat-admin-users-reports`).
- Không gửi email marketing hay tích hợp dịch vụ SMTP sản xuất bắt buộc (ADR-009).

---
## Phụ lục A: Ghi chú thiết kế
Tier Full — thiết kế chi tiết lập tại `PLAN.md` sau Gate 1.

## Phụ lục B: Câu hỏi mở & Phê duyệt
- [x] Q1: `SCR-13` (Profile) chỉ cập nhật `full_name` và `phone` (bỏ ô `Địa chỉ mặc định` từ Figma) để khớp `01_schema.sql` — _người trả lời:_ Đã chốt (2026-11-23).
- [x] Q2: User `LOCKED`/`DISABLED` hiện `"Tài khoản của bạn đã bị khóa."` chỉ sau khi xác minh đúng mật khẩu (sai mật khẩu/email không tồn tại vẫn hiện câu chung `"Email hoặc mật khẩu không đúng."`) — _người trả lời:_ Đã chốt (2026-11-23).

### Definition of Ready — chỉ khóa (LOCKED) khi đủ cả
- [x] Giải quyết xong Q1, Q2 ở Phụ lục B
- [x] Mỗi FR có ≥ 1 AC; `DESIGN.md` đồng duyệt cùng Gate 1
- [x] Out of Scope cụ thể; `scripts/spec-check` không báo lỗi

- **Duyệt (Gate 1):** Owner (BookHub) · 2026-11-23 · Version khóa: 1.0.0
