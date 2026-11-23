# TASKS — Xác thực, Phân quyền & Quản lý Tài khoản (`03-feat-auth-account`)

> **Pha 3 · Phân rã từ `PLAN.md`.** Task atomic ≤ 4h · gắn thẻ `@spec auth-account/AC-xx`.

**Trạng thái:** `⬜ todo` · `🔄 doing` · `✅ done` · `⛔ blocked`

| ID | Task | Phụ thuộc | Ước lượng | Truy vết | Trạng thái |
|---|---|---|---|---|---|
| T-001 | Tạo `RoleRepository`, `PasswordResetTokenRepository`, mở rộng `UserRepository` | — | 2h | FR-01..08 | ✅ |
| T-002 | Tạo `AuthService` (register, authenticate, forgot/reset token) + `ResetLinkNotifier` + tests | T-001 | 3.5h | FR-01..08, EC-01, EC-03, AC-01..08 | ✅ |
| T-003 | Tạo `UserService` (updateProfile allow-list, changePassword + invalidate tokens) + tests | T-001 | 2h | FR-09..11, EC-02, AC-09..10 | ✅ |
| T-004 | Tạo `RegisterServlet`, `LoginServlet`, `LogoutServlet` + `register.jsp`, `login.jsp` (`SCR-04`, `SCR-05`) | T-002 | 3h | FR-01..05, EC-04, AC-01..05 | ✅ |
| T-005 | Tạo `ForgotPasswordServlet`, `ResetPasswordServlet` + `forgot-password.jsp`, `reset-password.jsp` (`SCR-06`, `SCR-07`) | T-002 | 2.5h | FR-06..08, AC-06..08 | ✅ |
| T-006 | Tạo `ProfileServlet` + `profile.jsp` (`SCR-13`), nối Header menu & kiểm tra trực quan (Gate 4 `APPROVED`) | T-003, T-004 | 2.5h | FR-09..11, AC-09..10, VAC-01..02 | ✅ |

## Chi tiết task

### T-001 — Repositories cho Auth & Reset Token
- **Mục tiêu:** Viết truy vấn JPA parameter-bound cho `Users` (findByEmail, existsByEmail), `Roles` (findByCode), và `PasswordResetTokens` (findByTokenHash, consumeAtomically, invalidateAllOpenForUser).
- **File dự kiến:** `src/java/com/bookhub/repository/{RoleRepository,PasswordResetTokenRepository,UserRepository}.java`.
- **Acceptance:**
  - [ ] 100% JPQL dùng named parameters; không dùng JDBC thuần.
- **Không làm:** Không đặt business validation trong Repository.

### T-002 — `AuthService` & Bộ Integration Test Auth
- **Mục tiêu:** Triển khai toàn bộ nghiệp vụ đăng ký (trim/lowercase, BCrypt, role `CUSTOMER`), xác thực (`ACTIVE` vs `LOCKED` sau đúng pass), và phát hành/xác minh reset token 15 phút dùng 1 lần.
- **File dự kiến:** `src/java/com/bookhub/service/{AuthService,ResetLinkNotifier,LocalLogResetLinkNotifier}.java`, `tests/integration/AuthServiceIntegrationTest.java`.
- **Acceptance:**
  - [ ] Pass test `@spec auth-account/AC-01`..`AC-08` (gồm `TC-AUTH-01`..`10`).
- **Không làm:** Không trả JPA `User` entity chứa `passwordHash` ra Controller.

### T-003 — `UserService` (Profile & Change Password)
- **Mục tiêu:** Cập nhật chỉ `full_name`/`phone` theo `currentUserId` từ session và đổi mật khẩu kèm vô hiệu reset token cũ trong 1 transaction.
- **File dự kiến:** `src/java/com/bookhub/service/UserService.java`, `tests/integration/UserServiceIntegrationTest.java`.
- **Acceptance:**
  - [ ] Pass test `@spec auth-account/AC-09`, `AC-10` (`TC-AUTH-11`).
- **Không làm:** Không cho sửa `email`, `role_id`, `status` hay lưu `default_address`.

### T-004, T-005, T-006 — Servlets & JSP Views (`SCR-04..07`, `SCR-13`)
- **Mục tiêu:** Xây dựng 6 Servlet controller mỏng (PRG + `changeSessionId()` ở login) và 5 trang JSP chuẩn JSTL/EL khớp Figma, `noindex`, đủ nút hiện/ẩn mật khẩu và lỗi inline.
- **File dự kiến:** `src/java/com/bookhub/controller/site/{Register,Login,Logout,ForgotPassword,ResetPassword}Servlet.java`, `controller/customer/ProfileServlet.java`, `web/WEB-INF/views/auth/*.jsp`, `web/WEB-INF/views/customer/profile.jsp`.
- **Acceptance:**
  - [ ] Ảnh chụp 375/1280 khớp `scr-05__login__1280.png` (`VAC-01..02`); `scripts/verify.ps1` xanh.
- **Không làm:** Không thêm nút đăng nhập Google/Facebook hay Remember Me.

## Đồ thị phụ thuộc
```text
T-001 ─┬─► T-002 ─┬─► T-004 ─┬─► T-006
       │          └─► T-005  │
       └─► T-003 ────────────┘
```

## Phê duyệt (Gate 3)
Owner (BookHub) · 2026-11-23
