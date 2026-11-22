# specs/ — Đặc tả theo từng tính năng

Mỗi tính năng = 1 thư mục `feat-<slug>/` (nhân bản từ `_template/` bằng `scripts/new-feature`).

| File | Pha | Ai viết | Gate | Tier Standard | Tier Full |
|---|---|---|---|---|---|
| `CONTEXT.md` | 0 | Người | — | ✅ (ngắn) | ✅ |
| `SPEC.md` | 1 | Agent soạn, **người khóa** | 🔒 Gate 1 | ✅ rút gọn §1,3,7,8 | ✅ đủ 8 mục |
| `PLAN.md` | 2 | Agent | 🔒 Gate 2 | ❌ (thiết kế ở Phụ lục A SPEC) | ✅ |
| `TASKS.md` | 3 | Agent | 🔒 Gate 3 (Full) | ✅ | ✅ |
| `PROGRESS.md` | 4 | Agent | — | ✅ | ✅ |
| `REVIEW.md` | 5 | Reviewer độc lập | — | khuyến nghị | ✅ bắt buộc |
| `DESIGN.md` | 1b | Agent soạn, người duyệt cùng Gate 1 | 🔒 Gate 1 | chỉ khi `UI = Có` | chỉ khi `UI = Có` |
| `CHANGELOG.md` | mọi lúc | Agent/Người | — | ✅ | ✅ |

Tạo mới:
```powershell
./scripts/new-feature.ps1 <slug> -Tier Full [-Ui]     # hoặc Standard (bỏ PLAN.md); -Ui giữ DESIGN.md
```
```bash
./scripts/new-feature.sh <slug> full [ui]        # hoặc standard; "ui" giữ DESIGN.md
```
Tier **Quick** không tạo thư mục feature (chỉ dùng `plan.md`).

Ví dụ điền sẵn chuẩn văn phong: [`_example-create-todo/`](_example-create-todo/) (bỏ qua bởi `spec-check`).
Feature đã `done` hoặc `dropped`: chuyển thư mục sang `_archive/feat-<slug>/` (chống phình rác; `spec-check` tự bỏ qua thư mục bắt đầu bằng `_`).

## Chỉ mục feature
| Feature | Tier | Trạng thái | Pha hiện tại | Ghi chú |
|---|---|---|---|---|
| _(chưa có)_ | | | | |

Trạng thái: `idea → specifying → planning → implementing → verifying → done | dropped`
