# REVIEW — reviews-qa

> Review **độc lập** (agent/session khác, hoặc người) — prompt: `.sdd/prompts/06-review.md`. Reviewer **không sửa code**.
> Tier Full: bắt buộc `APPROVED` trước khi merge.

| Trường | Giá trị |
|---|---|
| Reviewer | Chờ Owner duyệt (bản nháp bằng chứng từ implementer) · Ngày: 2026-11-23 |
| Commit/nhánh | `feat/bookhub-mvp-01-08` (commit `50eebe1` + fix JPQL spacing & EL) |
| **Kết luận** | `PENDING` (chờ Owner xác nhận `APPROVED`) |

## 1. Bằng chứng chạy thật
```text
$ ant clean dist test-junit5
- ReviewsQaIntegrationTest: 2/2 OK (SQL Server 2022 BookHubDB)
  - verifiedBuyerReviewsRequireDeliveredOrderModerateApproveAndHideAndRejectDuplicates() [OK]
  - qaCustomerAskAdminSingleAnswerUpdateHideAndJspXssEscaping() [OK] (kèm regression status == null)
- Live Tomcat 10.1: /admin/reviews, /admin/reviews?status=PENDING, /admin/questions, /admin/questions?status=OPEN → 200 OK, 0 SEVERE
```

## 2. Truy vết
| Yêu cầu | Code | Test (`@spec`) | OK? |
|---|---|---|---|
| FR-01..04 | `ReviewRepository`, `ReviewService`, `ReviewServlet`, `AdminReviewServlet`, `admin/reviews.jsp` | `reviews-qa/AC-01..03` | ✅ |
| FR-05..09 | `QuestionRepository`, `QuestionService`, `QuestionServlet`, `AdminQuestionServlet`, `admin/questions.jsp` | `reviews-qa/AC-04..07` | ✅ |
| FR-10, EC-02 | `<c:out>` trên `site/book-detail.jsp`, `admin/{reviews,questions}.jsp`, link từ `order-detail.jsp` | `reviews-qa/AC-08` | ✅ |

## 3. Kiểm tra theo hiến pháp
- [x] Chỉ Customer đã có đơn `DELIVERED` chứa sách mới được gửi review, tối đa 1 review/user/book (`PENDING` mặc định)
- [x] Chỉ Admin trả lời Q&A (`ANSWERED` + 1 `Answer` trong 1 TX); hỗ trợ `updateAnswer` cập nhật phản hồi duy nhất theo Gate 1 Q1, vẫn chặn tạo `Answer` thứ 2
- [x] Toàn bộ nội dung người dùng render qua `<c:out>` chống XSS (`AC-08`)

## 4. Phát hiện
| # | Mức | File:dòng | Vấn đề | Cách sửa đề xuất | Trạng thái |
|---|---|---|---|---|---|
| 1 | 🟠 | `ReviewRepository.java`, `QuestionRepository.java` | Live Tomcat phát hiện thiếu dấu cách trước `ORDER BY` khi `status == null` (`uORDER BY` / `ruORDER BY`) | Đã sửa `" "` trong cả 2 Repository, thêm regression test `listAdmin*(null, 1)` & verify live Tomcat 200 OK | Đã sửa |
| 2 | 🟡 | `admin/{reviews,questions}.jsp` | Biểu thức EL `${pageResult.empty}` | Đã thay bằng `${empty pageResult.items}` & verify live Tomcat 200 OK | Đã sửa |

## 5. Phê duyệt cuối
_(Chờ Owner xác nhận `APPROVED` để chốt Gate 4)_
