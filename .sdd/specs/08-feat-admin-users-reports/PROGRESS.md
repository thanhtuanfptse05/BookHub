# PROGRESS — Quản trị Người dùng, Dashboard & Báo cáo Thống kê (`08-feat-admin-users-reports`)

| Trường | Giá trị |
|---|---|
| Feature | `08-feat-admin-users-reports` · Tier: Full · Nhánh: `feat/admin-users-reports` |
| Pha hiện tại | `tasking` (SPEC & DESIGN `LOCKED` v1.0.0 · PLAN & TASKS `DRAFT` chờ duyệt Gate 2–3) |
| Task đang làm | Chưa bắt đầu (phụ thuộc `05`, `06`, `07`) |
| Cập nhật | 2026-11-23 |

## 1. Plan của task hiện tại
- **Mục tiêu (đo được):** Sau khi xong `05`..`07` và duyệt Gate 2–3, triển khai `T-001` → `T-005`.
- [ ] Duyệt `PLAN.md` (Gate 2) & `TASKS.md` (Gate 3)

## 2. Nhật ký thực thi
| Giờ | Việc đã làm | Kết quả |
|---|---|---|
| 2026-11-23 | Khóa SPEC + DESIGN v1.0.0 (Gate 1); lập `PLAN.md` và `TASKS.md` (`T-001`..`T-005`) | Xong Pha 1–3 |

## 3. Kiểm tra
Lint ⬜ · Test ⬜ · Build ⬜ · spec-check ✅ · Secret scan ✅ · Coverage mới ⬜ %

## 4. Blockers / câu hỏi cho người
- ⬜ Chờ hoàn tất `05`, `06`, `07` và duyệt Gate 2–3.

## 5. Quyết định & giả định
- Dùng biên nửa mở `created_at < toDate + 1` để không sót đơn cuối ngày; thanh `COUNT` vẽ bằng HTML/CSS thuần.

## 6. Handoff
- **Đã xong:** CONTEXT, SPEC (LOCKED), DESIGN (LOCKED), PLAN, TASKS. · **Làm tiếp:** Triển khai sau khi xong `05`..`07`. · **Cẩn thận:** Luôn dùng `COALESCE` chống null/chia 0.
