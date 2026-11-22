# constraints/seo.md — Ràng buộc SEO · AEO · GEO (áp dụng cho trang/website công khai)

> SEO = được Google/Bing xếp hạng · AEO = được chọn làm *câu trả lời trực tiếp* · GEO = được AI (ChatGPT, Claude, Perplexity, Gemini, AI Overviews) *đọc và trích dẫn*.
> Nguồn sự thật theo dự án: `.sdd/seo/SEO.md`. Playbook & mẫu code: `.sdd/skills/seo-aeo-geo.md`. Luật 🤖 do `scripts/seo-check` quét trên HTML đã build.

| ID | Luật | Cưỡng chế |
|---|---|---|
| SE-01 | **Nội dung trang công khai phải có sẵn trong HTML trả về lần đầu** (SSR / SSG / prerender). Cấm trang chỉ hiện nội dung sau khi chạy JS (CSR/SPA thuần): AI crawler (GPTBot, ClaudeBot, PerplexityBot…) thường **không chạy JS** nên coi trang là trống. | 🤖 `seo-check` (thân trang gần như rỗng) |
| SE-02 | Mỗi trang indexable có: `<title>` **duy nhất** (≲ 60 ký tự), `meta description` (50–160), **đúng 1 `<h1>`**, `<html lang>`, `meta viewport`, `link rel=canonical` (URL tuyệt đối). | 🤖 `seo-check` |
| SE-03 | Open Graph + Twitter Card đầy đủ: `og:title`, `og:description`, `og:image`, `og:url`, `og:type`. | 🤖 `seo-check` |
| SE-04 | Heading phân cấp logic (h1→h2→h3, không nhảy cấp); mọi `<img>` có `alt` mô tả; liên kết có anchor text rõ nghĩa; nội dung quan trọng có liên kết nội bộ. | 🤖 `seo-check` (h1, alt) · review |
| SE-05 | Có `robots.txt` (trỏ tới sitemap) và `sitemap.xml` (URL tuyệt đối, `lastmod` thật) ở gốc; **production không** `noindex`/`Disallow: /` nhầm; staging phải `noindex` và không rò sang production. | 🤖 `seo-check` |
| SE-06 | **Structured data JSON-LD hợp lệ và khớp nội dung hiển thị:** `Organization` + `WebSite` toàn site; `Article`/`Product`/`LocalBusiness`/`BreadcrumbList`… theo loại trang. **Cấm** đánh giá/xếp hạng/FAQ bịa hoặc không hiện trên trang. | 🤖 `seo-check` (JSON hợp lệ, có hay không) · review |
| SE-07 | URL chữ thường, gạch nối, ổn định; đổi URL = **301**; trang 404 trả mã **404**; chỉ HTTPS; chốt một dạng `www`/non-`www`. | Review · Lighthouse CI |
| SE-08 | Core Web Vitals đạt: LCP < 2.5s · CLS < 0.1 · INP < 200ms (`skills/frontend-performance.md`). | Lighthouse CI (`LHCI_ENABLED`) |
| SE-09 | **AEO — viết để được trích:** mỗi trang xoay quanh câu hỏi/ý định rõ; heading dạng câu hỏi khi phù hợp; **trả lời trực tiếp ngay đầu mục (≈ 40–60 từ)** rồi mới giải thích; dùng danh sách/bảng/định nghĩa; một chủ đề mỗi trang. | Review (`14-ui-review`/`15-seo-page`) |
| SE-10 | **GEO — viết để AI tin và trích:** nêu **nguồn kiểm chứng được**, số liệu và trích dẫn có gán tên; **không bịa** số liệu/đánh giá/logo/khách hàng; hiển thị tác giả + ngày đăng/cập nhật (`datePublished`/`dateModified`); gọi thực thể (tên thương hiệu, sản phẩm) **nhất quán** mọi nơi; `Organization.sameAs` trỏ tới hồ sơ chính thức. | Review |
| SE-11 | **Chính sách AI crawler khai báo tường minh trong `robots.txt`** theo `AI_CRAWLERS` (xem dưới): phân biệt *training* với *search/retrieval*; hiểu rằng bot *user-triggered* có thể bỏ qua robots. | 🤖 `seo-check` (robots khớp chính sách) |
| SE-12 | `llms.txt` là **tùy chọn**: chưa có bằng chứng ảnh hưởng xếp hạng/trích dẫn (Google nói không dùng). Không đầu tư thời gian; thêm nếu rẻ và không thay cho SE-01…SE-10. | — |
| SE-13 | Đa ngôn ngữ: mỗi URL một ngôn ngữ; `hreflang` hai chiều + `x-default`. | Review |
| SE-14 | Cấm spam: văn bản ẩn, nhồi từ khóa, cloaking, trang cửa (doorway), nội dung sinh hàng loạt vô giá trị. | Review |
| SE-15 | Có dấu hiệu tin cậy (E-E-A-T): trang Giới thiệu, Liên hệ, Chính sách; tác giả có tiểu sử; thông tin nhận diện công ty thật. | Review |

## Chính sách AI crawler (`AI_CRAWLERS` trong `.sdd/project.conf`)
| Giá trị | Ý nghĩa | `robots.txt` |
|---|---|---|
| `allow` (mặc định) | Muốn được AI trích dẫn **và** cho phép huấn luyện | Không chặn bot AI nào |
| `block-training` | Không cho huấn luyện nhưng **vẫn được AI search trích dẫn** | `Disallow: /` cho `GPTBot`, `ClaudeBot`, `Google-Extended`, `Applebot-Extended`, `CCBot`; **cho phép** `OAI-SearchBot`, `Claude-SearchBot`, `PerplexityBot` |
| `block-all` | Không xuất hiện trong AI | Chặn cả nhóm training lẫn search |
| `off` | Không kiểm | — |
> Đây là **quyết định kinh doanh của người**, Agent không tự đổi. `Google-Extended` chỉ là token điều khiển (không phải crawler) và **không** ảnh hưởng xếp hạng Google Search.

## Quy trình xác minh SEO (trước khi báo xong trang công khai)
1. Build rồi chạy `scripts/seo-check` (cấu hình `SEO_BUILD_DIR`, `SEO_CHECK=warn|error`); sửa mọi phát hiện.
2. **Kiểm tra "HTML thô":** `curl -s <URL> | grep -i "<h1"` — nếu không thấy h1/nội dung chính ⇒ vi phạm SE-01.
3. Dán JSON-LD vào Rich Results Test / Schema Validator; chạy Lighthouse (mục SEO ≥ 95).
4. Sau deploy: gửi sitemap lên Google Search Console & Bing Webmaster Tools (làm tay một lần), theo dõi lập chỉ mục.

## Cấu hình
`.sdd/project.conf`: `SITE_URL`, `SEO_BUILD_DIR`, `SEO_CHECK=off|warn|error`, `AI_CRAWLERS`.
