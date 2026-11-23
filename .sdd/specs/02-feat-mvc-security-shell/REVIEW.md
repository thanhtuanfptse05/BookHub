# REVIEW — mvc-security-shell

> Review **độc lập** (agent/session khác, hoặc người) — prompt: `.sdd/prompts/06-review.md`. Reviewer **không sửa code**.
> Tier Full: bắt buộc `APPROVED` trước khi merge.

| Trường | Giá trị |
|---|---|
| Reviewer | Chờ Owner duyệt (bản nháp bằng chứng từ implementer) · Ngày: 2026-11-23 |
| Commit/nhánh | `feat/bookhub-mvp-01-08` (commit `6514c7d` + fix JSP EL) |
| **Kết luận** | `PENDING` (chờ Owner xác nhận `APPROVED`) |

## 1. Bằng chứng chạy thật
```text
$ ant clean dist test-junit5
- Compiling 116 source files (JDK 17, -Xlint:deprecation -Xlint:unchecked): 0 errors, 0 warnings
- MvcSecurityShellTest: 7/7 OK
  - webXmlEnforcesScriptingInvalidAndZeroScriptletsExistInViews() [OK]
  - encodingFilterForcesUtf8OnRequestAndResponse() [OK]
  - authenticationFilterRedirectsGuestsWithSafeReturnUrlAndRejectsOpenRedirect() [OK]
  - adminFilterBlocksNonAdminCustomerWith403AndAllowsAdmin() [OK]
  - csrfFilterBlocksMissingOrWrongTokenAndAllowsMatchingToken() [OK]
  - passwordUtilHashesWithDistinctSaltsAndVerifiesCorrectly() [OK]
  - sharedAssetsErrorPagesAndTokensExistWithoutHardcodedColorsInBookhubCss() [OK]
- Live Tomcat 10.1 (`dist/BookHub.war`): POST thiếu CSRF → 403, Customer → `/admin*` → 403, URL lạ → 404, 500.jsp không lộ stack trace
```

## 2. Truy vết
| Yêu cầu | Code | Test (`@spec`) | OK? |
|---|---|---|---|
| FR-01, FR-09 | `web/WEB-INF/web.xml`, `com.bookhub.exception.*`, `ErrorPageServlet.java`, `views/error/{403,404,500}.jsp` | `mvc-security-shell/AC-01`, `AC-07` | ✅ |
| FR-02..06 | `EncodingFilter`, `CsrfFilter`, `AuthenticationFilter`, `AdminAuthorizationFilter` | `mvc-security-shell/AC-02..05` | ✅ |
| FR-07, EC-01 | `PasswordUtil`, `CsrfUtil`, `UrlSafeUtil`, `FlashUtil`, `FormatUtil`, `SessionUser` | `mvc-security-shell/AC-06` | ✅ |
| FR-08, NFR-02..03 | `web/assets/{vendor/bootstrap,css,images,js}`, `views/common/*.jsp`, `views/admin/layout-*.jsp` | `mvc-security-shell/AC-01`, `AC-07` | ✅ |

## 3. Kiểm tra theo hiến pháp
- [x] Không có scriptlet `<% %>` trong toàn bộ JSP (`<scripting-invalid>true</scripting-invalid>` bật trong `web.xml`)
- [x] Bảo mật (`H-01..06`): `HttpOnly` cookie, CSRF bắt buộc trên mọi `POST`, chống Open Redirect trong `UrlSafeUtil`, không lộ stack trace ở `403`/`404`/`500`
- [x] UI (`constraints/ui.md`): `bookhub.css` chỉ dùng CSS variables từ `tokens.css`, `ui-check` 0 lỗi
- [ ] Ảnh chụp trực quan 375/768/1280 trên trình duyệt thực — cần Owner kiểm tra khi chạy Tomcat

## 4. Phát hiện
| # | Mức | File:dòng | Vấn đề | Cách sửa đề xuất | Trạng thái |
|---|---|---|---|---|---|
| 1 | 🟢 | `web/WEB-INF/views/common/head.jsp` | Đã sửa chuỗi quote lồng nhau bằng `<c:set>` để Jasper biên dịch sạch trên Tomcat 10.1 | Đã sửa & xác minh live Tomcat 200 OK | Đã sửa |

## 5. Phê duyệt cuối
_(Chờ Owner xác nhận `APPROVED` để chốt Gate 4)_
