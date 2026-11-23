# CONTEXT — Giỏ hàng, Thanh toán COD & Vòng đời Đơn hàng (cart-checkout-orders)

> **Pha 0 · Bối cảnh tính năng.** Nguồn chuẩn: `BookHub_PRJ301_Project_Plan_Requirements.md` (§7.4, §8.3–8.4, §11.1, §13.5, §18.3–18.4, §18.8, PRJ301 mục 3.6 — 1.0 điểm Cart + phần Order CRUD/State).
> Trạng thái: `ready`

## 1. Bối cảnh & Pain point
- Đây là luồng thương mại lõi và rủi ro cao nhất của BookHub: khách hàng quản lý giỏ hàng lưu trong DB, đặt đơn COD, theo dõi/hủy đơn đủ điều kiện; quản trị viên xác nhận và cập nhật trạng thái xử lý đơn.
- Các rủi ro nghiêm trọng phải chặn bằng thiết kế: gian lận giá/tổng tiền từ client, hai người checkout đồng thời làm `stock_qty < 0`, hủy đơn lặp hoàn kho 2 lần, IDOR xem/hủy đơn người khác, và đổi giá sách làm sai lịch sử đơn cũ.

## 2. Mục tiêu nghiệp vụ & thước đo thành công
| Mục tiêu | Chỉ số (đo được) | Hiện tại → Mục tiêu |
|---|---|---|
| Giỏ hàng bền vững & chính xác | Pass `TC-CART-01`..`TC-CART-06` | 1 Cart/Customer, 1 dòng/Book, giá tính 100% từ DB |
| Checkout COD nguyên tử & chống âm kho | Pass `TC-ORD-01`..`TC-ORD-12` | 1 transaction tạo `Orders` + `OrderItems` snapshot + trừ stock atomic + xóa `CartItems` |
| State machine đơn hàng chặt chẽ | Pass `TC-ADM-06`, `TC-ADM-07` | Chuyển trạng thái hợp lệ; hủy đơn hoàn kho **đúng 1 lần** |

## 3. Người dùng / Tác nhân
| Actor | Muốn gì | Ghi chú |
|---|---|---|
| Customer (`ACTIVE`) | Thêm/sửa/xóa sách trong giỏ, đặt hàng COD (ship 30.000 ₫), xem lịch sử/chi tiết đơn và hủy đơn trước khi giao | Chỉ thao tác trên giỏ và đơn của chính mình |
| Admin (`ADMIN`) | Xem, lọc mọi đơn hàng theo mã/trạng thái/ngày và cập nhật trạng thái theo state machine | Không được sửa giá/tên sách snapshot trên đơn đã tạo |

## 4. Thuật ngữ nghiệp vụ riêng của feature
| Thuật ngữ | Định nghĩa |
|---|---|
| `Atomic Stock Deduction` | Khóa hàng (`PESSIMISTIC_WRITE`) hoặc câu lệnh cập nhật có điều kiện `stock_qty >= qty` trong transaction để đảm bảo `stock_qty` không bao giờ âm |
| `Order Snapshot` | Bản sao `book_title_snapshot`, `unit_price`, `line_total`, `recipient_*`, `shipping_fee` lưu cố định khi tạo đơn (ADR-008) |
| `Order State Machine` | `PENDING → CONFIRMED → SHIPPING → DELIVERED`; hủy (`CANCELLED`) chỉ hợp lệ từ `PENDING` hoặc `CONFIRMED` |

## 5. Ràng buộc cố định (không thương lượng)
- Phương thức thanh toán duy nhất trong MVP là **COD**; phí vận chuyển cố định **30.000 VND/đơn** (ADR-005).
- Mọi tính toán tiền tệ dùng `BigDecimal` scale 2 ở server; bỏ qua hoàn toàn giá/tổng tiền từ trình duyệt.
- Mọi thao tác trên `Cart`, `CartItems`, `Orders` của Customer đều lọc và kiểm tra `user_id = currentUser.id`.

## 6. Tham chiếu
- Figma: `SCR-08`, `SCR-09`, `SCR-10`, `SCR-11`, `SCR-12`, `SCR-12b`, `SCR-18` · Baseline: `scr-08__cart__1280.png`, `scr-09__checkout-cod__1280.png`, `scr-18__admin-orders__1280.png` · UI Spec: `docs/ui-screens.md` · Business Rules: `.sdd/constraints/business.md` (`BR-CART`, `BR-ORD`).

## 7. Giả định & câu hỏi mở
- Không có câu hỏi mở chặn — toàn bộ quy tắc COD, phí ship 30.000 ₫, snapshot và state machine 5 trạng thái đã khóa trong yêu cầu & ADR-005/ADR-008.
