# Constitution — Hiến pháp bất biến của BookHub

> Chỉ sửa thông qua một ADR trong `.sdd/rfcs/` và có người duyệt. Agent **không được** tự sửa file này khi chưa được người xác nhận.
> Mức ưu tiên: Layer 1 > Layer 2 > Layer 3. Vi phạm Layer 1 ⇒ dừng ngay & báo người.

## Layer 1 — Hard Rules (tuyệt đối, không ngoại lệ)
| ID | Luật |
|---|---|
| H-01 | Không hardcode secret/key/token/mật khẩu DB trong source hoặc Git; cấu hình nhạy cảm đọc từ `bookhub-local.properties` (ngoài Git) hoặc biến môi trường. |
| H-02 | Mật khẩu người dùng băm bằng **BCrypt** hoặc **Argon2** có salt; token reset mật khẩu chỉ lưu hash; không bao giờ lưu/ghi log plaintext. |
| H-03 | Mọi input từ ngoài (HTTP params, form, query string, URL) phải validate tại server ở biên (`controller/` & `service/`). Sort key dùng allow-list; không tin giá/tổng tiền/role từ client. |
| H-04 | 100% truy vấn DB đi qua **JPA/Hibernate** với parameter binding (cấm nối chuỗi JPQL/SQL, cấm DAO JDBC thuần). **Cấm** Java scriptlet `<% %>` trong JSP, **cấm** JSTL `<sql:*>`, và **cấm** render HTML thô từ dữ liệu người dùng (bắt buộc `<c:out>`/escape chống XSS). |
| H-05 | Không log PII nhạy cảm, password, password hash, reset token hay session ID; không lộ stack trace/SQL ra trang lỗi. |
| H-06 | Mọi endpoint không công khai phải kiểm tra authN + authZ ở server (`AuthenticationFilter`, `AdminAuthorizationFilter`, Service); thao tác Cart/Order/Review/Question bắt buộc lọc và xác minh `currentUser.id` tại tầng Service/Query (chống IDOR/BOLA). Mọi POST thay đổi dữ liệu bắt buộc kiểm tra CSRF token. |
| H-07 | Mọi thay đổi schema SQL Server có script trong `database/` và kế hoạch rollback/khôi phục; chạy kiểm chứng trên DB sạch với `hibernate.hbm2ddl.auto=validate`. |
| H-08 | Không có thay đổi hành vi nào mà không có SPEC được duyệt và test tương ứng. Ngoại lệ duy nhất: tier **Quick** (sửa lỗi đưa hành vi về đúng đặc tả), bắt buộc có test hồi quy. |
| H-09 | Chỉ dùng thư viện thuộc allow-list (`constraints/global.md`), đúng namespace `jakarta.*` (cấm trộn `javax.*`), xác minh tồn tại thật và đúng bộ phiên bản tương thích Tomcat 10.1 / Java 17. |

## Layer 2 — Architectural Constraints
| ID | Ràng buộc |
|---|---|
| A-01 | **Layered MVC chuẩn:** `Filter → Servlet Controller → Service → Repository (JPA) → Entity/DB` và `Controller → DTO/ViewModel → JSP/JSTL`. Cấm gọi ngược chiều; cấm viết business logic hoặc JPQL trong Servlet/JSP. |
| A-02 | **JSP chỉ là View (JSTL-First):** Toàn bộ JSP đặt trong `web/WEB-INF/views/`, chỉ dùng JSTL 3.0 (`jakarta.tags.*`) + EL 5.0; bật `<scripting-invalid>true</scripting-invalid>` trong `web.xml`. |
| A-03 | **Quản lý vòng đời JPA chặt chẽ:** `EntityManagerFactory` là singleton theo `ServletContext`; `EntityManager` tạo/đóng theo phạm vi thao tác (không chia sẻ giữa thread, không lưu vào `HttpSession`); transaction bắt đầu/commit/rollback tập trung ở **Service layer**, Repository không tự commit. |
| A-04 | **An toàn dữ liệu View & PRG:** Không truyền JPA Entity có lazy association chưa load sang JSP (dùng DTO/ViewModel); thao tác thay đổi trạng thái dùng `POST` kết hợp **Post/Redirect/Get (PRG)**. |
| A-05 | **Toàn vẹn giao dịch & tồn kho:** Checkout tạo `Orders` + `OrderItems` (snapshot tên & giá) + trừ `stock_qty` + xóa `CartItems` trong **một transaction nguyên tử**; hủy đơn hoàn tồn kho **đúng một lần** trong transaction. |
| A-06 | **Hợp đồng & route ổn định:** Thay đổi route/DTO dùng chung phải cập nhật `.sdd/shared_context.md`; thay đổi kiến trúc phải ghi ADR trong `.sdd/rfcs/`. |
| A-07 | **Cấu hình tập trung:** `AppConfig` & `JpaUtil` đọc cấu hình tại một điểm, fail-fast khi thiếu kết nối hoặc sai schema (`hbm2ddl.auto=validate`). |
| A-08 | Dependency JAR mới trong `web/WEB-INF/lib` phải được người duyệt (xem `AGENTS.md §4`), không đóng gói trùng API do Tomcat 10.1 cung cấp. |
| A-09 | **DB tối thiểu bắt buộc:** cấm `SELECT *` không cần thiết, cấm N+1 query ở Home/Catalog/Order, mọi danh sách phải phân trang tại DB (mặc định 12 sách/trang), FK và cột lọc chính có index; cập nhật `stock_qty` dùng pessimistic lock hoặc conditional update `stock_qty >= requestedQty` (`skills/sql-performance.md`). |

