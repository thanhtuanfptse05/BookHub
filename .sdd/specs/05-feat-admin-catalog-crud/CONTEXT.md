# CONTEXT — Quản trị Sách & Danh mục (admin-catalog-crud)

> **Pha 0 · Bối cảnh tính năng.** Nguồn chuẩn: `BookHub_PRJ301_Project_Plan_Requirements.md` (§7.5, §8.7, §10.3, §13.6, §18.2, PRJ301 mục 3.3 — phần lõi 2.0 điểm CRUD).
> Trạng thái: `ready`

## 1. Bối cảnh & Pain point
- Quản trị viên cần tạo mới, chỉnh sửa, tìm kiếm/lọc và ngừng kinh doanh danh mục (`Categories`) và sách (`Books`) trực tiếp từ Admin Console mà không thao tác SQL thủ công.
- Module CRUD thực thể chính là phần lõi của mục **3.3 (2.0 điểm)** trong đề PRJ301; bắt buộc dùng **soft-deactivate** (`is_active = 0`) thay vì hard-delete để không phá vỡ khóa ngoại và lịch sử đơn hàng (`OrderItems`).

## 2. Mục tiêu nghiệp vụ & thước đo thành công
| Mục tiêu | Chỉ số (đo được) | Hiện tại → Mục tiêu |
|---|---|---|
| Quản trị danh mục & sách đầy đủ | Pass `TC-ADM-01`..`TC-ADM-05` | Chưa có CRUD Admin → đủ Create/Read/Update/Soft-Deactivate có phân trang |
| Toàn vẹn dữ liệu & lịch sử đơn | 0 lỗi FK khi ngừng bán sách/danh mục đã có đơn | Sách ngừng bán ẩn khỏi public nhưng snapshot `OrderItems` còn nguyên vẹn |
| Kiểm tra đầu vào chặt tại server | 100% validate giá > 0, stock ≥ 0, ISBN unique, category active | Không cho lưu bản ghi sai ràng buộc DB |

## 3. Người dùng / Tác nhân
| Actor | Muốn gì | Ghi chú |
|---|---|---|
| Admin (`ADMIN`) | Quản lý danh mục và kho sách tập trung, bật/tắt nổi bật & trạng thái kinh doanh an toàn | Mọi thao tác đi qua `/admin/*` + `CsrfFilter` |

## 4. Thuật ngữ nghiệp vụ riêng của feature
| Thuật ngữ | Định nghĩa |
|---|---|
| `Soft-Deactivate` | Đặt `is_active = 0` thay cho `DELETE`; có thể bật lại `is_active = 1` khi cần |
| `Slug` | Định danh URL chữ thường không dấu duy nhất cho mỗi danh mục (`UQ_Categories_slug`) |
| `Cover URL` | Đường dẫn ảnh bìa (`cover_url`) hoặc đường dẫn ảnh tĩnh trong `web/assets/images/` (MVP không upload file) |

## 5. Ràng buộc cố định (không thương lượng)
- Chỉ tài khoản role `ADMIN` truy cập `/admin/books*` và `/admin/categories*`.
- Cấm hard-delete `Categories` đang có sách hoặc `Books` đã có trong `OrderItems` (ADR-007).
- Danh mục `is_active = 0` không được chọn khi tạo sách mới (BR-CAT-002).

## 6. Tham chiếu
- Figma: `SCR-15`, `SCR-16`, `SCR-17` (`https://agenda-kale-62994833.figma.site/#/admin/books`) · UI Spec: `docs/ui-screens.md` · Business Rules: `.sdd/constraints/business.md` (`BR-CAT`, `BR-ADM`).

## 7. Giả định & câu hỏi mở
- Câu hỏi mở về trường `book_code` ở `SCR-16` (đồng bộ với `01-feat-foundation-jpa` Q1) được ghi nhận tại Phụ lục B của `SPEC.md`.
