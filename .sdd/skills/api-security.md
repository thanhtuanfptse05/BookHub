---
name: api-security
description: Chuẩn bảo mật API, validate input, authN/authZ, rate limit, xử lý secret. Kích hoạt khi tạo/sửa endpoint, middleware auth, upload file, hoặc xử lý dữ liệu người dùng.
---

# Skill: API Security

## Khi nào dùng
Tạo/sửa endpoint, middleware, auth, upload, webhook, tích hợp bên thứ ba.

## 1. Input validation (biên `interface/`)
- Validate bằng schema (allow-list): kiểu, độ dài, định dạng, range, enum. **Từ chối** thay vì "làm sạch" tùy tiện.
- Giới hạn kích thước body, số phần tử mảng, độ sâu JSON, tham số phân trang (`limit ≤ N`).
- Chống injection: query tham số hóa; không nối chuỗi vào SQL/NoSQL/shell/path/template.
- Upload: kiểm MIME thật + đuôi + kích thước; đổi tên file; lưu ngoài web root; quét virus nếu cần.
- Chuẩn hóa path (chống `../`); URL từ người dùng ⇒ chống **SSRF** (allow-list host, chặn IP nội bộ).

## 2. AuthN / AuthZ
- AuthN: token ngắn hạn + refresh có rotation; cookie `HttpOnly; Secure; SameSite`.
- AuthZ **ở server, mỗi request**: kiểm quyền theo **resource** (chống IDOR/BOLA: `order.userId == caller.id`), không chỉ theo route.
- Mặc định **deny**; endpoint công khai phải khai báo tường minh.
- So sánh secret bằng constant-time; khóa/giới hạn thử sai (brute force); không tiết lộ "user tồn tại hay không".

## 3. Chống lạm dụng
- Rate limit theo IP/user/key; chặt hơn cho endpoint nhạy cảm (login, OTP, reset).
- Idempotency-Key cho thao tác tạo/thanh toán; timeout cho mọi I/O.
- CORS allow-list cụ thể (không `*` khi có credential); CSRF token cho session cookie.

## 4. Dữ liệu & phản hồi
- Chỉ trả về field cần (DTO riêng, **không** trả thẳng entity) → chống lộ field nhạy cảm/mass-assignment.
- Lỗi theo format chuẩn, không lộ stack/SQL/đường dẫn; log chi tiết ở server kèm `requestId`.
- Header bảo mật: HSTS, `X-Content-Type-Options`, CSP, `Referrer-Policy`.
- TLS bắt buộc; không đưa secret/PII vào URL/query.

## 5. Secret & phụ thuộc
- Secret từ env/secret manager; không commit; rotate khi nghi lộ.
- Quét dependency định kỳ (audit); cập nhật bản vá bảo mật.
- Webhook: xác thực chữ ký + chống replay (timestamp/nonce).

## Checklist review (OWASP API Top 10)
- [ ] Validate schema đầy đủ · [ ] AuthZ theo resource (BOLA) · [ ] Không mass-assignment
- [ ] Rate limit · [ ] Không lộ dữ liệu thừa · [ ] Lỗi không rò rỉ nội bộ
- [ ] Không secret/PII trong log/URL · [ ] Có test cho trường hợp **bị từ chối** (401/403/422/429)
