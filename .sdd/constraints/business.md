# constraints/business.md — Ràng buộc nghiệp vụ & domain (BookHub)

> Nguồn chuẩn: `BookHub_PRJ301_Project_Plan_Requirements.md` (§6, §17, §18, §19). Mỗi luật có ID để SPEC/Test trích dẫn.

## 1. Dữ liệu & vòng đời
| ID | Luật | Lý do |
|---|---|---|
| BR-01 | **Soft-deactivate** cho thực thể có dữ liệu lịch sử: `Books` & `Categories` dùng `is_active = 0`; `Users` dùng `status = LOCKED/DISABLED`; `Reviews` & `Questions` dùng `status = HIDDEN`. Cấm hard-delete khi đã có tham chiếu lịch sử (ADR-007). | Bảo toàn lịch sử đơn hàng và toàn vẹn FK |
| BR-02 | Bảng nghiệp vụ lưu `created_at` và `updated_at` (`DATETIME2(0)`); `Orders` có thêm `cancelled_at`; `Answers` ghi `responder_user_id`. | Truy vết thay đổi & báo cáo theo khoảng ngày |
| BR-03 | `OrderItems` lưu snapshot `book_title_snapshot`, `unit_price`, `line_total` và bất biến sau khi tạo đơn (ADR-008). | Đổi tên/giá sách không làm sai lịch sử đơn |
| BR-04 | Trạng thái `Orders`, `Reviews`, `Questions` chỉ chuyển theo state machine đã chốt; cấm nhảy trạng thái tùy ý. | Toàn vẹn nghiệp vụ |

## 2. PII & dữ liệu cá nhân
| ID | Luật |
|---|---|
| BR-10 | Phân loại: **PII** (`full_name`, `email`, `phone`, `recipient_name`, `recipient_phone`, `shipping_address`); **Sensitive** (`password_hash`, `token_hash`); **Public** (danh mục, sách active, review `APPROVED`, Q&A `ANSWERED`). |
| BR-11 | Mật khẩu băm bằng BCrypt/Argon2 có salt; reset token chỉ lưu `token_hash` (SHA-256/hash an toàn); cookie phiên đặt `HttpOnly`, `SameSite`, và bật `Secure` khi chạy HTTPS. |
| BR-12 | Không ghi password, password hash, reset token, session ID hay PII nhạy cảm vào log, trang lỗi hoặc URL. |
| BR-13 | Môi trường local/demo chỉ dùng dữ liệu seed giả (`02_seed.sql`); không dùng dữ liệu cá nhân thật. |
| BR-14 | Tài khoản vi phạm được Admin chuyển `LOCKED`/`DISABLED` thay vì xóa cứng để giữ lịch sử giao dịch; reset token hết hạn được dọn theo cleanup policy. |

## 3. Quy tắc domain chi tiết (BookHub MVP)

### 3.1. Tài khoản & xác thực (`BR-ACC`)
| ID | Luật | Giá trị / ràng buộc |
|---|---|---|
| BR-ACC-001 | Email là định danh đăng nhập duy nhất; chuẩn hóa `trim()` + `lowercase` trước khi lưu/tra cứu. | `UQ_Users_email` |
| BR-ACC-002 | Tài khoản tự đăng ký luôn có role `CUSTOMER` và status `ACTIVE`; cấm đăng ký `ADMIN` qua form public. | Default `CUSTOMER` / `ACTIVE` |
| BR-ACC-003 | Mật khẩu tối thiểu 8 ký tự, gồm cả chữ và số; validate ở server, lưu hash. | `>= 8` chars, letter + digit |
| BR-ACC-004 | Form cập nhật profile chỉ sửa field cho phép (`full_name`, `phone`); cấm sửa `email`, `role_id`, `status`. | Server-side field allow-list |
| BR-ACC-005 | Tài khoản `LOCKED`/`DISABLED` không đăng nhập được; Admin không thể khóa chính mình hoặc Admin cuối cùng. | Bảo vệ tài khoản quản trị |
| BR-ACC-006 | Reset token là random opaque token, lưu hash, hết hạn sau **15 phút**, dùng **1 lần**; đổi/reset mật khẩu thành công phải vô hiệu các token chờ trước đó. | `TTL = 15 phút`, one-time use |

