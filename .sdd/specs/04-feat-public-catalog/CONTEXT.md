# CONTEXT — Trang chủ, Danh mục Sách & Chi tiết Sách công khai (public-catalog)

> **Pha 0 · Bối cảnh tính năng.** Nguồn chuẩn: `BookHub_PRJ301_Project_Plan_Requirements.md` (§3.1A, §8.2, §10.1, §13.2–13.4, PRJ301 mục 3.2 — 2.0 điểm).
> Trạng thái: `ready`

## 1. Bối cảnh & Pain point
- Khách truy cập (Guest) và khách hàng (Customer) cần khám phá cửa hàng sách mà không phải đăng nhập trước: xem sách nổi bật/mới/bán chạy ở Home, tìm kiếm nhanh theo tên/tác giả/ISBN, lọc theo danh mục & khoảng giá, sắp xếp, chuyển trang giữ nguyên bộ lọc và xem chi tiết cuốn sách kèm sách tương tự.
- Đây là module có trọng số cao (**2.0 điểm** mục 3.2 PRJ301) và yêu cầu 100% dữ liệu tải từ SQL Server qua JPA (không hardcode), phân trang tại DB và tránh N+1 query.

## 2. Mục tiêu nghiệp vụ & thước đo thành công
| Mục tiêu | Chỉ số (đo được) | Hiện tại → Mục tiêu |
|---|---|---|
| Trải nghiệm tìm kiếm & duyệt sách | Pass 9/9 test case `TC-CAT-01`..`TC-CAT-09` | Chỉ có `index.html` → Home, `/books`, `/books/detail` tải DB thật |
| Hiệu năng truy vấn danh mục | Phân trang tại DB (`12 sách/trang`), tránh N+1 | 100% query dùng parameter binding & DTO projection |
| An toàn hiển thị công khai | 0 sách inactive, 0 review PENDING/HIDDEN, 0 Q&A HIDDEN lọt ra public | Lọc cứng tại tầng Repository/Service |

## 3. Người dùng / Tác nhân
| Actor | Muốn gì | Ghi chú |
|---|---|---|
| Guest / Customer | Tìm đúng cuốn sách theo nhu cầu, kiểm tra giá/tồn kho, đọc đánh giá và hỏi đáp công khai trước khi mua | Sách hết hàng vẫn xem được nhưng khóa nút thêm giỏ |

## 4. Thuật ngữ nghiệp vụ riêng của feature
| Thuật ngữ | Định nghĩa |
|---|---|
| `Sort Whitelist` | Chỉ cho phép 5 khóa sắp xếp cố định ở server: `newest`, `price_asc`, `price_desc`, `title_asc`, `bestseller` |
| `Bestseller` | Xếp hạng theo tổng `OrderItems.quantity` của các đơn có `Orders.status = 'DELIVERED'` |
| `Similar Books` | Tối đa 4 cuốn sách cùng `category_id`, `is_active = 1`, khác `book_id` hiện tại |

## 5. Ràng buộc cố định (không thương lượng)
- Phân trang tại DB bằng JPA (`setFirstResult` / `setMaxResults`), mặc định `12` sách/trang; không tải toàn bộ bảng `Books` lên RAM rồi cắt trang.
- Chỉ hiển thị sách `is_active = 1` thuộc danh mục `is_active = 1` trên toàn bộ storefront công khai.
- Trang công khai là SSR từ JSP/JSTL — tuân thủ `.sdd/seo/SEO.md` (title, meta description, H1, alt ảnh).

## 6. Tham chiếu
- Figma: `SCR-01`, `SCR-02`, `SCR-03`, `SCR-03b` · Baseline: `scr-01__home__1280.png`, `scr-01__home__375.png`, `scr-02__books-catalogue__1280.png`, `scr-02__books-catalogue__375.png`, `scr-03__book-detail__1280.png` · UI Spec: `docs/ui-screens.md` · SEO: `.sdd/seo/SEO.md`.

## 7. Giả định & câu hỏi mở
- Không có câu hỏi mở chặn; phần form gửi Review và form gửi Question trên `SCR-03` sẽ nối vào endpoint của `07-feat-reviews-qa` (nhưng phần hiển thị đọc-chỉ Review `APPROVED` và Q&A `ANSWERED` thuộc ngay feature này).
