# REVIEW — auth-account

> Review **độc lập** (agent/session khác, hoặc người) — prompt: `.sdd/prompts/06-review.md`. Reviewer **không sửa code**.
> Tier Full: bắt buộc `APPROVED` trước khi merge.

| Trường | Giá trị |
|---|---|
| Reviewer | Chờ Owner duyệt (bản nháp bằng chứng từ implementer) · Ngày: 2026-11-23 |
| Commit/nhánh | `feat/bookhub-mvp-01-08` (commit `cb6007c`) |
| **Kết luận** | `PENDING` (chờ Owner xác nhận `APPROVED`) |

## 1. Bằng chứng chạy thật
```text
$ ant clean dist test-junit5
- AuthAccountIntegrationTest: 4/4 OK (SQL Server 2022 BookHubDB)
  - registerNormalizeEmailDuplicateWeakPasswordAndLoginActiveOrLocked() [OK]
  - logoutInvalidatesSessionAndRedirectsHome() [OK]
  - forgotAndResetPasswordEnforceSha256OneTimeUseAndExpiry() [OK]
  - profileUpdateAllowListAndChangePasswordInvalidateOpenTokens() [OK]
- Live Tomcat 10.1: /register, /login, /forgot-password, /reset-password?token=invalid, login Customer & Admin + /account/profile → 200 OK
```

## 2. Truy vết
| Yêu cầu | Code | Test (`@spec`) | OK? |
|---|---|---|---|
| FR-01..04 | `AuthService`, `RegisterServlet`, `LoginServlet`, `auth/{register,login}.jsp` | `auth-account/AC-01..04` | ✅ |
| FR-05 | `LogoutServlet` (`session.invalidate()` + PRG) | `auth-account/AC-05` | ✅ |
| FR-06..08 | `PasswordResetTokenRepository`, `LocalLogResetLinkNotifier`, `ForgotPasswordServlet`, `ResetPasswordServlet` | `auth-account/AC-06..08` | ✅ |
| FR-09..11 | `UserService`, `UserProfileDTO`, `ProfileServlet`, `customer/profile.jsp` | `auth-account/AC-09..10` | ✅ |

## 3. Kiểm tra theo hiến pháp
- [x] Không vi phạm Out of Scope (không thêm OAuth, không thêm cột `default_address` vào `Users`)
- [x] Bảo mật (`H-01..06`): BCrypt hash, chống enumeration khi forgot-password, chỉ hiện thông báo `"Tài khoản của bạn đã bị khóa."` sau khi mật khẩu đúng (theo Gate 1), đổi session ID (`changeSessionId()`) khi đăng nhập, token reset hash SHA-256 dùng 1 lần (`markUsedAtomically`)
- [x] Không trả `passwordHash` hay Entity JPA ra View; mọi POST dùng PRG + CSRF

## 4. Phát hiện
| # | Mức | File:dòng | Vấn đề | Cách sửa đề xuất | Trạng thái |
|---|---|---|---|---|---|
| — | — | — | Không có phát hiện tồn đọng | — | — |

## 5. Phê duyệt cuối
_(Chờ Owner xác nhận `APPROVED` để chốt Gate 4)_