### 3.2. Danh mục & Sách (`BR-CAT`)
| ID | Luật | Giá trị / ràng buộc |
|---|---|---|
| BR-CAT-001 | Website public chỉ hiển thị sách `is_active = 1` thuộc danh mục đang hoạt động. | `is_active = 1` |
| BR-CAT-002 | Giá sách `> 0`, tồn kho `>= 0`; ISBN nếu nhập phải duy nhất (cho phép `NULL`); năm xuất bản `1000..2200`. | `CK_Books_price`, `CK_Books_stock`, `UX_Books_isbn_not_null` |
| BR-CAT-003 | Sách hết hàng (`stock_qty = 0`) vẫn xem được trang chi tiết nhưng không thể thêm vào giỏ/checkout. | Hiển thị "Hết hàng" |
| BR-CAT-004 | Phân trang danh sách sách tại DB, mặc định **12 sách/trang**; sort key bắt buộc qua allow-list (`newest`, `price_asc`, `price_desc`, `title_asc`, `bestseller`). | `pageSize = 12` |

### 3.3. Giỏ hàng, Đơn hàng & Tồn kho (`BR-CART`, `BR-ORD`)
| ID | Luật | Giá trị / ràng buộc |
|---|---|---|
| BR-CART-001 | Mỗi Customer có tối đa **1 Cart**; mỗi sách xuất hiện tối đa **1 dòng** trong Cart (thêm trùng thì cộng dồn `quantity`). | `UQ_Carts_user`, `UQ_CartItems_cart_book` |
| BR-CART-002 | `quantity` là số nguyên `1 .. book.stock_qty`; giá và tạm tính luôn tính lại từ bảng `Books` ở server. | Không tin `max`/total từ HTML |
| BR-ORD-001 | Thanh toán MVP chỉ hỗ trợ **COD**; phí vận chuyển cố định **30.000 VND/đơn** (lưu snapshot vào `Orders`). | `payment_method = 'COD'`, `shipping_fee = 30000.00` |
| BR-ORD-002 | `subtotal = SUM(unit_price * quantity)`, `total_amount = subtotal + shipping_fee` tính tại server bằng `BigDecimal`. | Bỏ qua mọi giá/tổng tiền client gửi |
| BR-ORD-003 | Checkout tạo `Orders` (`PENDING`), `OrderItems` (snapshot), trừ `stock_qty` nguyên tử (lock/conditional update) và xóa `CartItems` trong **1 transaction**; lỗi bất kỳ ⇒ rollback toàn bộ. | Không cho `stock_qty < 0` |
| BR-ORD-004 | Mã đơn `order_code` duy nhất dạng `BH-YYYYMMDD-xxxxx` (retry nếu đụng unique). | `UQ_Orders_order_code` |
| BR-ORD-005 | Customer chỉ xem/hủy đơn thuộc `currentUser.id`, và chỉ hủy được khi `status IN (PENDING, CONFIRMED)`; hủy thành công set `CANCELLED`, ghi `cancelled_at` và **hoàn tồn kho đúng 1 lần** trong cùng transaction. | Cấm hủy `SHIPPING`/`DELIVERED`/`CANCELLED` |

