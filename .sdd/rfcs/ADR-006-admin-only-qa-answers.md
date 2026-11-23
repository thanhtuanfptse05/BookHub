# ADR-006: Chỉ Admin trả lời Q&A (tối đa 1 câu trả lời mỗi câu hỏi)

| Trường | Giá trị |
|---|---|
| Status | `Accepted` |
| Ngày | 2026-11-23 |
| Người quyết định | Nhóm phát triển BookHub (PRJ301) |
| Liên quan | `.sdd/constraints/business.md (BR-QA-002)`, PRJ301 mục 3.8 |

## 1. Bối cảnh
Mục 3.8 của đề PRJ301 (2.0 điểm) yêu cầu module Q&A gồm đặt câu hỏi, trả lời, duyệt và quản lý. BookHub là cửa hàng single-store với 3 vai trò `Guest`, `Customer`, `Admin`.

## 2. Các phương án
| # | Phương án | Ưu | Nhược | Chi phí/Rủi ro |
|---|---|---|---|---|
| 1 | Customer đặt câu hỏi (`OPEN`); chỉ Admin trả lời (`ANSWERED`, tối đa 1 `Answer`/`Question`) hoặc ẩn (`HIDDEN`) | Workflow đơn giản, kiểm soát chất lượng câu trả lời chính thức của cửa hàng, không phát sinh vai trò Moderator/Seller | Chưa hỗ trợ thảo luận nhiều tầng Customer-Customer | Thấp |
| 2 | Cho phép nhiều câu trả lời & Customer trả lời lẫn nhau | Giống diễn đàn | Phức tạp kiểm duyệt spam/XSS và phân quyền nhiều vai trò | Cao |

## 3. Quyết định
Chọn phương án **#1**: chỉ `Admin` trả lời Q&A, ép ràng buộc `UQ_Answers_question` (1:0..1 giữa `Questions` và `Answers`), tạo `Answer` và đổi `Questions.status = ANSWERED` trong cùng transaction.

## 4. Hệ quả & đánh đổi (Trade-offs)
- ➕ Đáp ứng gọn, chắc chắn toàn bộ tiêu chí chấm điểm Q&A (2.0đ) mà không phình vai trò.
- ➖ Nếu sau này cần nhiều câu trả lời/lịch sử thảo luận sẽ cần gỡ unique constraint `UQ_Answers_question` qua migration mới.

## 5. Cách kiểm chứng & điều kiện xem xét lại
- Thước đo thành công: Customer gửi Question (`OPEN`), Admin trả lời (`ANSWERED`) hoặc ẩn (`HIDDEN`); câu hỏi đã có Answer bị từ chối trả lời trùng.
- Xem lại khi: Mở rộng thành cộng đồng hỏi đáp nhiều phản hồi sau MVP.
