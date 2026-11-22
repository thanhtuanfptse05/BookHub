# constraints/business.md — Ràng buộc nghiệp vụ & domain

> Người viết, Agent tuân thủ. Mỗi luật có ID để SPEC/Test có thể trích dẫn (`BR-xx`).

## 1. Dữ liệu & vòng đời
| ID | Luật | Lý do |
|---|---|---|
| BR-01 | **Soft-delete** cho entity nghiệp vụ (`deleted_at`); không `DELETE` vật lý trừ job purge được duyệt. | Truy vết, khôi phục |
| BR-02 | Mọi bảng có `id`, `created_at`, `updated_at`; bảng nghiệp vụ có thêm `created_by`, `updated_by`. | Audit |
| BR-03 | Thay đổi dữ liệu nhạy cảm (tiền, quyền, trạng thái đơn) ghi **audit log** bất biến (append-only). | Tuân thủ |
| BR-04 | Trạng thái chuyển theo state machine đã định nghĩa; cấm nhảy trạng thái tùy ý. | Toàn vẹn |

## 2. PII & dữ liệu cá nhân
| ID | Luật |
|---|---|
| BR-10 | Phân loại: **PII** (email, SĐT, địa chỉ, CCCD…), **Sensitive** (thanh toán, sức khỏe), **Public**. |
| BR-11 | PII/Sensitive **mã hóa khi lưu** (field-level hoặc disk-level theo ADR) & khi truyền (TLS). |
| BR-12 | PII không xuất hiện trong log, error message, URL, analytics. Dùng mask: `a***@d***.com`. |
| BR-13 | Môi trường dev/test chỉ dùng dữ liệu giả (fixtures/faker); cấm dữ liệu prod. |
| BR-14 | Hỗ trợ xóa/ẩn danh dữ liệu người dùng khi có yêu cầu (GDPR/PDPD). Thời hạn lưu: {{N ngày/tháng}}. |

## 3. Quy tắc domain (điền theo dự án)
| ID | Luật | Ví dụ giá trị |
|---|---|---|
| BR-20 | {{vd: Đơn hàng chỉ hủy được khi chưa ship}} | |
| BR-21 | {{vd: Giá luôn tính ở server, không tin client}} | |
| BR-22 | {{vd: Mỗi user tối đa N phiên đăng nhập}} | |

## 4. Phân quyền (RBAC)
| Vai trò | Quyền chính | Không được |
|---|---|---|
| `guest` | | |
| `user` | | |
| `admin` | | |

## 5. Tuân thủ & vận hành
- Quy định áp dụng: {{GDPR / PCI-DSS / HIPAA / none}}
- SLA/SLO: {{99.9% / p95 < 300ms}}
- Rate limit mặc định: {{100 req/phút/IP}}
