# Quy Trình Nghiệp Vụ — {{PROJECT_NAME}} (hiện trạng đang chạy)

> **Loại:** Mô tả hệ thống **đang chạy thật (As-Is)**, KHÔNG phải yêu cầu mới (yêu cầu mới nằm ở `.sdd/specs/feat-<slug>/SPEC.md`).
> **Nguồn:** Trace trực tiếp từ `src/` (ưu tiên dùng `codegraph_explore` khi `.codegraph/` tồn tại). Cập nhật file này cùng PR khi hoàn thành feature hoặc thay đổi luồng nghiệp vụ.

| Mục | Giá trị |
|---|---|
| Cập nhật lần cuối | {{YYYY-MM-DD}} |
| Nguồn trace | `src/` |

## Mục lục quy trình
| Mã | Quy trình | Ai thực hiện | Trạng thái | Spec gốc |
|---|---|---|---|---|
| `QT-01` | {{vd: Đăng ký & đăng nhập}} | {{Khách / User}} | ⬜ | `feat-{{slug}}` |

---

## QT-01 — {{Tên quy trình}}
- **Ai:** {{actor / role}}
- **Đầu vào / Điều kiện:** {{...}}
- **Luồng chạy thực tế:**
  1. {{Bước 1 — vd: Client gửi `POST /api/v1/...`}}
  2. {{Bước 2 — validate DTO → usecase kiểm quyền & nghiệp vụ}}
  3. {{Bước 3 — ghi DB / phát event → trả kết quả}}
- **Ngoại lệ / Lỗi chính:** {{401 / 403 / 409 / 422…}}
- **Dev (đường dẫn file thật tính từ `src/`):**
  - Entry (`interface/`): `{{interface/...}}`
  - Logic (`usecase/` & `domain/`): `{{usecase/...}}`, `{{domain/...}}`
  - Data (`infra/`): `{{infra/...}}`
  - Test: `{{tests/...}}`
