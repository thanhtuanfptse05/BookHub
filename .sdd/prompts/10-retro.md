# 10 — Retro: chốt bài học

```text
Nhìn lại feat-<slug> / sự cố <mô tả>. Đọc PROGRESS.md, CHANGELOG.md, REVIEW.md, git log của nhánh.
1) Liệt kê: điều gì làm chậm/sai (spec mơ hồ? thiếu test? hiểu sai nghiệp vụ? agent tự bịa?).
2) Với mỗi bài học: thêm 1 dòng vào CLAUDE.md §5 (Ngày | Sự cố | Nguyên nhân gốc | Quy tắc).
3) Bài học nào có thể máy kiểm được → đề xuất cụ thể: lint rule / test kiến trúc / hook / mục trong skill. Bài học đã lặp ≥ 2 lần → BẮT BUỘC đề xuất.
4) Gộp các dòng §5 trùng chủ đề nếu > 20 dòng. Đề xuất cập nhật SPEC template hoặc skill nếu phát hiện thiếu sót có tính hệ thống.
5) Dọn rác & đồng bộ cuối feature: cập nhật `docs/business-flows.md` (trace luồng đang chạy từ `src/`) và `docs/roadmap.md` (`roadmap-sync`); chuyển `feat-<slug>` (nếu done/dropped) vào `.sdd/specs/_archive/`; xóa `.tmp/` & gói `.zip` trong `references/incoming/`; reset `plan.md` ở gốc nếu đã xong việc.
Chỉ đề xuất thay đổi constitution/constraints, không tự sửa.
```
