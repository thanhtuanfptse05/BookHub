# TASKS — Khung MVC, Bảo mật Filter & UI Shell dùng chung (`02-feat-mvc-security-shell`)

> **Pha 3 · Phân rã từ `PLAN.md`.** Task atomic ≤ 4h · gắn thẻ `@spec mvc-security-shell/AC-xx`.

**Trạng thái:** `⬜ todo` · `🔄 doing` · `✅ done` · `⛔ blocked`

| ID | Task | Phụ thuộc | Ước lượng | Truy vết | Trạng thái |
|---|---|---|---|---|---|
| T-001 | Tạo `web/WEB-INF/web.xml` (Servlet 6.0, `scripting-invalid=true`, `HttpOnly`, error pages) & bộ Exception | — | 2h | FR-01, FR-09, AC-01 | ⬜ |
| T-002 | Tạo `PasswordUtil`, `CsrfUtil`, `UrlSafeUtil`, `FlashUtil`, `SessionUser` + unit tests | — | 2.5h | FR-03, FR-05, FR-07, EC-01, AC-06 | ⬜ |
| T-003 | Tạo 4 Filter (`Encoding`, `Csrf`, `Authentication`, `AdminAuthorization`) + unit tests | T-001, T-002 | 3h | FR-02..06, EC-02, AC-02..05 | ⬜ |
| T-004 | Thêm Bootstrap 5.3 + đồng bộ `tokens.css`, `bookhub.css`, fallback SVG & minimal JS | — | 3h | FR-08, NFR-03, EC-04 | ⬜ |
| T-005 | Tạo JSP fragments dùng chung (`common/*.jsp`, `admin/layout-*.jsp`) chuẩn JSTL 3.0 | T-004 | 3h | FR-08, NFR-02, AC-07 | ⬜ |
| T-006 | Tạo `ErrorPageServlet` + `views/error/{403,404,500}.jsp` + kiểm tra trực quan 375/1280 & `verify` | T-003, T-005 | 2h | FR-09, EC-03, AC-07, VAC-01..02 | ⬜ |

## Chi tiết task

### T-001 — `web.xml` chuẩn Servlet 6.0 & Exception hierarchy
- **Mục tiêu:** Cưỡng chế không scriptlet ở mức compiler Tomcat và tạo 4 lớp Exception domain chuẩn.
- **File dự kiến:** `web/WEB-INF/web.xml`, `src/java/com/bookhub/exception/*.java`.
- **Acceptance:**
  - [ ] Thử biên dịch JSP có scriptlet bị Tomcat từ chối (`@spec mvc-security-shell/AC-01`).
- **Không làm:** Không khai báo Servlet nghiệp vụ trong task này.

### T-002 — Tiện ích bảo mật & phiên (`PasswordUtil`, `CsrfUtil`, `UrlSafeUtil`, `FlashUtil`, `SessionUser`)
- **Mục tiêu:** Cung cấp BCrypt hash/verify, SHA-256 token hash, xác thực internal returnUrl chống open redirect và flash message PRG.
- **File dự kiến:** `src/java/com/bookhub/util/*.java`, `src/java/com/bookhub/dto/SessionUser.java`, `tests/unit/{PasswordUtilTest,UrlSafeUtilTest}.java`.
- **Acceptance:**
  - [ ] Test `PasswordUtil` & `UrlSafeUtil` xanh (`@spec mvc-security-shell/AC-06`).
- **Không làm:** Không lưu Entity JPA hay passwordHash vào `SessionUser`.

### T-003 — Chuỗi 4 Servlet Filters & Unit Tests
- **Mục tiêu:** Triển khai `EncodingFilter`, `CsrfFilter`, `AuthenticationFilter`, `AdminAuthorizationFilter` đăng ký đúng thứ tự trong `web.xml`.
- **File dự kiến:** `src/java/com/bookhub/filter/*.java`, `tests/unit/{CsrfFilterTest,AuthFilterTest}.java`.
- **Acceptance:**
  - [ ] UTF-8 hoạt động, Guest vào `/cart` bị redirect `/login`, Customer vào `/admin` nhận `403`, POST thiếu/sai `_csrf` nhận `403` (`@spec mvc-security-shell/AC-02..05`).
- **Không làm:** Không nhúng truy vấn DB trong Filter.

### T-004 & T-005 — Tài nguyên UI & Bộ JSP Fragments 3 App Shells
- **Mục tiêu:** Đưa Bootstrap 5.3, `tokens.css`, `bookhub.css` (có `whitespace-nowrap` nút mobile & KPI VND) và 11 JSP fragments dùng JSTL 3.0 (`<c:out>`).
- **File dự kiến:** `web/assets/**`, `web/WEB-INF/views/common/*.jsp`, `web/WEB-INF/views/admin/layout-*.jsp`.
- **Acceptance:**
  - [ ] `scripts/ui-check.ps1` báo 0 lỗi hardcode màu/inline style/thiếu `alt`; 0 scriptlet JSP.
- **Không làm:** Không đưa nút nổi prototype Figma vào code thật.

### T-006 — Trang lỗi `/403`, `/404`, `/500` & Xác minh trực quan
- **Mục tiêu:** Hoàn thiện `ErrorPageServlet`, 3 JSP trang lỗi (`SCR-23`), kiểm ảnh 375/1280 và chạy `scripts/verify.ps1`.
- **File dự kiến:** `src/java/com/bookhub/controller/site/ErrorPageServlet.java`, `web/WEB-INF/views/error/{403,404,500}.jsp`.
- **Acceptance:**
  - [ ] 3 trang lỗi không lộ stack trace, khớp `VAC-01..02` (`@spec mvc-security-shell/AC-07`).

## Đồ thị phụ thuộc
```text
T-001 ─┬─► T-003 ─────────┐
T-002 ─┘                  ├─► T-006
T-004 ───► T-005 ─────────┘
```

## Phê duyệt (Gate 3)
(chờ duyệt) · —
