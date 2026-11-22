# 15 — SEO · AEO · GEO cho trang công khai

```text
Trang/feature công khai: <slug hoặc URL>. Không dùng cho app đăng nhập/dashboard (đặt noindex).
Đọc: AGENTS.md, .sdd/constraints/seo.md, .sdd/seo/SEO.md, .sdd/skills/seo-aeo-geo.md, SPEC.md + DESIGN.md của feature.
Việc:
1) Cập nhật bảng §4 trong .sdd/seo/SEO.md cho trang này: ý định/câu hỏi chính, title (≲ 60), description (≤ 160), H1, loại schema. Nếu §1–§3 SEO.md còn trống (định danh thực thể, chiến lược render, AI_CRAWLERS) → hỏi tôi, KHÔNG tự quyết chính sách AI crawler.
2) Soạn/viết nội dung theo AEO + GEO: heading dạng câu hỏi → đáp án trực tiếp 40–60 từ → giải thích; danh sách/bảng/định nghĩa; nêu nguồn kiểm chứng, số liệu & trích dẫn có gán tên. KHÔNG bịa số liệu/đánh giá/khách hàng; không có dữ liệu thật thì bỏ câu đó và hỏi tôi.
3) Bảo đảm SE-01: nội dung có trong HTML ban đầu (SSR/SSG/prerender). Head đủ (title, description, canonical tuyệt đối, viewport, lang, OG/Twitter). JSON-LD hợp lệ và khớp nội dung đang hiển thị (Organization + WebSite toàn site; Article/Product/... theo trang; BreadcrumbList).
4) Cập nhật robots.txt + sitemap.xml nếu thêm/đổi URL; robots phải khớp AI_CRAWLERS trong .sdd/project.conf.
5) Build rồi chạy `scripts/seo-check` + kiểm HTML thô (`curl -s <URL> | grep -i "<h1"`); sửa mọi phát hiện. Chạy `scripts/verify`.
Báo cáo: URL/title/description/schema · phát hiện đã sửa · dữ liệu còn thiếu cần tôi cung cấp.
```
