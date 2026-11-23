# ADR-005: Thanh toán COD duy nhất & Phí vận chuyển cố định trong MVP

| Trường | Giá trị |
|---|---|
| Status | `Accepted` |
| Ngày | 2026-11-23 |
| Người quyết định | Nhóm phát triển BookHub (PRJ301) |
| Liên quan | `.sdd/constraints/business.md (BR-ORD-001)`, `docs/roadmap.md` |

## 1. Bối cảnh
Dự án BookHub là MVP 4 tuần cho môn PRJ301. Tích hợp cổng thanh toán trực tuyến (VNPay/MoMo/Stripe), webhook đối soát, hoàn tiền tự động và tính phí ship theo vùng/khối lượng sẽ làm phình phạm vi và rủi ro trễ tiến độ.

## 2. Các phương án
| # | Phương án | Ưu | Nhược | Chi phí/Rủi ro |
|---|---|---|---|---|
| 1 | Chỉ hỗ trợ `COD`, phí ship cố định `30.000 VND/đơn` (lưu snapshot) | Gọn, dễ kiểm thử transactional checkout/hủy đơn, không cần sandbox đối tác ngoài | Chưa có thanh toán online | Thấp |
| 2 | Tích hợp cổng thanh toán sandbox (VNPay/MoMo) | Gần thực tế hơn | Thêm luồng callback/IPN, trạng thái thanh toán & hoàn tiền phức tạp | Cao (scope creep) |

## 3. Quyết định
Chọn phương án **#1 (COD-only + phí ship baseline `30.000 VND/đơn`)**, đồng thời lưu `payment_method` và `shipping_fee` vào từng `Order` để có thể mở rộng sau MVP mà không phá vỡ dữ liệu cũ.

## 4. Hệ quả & đánh đổi (Trade-offs)
- ➕ Tập trung hoàn thiện chất lượng giao dịch tồn kho, snapshot đơn hàng và state machine.
- ➖ Không có module thanh toán trực tuyến/đối soát/hoàn tiền trong MVP.

## 5. Cách kiểm chứng & điều kiện xem xét lại
- Thước đo thành công: DB constraint `CK_Orders_payment CHECK (payment_method = 'COD')` và `total_amount = subtotal + shipping_fee` hoạt động nhất quán.
- Xem lại khi: Sau khi hoàn tất toàn bộ Must-have MVP và chuyển sang giai đoạn mở rộng.
