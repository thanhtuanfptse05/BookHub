---
name: ui-accessibility
description: Kỹ thuật truy cập (a11y) khi viết UI trung lập stack — HTML ngữ nghĩa, focus, ARIA, form, modal, live region. Kích hoạt khi tạo/sửa component, form, modal, menu, bảng, hoặc khi review UI.
---

# Skill: UI Accessibility

## Khi nào dùng
Viết/sửa component tương tác, form, modal, dropdown, tab, bảng; review UI; sửa lỗi axe/Lighthouse.

## Nguyên tắc
1. **HTML ngữ nghĩa trước, ARIA sau.** `button` cho hành động, `a[href]` cho điều hướng, `label` + `input`, `ul/li` cho danh sách, `table` cho dữ liệu bảng. ARIA chỉ khi HTML không đủ; sai ARIA tệ hơn không ARIA.
2. **Điều khiển bằng bàn phím = mặc định.** Không `div onclick`. Thứ tự DOM = thứ tự Tab; tránh `tabindex > 0`.
3. **Focus luôn nhìn thấy** (`:focus-visible` + `--color-focus-ring`). Không `outline: none` nếu chưa thay thế.

## Mẫu thường gặp
- **Nút icon:** `aria-label="Đóng"` (hoặc chữ ẩn thị giác); icon trang trí `aria-hidden="true"`.
- **Form:** `label for`, lỗi trong phần tử có `id` + `aria-describedby` trên input + `aria-invalid="true"`; khi gửi lỗi → focus ô lỗi đầu tiên; tổng hợp lỗi `role="alert"`.
- **Modal:** `role="dialog"` + `aria-modal="true"` + nhãn (`aria-labelledby`); focus vào trong khi mở, nhốt Tab trong modal, Esc đóng, trả focus về phần tử đã mở.
- **Menu/Tab/Combobox:** theo mẫu APG của W3C (mũi tên di chuyển, Home/End, Enter/Space chọn); ưu tiên thư viện headless đã làm đúng.
- **Nội dung động:** `aria-live="polite"` cho toast/kết quả; `aria-busy` khi loading; không tự cướp focus.
- **Bảng:** `th scope`, `caption`; sắp xếp báo trạng thái bằng `aria-sort`.
- **Ảnh:** `alt` mô tả mục đích; trang trí `alt=""`.
- **Bỏ qua khối:** liên kết "Bỏ qua đến nội dung chính" ở đầu trang; landmark `header/nav/main/footer`.

## Màu & chuyển động
Tương phản chữ ≥ 4.5:1, UI ≥ 3:1 (cả chế độ tối); không chỉ dùng màu; tôn trọng `prefers-reduced-motion`.

## Kiểm
Tự động (axe/Lighthouse qua `A11Y_CMD`) + thủ công 3 phút (Tab hết trang, zoom 200%, tắt chuột). Ghi vào REVIEW.md.

## Checklist review
- [ ] Không `div/span` làm nút · [ ] Mọi input có nhãn · [ ] Focus thấy rõ & đúng thứ tự
- [ ] Modal quản lý focus · [ ] Lỗi gắn với ô và được thông báo · [ ] Không chỉ dùng màu
