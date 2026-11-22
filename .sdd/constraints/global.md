# constraints/global.md — Tech stack & quy ước toàn cục

> Đây là danh sách "đóng" (allow-list). Cái gì không có ở đây ⇒ **hỏi người trước khi dùng**.

## 1. Tech stack cố định
| Lớp | Công nghệ | Phiên bản | Ghi chú |
|---|---|---|---|
| Ngôn ngữ | {{LANGUAGE}} | {{x.y}} | |
| Framework | {{FRAMEWORK}} | {{x.y}} | |
| Database | {{DB}} | {{x.y}} | |
| Cache / Queue | {{Redis/…}} | | |
| Test | {{Jest/Pytest/…}} | | |
| Lint/Format | {{ESLint+Prettier/Ruff/…}} | | |
| CI/CD | {{GitHub Actions/…}} | | |
| Runtime/Deploy | {{Docker/K8s/Vercel/…}} | | |

## 2. Thư viện
- ✅ **Cho phép (đã duyệt):** {{liệt kê}}
- ⛔ **Cấm:** {{moment (dùng date-fns), lodash toàn bộ (import hàm lẻ), thư viện không bảo trì > 2 năm, …}}
- **Quy tắc thêm dependency:** kiểm tra package **tồn tại thật** trên registry (tránh bịa tên/slopsquatting); nêu lý do + kích thước + license + bảo trì + phương án thay thế; người duyệt. Không cài thư viện cho việc ≲ 20 dòng tự viết được hoặc stdlib đã có. Luôn giữ và commit lockfile.
- Cho phép license: MIT, Apache-2.0, BSD, ISC. Cấm: GPL/AGPL (trừ khi ADR duyệt).

## 3. Quy ước đặt tên (Naming)
| Đối tượng | Quy ước | Ví dụ |
|---|---|---|
| File/thư mục | `kebab-case` | `create-order.usecase.ts` |
| Class/Type/Interface | `PascalCase` (không tiền tố `I`) | `OrderRepository` |
| Hàm/biến | `camelCase` (hoặc `snake_case` nếu Python) | `calculateTotal` |
| Hằng số | `UPPER_SNAKE_CASE` | `MAX_RETRY` |
| Boolean | tiền tố `is/has/can/should` | `isActive` |
| Bảng DB | `snake_case`, số nhiều | `order_items` |
| Cột DB | `snake_case`; khóa ngoại `<bảng_số_ít>_id` | `user_id` |
| Endpoint REST | danh từ số nhiều, `kebab-case` | `/api/v1/order-items` |
| Env var | `UPPER_SNAKE_CASE` | `DATABASE_URL` |
| Test | `<đơn vị>.spec` / `test_<hành vi>` | `create-order.usecase.spec.ts` |
| Nhánh Git | `type/slug` | `feat/user-login` |

## 4. Cấu trúc thư mục & Quy tắc đặt file (Cấm xả rác ở root)
Xem `CLAUDE.md §3` cho `src/` (nhóm theo domain). **Mọi file mới phải đặt đúng chỗ — cấm tự tạo file `.md`, ảnh, hay script nháp ở thư mục gốc:**
| Loại file | Nơi đặt duy nhất | Ghi chú |
|---|---|---|
| Đặc tả feature (SPEC/PLAN/TASKS/DESIGN…) | `.sdd/specs/feat-<slug>/` | Xong/hủy → `.sdd/specs/_archive/` |
| Tài liệu người đọc (luồng nghiệp vụ, roadmap, kiến trúc, vận hành) | `docs/` | Không rải `.md` ở root |
| Ảnh/gói thiết kế từ Stitch/Figma & baseline UI | `.sdd/design/references/` | Gói `.zip` đặt ở `references/incoming/` (gitignore), đọc xong xóa |
| Tài nguyên tĩnh của app (ảnh/font/icon phục vụ user) | `public/` hoặc `src/assets/` | Theo convention của framework |
| File nháp / thử nghiệm tạm | `.tmp/` (đã gitignore) | **Xóa trước khi xong task** |

## 5. Định dạng & thời gian
- Thời gian lưu **UTC**, truyền ISO-8601; hiển thị theo timezone người dùng.
- Tiền: lưu số nguyên đơn vị nhỏ nhất + mã tiền tệ; không dùng float.
- ID: {{UUID v7 / ULID / bigserial}}.
- Mã hóa: UTF-8; xuống dòng LF.
