# plan.md — Con trỏ session + sổ nháp tier Quick

> Đọc file này **sau** `AGENTS.md`/`CLAUDE.md`. Agent sở hữu. Tiến độ chi tiết của Standard/Full nằm ở `.sdd/specs/<NN>-feat-<slug>/PROGRESS.md`.
> Hết việc: xóa nội dung, trả file về template.

## 1. Đang làm
| Trường | Giá trị |
|---|---|
| Feature | `01`..`08` (Toàn bộ MVP BookHub đã triển khai & kiểm thử, chờ Owner duyệt Gate 4 `REVIEW.md`) |
| Tier | `Full` |
| Tiến độ chi tiết | `.sdd/specs/01-feat-foundation-jpa/PROGRESS.md` .. `.sdd/specs/08-feat-admin-users-reports/PROGRESS.md` |
| Nhánh Git | `feat/bookhub-mvp-01-08` |
| Cập nhật lần cuối | 2026-11-23 |

## 2. Sổ nháp tier Quick (bỏ trống nếu không phải Quick)
- **Mục tiêu (1 câu, đo được):** —
- **Phạm vi file:** —
- [ ] Tái hiện lỗi bằng test đỏ
- [ ] Sửa tối thiểu → test xanh
- [ ] `scripts/verify` xanh
- [ ] Ghi bài học vào `CLAUDE.md §5` nếu lỗi có thể lặp lại

## 3. Blockers / câu hỏi cho người
- ⬜ Duyệt Gate 4 (`REVIEW.md` cho các tính năng `01`..`08`), chuyển `PENDING` → `APPROVED` và đánh `✅` task cuối của từng `TASKS.md`.
- ⬜ Merge nhánh `feat/bookhub-mvp-01-08` vào `main` sau khi duyệt (và chuyển các spec `done` vào `.sdd/specs/_archive/` nếu muốn chốt bản MVP).
- ⬜ Kiểm tra giao diện trực quan 375/768/1280 trên trình duyệt thực khi chạy từ NetBeans 17 + Tomcat 10.1 (`http://localhost:8080/BookHub/`).

## 4. Handoff cho session kế tiếp
- **Đã xong:** Toàn bộ 8 tính năng MVP (`01`..`08`) trên nhánh `feat/bookhub-mvp-01-08`: 116 file nguồn Java, 25 routes (`RT-001`..`RT-025`), 34 file JSP (0 scriptlet, 100% JSTL 3.0 + EL 5.0), 9 bộ JUnit 5 tests (37/37 PASS trên SQL Server 2022 `BookHubDB`, gồm test concurrency checkout & cancel), live Tomcat 10.1 smoke test toàn bộ routes 0 lỗi `SEVERE`/`ELException`, `dist/BookHub.war` build sạch 0 warning, `scripts/verify.ps1` xanh.
- **Làm tiếp:** Sau khi Owner review & duyệt `REVIEW.md` → đánh `APPROVED`, đóng task cuối, merge vào `main` và thực hiện EPIC 9 (chụp ảnh báo cáo nộp PRJ301 nếu cần).
- **Cẩn thận:** Tuyệt đối không commit `bookhub-local.properties`; luôn biên dịch Ant với JDK 17 (`C:\Program Files\Java\jdk-17`).
