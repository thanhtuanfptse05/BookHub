# ADR-007: Dùng Soft-Deactivate cho Books, Categories, Users, Reviews & Questions

| Trường | Giá trị |
|---|---|
| Status | `Accepted` |
| Ngày | 2026-11-23 |
| Người quyết định | Nhóm phát triển BookHub (PRJ301) |
| Liên quan | `.sdd/constraints/business.md (BR-01)`, `docs/architecture.md` |

## 1. Bối cảnh
`Books`, `Categories` và `Users` được tham chiếu bởi khóa ngoại từ `Orders`, `OrderItems`, `Reviews`, `Questions`, `Answers`. Việc xóa vật lý (`DELETE`) sẽ làm vỡ toàn vẹn tham chiếu hoặc làm mất lịch sử đơn hàng và báo cáo doanh thu.

## 2. Các phương án
| # | Phương án | Ưu | Nhược | Chi phí/Rủi ro |
|---|---|---|---|---|
| 1 | Soft-deactivate (`is_active = 0` cho Book/Category; `status = LOCKED/DISABLED` cho User; `HIDDEN` cho Review/Question) | Bảo toàn 100% lịch sử đơn hàng, FK và báo cáo thống kê; khôi phục được khi cần | Mọi truy vấn public phải luôn lọc `is_active = 1` / `status` hợp lệ | Thấp |
| 2 | Hard-delete + `ON DELETE CASCADE` | Giảm bản ghi cũ | Phá hủy lịch sử mua hàng, làm sai doanh thu và vi phạm toàn vẹn dữ liệu | Rất cao |

## 3. Quyết định
Chọn phương án **#1 (Soft-deactivate)** và không cấu hình `CascadeType.REMOVE` xuyên qua `Orders`/`OrderItems`/`Books`.

## 4. Hệ quả & đánh đổi (Trade-offs)
- ➕ Lịch sử bán hàng, báo cáo doanh thu và khóa ngoại luôn toàn vẹn; Admin có thể kích hoạt lại khi cần.
- ➖ Mọi truy vấn catalog công khai, thống kê giá/tồn kho sách active, và kiểm tra thêm giỏ/checkout phải lọc rõ `is_active = 1`.

## 5. Cách kiểm chứng & điều kiện xem xét lại
- Thước đo thành công: Deactivate Book đã có trong `OrderItems` không lỗi FK, sách biến mất khỏi public nhưng chi tiết đơn cũ vẫn nguyên vẹn (`TC-ADM-04`, `TC-ADM-05`).
- Xem lại khi: Có yêu cầu ẩn danh/xóa dữ liệu cá nhân theo chính sách lưu trữ production.
