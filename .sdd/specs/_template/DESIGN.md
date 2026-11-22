# DESIGN — {{Tên feature}}

> **Bản thiết kế gọn cho 1 feature có UI** (`new-feature -Ui`). Agent tự soạn từ `CONTEXT.md` / `SPEC.md` / ảnh Stitch-Figma; **người liếc qua 30 giây và duyệt cùng Gate 1**.
> Giá trị mặc định (màu, spacing, form, a11y) đã có ở `.sdd/design/DESIGN.md` — **chỉ ghi những gì riêng của feature này**.

| Trường | Giá trị |
|---|---|
| Feature | `feat-{{slug}}` |
| Status | `DRAFT → LOCKED` |
| Tham chiếu Stitch/Figma | {{link hoặc đường dẫn ảnh trong `.sdd/design/references/` (ghi "không có" nếu AI tự đề xuất)}} |

## 1. Màn hình & Luồng đi
| ID | Màn hình / View | Vào từ đâu | Mục đích |
|---|---|---|---|
| SCR-01 | {{...}} | {{route / nút bấm}} | {{...}} |

```text
{{Luồng ngắn: Danh sách → bấm "Tạo" → SCR-01 → Lưu → Toast "Đã lưu" (hoặc báo lỗi tại chỗ)}}
```

## 2. Component & Token dùng
- **Dùng lại từ `components.md`:** {{vd: `Button (primary)`, `TextField`, `Modal`, `Toast`}}
- **Component mới (nếu bắt buộc):** {{không có | Tên + lý do}}
- **Token mới ngoài `tokens.css`?** {{Không | đề xuất: ... (đổi token = tier Full)}}

## 3. Trạng thái, Responsive & Copy (chỉ ghi điểm riêng)
- **Trạng thái (ngoài mặc định ở `.sdd/design/DESIGN.md §3`):**
  - **Loading:** {{vd: skeleton 3 dòng / nút Lưu loading}}
  - **Empty:** {{vd: "Chưa có việc nào." + nút "Tạo việc"}}
  - **Error:** {{vd: lỗi 422 hiện ngay dưới ô nhập, focus vào ô lỗi}}
- **Responsive (375 vs 1280):** {{vd: 375 hiện dạng bottom-sheet 1 cột, 1280 hiện modal 480px}}
- **Copy / i18n chính:** {{liệt kê 3–5 câu chữ quan trọng: tiêu đề nút, thông báo lỗi/thành công}}
- **a11y cần lưu ý:** {{vd: Esc đóng modal, trả focus về nút mở}}
- **SEO/AEO/GEO (chỉ trang công khai):** {{title ≲ 60 · description ≤ 160 · H1 · loại schema · câu hỏi chính + đáp án 40–60 từ · nguồn/số liệu thật — xem `.sdd/seo/SEO.md`; app nội bộ ghi "noindex"}}

## 4. Tiêu chí nghiệm thu bằng mắt (VAC) & Duyệt
- [ ] **VAC-01:** Ở 375px và 1280px, bố cục khớp ảnh tham chiếu (nếu có), không cuộn ngang, chữ/tương phản rõ.
- [ ] **VAC-02:** Hiển thị đúng trạng thái Loading / Empty / Error theo mục 3.
- **Duyệt (cùng Gate 1):** {{tên}} · {{YYYY-MM-DD}}
