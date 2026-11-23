# CONTEXT — Khung MVC, Bảo mật Filter & UI Shell dùng chung (mvc-security-shell)

> **Pha 0 · Bối cảnh tính năng.** Nguồn chuẩn: `BookHub_PRJ301_Project_Plan_Requirements.md` (§8.8, §9, §14.6, §15) và `.sdd/design/DESIGN.md`.
> Trạng thái: `ready`

## 1. Bối cảnh & Pain point
- Mọi tính năng phía sau (Auth, Catalog, Cart/Checkout, Review/Q&A, Admin) đều dùng chung cơ chế UTF-8, kiểm tra phiên đăng nhập, phân quyền `/admin/*`, chống CSRF cho mọi form `POST`, xử lý ngoại lệ thống nhất (`/403`, `/404`, `/500`) và bộ giao diện dùng chung (Storefront Shell, Auth Shell, Admin Shell, CSS tokens từ Figma).
- Nếu không dựng và cưỡng chế ngay từ đầu (như `<scripting-invalid>true</scripting-invalid>` trong `web.xml`, `CsrfFilter`, `PasswordUtil`), từng màn hình sẽ dễ rơi vào scriptlet JSP, lỗ hổng XSS/CSRF hoặc giao diện thiếu đồng bộ.

## 2. Mục tiêu nghiệp vụ & thước đo thành công
| Mục tiêu | Chỉ số (đo được) | Hiện tại → Mục tiêu |
|---|---|---|
| Cưỡng chế JSTL-First ở mức compiler | 0 scriptlet `<% %>` trong toàn bộ `.jsp` | Chưa có `web.xml` → bật `<scripting-invalid>true</scripting-invalid>` |
| Bảo vệ biên request tập trung | 100% route `/account/*`, `/cart`, `/checkout`, `/admin/*` & POST được bảo vệ | Chưa có Filter → 4 Filter hoạt động theo thứ tự chuẩn |
| Hệ thống UI Shell khớp Figma | Khớp 100% token trong `.sdd/design/tokens.css` + Bootstrap 5.3 | Chỉ có `web/index.html` mặc định → đủ 3 Shell + fragments dùng chung + `SCR-23` |

## 3. Người dùng / Tác nhân
| Actor | Muốn gì | Ghi chú |
|---|---|---|
| Guest / Customer / Admin | Giao diện đồng nhất, dễ điều hướng trên cả desktop 1280px và mobile 375px; trang lỗi 403/404/500 rõ ràng | Không bao giờ thấy stack trace |
| Developer | Tái sử dụng 14 component & JSP fragments chuẩn hóa, không lặp lại header/footer/CSRF/alert ở từng trang | Tuân thủ `components.md` (U-04) |

## 4. Thuật ngữ nghiệp vụ riêng của feature
| Thuật ngữ | Định nghĩa |
|---|---|
| `JSTL-First` | Toàn bộ JSP nằm trong `web/WEB-INF/views/`, chỉ dùng JSTL 3.0 (`jakarta.tags.*`) + EL 5.0 + `<c:out>` |
| `CSRF Token` | Chuỗi ngẫu nhiên lưu trong `HttpSession`, bắt buộc gửi kèm qua hidden field `_csrf` trên mọi form `POST` |
| `PRG Flash` | Thông báo thành công/lỗi lưu tạm trong session và xóa ngay sau khi render ở request GET tiếp theo |

## 5. Ràng buộc cố định (không thương lượng)
- `web.xml` đúng schema Jakarta Servlet 6.0; bật `<scripting-invalid>true</scripting-invalid>`.
- Không hardcode màu (`#hex`/`rgb`) trong JSP/CSS component (U-01) — dùng biến từ `web/assets/css/tokens.css`.
- Không để lộ stack trace, SQL hay thông tin nội bộ tại `/403`, `/404`, `/500`.

## 6. Tham chiếu
- Figma Prototype: `https://agenda-kale-62994833.figma.site` · Design System: `.sdd/design/DESIGN.md`, `tokens.css`, `components.md` · Screens: `docs/ui-screens.md` (`SCR-23`, 3 App Shells) · Baseline: `.sdd/design/references/baseline/`.

## 7. Giả định & câu hỏi mở
- Không có câu hỏi mở chặn — toàn bộ màu sắc, font, 3 App Shells và trang lỗi `SCR-23` đã được chốt từ Figma.
