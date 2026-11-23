# TASKS — Trang chủ, Danh mục Sách & Chi tiết Sách công khai (`04-feat-public-catalog`)

> **Pha 3 · Phân rã từ `PLAN.md`.** Task atomic ≤ 4h · gắn thẻ `@spec public-catalog/AC-xx`.

**Trạng thái:** `⬜ todo` · `🔄 doing` · `✅ done` · `⛔ blocked`

| ID | Task | Phụ thuộc | Ước lượng | Truy vết | Trạng thái |
|---|---|---|---|---|---|
| T-001 | Tạo bộ DTO (`BookCardDTO`, `BookDetailDTO`, `BookFilterCriteria`, `BookSortOption`, `PageResult<T>`, `CategoryShortcutDTO`, `PublicReviewDTO`, `PublicQaDTO`) + unit test sort/filter | — | 2h | FR-04..05, EC-03, AC-04 | ⬜ |
| T-002 | Bổ sung truy vấn JPA trong `BookRepository` & `CategoryRepository` (Home 3 nhóm, search/filter/sort/paging DB, similar, public review/Q&A) | T-001 | 3.5h | FR-01..03, FR-05, FR-07, EC-01, AC-01..06 | ⬜ |
| T-003 | Tạo `CatalogService` + bộ integration test `AC-01..08` | T-002 | 3h | FR-01..09, EC-01..04, AC-01..08 | ⬜ |
| T-004 | Tạo `HomeServlet` + `views/site/home.jsp` (`SCR-01`) | T-003 | 2.5h | FR-01, FR-02, AC-01 | ⬜ |
| T-005 | Tạo `BookListServlet` + `views/site/books.jsp` (`SCR-02` + filter chips + pagination + empty state) | T-003 | 3h | FR-03..06, EC-02, AC-02..05 | ⬜ |
| T-006 | Tạo `BookDetailServlet` + `views/site/book-detail.jsp` (`SCR-03` & `SCR-03b`) + kiểm trực quan 375/1280 & SEO | T-003 | 3h | FR-07..09, AC-06..08, VAC-01..02 | ⬜ |

## Chi tiết task

### T-001 — DTOs & `BookSortOption` Whitelist
- **Mục tiêu:** Định nghĩa toàn bộ ViewModel sạch (không lộ lazy Entity) và enum `BookSortOption` chống SQL/JPQL injection.
- **File dự kiến:** `src/java/com/bookhub/dto/*.java`, `tests/unit/BookSortOptionTest.java`.
- **Acceptance:**
  - [ ] Test payload sort lạ fallback về `newest` (`@spec public-catalog/AC-04`).
- **Không làm:** Không trả Entity JPA trực tiếp ra JSP.

### T-002 — JPA Queries công khai (0 N+1, DB Pagination)
- **Mục tiêu:** Viết truy vấn parameter-bound lọc cứng `Books.is_active = 1 AND Categories.is_active = 1`, phân trang DB 12/trang, bestseller theo đơn `DELIVERED`, sách tương tự 4 cuốn, review `APPROVED` và Q&A `ANSWERED`.
- **File dự kiến:** `src/java/com/bookhub/repository/{BookRepository,CategoryRepository}.java`.
- **Acceptance:**
  - [ ] Query phân trang bằng `setFirstResult`/`setMaxResults`; `LEFT JOIN` bestseller xử lý sạch khi chưa có đơn `DELIVERED`.
- **Không làm:** Không cắt trang trong Java RAM.

### T-003 — `CatalogService` & Integration Tests
- **Mục tiêu:** Đóng gói nghiệp vụ clamp page an toàn, validate `minPrice`/`maxPrice`, trả `NotFoundException` (404) cho sách inactive và viết đủ test `AC-01..08`.
- **File dự kiến:** `src/java/com/bookhub/service/CatalogService.java`, `tests/integration/CatalogServiceIntegrationTest.java`.
- **Acceptance:**
  - [ ] Pass toàn bộ `@spec public-catalog/AC-01..08` (`TC-CAT-01..09`).

### T-004, T-005, T-006 — Servlets & JSP Views (`SCR-01`, `SCR-02`, `SCR-03`/`03b`)
- **Mục tiêu:** Dựng 3 màn hình public SSR khớp 5 ảnh baseline Figma (Hero `#1e5649`, 6 danh mục, 3 kệ sách, sidebar lọc + chips + empty state, chi tiết + hết hàng `SCR-03b` + sách tương tự) kèm thẻ SEO chuẩn.
- **File dự kiến:** `src/java/com/bookhub/controller/site/{Home,BookList,BookDetail}Servlet.java`, `web/WEB-INF/views/site/{home,books,book-detail}.jsp`.
- **Acceptance:**
  - [ ] Ảnh chụp 375/1280 khớp baseline (`VAC-01..02`); `scripts/verify.ps1` xanh.
- **Không làm:** Không xử lý submit thêm giỏ hay submit review/Q&A trong feature này.

## Đồ thị phụ thuộc
```text
T-001 ──► T-002 ──► T-003 ─┬─► T-004
                           ├─► T-005
                           └─► T-006
```

## Phê duyệt (Gate 3)
(chờ duyệt) · —
