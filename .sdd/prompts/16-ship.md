# 16 — Ship (push an toàn + PR + auto-merge)

```text
Tôi muốn ship công việc hiện tại. Tuân AGENTS.md §4 và .sdd/constraints/safety.md.
1) Kiểm tra: đang ở nhánh `type/slug` (feat|fix|chore|docs|refactor|perf|test|build|ci). Nếu đang ở main/master → DỪNG, tạo nhánh mới.
2) Đảm bảo checklist Definition of Done (AGENTS.md §7) đã đạt; cập nhật PROGRESS.md/TASKS.md/CHANGELOG.
3) Chạy ĐÚNG: `./scripts/ship.ps1 -Message "<type(scope): mô tả tiếng Anh>"` (hoặc `./scripts/ship.sh "<...>"`). KHÔNG tự chạy `git push` thủ công, KHÔNG `--force`, KHÔNG `--no-verify`, KHÔNG push main.
4) Nếu verify đỏ → sửa nguyên nhân gốc rồi chạy lại (không sửa/skip test để qua).
5) Báo cáo: nhánh · commit · link PR · trạng thái auto-merge. CI xanh thì GitHub tự merge; deploy & release do CI/CD (docs/ci-cd.md) — KHÔNG deploy tay.
```
