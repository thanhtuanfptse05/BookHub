# 07 — Debug

```text
Triệu chứng: <mô tả, log, bước tái hiện>. Đọc AGENTS.md, CLAUDE.md §5 (bài học cũ), skills/error-handling.md.
Quy trình — KHÔNG sửa trước khi hiểu:
1) Tái hiện bằng test đỏ (hoặc lệnh tái hiện tối thiểu).
2) Nêu ≥ 3 giả thuyết, xếp theo khả năng, kèm cách kiểm chứng từng cái; kiểm chứng bằng bằng chứng (log/test/bisect), không đoán.
3) Xác định nguyên nhân gốc (không chỉ triệu chứng). Nêu tại sao các test hiện có không bắt được.
4) Sửa tối thiểu → test xanh → scripts/verify. Nếu chạm vùng Full → dừng và nâng tier.
5) Thêm dòng vào CLAUDE.md §5; nếu lỗi có thể chặn bằng máy → đề xuất lint/test/hook.
Cùng một lỗi 3 lần chưa xong → dừng, báo những gì đã loại trừ.
```
