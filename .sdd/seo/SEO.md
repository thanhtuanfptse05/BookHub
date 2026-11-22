# SEO.md — Nguồn sự thật SEO · AEO · GEO của {{PROJECT_NAME}}

> Điền **1 lần** đầu dự án (prompt `15-seo-page` hỗ trợ), cập nhật khi thêm trang công khai. Luật: `.sdd/constraints/seo.md`. Mẫu & playbook: `.sdd/skills/seo-aeo-geo.md`.
> Chỉ dùng cho website/trang công khai. App đăng nhập, API, dashboard nội bộ: đặt `noindex` và bỏ qua file này.

## 1. Định danh thực thể (giữ **nhất quán** ở mọi nơi — AI dựa vào đây để nhận ra bạn)
| Trường | Giá trị |
|---|---|
| Domain chuẩn (canonical) | {{SITE_URL}} |
| Tên thương hiệu (viết đúng 1 cách) | {{PROJECT_NAME}} |
| Mô tả 1 câu (dùng lại làm mô tả Organization) | {{PROJECT_ONE_LINER}} |
| Ngôn ngữ / thị trường | {{vi-VN}} |
| Hồ sơ chính thức (`sameAs`) | {{LinkedIn, Facebook, GitHub, Wikipedia/Wikidata nếu có}} |
| Liên hệ / địa chỉ (nếu là doanh nghiệp) | {{...}} |

## 2. Chiến lược render (bắt buộc cho trang công khai — SE-01)
- Chế độ: `SSG | SSR | Prerender` — framework: {{UI_STACK}}.
- Trang chỉ-đăng-nhập / app: `noindex` + không đưa vào sitemap.
- Kiểm tra: `curl -s {{SITE_URL}} | grep -i "<h1"` phải thấy tiêu đề chính.

## 3. Chính sách AI crawler (quyết định của người)
`AI_CRAWLERS = {{allow | block-training | block-all}}` (đặt trong `.sdd/project.conf`) — lý do: {{...}}

## 4. Danh mục trang công khai
| URL | Ý định / câu hỏi chính trang trả lời | Title (≲ 60) | Description (≲ 160) | H1 | Schema | Trạng thái |
|---|---|---|---|---|---|---|
| `/` | {{vd: {{PROJECT_NAME}} là gì & dành cho ai?}} | | | | Organization + WebSite | ⬜ |

## 5. Quy tắc nội dung AEO/GEO (áp dụng khi viết mọi trang)
1. **Trả lời trước, giải thích sau:** dưới mỗi heading dạng câu hỏi, câu đầu là đáp án trực tiếp (≈ 40–60 từ).
2. **Có bằng chứng:** nêu nguồn, số liệu, trích dẫn có gán tên; không bịa. Không có dữ liệu thật ⇒ bỏ câu đó.
3. **Dễ trích:** danh sách, bảng, định nghĩa ngắn, một ý một đoạn.
4. **Tươi & có tác giả:** hiển thị ngày đăng/cập nhật và tác giả; cập nhật `dateModified` khi sửa thật.
5. **Một trang – một chủ đề**; liên kết nội bộ tới trang liên quan bằng anchor có nghĩa.

## 6. Đo lường
| Kênh | Việc làm | Tần suất |
|---|---|---|
| Google Search Console + Bing Webmaster | Gửi `sitemap.xml`, xem lập chỉ mục & lỗi | Sau mỗi lần deploy lớn |
| Lighthouse CI (`LHCI_ENABLED`) | Điểm SEO ≥ 95, CWV đạt | Mỗi PR |
| Kiểm trích dẫn AI (thủ công) | Hỏi ChatGPT/Perplexity/Gemini các câu hỏi mục tiêu, ghi có/không được trích | Hằng tháng |
