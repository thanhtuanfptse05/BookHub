# REVIEW — public-catalog

> Review **độc lập** (agent/session khác, hoặc người) — prompt: `.sdd/prompts/06-review.md`. Reviewer **không sửa code**.
> Tier Full: bắt buộc `APPROVED` trước khi merge.

| Trường | Giá trị |
|---|---|
| Reviewer | Owner (BookHub) · Ngày: 2026-11-23 |
| Commit/nhánh | `feat/bookhub-mvp-01-08` (commit `5a0ee73` + fix JSP EL) |
| **Kết luận** | `APPROVED` (Owner đã duyệt Gate 4 ngày 2026-11-23) |

## 1. Bằng chứng chạy thật
```text
$ ant clean dist test-junit5
- PublicCatalogIntegrationTest: 3/3 OK (SQL Server 2022 BookHubDB)
  - homeDataLoadsFeaturedNewestBestsellerShelvesAndExcludesInactiveBooks() [OK]
  - searchFilterSortWhitelistAndPaginationWorkCorrectly() [OK]
  - bookDetailLoadsApprovedReviewsAnsweredQaSimilarOutOfStockAndRejectsInactive() [OK]
- Live Tomcat 10.1: /, /books, /books/detail?id=1 → 200 OK; /books/detail?id=999999 → 404
```

## 2. Truy vết
| Yêu cầu | Code | Test (`@spec`) | OK? |
|---|---|---|---|
| FR-01..02 | `HomeServlet`, `CatalogService.loadHomePage`, `site/home.jsp` | `public-catalog/AC-01` | ✅ |
| FR-03..06 | `BookListServlet`, `BookSortOption`, `BookRepository.searchPublicBooks`, `site/books.jsp` | `public-catalog/AC-02..05` | ✅ |
| FR-07..09 | `BookDetailServlet`, `CatalogService.loadBookDetail`, `site/book-detail.jsp` | `public-catalog/AC-06..08` | ✅ |

## 3. Kiểm tra theo hiến pháp
- [x] Mọi truy vấn công khai lọc cứng `Books.is_active = 1 AND Categories.is_active = 1`, named parameters 100%, sort qua whitelist `BookSortOption`
- [x] Phân trang trực tiếp ở DB (`setFirstResult`/`setMaxResults`), 12 sách/trang, clamp page an toàn
- [x] Toàn bộ text người dùng escape qua `<c:out>`, không truyền Entity JPA lazy ra JSP
- [x] `HomeServlet` dùng `urlPatterns = {""}` giữ nguyên phục vụ `/assets/*` và `/robots.txt`

## 4. Phát hiện
| # | Mức | File:dòng | Vấn đề | Cách sửa đề xuất | Trạng thái |
|---|---|---|---|---|---|
| 1 | 🟡 | `web/WEB-INF/views/site/books.jsp` | Biểu thức EL `${pageResult.empty}` xung đột từ khóa dành riêng `empty` của JSP EL 5.0 | Đã thay bằng `${empty pageResult.items}` & xác minh live Tomcat 200 OK | Đã sửa |

## 5. Phê duyệt cuối
**`APPROVED`** — Owner (BookHub) · 2026-11-23
