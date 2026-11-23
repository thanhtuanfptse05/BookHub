# ADR-002: Chuẩn hóa Apache Tomcat 10.1.x & Namespace Jakarta EE 10

| Trường | Giá trị |
|---|---|
| Status | `Accepted` |
| Ngày | 2026-11-23 |
| Người quyết định | Nhóm phát triển BookHub (PRJ301) |
| Liên quan | `constitution H-09`, `.sdd/constraints/global.md` |

## 1. Bối cảnh
Tomcat 9 dùng `javax.*`, Tomcat 10.1 dùng `jakarta.*` (Jakarta EE 10: Servlet 6.0, JSP 3.1, EL 5.0), còn Tomcat 11 đòi hỏi chuẩn Jakarta EE 11 mới hơn. Trộn lẫn `javax.*` và `jakarta.*` là nguyên nhân lỗi compile/deploy phổ biến nhất.

## 2. Các phương án
| # | Phương án | Ưu | Nhược | Chi phí/Rủi ro |
|---|---|---|---|---|
| 1 | Tomcat 10.1.x + Jakarta EE 10 (`jakarta.*`) | Hỗ trợ tốt JDK 17, khớp Hibernate 6.6 & JSTL 3.0 | Không dùng lại được các JAR `javax.*` cũ | Thấp nếu khóa chặt allow-list |
| 2 | Tomcat 9 (`javax.*`) | Quen thuộc với tài liệu cũ | Đã cũ, lệch chuẩn Jakarta Persistence 3.1 / Hibernate 6.6 | Khó nâng cấp, lệch môi trường Tomcat 10.1 đã cài |

## 3. Quyết định
Chọn phương án **#1 (Tomcat 10.1.x + `jakarta.*`)** để đồng bộ hoàn toàn Servlet 6.0, JSP 3.1, JSTL 3.0, JPA 3.1 và Hibernate 6.6 trên JDK 17.

## 4. Hệ quả & đánh đổi (Trade-offs)
- ➕ Hệ sinh thái thư viện thống nhất, hiện đại, tương thích JDK 17 LTS.
- ➖ Mọi import, `web.xml` schema, `persistence.xml` và taglib URI (`jakarta.tags.*`) bắt buộc tuân chuẩn Jakarta EE 10; cấm tuyệt đối `javax.servlet.*` / `javax.persistence.*`.

## 5. Cách kiểm chứng & điều kiện xem xét lại
- Thước đo thành công: 0 import `javax.servlet`/`javax.persistence` trong `src/`; WAR deploy sạch lên Tomcat 10.1.x.
- Xem lại khi: Môi trường server chuyển sang Jakarta EE 11 / Tomcat 11 sau đồ án.
