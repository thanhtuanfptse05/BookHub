# ADR-009: Ghi Reset Password URL ra Server Log cho Local Demo

| Trường | Giá trị |
|---|---|
| Status | `Accepted` |
| Ngày | 2026-11-23 |
| Người quyết định | Nhóm phát triển BookHub (PRJ301) |
| Liên quan | `FR-AUTH-007..009`, `.sdd/constraints/business.md (BR-ACC-006)`, `docs/architecture.md` |

## 1. Bối cảnh
Tính năng Quên/Đặt lại mật khẩu thuộc mục 3.1 (1.0đ) của đề PRJ301. Khi chấm bài và demo local trên NetBeans/Tomcat, không phải lúc nào môi trường cũng có sẵn cấu hình SMTP hoặc hộp thư thật; việc phụ thuộc cứng vào SMTP ngoài dễ gây lỗi khi trình bày.

## 2. Các phương án
| # | Phương án | Ưu | Nhược | Chi phí/Rủi ro |
|---|---|---|---|---|
| 1 | Sinh random opaque token, lưu `token_hash` (TTL 15 phút, dùng 1 lần) trong `PasswordResetTokens`; ở chế độ local demo ghi reset URL ra NetBeans/Tomcat Output (và tách interface gửi mail để gắn SMTP sau) | Demo offline/local ổn định 100%, vẫn chứng minh đủ cơ chế bảo mật token hash + expiry + one-time use | Không dùng cơ chế log URL này cho production | Thấp trong phạm vi đồ án local |
| 2 | Bắt buộc cấu hình Gmail/SMTP thật | Gửi email thật | Phụ thuộc mạng, App Password dễ lộ hoặc bị chặn khi đổi máy chấm bài | Trung bình–Cao |

## 3. Quyết định
Chọn phương án **#1**: thiết kế luồng `PasswordResetTokens` chuẩn bảo mật (hash trong DB, hết hạn 15 phút, dùng 1 lần, phản hồi giao diện trung tính không dò email), và xuất liên kết đặt lại mật khẩu qua kênh demo local (server output) với khả năng cắm SMTP provider tùy chọn về sau.

## 4. Hệ quả & đánh đổi (Trade-offs)
- ➕ Demo mượt trên mọi máy local mà không cần lưu SMTP credential vào repo.
- ➖ Khi triển khai ngoài môi trường local/demo bắt buộc thay kênh xuất log bằng dịch vụ email thật + rate limiting, và tắt hoàn toàn việc in reset URL ra log.

## 5. Cách kiểm chứng & điều kiện xem xét lại
- Thước đo thành công: `TC-AUTH-09`, `TC-AUTH-10` — reset thành công trong 15 phút, token đã dùng/hết hạn/sai đều bị từ chối; DB chỉ lưu `token_hash`.
- Xem lại khi: Triển khai BookHub ra môi trường internet/production.
