---
name: architecture-guard
description: Cưỡng chế kiến trúc phân tầng bằng công cụ (cấm import ngược chiều, ranh giới module). Kích hoạt khi thêm module/tầng, sửa quy tắc phụ thuộc, hoặc CI báo vi phạm kiến trúc.
---

# Skill: Architecture Guard

Luật `A-01..A-03` trong constitution phải được **máy kiểm**, không chỉ ghi chữ. Chọn công cụ theo stack, chạy trong `LINT_CMD` hoặc `TEST_CMD` để `scripts/verify` bắt được.

## Quy tắc cần cưỡng chế
- `domain/` không import `usecase|interface|infra` hay framework/DB/HTTP.
- `usecase/` chỉ import `domain/` và port của chính nó.
- `interface/` gọi `usecase/`; không import `infra/` trực tiếp.
- `infra/` implement port; không bị `domain|usecase` import.
- Module A không import nội bộ module B (chỉ qua API công khai/port).

## Công thức theo stack
| Stack | Công cụ | Gợi ý |
|---|---|---|
| TypeScript/JS | `dependency-cruiser` | Rule `forbidden` theo thư mục `src/domain` → `src/(usecase\|interface\|infra)`; script `depcruise src --config` đưa vào `LINT_CMD` |
| Python | `import-linter` | Contract `layers`/`forbidden` trong `pyproject.toml`; chạy `lint-imports` |
| Java/Kotlin | `ArchUnit` | Test `layeredArchitecture()` chạy cùng test suite |
| Go | `go-arch-lint` hoặc test `go list -deps` | Kiểm phụ thuộc theo package |
| .NET | `NetArchTest` | Test phụ thuộc giữa assembly/namespace |

## Quy trình thêm cưỡng chế
1. Viết rule vi phạm cố ý → thấy máy báo đỏ → xóa vi phạm → xanh.
2. Thêm lệnh vào `.sdd/project.conf` (`LINT_CMD`), ghi vào `CLAUDE.md §2`.
3. Ghi dòng `→ enforced: <đường dẫn cấu hình>` ở bài học tương ứng trong `CLAUDE.md §5`.

## Checklist
- [ ] Mỗi luật A-xx liên quan có rule máy · [ ] CI chạy rule · [ ] Có test minh họa vi phạm bị bắt
