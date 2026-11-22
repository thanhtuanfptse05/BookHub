# PLAN — {{Tên feature}}

> **Pha 2 · Agent lập kế hoạch kiến trúc** SAU KHI SPEC = LOCKED. Người duyệt (Gate 2) trước khi phân rã task.
> **Không viết code implement trong file này** — chỉ thiết kế.

| Trường | Giá trị |
|---|---|
| SPEC version | {{x.y.z}} |
| Status | `DRAFT → APPROVED` |

## 1. Tóm tắt giải pháp
Hướng tiếp cận trong ≤ 5 câu; vì sao chọn. Nếu là quyết định lớn ⇒ tạo ADR và link: `rfcs/ADR-{{NNN}}`.

## 2. Phương án đã cân nhắc
| Phương án | Ưu | Nhược | Chọn? |
|---|---|---|---|
| A | | | ✅ |
| B | | | ❌ vì {{...}} |

## 3. Kiến trúc & luồng xử lý
```text
{{Sơ đồ/sequence dạng text hoặc mermaid: Client → interface → usecase → port → infra}}
```

## 4. Thay đổi theo tầng
| Tầng | File tạo mới | File sửa | Ghi chú |
|---|---|---|---|
| `domain/` | | | |
| `usecase/` | | | |
| `interface/` | | | |
| `infra/` | | | |
| `tests/` | | | |
| `docs/` | | | |

## 5. Data model & Migration
- Bảng/cột/index mới: {{...}}
- Migration `up/down`: {{...}} · Backfill: {{...}} · Tương thích ngược: {{...}}

## 6. API / Contract & ảnh hưởng chéo
- Endpoint/event mới hoặc đổi: {{...}} → cập nhật `shared_context.md` & OpenAPI.
- Breaking change? `có/không` — nếu có: kế hoạch migrate: {{...}}

## 7. Truy vết SPEC → thiết kế
| Yêu cầu | Thành phần đáp ứng | Test dự kiến |
|---|---|---|
| FR-01 | | AC-01 |
| NFR-01 | | |
| EC-01 | | |

## 8. Chiến lược kiểm thử
Unit: {{...}} · Integration: {{...}} · E2E: {{...}} · Dữ liệu test/fixture: {{...}}

## 9. Rủi ro & giảm thiểu
| # | Rủi ro | Xác suất | Tác động | Giảm thiểu / Rollback |
|---|---|---|---|---|
| R1 | | | | |

## 10. Rollout & Quan sát
Feature flag: {{tên}} · Triển khai từng bước: {{...}} · Metric/log/alert: {{...}} · Rollback: {{...}}

## 11. ❓ Câu hỏi cho con người
- [ ] Q1: {{...}}

## 12. Phê duyệt (Gate 2)
{{tên}} · {{YYYY-MM-DD}}
