# docs/

Tài liệu cho **con người**. Cập nhật cùng PR làm đổi hành vi.

| File | Nội dung | Nguồn |
|---|---|---|
| `business-flows.md` | Quy trình nghiệp vụ **hiện trạng đang chạy** (`QT-01`…), trace từ `src/` | Cập nhật sau mỗi feature (dùng `codegraph` trace) |
| `roadmap.md` | Lộ trình Now / Next / Later / Done | Đồng bộ cùng `.sdd/specs/README.md` khi đổi trạng thái |
| `architecture.md` | Sơ đồ hệ thống, luồng dữ liệu | Viết tay, link từ `CLAUDE.md §3` |
| `ci-cd.md` | Luồng ship, PR auto-merge, deploy, release, rollback | Viết tay |
| `runbook.md` | Vận hành, rollback, xử lý sự cố | Viết tay |
| `openapi.yaml` | API contract | **Sinh từ code** nếu framework hỗ trợ rồi commit; đừng viết tay song song |

## Chống lệch tài liệu
- Thêm bước sinh OpenAPI vào `BUILD_CMD` hoặc `LINT_CMD` và để CI báo diff nếu file sinh ra khác bản đã commit.
- `shared_context.md` chỉ ghi **tóm tắt** + link tới `openapi.yaml`, không chép chi tiết.
