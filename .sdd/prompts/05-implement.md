# 05 — Implement (Pha 4)

```text
Làm task <T-00x> của <NN>-feat-<slug>. Tuân AGENTS.md §5 (Plan→Act→Check→Report).
Đọc: TASKS.md (task này), SPEC (các FR/AC liên quan), PLAN (tầng liên quan), PROGRESS.md, skills khớp domain.
Quy trình bắt buộc:
1) Cập nhật PROGRESS.md (mục tiêu + checklist). Điều kiện: SPEC LOCKED + TASKS đã duyệt, nếu không → dừng.
2) Viết test đỏ cho AC, gắn thẻ `@spec <slug>/AC-xx`. Chạy để thấy đỏ.
3) Trước khi tạo hàm/file mới → tìm trong repo xem đã có chưa (luật S-11). Code tối thiểu cho xanh; không thêm gì ngoài task; không đụng Out of Scope.
4) Dọn rác trước khi kiểm tra: xóa code comment-out, import/biến thừa, `console.log`/file nháp. Giữ file ≲ 300 dòng.
5) Chạy `scripts/verify` — phải xanh. Không sửa/skip test để qua.
6) Đánh dấu task trong TASKS.md, cập nhật PROGRESS.md, báo cáo: file đổi · cách verify · rủi ro.
Nếu phải đổi SPEC/contract/dependency/schema → DỪNG và hỏi. Kẹt 3 lần → ghi Blockers và hỏi.
```
