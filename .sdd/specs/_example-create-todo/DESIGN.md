# DESIGN — Tạo công việc (create-todo)

> VÍ DỤ mẫu gọn 4 mục cho feature có UI.

| Trường | Giá trị |
|---|---|
| Feature | `feat-create-todo` |
| Status | LOCKED |
| Tham chiếu Stitch/Figma | `.sdd/design/references/create-todo__form__375.png` (giả định) |

## 1. Màn hình & Luồng đi
| ID | Màn hình | Vào từ đâu | Mục đích |
|---|---|---|---|
| SCR-01 | Form tạo việc | Nút "Tạo việc" ở trang danh sách | Nhập tiêu đề + hạn chót (tùy chọn) |

```text
Danh sách → bấm "Tạo việc" → SCR-01 → nhập → bấm "Lưu" → đóng form + Toast "Đã lưu."
                                              └─ lỗi 422 → hiện lỗi dưới ô → sửa
```

## 2. Component & Token dùng
- **Dùng lại từ `components.md`:** `TextField`, `Button (primary, loading)`, `Modal`, `Toast (success)`
- **Component mới:** Không có
- **Token mới ngoài `tokens.css`?** Không

## 3. Trạng thái, Responsive & Copy
- **Trạng thái:**
  - **Loading:** Nút "Lưu" hiện loading + chặn bấm lặp khi đang gửi API.
  - **Empty:** Trang danh sách khi trống hiện *"Chưa có việc nào. Tạo việc đầu tiên để bắt đầu."* + nút *"Tạo việc"*.
  - **Error:** Tiêu đề rỗng / > 200 ký tự hiện lỗi ngay dưới ô và focus vào ô đó.
- **Responsive:** 375px mở dạng bottom-sheet full-width, nút Lưu dính đáy; 768/1280px mở Modal rộng 480px giữa màn hình.
- **Copy / i18n chính:** `todo.create.title.label` ("Tiêu đề") · `todo.create.submit` ("Lưu") · `todo.create.title.error.tooLong` ("Tiêu đề quá dài. Hãy rút gọn dưới 200 ký tự.") · `todo.create.success` ("Đã lưu.")
- **a11y cần lưu ý:** Auto-focus ô tiêu đề khi mở; Esc đóng modal và trả focus về nút "Tạo việc"; lỗi gắn `aria-describedby`.

## 4. Tiêu chí nghiệm thu bằng mắt (VAC) & Duyệt
- [ ] **VAC-01:** 375px & 1280px khớp layout tham chiếu, nút Lưu đủ vùng chạm ≥ 44px, không cuộn ngang.
- [ ] **VAC-02:** Gửi tiêu đề 201 ký tự hiện viền `--color-danger` + thông báo lỗi ngay dưới ô.
- **Duyệt (cùng Gate 1):** Trần B · 2026-01-10
