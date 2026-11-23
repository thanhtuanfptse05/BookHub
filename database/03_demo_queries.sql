/* ============================================================================
   BookHub — 03_demo_queries.sql
   Phần A: Đối chiếu báo cáo tổng hợp PRJ301 (COUNT, SUM, AVG, MIN, MAX, Bestseller)
   Phần B: Kiểm toán toàn vẹn logic nghiệp vụ trên dữ liệu seed (mọi query kiểm toán = 0 dòng lỗi)
   ============================================================================ */

USE BookHubDB;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* ============================================================================
   PHẦN A — BÁO CÁO THỐNG KÊ (Khớp SPEC 08-feat-admin-users-reports & §19.3)
   ============================================================================ */
DECLARE @fromDate DATETIME2(0) = '2026-11-01 00:00:00';
DECLARE @toDate   DATETIME2(0) = '2026-12-01 00:00:00';

-- A1. COUNT đơn hàng theo từng trạng thái (kỳ vọng: DELIVERED=4, PENDING=1, CONFIRMED=1, SHIPPING=1, CANCELLED=1)
SELECT status, COUNT_BIG(*) AS order_count
FROM dbo.Orders
WHERE created_at >= @fromDate AND created_at < @toDate
GROUP BY status
ORDER BY status;

-- A2. SUM doanh thu đơn ĐÃ GIAO (kỳ vọng: 2,446,000.00 VND)
SELECT COALESCE(SUM(total_amount), 0) AS delivered_revenue
FROM dbo.Orders
WHERE status = 'DELIVERED'
  AND created_at >= @fromDate AND created_at < @toDate;

-- A3. AVG giá trị đơn ĐÃ GIAO (kỳ vọng: 611,500.00 VND)
SELECT COALESCE(AVG(CAST(total_amount AS DECIMAL(18,2))), 0) AS average_order_value
FROM dbo.Orders
WHERE status = 'DELIVERED'
  AND created_at >= @fromDate AND created_at < @toDate;

-- A4. MIN / MAX / AVG giá sách ĐANG BÁN (kỳ vọng: MIN=45,000.00, MAX=320,000.00 — KHÔNG bị kéo bởi sách inactive 15,000)
SELECT MIN(b.price) AS min_price,
       MAX(b.price) AS max_price,
       AVG(CAST(b.price AS DECIMAL(18,2))) AS average_price
FROM dbo.Books b
JOIN dbo.Categories c ON c.category_id = b.category_id
WHERE b.is_active = 1 AND c.is_active = 1;

-- A5. SUM tổng số bản sách tồn kho đang kinh doanh
SELECT COALESCE(SUM(b.stock_qty), 0) AS active_stock_units
FROM dbo.Books b
JOIN dbo.Categories c ON c.category_id = b.category_id
WHERE b.is_active = 1 AND c.is_active = 1;

-- A6. Top 10 sách bán chạy từ đơn DELIVERED (SUM quantity)
SELECT TOP (10)
       oi.book_id,
       oi.book_title_snapshot,
       SUM(oi.quantity) AS sold_quantity,
       SUM(oi.line_total) AS item_revenue
FROM dbo.OrderItems oi
JOIN dbo.Orders o ON o.order_id = oi.order_id
WHERE o.status = 'DELIVERED'
GROUP BY oi.book_id, oi.book_title_snapshot
ORDER BY SUM(oi.quantity) DESC, oi.book_id ASC;

-- A7. Hàng chờ quản trị (Pending Reviews = 2, Open Questions = 2, Pending Orders = 1)
SELECT
    (SELECT COUNT_BIG(*) FROM dbo.Reviews   WHERE status = 'PENDING') AS pending_reviews,
    (SELECT COUNT_BIG(*) FROM dbo.Questions WHERE status = 'OPEN')    AS open_questions,
    (SELECT COUNT_BIG(*) FROM dbo.Orders    WHERE status = 'PENDING') AS pending_orders;

/* ============================================================================
   PHẦN B — KIỂM TOÁN LOGIC NGHIỆP VỤ (MỌI GIÁ TRỊ VIOLATION PHẢI BẰNG 0)
   ============================================================================ */
SELECT N'1. Order subtotal ≠ SUM(OrderItems.line_total)' AS audit_rule, COUNT(*) AS violations
FROM dbo.Orders o
LEFT JOIN (
    SELECT order_id, SUM(line_total) AS calc_subtotal
    FROM dbo.OrderItems
    GROUP BY order_id
) oi ON oi.order_id = o.order_id
WHERE o.subtotal <> COALESCE(oi.calc_subtotal, 0)

UNION ALL
SELECT N'2. Review không thuộc đơn DELIVERED của chính user (vi phạm BR-REV-001)', COUNT(*)
FROM dbo.Reviews r
WHERE NOT EXISTS (
    SELECT 1
    FROM dbo.Orders o
    JOIN dbo.OrderItems oi ON oi.order_id = o.order_id
    WHERE o.user_id = r.user_id
      AND o.status = 'DELIVERED'
      AND oi.book_id = r.book_id
)

UNION ALL
SELECT N'3. Question ANSWERED nhưng thiếu Answer hoặc không phải ADMIN trả lời (vi phạm BR-QA)', COUNT(*)
FROM dbo.Questions q
LEFT JOIN dbo.Answers a ON a.question_id = q.question_id
LEFT JOIN dbo.Users u   ON u.user_id = a.responder_user_id
LEFT JOIN dbo.Roles rl  ON rl.role_id = u.role_id
WHERE q.status = 'ANSWERED' AND (a.answer_id IS NULL OR rl.role_code <> 'ADMIN')

UNION ALL
SELECT N'4. Question OPEN/HIDDEN nhưng lại có bản ghi Answer', COUNT(*)
FROM dbo.Questions q
JOIN dbo.Answers a ON a.question_id = q.question_id
WHERE q.status <> 'ANSWERED'

UNION ALL
SELECT N'5. CartItem vượt tồn kho hoặc sách/danh mục inactive (vi phạm BR-CART)', COUNT(*)
FROM dbo.CartItems ci
JOIN dbo.Books b      ON b.book_id = ci.book_id
JOIN dbo.Categories c ON c.category_id = b.category_id
WHERE b.is_active = 0 OR c.is_active = 0 OR ci.quantity > b.stock_qty

UNION ALL
SELECT N'6. Password chưa băm BCrypt ($2a$/$2b$) hoặc email chưa chuẩn hóa lowercase', COUNT(*)
FROM dbo.Users
WHERE (password_hash NOT LIKE '$2a$%' AND password_hash NOT LIKE '$2b$%')
   OR email <> LOWER(LTRIM(RTRIM(email)));
GO
