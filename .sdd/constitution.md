# Constitution — Hiến pháp bất biến của {{PROJECT_NAME}}

> Chỉ sửa thông qua một ADR trong `.sdd/rfcs/` và có người duyệt. Agent **không được** tự sửa file này.
> Mức ưu tiên: Layer 1 > Layer 2 > Layer 3. Vi phạm Layer 1 ⇒ dừng ngay & báo người.

## Layer 1 — Hard Rules (tuyệt đối, không ngoại lệ)
| ID | Luật |
|---|---|
| H-01 | Không hardcode secret/key/token/mật khẩu; dùng biến môi trường hoặc secret manager. |
| H-02 | Mật khẩu băm bằng thuật toán chuẩn (argon2id / bcrypt); không bao giờ lưu/ghi log plaintext. |
| H-03 | Mọi input từ ngoài (HTTP, file, queue, CLI) phải validate schema (allow-list) ở biên (`interface/`). Chuẩn hóa path (chống `../`); URL ngoài phải chống SSRF. |
| H-04 | Truy vấn DB phải tham số hóa (cấm nối chuỗi SQL). **Cấm** `eval`/`new Function`, nối input vào shell (`exec`/`shell=True`), và HTML thô chưa escape (`innerHTML`/`dangerouslySetInnerHTML`). |
| H-05 | Không log PII/secret; mask khi cần. |
| H-06 | Mọi endpoint không công khai phải có authN + authZ kiểm tra ở server; dữ liệu thuộc user/tenant bắt buộc lọc theo `owner_id`/`tenant_id` tại tầng truy vấn (chống IDOR/BOLA). |
| H-07 | Migration dữ liệu phải có `down`/kế hoạch rollback và chạy được trên bản sao trước. |
| H-08 | Không có thay đổi hành vi nào mà không có SPEC được duyệt và test tương ứng. Ngoại lệ duy nhất: tier **Quick** (sửa lỗi đưa hành vi về đúng đặc tả), bắt buộc có test hồi quy. |
| H-09 | Không dùng thư viện có CVE nghiêm trọng hoặc license cấm (`constraints/global.md`). Xác minh package tồn tại thật (chống slopsquatting) và khóa qua lockfile. |

## Layer 2 — Architectural Constraints
| ID | Ràng buộc |
|---|---|
| A-01 | Clean Architecture: `interface → usecase → domain`; `infra` implement port. Cấm import ngược chiều. |
| A-02 | `domain/` thuần: không phụ thuộc framework, DB, HTTP. |
| A-03 | Mỗi service/module sở hữu dữ liệu của mình; **không truy cập chéo DB/bảng** của module khác — đi qua API/port. |
| A-04 | Tác vụ chậm/không cần đồng bộ (email, báo cáo, webhook) đi qua hàng đợi/async; xử lý phải **idempotent**. |
| A-05 | Mọi I/O ngoài (HTTP/DB/queue) có **timeout, giới hạn connection pool**, retry có giới hạn (backoff, chỉ khi idempotent), và xử lý lỗi tường minh. |
| A-06 | API contract công khai có versioning; thay đổi phá vỡ phải qua ADR + ghi `shared_context.md`. |
| A-07 | Cấu hình qua env, đọc tập trung một chỗ (`infra/config`), validate khi khởi động (fail fast). |
| A-08 | Dependency mới phải được người duyệt (xem `AGENTS.md §4`). |
| A-09 | **DB tối thiểu bắt buộc:** cấm `SELECT *`, cấm query trong vòng lặp (N+1), truy vấn danh sách bắt buộc có `LIMIT`/phân trang, khóa ngoại & cột lọc chính có index. Cập nhật cạnh tranh (tiền/tồn kho/trạng thái) phải atomic hoặc có lock (`skills/sql-performance.md`). |

## Layer 3 — Engineering Standards & Chống phình rác
| ID | Chuẩn |
|---|---|
| S-01 | Test coverage ≥ {{COVERAGE_MIN}}% cho code mới; mọi bug fix có test hồi quy. |
| S-02 | Lint/format tự động, 0 warning mới; CI chặn merge nếu đỏ. |
| S-03 | Kiểu dữ liệu tường minh; cấm `any`/ép kiểu không kiểm soát. |
| S-04 | Hàm ≲ 40 dòng, file `src/` ≲ 300 dòng (`spec-check` cảnh báo); ưu tiên đặt tên rõ nghĩa hơn comment. |
| S-05 | Comment giải thích *vì sao*, không giải thích *cái gì*. |
| S-06 | Log có cấu trúc (JSON) + correlation/request ID; mức log đúng (error/warn/info/debug). |
| S-07 | Lỗi trả ra ngoài theo format thống nhất (`skills/error-handling.md`); không lộ stack trace/nội bộ. |
| S-08 | Conventional Commits; PR nhỏ; ≥ 1 review trước khi merge `{{MAIN_BRANCH}}`. |
| S-09 | Tài liệu (OpenAPI, sơ đồ) cập nhật cùng PR làm đổi hành vi. |
| S-10 | Dự án có UI tuân `constraints/ui.md` và `.sdd/design/`: dùng token, dùng lại component, đủ trạng thái, truy cập AA, có ảnh chụp xác minh. |
| S-11 | **Anti-Bloat (không phình rác):** tìm helper/service sẵn có trước khi tạo mới; đặt file đúng thư mục (cấm xả `.md`/ảnh/nháp ở root — `global.md §4`); cấm dead code, code comment-out (`// old...`), import/biến thừa, `console.log`/file nháp; không tạo wrapper thừa cho 1 chỗ dùng; không cài thư viện ngoài cho việc tự viết ≲ 20 dòng hoặc đã có trong stdlib. |
| S-12 | **CI/CD tự động:** mọi thay đổi vào `{{MAIN_BRANCH}}` qua PR + CI xanh (branch protection + auto-merge); deploy & release do CI thực hiện; Agent chỉ push nhánh feature bằng `scripts/ship`; luôn có kế hoạch rollback (`docs/ci-cd.md`). |
| S-13 | Website công khai tuân `constraints/seo.md`: SSR/SSG, meta + JSON-LD hợp lệ, robots/sitemap, chính sách AI crawler, nội dung trả lời trực tiếp & có nguồn thật (SEO · AEO · GEO). |

## Quy trình sửa đổi hiến pháp
1. Tạo ADR (`rfcs/ADR-NNN-*.md`) nêu lý do & đánh đổi → 2. Người duyệt → 3. Sửa file này + ghi vào `Lịch sử` dưới → 4. Cập nhật `CLAUDE.md` nếu cần.

## Lịch sử
| Ngày | Thay đổi | ADR |
|---|---|---|
| {{YYYY-MM-DD}} | Khởi tạo | — |
