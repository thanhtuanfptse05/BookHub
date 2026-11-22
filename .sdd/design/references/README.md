# references/ — Ảnh thiết kế, gói bàn giao & baseline

- Đặt ảnh chụp từ **Stitch / Figma** (hoặc ảnh chuẩn `baseline/`) vào đây để AI đọc và đối chiếu khi code: `<slug>__<màn-hình>__<375|1280>.png`.
- **Gói nén `.zip` từ Stitch/Figma:** đặt vào `references/incoming/` (đã nằm trong `.gitignore` để không phình Git). Agent giải nén đọc cấu trúc/assets rồi **xóa `.zip`**, chỉ giữ lại ảnh PNG nhẹ trong `baseline/` — **tuyệt đối không copy code HTML/CSS rác vào `src/`**.

| Feature | Công cụ | Link Stitch / Figma | Ghi chú |
|---|---|---|---|
| {{...}} | Stitch / Figma | {{url}} | |

> Thư mục này được mở ngoại lệ trong `.agentignore` để AI đọc được ảnh. Không để dữ liệu thật/PII trong ảnh.
