# PROGRESS — Khung MVC, Bảo mật Filter & UI Shell dùng chung (`02-feat-mvc-security-shell`)

| Trường | Giá trị |
|---|---|
| Feature | `02-feat-mvc-security-shell` · Tier: Full · Nhánh: `feat/bookhub-mvp-01-08` |
| Pha hiện tại | `done` (Owner đã duyệt `REVIEW.md` `APPROVED` — đóng đủ `T-001`..`T-006`) |
| Task đang làm | — (đã hoàn tất toàn bộ `T-001`..`T-006`) |
| Cập nhật | 2026-11-23 |

## 1. Plan của task hiện tại
- [x] `T-001`: `web/WEB-INF/web.xml` (Servlet 6.0, `<scripting-invalid>true</scripting-invalid>`, `<http-only>true</http-only>`, error pages `/403`/`/404`/`/500`) + 4 Exception (`BusinessException`, `NotFoundException`, `AccessDeniedException`, `OutOfStockException`)
- [x] `T-002`: `SessionUser`, `PasswordUtil` (BCrypt + SHA-256), `CsrfUtil`, `UrlSafeUtil` (anti-open-redirect), `FlashUtil`, `FormatUtil`
- [x] `T-003`: 4 Filters (`EncodingFilter`, `CsrfFilter`, `AuthenticationFilter`, `AdminAuthorizationFilter`) ánh xạ theo đúng thứ tự trong `web.xml`
- [x] `T-004`: Bootstrap 5.3.3 local (`web/assets/vendor/bootstrap/`), `tokens.css`, `bookhub.css` (0 hardcode màu), `book-placeholder.svg`, `bookhub.js`, `robots.txt`
- [x] `T-005`: 11 JSP fragments dùng chung chuẩn JSTL 3.0 + EL 5.0 (`common/{head,header,footer,messages,book-card,pagination,confirm-modal}.jsp`, `admin/{layout-start,layout-end}.jsp`)
- [x] `T-006`: `ErrorPageServlet` + `views/error/{403,404,500}.jsp` + 7/7 tests (`MvcSecurityShellTest`) & live Tomcat verify — Owner đã duyệt `REVIEW.md` (`APPROVED`)

## 2. Nhật ký thực thi
| Giờ | Việc đã làm | Kết quả |
|---|---|---|
| 2026-11-23 | Triển khai `T-001`..`T-006`, xóa `web/index.html` cũ, commit `6514c7d` | 7/7 `MvcSecurityShellTest` PASS (`@spec mvc-security-shell/AC-01..07`) |
| 2026-11-23 | Kiểm tra thực tế trên Tomcat 10.1 (`dist/BookHub.war`) | POST thiếu CSRF trả `403`, Customer vào `/admin` trả `403`, URL sai trả `404`, `500.jsp` không lộ stack trace |

## 3. Kiểm tra
Test ✅ (7/7 JUnit 5 `MvcSecurityShellTest`) · Build ✅ (`dist/BookHub.war`, 0 warning) · ui-check ✅ (0 phát hiện) · spec-check ✅ · Secret scan ✅

## 4. Blockers / câu hỏi cho người
- ⬜ Chờ Owner review & duyệt `.sdd/specs/02-feat-mvc-security-shell/REVIEW.md` (Gate 4) và kiểm trực quan trình duyệt 375/768/1280.

## 5. Quyết định & giả định
- Dùng Bootstrap 5.3.3 bản cục bộ trong `web/assets/vendor/bootstrap/` để demo PRJ301 hoạt động ổn định cả khi không có Internet.
- Không truyền Entity JPA vào session; chỉ lưu `SessionUser` bất biến (không chứa `passwordHash`).

## 6. Handoff
- **Đã xong:** Toàn bộ khung bảo mật Filter, `web.xml`, assets và JSP shells cho cả 3 vùng Public / Customer / Admin.
- **Làm tiếp:** Sau khi Owner duyệt `REVIEW.md` → đánh `APPROVED` và đóng `T-006`.