### 3.4. Đánh giá & Hỏi đáp (`BR-REV`, `BR-QA`)
| ID | Luật | Giá trị / ràng buộc |
|---|---|---|
| BR-REV-001 | Customer chỉ được viết Review cho sách nằm trong đơn hàng của chính mình có trạng thái `DELIVERED`. | Kiểm tra qua `Orders` + `OrderItems` |
| BR-REV-002 | Rating số nguyên **1–5**; tối đa **1 Review/User/Book** trong MVP (không sửa sau khi gửi); mới tạo ở trạng thái `PENDING`. | `UQ_Reviews_user_book`, `CK_Reviews_rating` |
| BR-REV-003 | Chỉ Admin chuyển Review sang `APPROVED`/`HIDDEN`; điểm trung bình và danh sách công khai chỉ tính Review `APPROVED`. | Không tính `PENDING`/`HIDDEN` |
| BR-QA-001 | Chỉ Customer đã đăng nhập mới gửi Question (`OPEN`, `subject <= 150`, `content <= 1500` ký tự) cho Book active. | `status = 'OPEN'` |
| BR-QA-002 | MVP chỉ **Admin** được trả lời; mỗi Question có tối đa **1 Answer** (`UQ_Answers_question`); tạo Answer và chuyển Question → `ANSWERED` trong cùng transaction. | ADR-006 |
| BR-QA-003 | Nội dung Review/Q&A là plain text, bắt buộc encode đầu ra (`<c:out>`), không render HTML thô; nội dung `HIDDEN` ẩn hoàn toàn ở public. | Chống stored XSS |

### 3.5. Thống kê & Báo cáo (`BR-ADM`)
| ID | Luật |
|---|---|
| BR-ADM-001 | Doanh thu (`SUM(total_amount)`), giá trị đơn trung bình (`AVG`) và sách bán chạy (`SUM(quantity)`) **chỉ tính đơn `DELIVERED`** (lọc khoảng ngày theo `Orders.created_at`). Không gây lỗi chia 0 khi không có dữ liệu. |
| BR-ADM-002 | `MIN`/`MAX`/`AVG` giá sách và `SUM(stock_qty)` chỉ tính trên `Books` có `is_active = 1` và ghi rõ phạm vi này trên giao diện báo cáo. |

### 3.6. Order State Machine
- `PENDING` → `CONFIRMED` (Admin) | `CANCELLED` (Customer chủ đơn / Admin — hoàn kho 1 lần)
- `CONFIRMED` → `SHIPPING` (Admin) | `CANCELLED` (Customer chủ đơn / Admin — hoàn kho 1 lần)
- `SHIPPING` → `DELIVERED` (Admin — tính doanh thu & mở quyền review)
- `DELIVERED`, `CANCELLED`: trạng thái kết thúc (terminal trong MVP, không chuyển ngược).

## 4. Phân quyền (RBAC)
| Vai trò | Quyền chính | Không được |
|---|---|---|
| `Guest` | Xem Home, Catalog (search/filter/sort/paging), Book Detail, Review `APPROVED`, Q&A công khai; Register, Login, Forgot/Reset password | Dùng Cart/Checkout, xem/hủy Order, gửi Review/Question, truy cập `/account/*` hay `/admin/*` |
| `Customer` | Toàn bộ quyền Guest + cập nhật profile/đổi password của mình, quản lý Cart của mình, Checkout COD, xem & hủy đơn của mình (`PENDING`/`CONFIRMED`), gửi Review (đã mua & `DELIVERED`), gửi Question | Xem/sửa Cart hay Order người khác, tự sửa `role_id`/`status`, trả lời Q&A, duyệt Review, truy cập `/admin/*` |
| `Admin` | Toàn bộ quyền quản trị `/admin/*`: Dashboard & Reports, CRUD/deactivate Book & Category, xem & cập nhật trạng thái mọi Order, khóa/mở khóa Customer, duyệt/ẩn Review, trả lời/ẩn Question | Tự đăng ký qua form public, khóa chính mình hoặc Admin cuối cùng, sửa snapshot đơn đã đặt, hard-delete dữ liệu đã có lịch sử |

## 5. Tuân thủ & vận hành
- **Quy định áp dụng:** An toàn dữ liệu cơ bản cho đồ án học phần PRJ301 (bảo vệ PII/password/CSRF/XSS/SQLi/IDOR); chưa áp dụng chứng chỉ PCI-DSS (do chỉ thanh toán COD).
- **Mục tiêu hiệu năng (local/demo):** Trang Home/Catalog/Detail tải `< 3s` với 1.000 sách & 20 người dùng đồng thời trong mạng nội bộ; không cam kết SLA production (NFR-PERF-001, NFR-AVAIL-001).
- **Múi giờ nghiệp vụ:** `Asia/Ho_Chi_Minh`.
