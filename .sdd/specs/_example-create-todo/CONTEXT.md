# CONTEXT — Tạo công việc (create-todo)

> VÍ DỤ MINH HỌA văn phong chuẩn (Tier Full, task đang dở). Không phải feature thật.

## 1. Bối cảnh & Pain point
Người dùng ghi việc cần làm bằng ghi chú rời rạc nên hay quên deadline. Họ muốn một API tạo công việc có hạn chót để app nhắc việc phía sau.

## 2. Mục tiêu nghiệp vụ & thước đo
| Mục tiêu | Chỉ số | Hiện tại → Mục tiêu |
|---|---|---|
| Người dùng tạo được việc nhanh | thời gian tạo 1 việc | 15 s (ghi tay) → < 3 s |
| Giảm việc quá hạn không ai nhắc | % việc quá hạn | 30% → 15% |

## 3. Người dùng / Tác nhân
| Actor | Muốn gì | Ghi chú |
|---|---|---|
| `user` | Tạo việc của riêng mình, có hạn chót | Đã đăng nhập |

## 4. Thuật ngữ
| Thuật ngữ | Định nghĩa |
|---|---|
| Todo | Một công việc thuộc về đúng 1 user, có tiêu đề, hạn chót (tuỳ chọn), trạng thái `open/done` |

## 5. Ràng buộc cố định
- Dữ liệu của user này không được lộ cho user khác (BR-21 trong ví dụ).
- Tiêu đề là dữ liệu người dùng nhập ⇒ không log nội dung.

## 6. Tham chiếu
Ticket: DEMO-12 · Thiết kế: n/a

## 7. Giả định & câu hỏi mở
- [x] Hạn chót có bắt buộc? → Không bắt buộc (đã chốt).
