# PLAN — Khung MVC, Bảo mật Filter & UI Shell dùng chung (`02-feat-mvc-security-shell`)

> **Pha 2 · Thiết kế kiến trúc** sau khi SPEC & DESIGN v1.0.0 đã `LOCKED`. Duyệt (Gate 2) cùng `TASKS.md`.

| Trường | Giá trị |
|---|---|
| SPEC version | 1.0.0 |
| Status | `DRAFT` |

## 1. Tóm tắt giải pháp
Cấu hình `web/WEB-INF/web.xml` theo chuẩn Jakarta Servlet 6.0, khóa cứng `<scripting-invalid>true</scripting-invalid>` và map thứ tự rõ ràng 4 Filter (`EncodingFilter` → `CsrfFilter` → `AuthenticationFilter` → `AdminAuthorizationFilter`) để tránh lệch thứ tự khi dùng `@WebFilter`. Đồng thời xây dựng tầng tiện ích bảo mật (`PasswordUtil`, `CsrfUtil`, `UrlSafeUtil`, `FlashUtil`), bộ Exception chuẩn, tài nguyên CSS đồng bộ 100% từ `.sdd/design/tokens.css` + Bootstrap 5.3, các JSP fragments dùng chung cho 3 App Shells và 3 trang lỗi `/403`, `/404`, `/500` (`ADR-004`).

## 2. Phương án đã cân nhắc
| Phương án | Ưu | Nhược | Chọn? |
|---|---|---|---|
| A: Khai báo thứ tự 4 Filter tập trung trong `web.xml` + Bootstrap 5.3 cục bộ + `tokens.css` | Đảm bảo 100% thứ tự thực thi Filter trên Tomcat 10.1; hoạt động offline khi chấm bài | Phải duy trì `<filter-mapping>` đồng bộ trong `web.xml` | ✅ |
| B: Chỉ dùng `@WebFilter` annotation + CDN Bootstrap ngoài | Ít XML hơn | Servlet spec không đảm bảo thứ tự thực thi giữa nhiều `@WebFilter`; dễ lỗi khi mất mạng | ❌ |

## 3. Kiến trúc & luồng xử lý
```text
HTTP Request → [1] EncodingFilter (UTF-8)
             → [2] CsrfFilter (gắn _csrf vào session; chặn POST thiếu/sai _csrf → 403)
             → [3] AuthenticationFilter (/cart, /checkout, /orders/*, /account/*, /books/review|question)
             → [4] AdminAuthorizationFilter (/admin, /admin/* → chỉ role ADMIN)
             → Servlet Controller → (forward) WEB-INF/views/**/*.jsp (JSTL 3.0 + EL 5.0)
             → Lỗi 403/404/500 → ErrorPageServlet → WEB-INF/views/error/{403,404,500}.jsp
```

## 4. Thay đổi theo tầng
| Tầng | File tạo mới | File sửa | Ghi chú |
|---|---|---|---|
| `config/util/` | `PasswordUtil`, `CsrfUtil`, `FlashUtil`, `UrlSafeUtil`, `SessionUser` (immutable DTO trong session) | — | BCrypt cost 10+, SHA-256, chống open redirect |
| `exception/` | `BusinessException`, `NotFoundException`, `AccessDeniedException`, `OutOfStockException` | — | Map nhất quán sang HTTP status / flash |
| `filter/` | `EncodingFilter`, `CsrfFilter`, `AuthenticationFilter`, `AdminAuthorizationFilter` | — | Khai báo thứ tự trong `web.xml` |
| `controller/` | `ErrorPageServlet` (`/403`, `/404`, `/500`) | — | Không lộ stack trace |
| `web/WEB-INF/` | `web.xml`, `views/common/{header,nav,footer,messages,book-card,pagination,status-badge,empty-state,confirm-modal}.jsp`, `views/admin/{layout-sidebar,layout-topbar}.jsp`, `views/error/{403,404,500}.jsp` | `web/index.html` (gỡ/chuyển sang route `/`) | 0 scriptlet, `<c:out>` 100% |
| `web/assets/` | `vendor/bootstrap/`, `css/tokens.css`, `css/bookhub.css`, `js/{quantity-stepper,confirm-modal,form-submit}.js`, `images/book-placeholder.svg` | — | Đồng bộ `.sdd/design/tokens.css`, fix polish mobile |
| `tests/` | `tests/unit/{PasswordUtilTest,UrlSafeUtilTest,CsrfFilterTest,AuthFilterTest}.java` | — | Gắn `@spec mvc-security-shell/AC-01..07` |

## 5. Data model & Migration
- Không đổi DB schema.

## 6. API / Contract & ảnh hưởng chéo
- Thiết lập khóa session chuẩn (`AUTH_USER`, `CSRF_TOKEN`, `FLASH_MESSAGES`) và bộ JSP fragments dùng chung cho toàn bộ các feature `03`..`08`.

## 7. Truy vết SPEC → thiết kế
| Yêu cầu | Thành phần đáp ứng | Test dự kiến |
|---|---|---|
| FR-01 | `web.xml` (`scripting-invalid=true`, `HttpOnly`, error pages) | AC-01 |
| FR-02 | `EncodingFilter` | AC-02 |
| FR-03, EC-01 | `AuthenticationFilter` + `UrlSafeUtil` | AC-03 |
| FR-04 | `AdminAuthorizationFilter` | AC-04 |
| FR-05, FR-06, EC-02 | `CsrfFilter` + `CsrfUtil` (`MessageDigest.isEqual`) | AC-05 |
| FR-07 | `PasswordUtil` + `FlashUtil` | AC-06 |
| FR-08, FR-09, EC-03..04 | `web/assets/`, `views/common/`, `views/admin/layout-*`, `views/error/` | AC-07 |

## 8. Chiến lược kiểm thử
- **Unit (JUnit 5):** `PasswordUtil`, `UrlSafeUtil` (chống `//evil.com`, `https://evil.com`), Filter logic (mock `HttpServletRequest`/`Response`/`FilterChain`).
- **Tomcat / Visual Verification:** Deploy thử, kiểm tra trang `/403`, `/404`, `/500` và 3 App Shells ở `375px` & `1280px`, chụp ảnh so với baseline; chạy `scripts/verify.ps1` + quét `0` scriptlet `<%`.

## 9. Rủi ro & giảm thiểu
| # | Rủi ro | Xác suất | Tác động | Giảm thiểu / Rollback |
|---|---|---|---|---|
| R1 | Trùng mapping giữa `@WebFilter` và `web.xml` khiến Filter chạy 2 lần | Trung bình | Trung bình | Chỉ khai báo mapping 4 Filter trong `web.xml`, không gắn `@WebFilter` song song |
| R2 | Static assets (`/assets/*`) bị chặn nhầm bởi Filter | Thấp | Trung bình | Giới hạn URL pattern `AuthenticationFilter`/`AdminAuthorizationFilter`, bỏ qua static GET ở `CsrfFilter` |

## 10. Rollout & Quan sát
- Ghi `WARN` có cấu trúc (không log secret/token) khi chặn CSRF hoặc truy cập trái quyền `/admin/*`. Rollback qua nhánh `feat/mvc-security-shell`.

## 11. ❓ Câu hỏi cho con người
- Không có câu hỏi mở.

## 12. Phê duyệt (Gate 2)
(chờ duyệt) · —
