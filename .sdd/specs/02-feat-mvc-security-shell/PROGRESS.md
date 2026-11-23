# PROGRESS — Khung MVC, Bảo mật Filter & UI Shell dùng chung (`02-feat-mvc-security-shell`)

| Trường | Giá trị |
|---|---|
| Feature | `02-feat-mvc-security-shell` · Tier: Full · Nhánh: `feat/mvc-security-shell` |
| Pha hiện tại | `tasking` (SPEC & DESIGN `LOCKED` v1.0.0 · PLAN & TASKS `DRAFT` chờ duyệt Gate 2–3) |
| Task đang làm | Chưa bắt đầu (phụ thuộc xong `01-feat-foundation-jpa`) |
| Cập nhật | 2026-11-23 |

## 1. Plan của task hiện tại
- **Mục tiêu (đo được):** Sau khi xong `01` và duyệt Gate 2–3, tạo nhánh `feat/mvc-security-shell` và làm `T-001` → `T-006`.
- [ ] Duyệt `PLAN.md` (Gate 2) & `TASKS.md` (Gate 3)

## 2. Nhật ký thực thi
| Giờ | Việc đã làm | Kết quả |
|---|---|---|
| 2026-11-23 | Khóa SPEC & DESIGN v1.0.0 (Gate 1); lập `PLAN.md` và `TASKS.md` (`T-001`..`T-006`) | Xong Pha 1–3 |

## 3. Kiểm tra
Lint ⬜ · Test ⬜ · Build ⬜ · spec-check ✅ · Secret scan ✅ · Coverage mới ⬜ %

## 4. Blockers / câu hỏi cho người
- ⬜ Chờ hoàn tất `01-feat-foundation-jpa` và duyệt Gate 2–3.

## 5. Quyết định & giả định
- Dùng Bootstrap 5.3 cục bộ trong `web/assets/vendor/bootstrap/` + ánh xạ biến từ `tokens.css` để demo ổn định kể cả khi không có Internet.

## 6. Handoff
- **Đã xong:** CONTEXT, SPEC (LOCKED), DESIGN (LOCKED), PLAN, TASKS. · **Làm tiếp:** Triển khai sau khi xong `01`. · **Cẩn thận:** Khai báo thứ tự 4 Filter duy nhất trong `web.xml`.
