# PROGRESS — Xác thực, Phân quyền & Quản lý Tài khoản (`03-feat-auth-account`)

| Trường | Giá trị |
|---|---|
| Feature | `03-feat-auth-account` · Tier: Full · Nhánh: `feat/bookhub-mvp-01-08` |
| Pha hiện tại | `done` (Owner đã duyệt `REVIEW.md` `APPROVED` — đóng đủ `T-001`..`T-006`) |
| Task đang làm | — (đã hoàn tất toàn bộ `T-001`..`T-006`) |
| Cập nhật | 2026-11-23 |

## 1. Plan của task hiện tại
- [x] `T-001`: `RoleRepository`, `PasswordResetTokenRepository` (`markUsedAtomically`, `invalidateOpenTokens`), mở rộng `UserRepository`
- [x] `T-002`: `AuthService`, `ResetLinkNotifier`, `LocalLogResetLinkNotifier` (BCrypt, email normalize, thông báo `LOCKED` chỉ sau đúng mật khẩu, SHA-256 token 15 phút dùng 1 lần)
- [x] `T-003`: `UserService` (`UserProfileDTO`, `updateProfile` allow-list `fullName`/`phone`, `changePassword` vô hiệu token cũ trong 1 TX)
- [x] `T-004`: `RegisterServlet`, `LoginServlet` (rotate `changeSessionId()` + nạp `cartCount`), `LogoutServlet` + `auth/{register,login}.jsp` (`SCR-04`, `SCR-05`)
- [x] `T-005`: `ForgotPasswordServlet`, `ResetPasswordServlet` + `auth/{forgot-password,reset-password}.jsp` (`SCR-06`, `SCR-07`)
- [x] `T-006`: `ProfileServlet` + `customer/profile.jsp` (`SCR-13`), nối Header menu & 4/4 integration tests xanh — Owner đã duyệt `REVIEW.md` (`APPROVED`)

## 2. Nhật ký thực thi
| Giờ | Việc đã làm | Kết quả |
|---|---|---|
| 2026-11-23 | Triển khai `T-001`..`T-006` và `AuthAccountIntegrationTest`, commit `cb6007c` | 4/4 integration tests PASS trên SQL Server 2022 (`@spec auth-account/AC-01..10`) |
| 2026-11-23 | Kiểm tra live Tomcat 10.1 | `/register`, `/login`, `/forgot-password`, `/reset-password?token=invalid`, login Customer & Admin + `/account/profile` đều hoạt động 200 OK |

## 3. Kiểm tra
Test ✅ (4/4 JUnit 5 `AuthAccountIntegrationTest`) · Build ✅ (`dist/BookHub.war`, 0 warning) · ui-check ✅ · spec-check ✅ · Secret scan ✅

## 4. Blockers / câu hỏi cho người
- ⬜ Chờ Owner review & duyệt `.sdd/specs/03-feat-auth-account/REVIEW.md` (Gate 4).

## 5. Quyết định & giả định
- Giữ đúng quyết định Gate 1: thông báo `"Tài khoản của bạn đã bị khóa."` chỉ hiện sau khi xác minh đúng mật khẩu BCrypt; không thêm cột `default_address` vào `dbo.Users`.
- `LocalLogResetLinkNotifier` ghi link reset ra server log phục vụ demo local PRJ301 (ADR-009).

## 6. Handoff
- **Đã xong:** Đầy đủ luồng Auth & Profile (`RT-004`..`RT-008`, `RT-015`).
- **Làm tiếp:** Sau khi Owner duyệt `REVIEW.md` → đánh `APPROVED` và đóng `T-006`.
