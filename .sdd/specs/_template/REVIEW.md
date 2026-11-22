# REVIEW — {{Tên feature}}

> Review **độc lập** (agent/session khác, hoặc người) — prompt: `.sdd/prompts/06-review.md`. Reviewer **không sửa code**.
> Tier Full: bắt buộc `APPROVED` trước khi merge. Tier Standard: khuyến nghị.

| Trường | Giá trị |
|---|---|
| Reviewer | {{agent/người}} · Ngày: {{YYYY-MM-DD}} |
| Commit/nhánh | {{sha}} / `feat/{{slug}}` |
| **Kết luận** | `PENDING` → `APPROVED` hoặc `CHANGES_REQUESTED` |

## 1. Bằng chứng chạy thật
```text
$ scripts/verify
{{dán output}}
```

## 2. Truy vết
| Yêu cầu | Code | Test (`@spec`) | OK? |
|---|---|---|---|
| FR-01 | | {{slug}}/AC-01 | ⬜ |

## 3. Kiểm tra theo hiến pháp
- [ ] Không vi phạm Out of Scope · [ ] Không vi phạm constitution/constraints (nêu ID nếu vi phạm)
- [ ] Bảo mật (`skills/api-security.md`) · [ ] Hiệu năng/SQL (`skills/sql-performance.md`)
- [ ] Xử lý lỗi (`skills/error-handling.md`) · [ ] Test chất lượng (`skills/testing.md`), có test đường lỗi
- [ ] Docs/contract/`shared_context.md` đã cập nhật · [ ] Migration có `down`

## 3b. UI (chỉ khi `UI = Có`) — reviewer tự chụp, không dùng ảnh của người viết code
| Màn hình · trạng thái | 375 | 768 | 1280 | Khớp thiết kế? |
|---|---|---|---|---|
| SCR-01 · default | ảnh | ảnh | ảnh | ⬜ |
- [ ] VAC đạt hết · [ ] Luật U-01…U-08 (không hardcode, đủ trạng thái, i18n, bàn phím/alt) · [ ] `A11Y_CMD` (nếu có)

## 4. Phát hiện
| # | Mức | File:dòng | Vấn đề | Cách sửa đề xuất | Trạng thái |
|---|---|---|---|---|---|
| 1 | 🔴/🟡/🟢 | | | | ⬜ |

## 5. Phê duyệt cuối
{{tên người}} · {{YYYY-MM-DD}}
