# CONTEXT — Xác thực, Phân quyền & Quản lý Tài khoản (auth-account)

> **Pha 0 · Bối cảnh tính năng.** Nguồn chuẩn: `BookHub_PRJ301_Project_Plan_Requirements.md` (§6, §8.1, §10.1–10.2, §18.1, PRJ301 mục 3.1 — 1.0 điểm).
> Trạng thái: `ready`

## 1. Bối cảnh & Pain point
- Khách truy cập cần tạo tài khoản và đăng nhập để dùng giỏ hàng, đặt sách COD, theo dõi đơn, viết đánh giá và đặt câu hỏi; quản trị viên cần đăng nhập an toàn vào khu vực `/admin`.
- Module Authentication chiếm **1.0 điểm** riêng trong rubric PRJ301 (gồm Register, Login, Logout, Forgot/Reset Password, cập nhật hồ sơ, đổi mật khẩu và phân quyền `ADMIN`/`CUSTOMER`).

## 2. Mục tiêu nghiệp vụ & thước đo thành công
| Mục tiêu | Chỉ số (đo được) | Hiện tại → Mục tiêu |
|---|---|---|
| Đầy đủ luồng tài khoản PRJ301 | Pass 11/11 test case `TC-AUTH-01`..`TC-AUTH-11` | Chưa có auth → đủ Register, Login, Logout, Forgot/Reset, Profile, Change Password |
| Bảo mật mật khẩu & phiên | 0 plaintext password; session ID đổi sau login; token reset 15 phút dùng 1 lần | Đạt 100% tiêu chí `NFR-SEC-001`, `FR-AUTH-001..016` |
| Chống leo thang đặc quyền | Không sửa được `role_id`/`status` qua form; không đăng ký Admin public | Bảo vệ tại Service & Filter |

## 3. Người dùng / Tác nhân
| Actor | Muốn gì | Ghi chú |
|---|---|---|
| Guest | Đăng ký tài khoản Customer, đăng nhập hoặc khôi phục mật khẩu khi quên | Không có OAuth Google/Facebook trong MVP |
| Customer | Cập nhật họ tên, số điện thoại và đổi mật khẩu; đăng xuất an toàn | Không được tự đổi email/role/status |
| Admin | Đăng nhập vào `/admin`, cập nhật hồ sơ cá nhân, đăng xuất | Tài khoản Admin chỉ tạo qua seed/tool, không đăng ký public |

## 4. Thuật ngữ nghiệp vụ riêng của feature
| Thuật ngữ | Định nghĩa |
|---|---|
| `Reset Token` | Chuỗi ngẫu nhiên opaque gửi qua URL `/reset-password?token=...`; DB chỉ lưu `token_hash`, hiệu lực 15 phút, dùng đúng 1 lần |
| `Session Rotation` | Gọi `request.changeSessionId()` (hoặc tạo session mới) ngay sau khi đăng nhập thành công để chống session fixation |
| `Non-enumerating response` | Phản hồi trung tính ở Login và Forgot Password nhằm không tiết lộ email có tồn tại trong hệ thống hay không |

## 5. Ràng buộc cố định (không thương lượng)
- Email chuẩn hóa `trim()` + `lowercase`, duy nhất (`UQ_Users_email`).
- Mật khẩu tối thiểu 8 ký tự, bắt buộc có cả chữ và số; băm bằng BCrypt.
- Local demo ghi reset password URL ra server log/NetBeans Output (ADR-009), không phụ thuộc cứng vào SMTP ngoài.

## 6. Tham chiếu
- Figma: `https://agenda-kale-62994833.figma.site/#/login` (`SCR-04`..`07`, `SCR-13`) · Baseline: `.sdd/design/references/baseline/scr-05__login__1280.png` · UI Spec: `docs/ui-screens.md` · Business rules: `.sdd/constraints/business.md` (`BR-ACC-*`).

## 7. Giả định & câu hỏi mở
- Câu hỏi mở về ô `Địa chỉ mặc định` trên `SCR-13` (đồng bộ với `01-feat-foundation-jpa` Q2) được ghi nhận ở Phụ lục B của `SPEC.md`.
