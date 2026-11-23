# SEO.md — Nguồn sự thật SEO · AEO · GEO của BookHub

> Luật: `.sdd/constraints/seo.md`. Playbook: `.sdd/skills/seo-aeo-geo.md`.
> Lưu ý: BookHub MVP chạy local/demo (`localhost:8080/BookHub`); các quy chuẩn HTML/meta/SSR vẫn được tuân thủ để sẵn sàng nếu triển khai công khai. Khu vực đăng nhập (`/account/*`, `/cart`, `/checkout`, `/admin/*`) luôn đặt `noindex`.

## 1. Định danh thực thể
| Trường | Giá trị |
|---|---|
| Domain chuẩn (canonical) | `http://localhost:8080/BookHub` _(cập nhật domain thật nếu deploy công khai)_ |
| Tên thương hiệu (viết đúng 1 cách) | BookHub |
| Mô tả 1 câu | BookHub — Website bán sách trực tuyến đơn giản, tìm kiếm nhanh, đặt mua COD tiện lợi kèm đánh giá và hỏi đáp trực tiếp cùng cửa hàng. |
| Ngôn ngữ / thị trường | `vi-VN` (Việt Nam, tiền tệ VND) |
| Hồ sơ chính thức (`sameAs`) | `https://github.com/thanhtuanfptse05/BookHub` |
| Liên hệ / cửa hàng | Cửa hàng sách trực tuyến BookHub (dự án học phần PRJ301 — single-store demo) |

## 2. Chiến lược render (bắt buộc cho trang công khai — SE-01)
- Chế độ: **SSR (Server-Side Rendering)** thuần bằng **Jakarta Servlet 6.0 + JSP 3.1 / JSTL 3.0** — toàn bộ tiêu đề, giá, mô tả sách, review và Q&A đã có sẵn trong HTML trả về lần đầu.
- Trang chỉ-đăng-nhập / nội bộ (`/login`, `/register`, `/forgot-password`, `/reset-password`, `/cart`, `/checkout`, `/orders/*`, `/account/*`, `/admin/*`): đặt `<meta name="robots" content="noindex, nofollow">` và không đưa vào sitemap.
- Kiểm tra nhanh (khi Tomcat chạy local): nội dung HTML trả về từ `GET /` và `GET /books` phải chứa `<h1>` và danh sách sách không phụ thuộc JS client-render.

## 3. Chính sách AI crawler
`AI_CRAWLERS = allow` (đặt trong `.sdd/project.conf`) — lý do: dự án học tập/demo website bán sách công khai, cho phép bot tìm kiếm và trích dẫn danh mục sách công khai trong khi vẫn chặn các đường dẫn riêng tư (`/account/`, `/cart`, `/checkout`, `/admin/`).

## 4. Danh mục trang công khai
| URL | Ý định / câu hỏi chính trang trả lời | Title (≲ 60) | Description (≲ 160) | H1 | Schema | Trạng thái |
|---|---|---|---|---|---|---|
| `/` | BookHub bán sách gì, sách nổi bật/mới/bán chạy nào đang có? | `BookHub — Nhà sách trực tuyến` | `Khám phá sách nổi bật, sách mới và sách bán chạy tại BookHub. Đặt mua COD nhanh chóng, phí ship cố định 30.000 ₫.` | `Khám phá thế giới sách tại BookHub` | Organization + WebSite | ⬜ |
| `/books` | Tìm, lọc theo danh mục & khoảng giá, sắp xếp sách tại BookHub | `Danh mục sách — BookHub` | `Tìm kiếm sách theo tên, tác giả, ISBN; lọc theo danh mục và khoảng giá tại BookHub.` | `Danh mục sách` | CollectionPage + BreadcrumbList | ⬜ |
| `/books/detail?id={id}` | Thông tin, giá, tồn kho, đánh giá và hỏi đáp của một cuốn sách | `{Tên sách} — BookHub` | `Mua {Tên sách} của {Tác giả} giá {Giá} ₫ tại BookHub. Xem đánh giá đã xác thực và hỏi đáp cùng cửa hàng.` | `{Tên sách}` | Book / Product + BreadcrumbList | ⬜ |

## 5. Quy tắc nội dung AEO/GEO (áp dụng cho trang công khai)
1. **Thông tin rõ ràng, có cấu trúc:** Trang chi tiết sách hiển thị rõ tên sách, tác giả, nhà xuất bản, năm xuất bản, ISBN, giá VND, tình trạng còn/hết hàng, điểm đánh giá trung bình từ Review `APPROVED`.
2. **Dữ liệu thật từ DB:** Không hardcode danh sách sách hay thống kê giả trên trang công khai.
3. **Liên kết nội bộ rõ nghĩa:** Breadcrumb (`Trang chủ → Danh mục → Tên sách`), danh mục tắt, sách tương tự cùng danh mục.
4. **Ảnh bìa có `alt`:** Mọi ảnh bìa dùng `alt="Bìa sách {Tên sách}"` với ảnh fallback khi thiếu URL.

## 6. Đo lường
| Kênh | Việc làm | Tần suất |
|---|---|---|
| `scripts/seo-check.ps1` | Kiểm tra title/description/H1/alt/HTML thô khi có trang build | Trước khi nộp/deploy |
| Kiểm tra thủ công HTML nguồn | View-source kiểm tra `<c:out>`, meta, heading và `noindex` trên trang riêng tư | Cuối EPIC 4 & EPIC 9 |
