# PLAN — Xác thực, Phân quyền & Quản lý Tài khoản (`03-feat-auth-account`)

> **Pha 2 · Thiết kế kiến trúc** sau khi SPEC & DESIGN v1.0.0 đã `LOCKED`. Duyệt (Gate 2) cùng `TASKS.md`.

| Trường | Giá trị |
|---|---|
| SPEC version | 1.0.0 |
| Status | `DRAFT` |

## 1. Tóm tắt giải pháp
Tách rõ `AuthService` (đăng ký, đăng nhập, quên/đặt lại mật khẩu) và `UserService` (xem/sửa hồ sơ `full_name`/`phone`, đổi mật khẩu), đi qua `UserRepository` & `PasswordResetTokenRepository` (JPA). Mọi thao tác mật khẩu dùng `PasswordUtil` (BCrypt); token reset là 32 byte ngẫu nhiên (Base64URL), chỉ lưu SHA-256 `token_hash` với TTL 15 phút, và xuất URL qua interface `ResetLinkNotifier` (mặc định `LocalLogResetLinkNotifier` theo `ADR-009`). Sau đăng nhập thành công luôn gọi `request.changeSessionId()` và lưu `SessionUser` bất biến.

## 2. Phương án đã cân nhắc
| Phương án | Ưu | Nhược | Chọn? |
|---|---|---|---|
| A: Tách `AuthService` + `UserService` + `ResetLinkNotifier` interface + 6 Servlet chuyên biệt | Mỗi Servlet ngắn (< 120 dòng, tuân S-04), dễ unit test Service độc lập, dễ cắm SMTP sau | Nhiều file Servlet hơn | ✅ |
| B: Gom toàn bộ auth vào 1 `AuthServlet` khổng lồ dùng `action=...` | Ít file | Dễ phình > 400 dòng, khó bảo trì URL sạch `/login`, `/register`, `/reset-password` | ❌ |

## 3. Kiến trúc & luồng xử lý
```text
POST /register → RegisterServlet (validate input) → AuthService.register() [normalize email, check unique, BCrypt, role CUSTOMER] → PRG /login
POST /login → LoginServlet → AuthService.authenticate() [BCrypt check → status ACTIVE check] → changeSessionId() → session.setAttribute(AUTH_USER) → /admin | returnUrl | /
POST /forgot-password → ForgotPasswordServlet → AuthService.requestPasswordReset() [random 32B token → SHA-256 DB + 15m TTL → LocalLogResetLinkNotifier] → neutral alert
POST /reset-password → ResetPasswordServlet → AuthService.resetPassword() [1 TX: lock/verify token unused & unexpired → update BCrypt → mark used_at + invalidate open tokens]
POST /account/profile → ProfileServlet → UserService.updateProfile() [chỉ full_name, phone] | changePassword() [verify old → new hash + invalidate reset tokens]
```

## 4. Thay đổi theo tầng
| Tầng | File tạo mới | File sửa | Ghi chú |
|---|---|---|---|
| `repository/` | `PasswordResetTokenRepository`, `RoleRepository` | `UserRepository` (thêm `findByEmail`, `existsByEmail`) | Named parameters 100% |
| `service/` | `AuthService`, `UserService`, `ResetLinkNotifier`, `LocalLogResetLinkNotifier` | — | Quản lý transaction qua `JpaUtil` |
| `controller/` | `site/{Register,Login,Logout,ForgotPassword,ResetPassword}Servlet`, `customer/ProfileServlet` | — | PRG sau mọi POST thành công |
| `web/views/` | `auth/{register,login,forgot-password,reset-password}.jsp`, `customer/profile.jsp` | `common/header.jsp` (nối link đăng nhập/đăng xuất/tài khoản) | Khớp `SCR-04..07`, `SCR-13` (không ô địa chỉ, không OAuth) |
| `tests/` | `tests/unit/AuthValidationTest.java`, `tests/integration/AuthServiceIntegrationTest.java` | — | Gắn `@spec auth-account/AC-01..10` |

## 5. Data model & Migration
- Sử dụng nguyên vẹn 3 bảng `Roles`, `Users`, `PasswordResetTokens` từ `01-feat-foundation-jpa` (không đổi schema — đã khóa bỏ `default_address`).

## 6. API / Contract & ảnh hưởng chéo
- Hiện thực hóa `RT-004`..`RT-008` và `RT-015` trong `.sdd/shared_context.md`; cập nhật trạng thái route `planned → implemented` khi hoàn tất.

## 7. Truy vết SPEC → thiết kế
| Yêu cầu | Thành phần đáp ứng | Test dự kiến |
|---|---|---|
| FR-01, FR-02, EC-01 | `RegisterServlet` + `AuthService.register()` | AC-01, AC-02 |
| FR-03, FR-04, EC-04 | `LoginServlet` + `AuthService.authenticate()` | AC-03, AC-04 |
| FR-05 | `LogoutServlet` (`session.invalidate()`) | AC-05 |
| FR-06 | `ForgotPasswordServlet` + `AuthService.requestPasswordReset()` + `ResetLinkNotifier` | AC-06 |
| FR-07, FR-08, EC-03 | `ResetPasswordServlet` + `AuthService.resetPassword()` (lock token row) | AC-07, AC-08 |
| FR-09, EC-02 | `ProfileServlet` + `UserService.updateProfile()` (field allow-list) | AC-09 |
| FR-10, FR-11 | `ProfileServlet` + `UserService.changePassword()` | AC-10 |

## 8. Chiến lược kiểm thử
- **Unit (JUnit 5):** Validate email normalization, password policy (≥ 8 chars, chữ + số), allow-list profile.
- **Integration (SQL Server test DB):** Đủ 11 kịch bản `TC-AUTH-01`..`TC-AUTH-11`, bao gồm race condition 2 request dùng cùng 1 reset token, user `LOCKED` nhập đúng mật khẩu vs sai mật khẩu.
- **Manual / Visual:** Kiểm 5 màn hình `SCR-04..07`, `SCR-13` ở 375px & 1280px so với `scr-05__login__1280.png`.

## 9. Rủi ro & giảm thiểu
| # | Rủi ro | Xác suất | Tác động | Giảm thiểu / Rollback |
|---|---|---|---|---|
| R1 | Lộ email tồn tại qua thời gian phản hồi hoặc câu báo lỗi user `LOCKED` | Trung bình | Trung bình | Chỉ báo `"Tài khoản của bạn đã bị khóa."` sau khi BCrypt verify đúng mật khẩu (quyết định Gate 1 Q2) |
| R2 | Hai request dùng trùng 1 reset token đồng thời | Thấp | Cao | Dùng conditional update `WHERE reset_id = :id AND used_at IS NULL AND expires_at > :now` kiểm tra `updatedRows == 1` |

## 10. Rollout & Quan sát
- Log `INFO` đăng nhập/đăng ký thành công (chỉ `userId`, không log email/password/sessionId); `LocalLogResetLinkNotifier` chỉ bật ở chế độ local demo (`ADR-009`).

## 11. ❓ Câu hỏi cho con người
- Không còn câu hỏi mở (Q1, Q2 đã khóa ở Gate 1).

## 12. Phê duyệt (Gate 2)
(chờ duyệt) · —
