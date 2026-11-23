# plan.md — Con trỏ session + sổ nháp tier Quick

> Đọc file này **sau** `AGENTS.md`/`CLAUDE.md`. Agent sở hữu. Tiến độ chi tiết của Standard/Full nằm ở `.sdd/specs/<NN>-feat-<slug>/PROGRESS.md`.
> Hết việc: xóa nội dung, trả file về template.

## 1. Đang làm
| Trường | Giá trị |
|---|---|
| Feature | _(trống — đã hoàn tất & duyệt Gate 4 `APPROVED` toàn bộ `01`..`08` MVP BookHub)_ |
| Tier | — |
| Tiến độ chi tiết | `.sdd/specs/01-feat-foundation-jpa/` .. `.sdd/specs/08-feat-admin-users-reports/` (`done`) |
| Nhánh Git | `feat/bookhub-mvp-01-08` (sẵn sàng merge vào `main`) |
| Cập nhật lần cuối | 2026-11-23 |

## 2. Sổ nháp tier Quick (bỏ trống nếu không phải Quick)
- **Mục tiêu (1 câu, đo được):** —
- **Phạm vi file:** —
- [ ] Tái hiện lỗi bằng test đỏ
- [ ] Sửa tối thiểu → test xanh
- [ ] `scripts/verify` xanh
- [ ] Ghi bài học vào `CLAUDE.md §5` nếu lỗi có thể lặp lại

## 3. Blockers / câu hỏi cho người
- ⬜ Merge nhánh `feat/bookhub-mvp-01-08` vào `main`.
- ⬜ (Tùy chọn) Archive thư mục `01`..`08` vào `.sdd/specs/_archive/` nếu muốn bắt đầu tính năng mới (`09-feat-*`).

## 4. Handoff cho session kế tiếp
- **Đã xong:** Toàn bộ 8/8 tính năng MVP (`01`..`08`) đã được Owner duyệt Gate 4 (`APPROVED`), 48/48 tasks `✅`, `37/37` JUnit 5 tests + live Tomcat 10.1 pass, `scripts/verify.ps1` xanh (`0 lỗi, 0 cảnh báo`).
- **Làm tiếp:** Merge `feat/bookhub-mvp-01-08` vào `main` và thực hiện EPIC 9 (chụp ảnh giao diện / kịch bản demo nộp PRJ301 nếu cần).
- **Cẩn thận:** Không commit `bookhub-local.properties`; luôn chạy Ant với JDK 17 (`C:\Program Files\Java\jdk-17`).
