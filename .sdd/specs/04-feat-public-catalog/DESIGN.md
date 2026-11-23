# DESIGN — Trang chủ, Danh mục Sách & Chi tiết Sách công khai (public-catalog)

> **Bản thiết kế gọn cho feature có UI.** Mặc định màu/font/card/a11y tuân theo `.sdd/design/DESIGN.md` & `tokens.css`.

| Trường | Giá trị |
|---|---|
| Feature | `04-feat-public-catalog` |
| Status | `LOCKED` |
| Tham chiếu Stitch/Figma | `https://agenda-kale-62994833.figma.site/#/` (`SCR-01`, `SCR-02`, `SCR-03`, `SCR-03b`) · `.sdd/design/references/baseline/scr-01__home__1280.png`, `scr-01__home__375.png`, `scr-02__books-catalogue__1280.png`, `scr-02__books-catalogue__375.png`, `scr-03__book-detail__1280.png` |

## 1. Màn hình & Luồng đi
| ID | Màn hình / View | Vào từ đâu | Mục đích |
|---|---|---|---|
| SCR-01 | Trang chủ (`views/site/home.jsp`) | `GET /` / Logo BookHub | Hero banner, 6 lối tắt thể loại, 3 kệ sách (nổi bật, mới nhất, bán chạy) |
| SCR-02 | Danh mục sách (`views/site/books.jsp`) | `GET /books` / Search bar / `Xem tất cả` | Tìm kiếm, lọc danh mục & giá, sắp xếp, phân trang 12 sách |
| SCR-03 | Chi tiết sách (`views/site/book-detail.jsp`) | `GET /books/detail?id={id}` / `BookCard` | Xem chi tiết, tồn kho, đánh giá, Q&A và sách tương tự |
| SCR-03b | Chi tiết sách hết hàng | Cùng `book-detail.jsp` khi `stock_qty == 0` | Hiển thị badge `Hết hàng`, khóa stepper và nút thêm giỏ |

```text
SCR-01 Home ──(click CTA/thể loại/search)──► SCR-02 Catalogue ──(lọc/sort/paging)──► SCR-02
     └────────────────(click BookCard)───────────────┴──────────(click BookCard)──► SCR-03 / 03b Detail
```

## 2. Component & Token dùng
- **Dùng lại từ `components.md`:** `StorefrontHeader`, `StorefrontFooter`, `BookCard`, `Button` (`primary-teal`, `accent-terra` cho Hero CTA, `outline-teal`), `FormControl`, `StatusBadge` (`Còn hàng`, `Hết hàng`), `QuantityStepper`, `Pagination`, `EmptyState`.
- **Component mới:** Không có.
- **Token mới ngoài `tokens.css`?** Không.

## 3. Trạng thái, Responsive & Copy (chỉ ghi điểm riêng)
- **Trạng thái:**
  - **Empty:** `SCR-02` hiển thị `EmptyState` (`"Không tìm thấy sách phù hợp"` + `"Xóa bộ lọc"`); `SCR-03` khi chưa có đánh giá/Q&A hiện thông báo gọn.
  - **Disabled (`SCR-03b`):** Badge xám `"Hết hàng"`, stepper `0` disabled, nút `"Thêm vào giỏ hàng"` disabled kèm `"Sách tạm hết hàng. Vui lòng quay lại sau."`.
  - **Error:** Khoảng giá nhập sai hiển thị `AlertBanner` lỗi ngay trên khung bộ lọc; ảnh bìa lỗi dùng `book-placeholder.svg`.
- **Responsive (375 vs 1280):**
  - `SCR-01`: Category 6 cột → 2 cột; 3 kệ sách 4 cột → 2 cột; Hero ẩn bìa trang trí bên phải.
  - `SCR-02`: Sidebar `Bộ lọc` (3/12) + Grid 3 cột (9/12) → xếp dọc 1 cột (bộ lọc ở trên, lưới sách 2 cột ở dưới).
  - `SCR-03`: Khối sản phẩm 2 cột + khối Review/Q&A 2 cột → xếp dọc 1 cột trên 375px.
- **Copy / i18n chính:** `"Khám phá danh mục sách"`, `"Phí giao cố định 30.000 ₫"`, `"Áp dụng bộ lọc"`, `"Xóa bộ lọc"`, `"Thêm vào giỏ hàng"`, `"Sách tương tự"`.
- **a11y cần lưu ý:** Breadcrumb dùng `<nav aria-label="Breadcrumb">`, ảnh bìa dùng `alt="Bìa sách <c:out value='${book.title}'/>"`, chip lọc có nút xóa gắn `aria-label`.
- **SEO/AEO/GEO:** Khớp `.sdd/seo/SEO.md` — `SCR-01` (`Organization + WebSite`), `SCR-02` (`CollectionPage`), `SCR-03` (`Book / Product + BreadcrumbList`), HTML SSR đầy đủ `<h1>`, `<title>`, `<meta name="description">`.

## 4. Tiêu chí nghiệm thu bằng mắt (VAC) & Duyệt
- [ ] **VAC-01:** `SCR-01`, `SCR-02`, `SCR-03` khớp ảnh baseline 1280px và 375px tại `.sdd/design/references/baseline/`, không cuộn ngang.
- [ ] **VAC-02:** Hiển thị chính xác Empty State khi tìm không ra sách, và Disabled State (`SCR-03b`) khi sách hết hàng.
- **Duyệt (cùng Gate 1):** Owner (BookHub) · 2026-11-23
