# DESIGN — Xác thực, Phân quyền & Quản lý Tài khoản (auth-account)

> **Bản thiết kế gọn cho feature có UI.** Mặc định màu/font/form/a11y tuân theo `.sdd/design/DESIGN.md` & `tokens.css`.

| Trường | Giá trị |
|---|---|
| Feature | `03-feat-auth-account` |
| Status | `LOCKED` |
| Tham chiếu Stitch/Figma | `https://agenda-kale-62994833.figma.site/#/login` (`SCR-04`..`07`, `SCR-13`) · `.sdd/design/references/baseline/scr-05__login__1280.png` |

## 1. Màn hình & Luồng đi
| ID | Màn hình / View | Vào từ đâu | Mục đích |
|---|---|---|---|
| SCR-04 | Register (`views/auth/register.jsp`) | Link từ `SCR-05` / Header | Tạo tài khoản Customer |
| SCR-05 | Login (`views/auth/login.jsp`) | Nút `Đăng nhập` ở Header / redirect từ trang bảo vệ | Đăng nhập Customer hoặc Admin |
| SCR-06 | Forgot Password (`views/auth/forgot-password.jsp`) | Link `Quên mật khẩu?` ở `SCR-05` | Gửi yêu cầu liên kết đặt lại mật khẩu |
| SCR-07 | Reset Password (`views/auth/reset-password.jsp`) | Link `/reset-password?token=...` | Đặt mật khẩu mới |
| SCR-13 | My Profile (`views/customer/profile.jsp`) | Menu tài khoản ở Header (`MA Minh Anh`) | Sửa hồ sơ cá nhân & đổi mật khẩu |

```text
SCR-05 Login ──► SCR-04 Register ──(PRG)──► SCR-05 Login (flash xanh)
             ├──► SCR-06 Forgot ──► Alert xanh trung tính ──► SCR-07 Reset ──► SCR-05 Login
             └──(Thành công)──► Home / returnUrl / Admin Dashboard
Header tài khoản ──► SCR-13 Profile (2 card: Thông tin cá nhân + Đổi mật khẩu)
```

## 2. Component & Token dùng
- **Dùng lại từ `components.md`:** `Auth Shell`, `StorefrontHeader`/`Footer` (cho `SCR-13`), `FormControl` (kèm toggle hiện/ẩn mật khẩu), `Button (primary-teal)`, `AlertBanner` (`error`, `success`, `warning`).
- **Component mới:** Không có.
- **Token mới ngoài `tokens.css`?** Không.

## 3. Trạng thái, Responsive & Copy (chỉ ghi điểm riêng)
- **Trạng thái:**
  - **Loading:** Nút submit (`Đăng nhập`, `Đăng ký`, `Lưu thay đổi`) disable khi đang gửi.
  - **Error:** `SCR-05` dùng `AlertBanner` đỏ (`"Email hoặc mật khẩu không đúng."`); `SCR-04`/`SCR-13` hiện lỗi inline dưới từng field; `SCR-07` có màn hình cảnh báo token hết hạn + nút `"Yêu cầu liên kết mới"`.
  - **Disabled:** Ô `Email` và `Vai trò` trong `SCR-13` hiển thị ở trạng thái `disabled` nền `--color-surface-subtle`.
- **Responsive (375 vs 1280):** Card Auth rộng tối đa `440px` căn giữa ở 1280px, full-width `px-4` ở 375px; `SCR-13` xếp 2 card theo cột dọc dễ đọc.
- **Copy / i18n chính:** `"Đăng ký tài khoản"`, `"Tối thiểu 8 ký tự, gồm cả chữ và số"`, `"Nếu email tồn tại trong hệ thống, liên kết đặt lại mật khẩu có hiệu lực trong 15 phút đã được tạo."`, `"Lưu thay đổi"`, `"Cập nhật mật khẩu"`.
- **a11y cần lưu ý:** Mọi input có `<label for>`, `aria-invalid="true"` khi lỗi, nút hiện/ẩn mật khẩu có `aria-label`.
- **SEO/AEO/GEO:** Toàn bộ 5 màn hình `SCR-04`..`07`, `SCR-13` đặt `<meta name="robots" content="noindex, nofollow">`.

## 4. Tiêu chí nghiệm thu bằng mắt (VAC) & Duyệt
- [ ] **VAC-01:** `SCR-05` khớp `scr-05__login__1280.png` (không có nút OAuth Google/Facebook), không cuộn ngang ở 375px.
- [ ] **VAC-02:** Hiển thị đủ lỗi validation inline, thông báo trung tính Forgot Password, trạng thái token hết hạn ở Reset Password và flash thành công ở Profile.
- **Duyệt (cùng Gate 1):** Owner (BookHub) · 2026-11-23
