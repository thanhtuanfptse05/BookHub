# constraints/safety.md — Hàng rào an toàn (Guardrails)

> Ưu tiên **cao nhất** trong mọi tài liệu. Không có ngoại lệ trừ khi **người** xác nhận rõ ràng bằng văn bản trong session ("tôi cho phép X, một lần").

## 1. Git & môi trường
- ⛔ Cấm commit/push trực tiếp vào `main`, `release/*`, `prod`. Chỉ qua PR.
- ✅ **Được phép:** push nhánh `feat|fix|chore|docs|refactor|perf|test|build|ci/<slug>` **chỉ bằng `scripts/ship`** (verify đầy đủ xanh trước, không force) rồi mở PR + bật auto-merge. Vào `main` do GitHub auto-merge khi CI xanh (branch protection).
- ⛔ Cấm `git push --force`, `git reset --hard`, `git clean -fdx`, `git rebase` trên nhánh chung.
- ⛔ Cấm sửa lịch sử đã push; cấm `--no-verify` để bỏ qua hook.
- ⛔ Cấm chạy lệnh nhắm vào môi trường **staging/production** (deploy, migrate, seed, kubectl, terraform apply…). **Deploy/release chỉ do CI/CD** (`.github/workflows/deploy.yml`, `release.yml`) thực hiện sau khi CI xanh — Agent không deploy tay.

## 2. Dữ liệu
- ⛔ Cấm `DROP TABLE/DATABASE`, `TRUNCATE`, `DELETE`/`UPDATE` không có `WHERE`.
- ⛔ Cấm sửa migration đã chạy; muốn đổi ⇒ tạo migration mới trong `database/`.
- ⛔ Migration phá hủy (drop cột/bảng) phải 2 bước: ngừng dùng → (release sau) mới xóa; có backup + `down`.
- ⛔ Cấm kết nối DB production từ máy dev/Agent; không expose SQL Server port `1433` ra Internet.

## 3. File & hệ thống
- ⛔ Cấm xóa file/thư mục ngoài phạm vi task; cấm `rm -rf`, `del /s`, `Remove-Item -Recurse -Force` trên đường dẫn rộng.
- ⛔ Cấm đọc/ghi ngoài thư mục dự án; cấm đọc file khớp `.agentignore`.
- ⛔ Cấm cài đặt gói toàn cục / sửa cấu hình hệ thống.
- ⛔ Cấm tải & chạy script từ internet (`curl | sh`) khi chưa được duyệt.

## 4. Bí mật
- ⛔ Cấm in/ghi secret vào: code, log, commit, PR, tài liệu, chat — bao gồm `bookhub-local.properties`, mật khẩu DB SQL Server, `persistence.xml` có password thật, mật khẩu user hay reset token.
- Phát hiện secret bị lộ ⇒ **dừng**, báo người, đề xuất rotate; không tự "xóa lịch sử".

## 5. Chất lượng
- ⛔ Cấm xóa/skip/làm yếu test hoặc hạ ngưỡng coverage để qua CI.
- ⛔ Cấm tắt lint/security rule hàng loạt; cấm tắt `<scripting-invalid>true</scripting-invalid>` để viết scriptlet JSP.
- ⛔ Cấm bịa kết quả ("tests pass") khi chưa chạy thật.

## 6. Mạng & dịch vụ ngoài
- Chỉ gọi dịch vụ trong allow-list: `localhost:1433` (SQL Server local `BookHubDB`), `localhost:8080` (Tomcat local), và SMTP tùy chọn khi được cấu hình. Dịch vụ mới ⇒ hỏi người.
- Không gửi mã nguồn/dữ liệu dự án lên dịch vụ ngoài chưa duyệt.

## 7. Hành động cần xác nhận của người (human-in-the-loop)
Xóa dữ liệu · đổi schema DB · đổi contract/route công khai · thêm JAR dependency mới · đổi CI/CD · đụng auth/phân quyền/checkout transaction · bất kỳ việc nào **không thể hoàn tác**.

## 8. Khi nghi ngờ
**Dừng → hỏi.** Chi phí hỏi < chi phí sự cố.
