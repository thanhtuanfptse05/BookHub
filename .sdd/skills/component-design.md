---
name: component-design
description: Thiết kế & dùng component UI theo design system/token, trung lập stack. Kích hoạt khi tạo component mới, thêm variant, hoặc ánh xạ thiết kế sang code.
---

# Skill: Component Design

## Quy trình
1. **Tra `design/components.md`**: đã có chưa? Đủ qua variant/composition chưa? → dùng lại.
2. Chưa có: khai báo trong `DESIGN.md` feature §5 → được duyệt → mới tạo.
3. Tạo xong: thêm dòng vào `components.md`, kèm ví dụ sử dụng + test hành vi chính.

## Thiết kế API
- Prop theo **hành vi/ngữ nghĩa**: `variant`, `size`, `disabled`, `loading`, `invalid`. Không `isBlue`, `bigPadding`.
- Ít prop; mở rộng bằng **composition** (children/slot) thay vì cờ boolean cho mỗi trường hợp.
- Có giá trị mặc định hợp lý; component dùng được với 0–1 prop bắt buộc.
- Điều khiển (controlled) và không điều khiển (uncontrolled) rõ ràng cho input.
- Chuyển tiếp thuộc tính HTML chuẩn (`className/class`, `aria-*`, `data-*`, `id`) tới phần tử gốc.

## Token & kiểu dáng
- Mọi giá trị qua token: `var(--color-*)`, `--space-*`, `--radius-*`, `--text-*`, `--shadow-*`, `--motion-*`.
- Dùng **vai trò** (`--color-danger`) không dùng giá trị (`#b91c1c`). Cấm inline style tĩnh.
- Biến thể nhà thiết kế gọi tên gì, code gọi tên đó.

## Trạng thái bắt buộc
default · hover · `:focus-visible` · active · disabled · (loading · error · selected nếu áp dụng). Kiểm cả chế độ tối.

## Phân tầng
- **Presentational** (hiển thị, không biết nghiệp vụ/API) và **container** (lấy dữ liệu, nối usecase). Component hiển thị không gọi API.
- Tuân kiến trúc: UI nằm ở `interface/`, không import `infra/` trực tiếp (luật A-01).

## Tránh
- Sao chép component với khác biệt nhỏ → thêm variant.
- Component "thần" nhận 20 prop → tách.
- Logic định dạng ngày/tiền rải rác → dùng tiện ích theo locale (`.sdd/design/DESIGN.md §1`).

## Checklist
- [ ] Đã tra components.md · [ ] Chỉ dùng token · [ ] Đủ trạng thái · [ ] Truy cập được
- [ ] Có ví dụ + test · [ ] Đã cập nhật danh mục
