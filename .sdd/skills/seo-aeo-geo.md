---
name: seo-aeo-geo
description: Playbook SEO + AEO (trả lời trực tiếp) + GEO (được AI trích dẫn) cho trang công khai — render, thẻ meta, JSON-LD, robots/sitemap, chính sách AI crawler, cách viết nội dung. Kích hoạt khi tạo/sửa trang công khai, landing page, blog, trang sản phẩm, hoặc khi cấu hình robots/sitemap/schema.
---

# Skill: SEO · AEO · GEO

Luật nằm ở `constraints/seo.md`; skill này là **cách làm**. Nguồn sự thật dự án: `.sdd/seo/SEO.md`.

## Quy trình cho mỗi trang công khai
1. **Chốt ý định:** trang trả lời câu hỏi nào? Ghi vào bảng §4 của `SEO.md`.
2. **Render phía server:** SSG/SSR/prerender (SE-01). Kiểm: `curl -s URL | grep -i "<h1"`.
3. **Thẻ head:** title duy nhất, description, canonical tuyệt đối, viewport, `lang`, OG/Twitter.
4. **Nội dung answer-first:** heading dạng câu hỏi → đáp án 40–60 từ → giải thích → danh sách/bảng.
5. **Bằng chứng:** nguồn + số liệu + trích dẫn có gán tên; không bịa.
6. **JSON-LD** khớp nội dung hiển thị (mẫu bên dưới).
7. `scripts/seo-check` + Lighthouse; sửa hết phát hiện.

## Mẫu `<head>` tối thiểu
```html
<html lang="vi">
<title>Tên trang cụ thể | Thương hiệu</title>           <!-- ≲ 60 ký tự, duy nhất -->
<meta name="description" content="Tóm tắt 50–160 ký tự, có ý định chính.">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="canonical" href="https://example.com/duong-dan">
<meta property="og:title" content="..."> <meta property="og:description" content="...">
<meta property="og:image" content="https://example.com/og.png"> <meta property="og:url" content="https://example.com/duong-dan">
<meta property="og:type" content="website"> <meta name="twitter:card" content="summary_large_image">
```

## JSON-LD (đặt `<script type="application/ld+json">`)
```json
{ "@context": "https://schema.org", "@type": "Organization",
  "name": "Thương hiệu", "url": "https://example.com", "logo": "https://example.com/logo.png",
  "description": "Mô tả 1 câu nhất quán", "sameAs": ["https://www.linkedin.com/company/..."] }
```
```json
{ "@context": "https://schema.org", "@type": "Article",
  "headline": "Tiêu đề", "author": { "@type": "Person", "name": "Tên tác giả" },
  "datePublished": "2026-01-10", "dateModified": "2026-02-01",
  "image": "https://example.com/cover.png", "publisher": { "@type": "Organization", "name": "Thương hiệu" } }
```
```json
{ "@context": "https://schema.org", "@type": "FAQPage", "mainEntity": [
  { "@type": "Question", "name": "Câu hỏi hiển thị trên trang?",
    "acceptedAnswer": { "@type": "Answer", "text": "Đúng câu trả lời đang hiển thị." } } ] }
```
- Thêm `WebSite`, `BreadcrumbList`; `Product`/`LocalBusiness`/`SoftwareApplication` theo loại trang.
- **Sự thật 2026:** Google đã **ngừng** hiển thị FAQ rich result (mọi site); `FAQPage` vẫn hợp lệ, vô hại và dễ cho máy đọc, nhưng **không** phải đòn bẩy xếp hạng. Giá trị thật nằm ở **nội dung hiển thị rõ ràng**. Schema chỉ được mô tả thứ **đang hiện** trên trang.

## AEO — viết để được chọn làm câu trả lời
- Một câu hỏi = một heading + đáp án trực tiếp ngay dưới (không dẫn dắt dài).
- Định nghĩa ngắn ("X là …"), so sánh bằng bảng, các bước bằng danh sách đánh số.
- Độ dài đoạn đáp án vừa đủ để trích nguyên (≈ 40–60 từ; mở rộng ở đoạn sau).

## GEO — viết để AI tin và trích
- Nghiên cứu GEO (Princeton và cộng sự) cho thấy **thêm nguồn trích dẫn, số liệu, trích dẫn có gán tên** và văn phong mạch lạc làm tăng khả năng được trích rõ rệt (kết quả khác nhau theo lĩnh vực — đừng xem là phép màu).
- AI crawler **không chạy JS** → nội dung phải có trong HTML ban đầu.
- Tên thương hiệu/sản phẩm viết **một cách duy nhất** ở mọi trang, mạng xã hội, schema (`sameAs`).
- Hiển thị tác giả, ngày đăng/cập nhật; cập nhật nội dung định kỳ.
- Thương hiệu được nhắc ở nơi uy tín bên ngoài (báo chí, thư mục ngành, Wikipedia nếu đủ điều kiện) giúp AI nhận diện thực thể.

## Chính sách AI crawler
Xem `constraints/seo.md` và `.sdd/seo/templates/robots.txt`. Nhớ: chặn *training* ≠ chặn *search*; `Google-Extended` không ảnh hưởng xếp hạng Google; bot user-triggered có thể bỏ qua robots.txt.

## Checklist review
- [ ] Nội dung có trong HTML thô · [ ] title/description/h1/canonical/lang đủ · [ ] OG đủ
- [ ] JSON-LD hợp lệ & khớp nội dung · [ ] robots.txt + sitemap.xml · [ ] AI_CRAWLERS khớp robots
- [ ] Đáp án trực tiếp đầu mục · [ ] Có nguồn/số liệu thật · [ ] Có tác giả + ngày
- [ ] `seo-check` sạch · [ ] Lighthouse SEO ≥ 95
