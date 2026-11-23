# ADR-004: Chính sách JSTL-First cho JSP, cấm tuyệt đối Java Scriptlet

| Trường | Giá trị |
|---|---|
| Status | `Accepted` |
| Ngày | 2026-11-23 |
| Người quyết định | Nhóm phát triển BookHub (PRJ301) |
| Liên quan | `constitution H-04, A-02`, `.sdd/design/DESIGN.md` |

## 1. Bối cảnh
Trong ứng dụng Servlet/JSP, việc nhúng Java scriptlet (`<% ... %>`) hoặc truy vấn DB trực tiếp vào JSP phá vỡ mô hình MVC, gây khó bảo trì và dễ phát sinh lỗ hổng XSS. Yêu cầu dự án bắt buộc toàn bộ View dùng JSTL/EL.

## 2. Các phương án
| # | Phương án | Ưu | Nhược | Chi phí/Rủi ro |
|---|---|---|---|---|
| 1 | JSP 3.1 + JSTL 3.0 (`jakarta.tags.*`) + EL 5.0 + bật `<scripting-invalid>true</scripting-invalid>` | Cưỡng chế ở mức compiler Tomcat, tách sạch View khỏi nghiệp vụ, ép dùng `<c:out>` chống XSS | Phải chuẩn bị DTO/ViewModel đầy đủ từ Servlet/Service | Thấp, rất an toàn |
| 2 | Chỉ quy ước miệng không viết scriptlet | Không cần cấu hình `web.xml` | Dễ vi phạm khi gấp tiến độ | Cao |

## 3. Quyết định
Chọn phương án **#1**: đặt toàn bộ JSP trong `web/WEB-INF/views/`, chỉ dùng JSTL 3.0 + EL 5.0 và cấu hình `<scripting-invalid>true</scripting-invalid>` trong `web/WEB-INF/web.xml`.

## 4. Hệ quả & đánh đổi (Trade-offs)
- ➕ Tomcat từ chối biên dịch bất kỳ file JSP nào chứa `<% %>`, `<%= %>`, `<%! %>`; dữ liệu động luôn được escape qua `<c:out>`.
- ➖ Mọi tính toán hiển thị (tổng tiền, trạng thái nút, định dạng DTO) phải chuẩn bị sẵn ở Service/Controller trước khi forward.

## 5. Cách kiểm chứng & điều kiện xem xét lại
- Thước đo thành công: Quét toàn bộ `.jsp` không chứa scriptlet và `<sql:*>`; cấu hình `scripting-invalid` hoạt động trên Tomcat 10.1.
- Xem lại khi: Không thay đổi trong phạm vi MVP.
