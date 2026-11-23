# ADR-008: Lưu Snapshot Tên Sách và Đơn Giá trong `OrderItems`

| Trường | Giá trị |
|---|---|
| Status | `Accepted` |
| Ngày | 2026-11-23 |
| Người quyết định | Nhóm phát triển BookHub (PRJ301) |
| Liên quan | `.sdd/constraints/business.md (BR-03, BR-ORD-002)`, `constitution A-05` |

## 1. Bối cảnh
Admin có thể cập nhật tên sách hoặc điều chỉnh giá bán trong bảng `Books` sau khi khách hàng đã đặt mua. Nếu `OrderItems` chỉ tham chiếu `book_id` mà không lưu snapshot, chi tiết đơn cũ và tổng doanh thu lịch sử sẽ bị biến đổi sai lệch.

## 2. Các phương án
| # | Phương án | Ưu | Nhược | Chi phí/Rủi ro |
|---|---|---|---|---|
| 1 | Lưu `book_title_snapshot`, `unit_price`, `line_total` trực tiếp trong `OrderItems` (kèm `book_id` FK) và `recipient_*`/`shipping_fee` trong `Orders` | Lịch sử đơn và doanh thu bất biến 100%, query xem đơn nhanh | Dư lặp dữ liệu có chủ đích trong bảng `OrderItems` | Rất thấp |
| 2 | Chỉ join bảng `Books` để lấy tên/giá hiện tại | Ít cột hơn | Sai lệch hóa đơn & doanh thu lịch sử ngay khi sách đổi giá | Rất cao |

## 3. Quyết định
Chọn phương án **#1**: lưu snapshot tên sách, đơn giá và thành tiền dòng trong `OrderItems` ngay trong transaction checkout, đồng thời giữ `book_id` FK để phục vụ thống kê bestseller.

## 4. Hệ quả & đánh đổi (Trade-offs)
- ➕ Sửa tên/giá hoặc ngừng kinh doanh sách không bao giờ làm thay đổi hóa đơn lịch sử của khách hàng hay báo cáo của Admin.
- ➖ `OrderItems` là bản ghi bất biến (immutable); Admin không được phép sửa giá/số lượng dòng đơn sau khi đơn đã tạo.

## 5. Cách kiểm chứng & điều kiện xem xét lại
- Thước đo thành công: `TC-DB-02` — đổi tên và giá một cuốn sách sau khi đặt đơn, kiểm tra lại chi tiết đơn hàng cũ vẫn hiển thị đúng tên và giá tại thời điểm mua.
- Xem lại khi: Không thay đổi — đây là chuẩn bắt buộc cho hệ thống e-commerce.
