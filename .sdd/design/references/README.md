# references/ — Ảnh thiết kế, nguồn Figma & baseline đối chiếu

- Đặt ảnh chụp từ **Figma / Stitch** (ảnh chuẩn `baseline/`) vào đây để AI đọc và đối chiếu khi code JSP/CSS: `<scr-id>__<màn-hình>__<375|1280>.png`.
- **Gói nén `.zip` từ Figma/Stitch:** đặt vào `references/incoming/` (đã nằm trong `.gitignore`). Đọc xong xóa `.zip`, chỉ giữ ảnh PNG trong `baseline/` — **không copy code React/Tailwind thô vào `web/`**.

## 1. Nguồn thiết kế chính thức
| Phạm vi | Công cụ | Link thiết kế | Trạng thái | Ghi chú |
|---|---|---|---|---|
| Toàn bộ 25 view BookHub MVP (`SCR-01` → `SCR-23`, gồm `SCR-03b`, `SCR-12b`) | Figma Make | `https://agenda-kale-62994833.figma.site` | Đã chốt & đồng bộ (`2026-11-23`) | Token đã trích vào `.sdd/design/tokens.css`, đặc tả 25 màn hình ở `docs/ui-screens.md` |

## 2. Danh mục ảnh chụp Baseline (`.sdd/design/references/baseline/`)
| File ảnh | Màn hình | Viewport | Mục đích đối chiếu |
|---|---|---|---|
| `baseline/scr-01__home__1280.png` | SCR-01 Trang chủ | 1280px | Hero teal, 6 danh mục, 3 kệ 8 sách, header/footer |
| `baseline/scr-01__home__375.png` | SCR-01 Trang chủ | 375px | Header 2 hàng mobile, 2 cột danh mục/sách |
| `baseline/scr-02__books-catalogue__1280.png` | SCR-02 Danh mục sách | 1280px | Sidebar lọc 3/12, filter chips, grid 3 cột, pagination, empty state |
| `baseline/scr-02__books-catalogue__375.png` | SCR-02 Danh mục sách | 375px | Bố cục 1 cột mobile, bộ lọc xếp trên lưới sách |
| `baseline/scr-03__book-detail__1280.png` | SCR-03 Chi tiết sách | 1280px | Chi tiết, tồn kho, đánh giá + form, hỏi đáp + form, sách tương tự |
| `baseline/scr-05__login__1280.png` | SCR-05 Đăng nhập | 1280px | Auth Shell căn giữa, alert lỗi chung, không OAuth |
| `baseline/scr-08__cart__1280.png` | SCR-08 Giỏ hàng | 1280px | Danh sách sản phẩm + stepper + tóm tắt tạm tính + empty state |
| `baseline/scr-09__checkout-cod__1280.png` | SCR-09 Thanh toán COD | 1280px | Form nhận hàng, khóa COD, phí ship 30.000 ₫, tóm tắt đơn |
| `baseline/scr-14__admin-dashboard__1280.png` | SCR-14 Admin Dashboard | 1280px | Sidebar `#164238`, 4 KPI, 2 thẻ chờ duyệt, 2 bảng tổng hợp |
| `baseline/scr-14__admin-dashboard__375.png` | SCR-14 Admin Dashboard | 375px | Topbar nút drawer off-canvas, KPI xếp 1 cột |
| `baseline/scr-18__admin-orders__1280.png` | SCR-18 Quản lý đơn hàng | 1280px | Bộ lọc đơn, bảng đơn, panel chi tiết snapshot + nút state machine |
| `baseline/scr-22__admin-reports__1280.png` | SCR-22 Báo cáo & thống kê | 1280px | Bộ lọc ngày, 4 KPI SQL, bảng MIN/MAX/AVG giá, thanh COUNT, Top 10 |

> Thư mục này được mở ngoại lệ trong `.agentignore` để AI đọc được ảnh đối chiếu trực quan khi kiểm thử UI (`prompts/13-ui-implement.md` & `14-ui-review.md`). Không để dữ liệu cá nhân thật/PII trong ảnh.
