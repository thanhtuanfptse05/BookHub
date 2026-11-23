# CONTEXT — Đánh giá Sách (Review) & Hỏi đáp (Q&A) kèm Kiểm duyệt (reviews-qa)

> **Pha 0 · Bối cảnh tính năng.** Nguồn chuẩn: `BookHub_PRJ301_Project_Plan_Requirements.md` (§7.6, §8.5–8.6, §11.3–11.4, §18.5–18.6, PRJ301 mục 3.8 — 2.0 điểm Q&A).
> Trạng thái: `ready`

## 1. Bối cảnh & Pain point
- Khách hàng cần tham khảo đánh giá xác thực từ người đã nhận sách và đặt câu hỏi trực tiếp cho cửa hàng trên trang chi tiết sách; quản trị viên cần kiểm duyệt đánh giá (`APPROVE`/`HIDE`) và trả lời/ẩn câu hỏi (`ANSWERED`/`HIDDEN`).
- Module Q&A chiếm tới **2.0 điểm** riêng trong rubric PRJ301 (mục 3.8: đặt câu hỏi, trả lời, duyệt và quản lý). Toàn bộ nội dung do người dùng nhập phải được chống **Stored XSS** tuyệt đối.

## 2. Mục tiêu nghiệp vụ & thước đo thành công
| Mục tiêu | Chỉ số (đo được) | Hiện tại → Mục tiêu |
|---|---|---|
| Review xác thực sau mua hàng | Pass `TC-REV-01`..`TC-REV-05` | Chỉ khách có đơn `DELIVERED` chứa sách mới gửi được, tối đa 1/user/book, duyệt mới hiện |
| Module Q&A trọn vẹn 2.0đ PRJ301 | Pass `TC-QA-01`..`TC-QA-05` | Customer hỏi (`OPEN`), Admin trả lời (`ANSWERED`, 1 Answer/Question) hoặc ẩn (`HIDDEN`) |
| Chống Stored XSS 100% | Pass `TC-UI-02` | Mọi text `<script>...</script>` hiển thị dạng chuỗi an toàn qua `<c:out>` |

## 3. Người dùng / Tác nhân
| Actor | Muốn gì | Ghi chú |
|---|---|---|
| Customer | Đánh giá 1–5 sao sách đã mua & đã giao; đặt câu hỏi về sách đang bán | Không sửa review sau khi gửi trong MVP; không trả lời ngang hàng Q&A |
| Admin (`ADMIN`) | Duyệt/ẩn Review theo tab trạng thái; lọc, trả lời chính thức và ẩn Question không phù hợp | Chỉ Admin được tạo `Answer` (ADR-006) |

## 4. Thuật ngữ nghiệp vụ riêng của feature
| Thuật ngữ | Định nghĩa |
|---|---|
| `Verified Purchase Eligibility` | Điều kiện tồn tại bản ghi `Orders` (`user_id = currentUser.id`, `status = 'DELIVERED'`) nối `OrderItems` (`book_id = targetBookId`) |
| `Review Moderation` | Trạng thái `PENDING` (mặc định khi mới gửi) → `APPROVED` (công khai & tính điểm TB) hoặc `HIDDEN` (ẩn) |
| `1-to-1 Q&A Answer` | Mỗi `Question` có tối đa 1 `Answer` (`UQ_Answers_question`) do Admin phản hồi (ADR-006) |

## 5. Ràng buộc cố định (không thương lượng)
- Unique `(user_id, book_id)` cho `Reviews`; rating nguyên `1..5`; không xóa cứng review vi phạm mà chuyển `HIDDEN`.
- `Questions.subject <= 150` ký tự, `content <= 1.500` ký tự; `Answers.content <= 2.000` ký tự; chỉ Admin tạo `Answer`, tạo `Answer` và chuyển `Questions.status = 'ANSWERED'` trong **cùng 1 transaction**.
- Mọi nội dung Review/Question/Answer là plain text, bắt buộc `<c:out>` khi render JSP.

## 6. Tham chiếu
- Figma: `SCR-03` (khu Review & Q&A), `SCR-12b` (CTA đánh giá), `SCR-20`, `SCR-21` · Baseline: `scr-03__book-detail__1280.png` · UI Spec: `docs/ui-screens.md` · Business Rules: `.sdd/constraints/business.md` (`BR-REV`, `BR-QA`).

## 7. Giả định & câu hỏi mở
- Không có câu hỏi mở chặn — luồng Review (`PENDING → APPROVED/HIDDEN`, 1/user/book, không edit) và Q&A (Admin-only, 1 answer/question) đã chốt rõ trong yêu cầu & ADR-006.