## Layer 3 — Engineering Standards & Chống phình rác
| ID | Chuẩn |
|---|---|
| S-01 | Test coverage ≥ 80% cho business logic mới trong Service; mọi bug fix có test hồi quy. |
| S-02 | Không còn cảnh báo biên dịch mới, không còn import `javax.*`, không scriptlet JSP. |
| S-03 | Kiểu dữ liệu tường minh; tiền tệ bắt buộc dùng `BigDecimal` (`DECIMAL(18,2)`), cấm `float`/`double`. |
| S-04 | Hàm ≲ 40 dòng, file `src/` ≲ 300 dòng (`spec-check` cảnh báo); một class một trách nhiệm, tránh Servlet khổng lồ. |
| S-05 | Comment giải thích *vì sao*, không giải thích *cái gì*. |
| S-06 | Log rõ mức độ (`ERROR/WARN/INFO/DEBUG`) qua SLF4J; tuyệt đối không in password, token hay dữ liệu nhạy cảm. |
| S-07 | Lỗi trả ra ngoài nhất quán (validation lỗi gắn từng field; lỗi hệ thống hiển thị trang `403`/`404`/`500` thân thiện). |
| S-08 | Conventional Commits; PR nhỏ; kiểm tra đầy đủ trước khi merge vào `main`. |
| S-09 | Tài liệu (`docs/`, `README.md`, `database/`) cập nhật cùng PR làm đổi hành vi hoặc schema. |
| S-10 | UI tuân `constraints/ui.md` và `.sdd/design/`: Bootstrap 5.3 + token CSS trong `tokens.css`, đủ 5 trạng thái, tiếng Việt nhất quán, định dạng VND, đạt WCAG 2.2 AA. |
| S-11 | **Anti-Bloat:** tái sử dụng helper/fragment JSP sẵn có; đặt file đúng thư mục (`global.md §4`); cấm dead code, code comment-out, `System.out.println` debug/file nháp. |
| S-12 | **Git & CI/CD an toàn:** không commit trực tiếp vào `main` sau khi thiết lập nhánh; đẩy nhánh feature qua `scripts/ship` và PR. |
| S-13 | Trang công khai (`/`, `/books`, `/books/detail`) tuân `constraints/seo.md` (HTML SSR từ JSP, title/description/H1/alt đầy đủ); khu vực `/account/*`, `/cart`, `/checkout`, `/admin/*` đặt `noindex`. |

## Quy trình sửa đổi hiến pháp
1. Tạo ADR (`rfcs/ADR-NNN-*.md`) nêu lý do & đánh đổi → 2. Người duyệt → 3. Sửa file này + ghi vào `Lịch sử` dưới → 4. Cập nhật `CLAUDE.md` nếu cần.

## Lịch sử
| Ngày | Thay đổi | ADR |
|---|---|---|
| 2026-11-23 | Khởi tạo hiến pháp cho BookHub (PRJ301 MVP — Java 17 / Jakarta EE 10 / Hibernate 6.6 / SQL Server) | ADR-001..ADR-009 |
