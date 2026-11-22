---
name: sql-performance
description: Tối ưu truy vấn SQL, thiết kế index, tránh N+1, an toàn khi migrate. Kích hoạt khi viết/sửa query, repository, migration hoặc khi có cảnh báo chậm.
---

# Skill: SQL Performance

## Khi nào dùng
Viết/sửa query, repository, migration; endpoint chậm; thêm bộ lọc/sắp xếp/phân trang.

## Quy trình
1. **Đo trước, tối ưu sau:** chạy `EXPLAIN (ANALYZE, BUFFERS)` trên dữ liệu đủ lớn; ghi kết quả vào PR.
2. Xác định mẫu truy cập (filter, join, sort, limit) → mới thiết kế index.
3. Sửa → đo lại → chỉ giữ thay đổi có cải thiện thật.

## Quy tắc
- ❌ Không `SELECT *`; chỉ lấy cột cần.
- ❌ Không query trong vòng lặp (**N+1**) ⇒ dùng `JOIN`, `IN (...)`, hoặc batch/dataloader.
- ✅ Phân trang bằng **keyset/cursor** (`WHERE (created_at,id) < (?,?) ORDER BY … LIMIT n`); tránh `OFFSET` lớn.
- ✅ Index theo thứ tự: cột `=` → cột range → cột `ORDER BY`. Composite index tuân quy tắc leftmost-prefix.
- ✅ Khóa ngoại và cột hay lọc/join phải có index. Dùng **partial index** cho soft-delete (`WHERE deleted_at IS NULL`).
- ❌ Không bọc cột trong hàm ở `WHERE` (`LOWER(col)`, `DATE(col)`) nếu không có functional index; tránh `LIKE '%x'`.
- ✅ Giữ transaction ngắn; không gọi HTTP/IO ngoài trong transaction.
- ✅ **Chống race condition & deadlock:** cập nhật số dư/tồn kho/slot dùng atomic update (`UPDATE ... SET qty = qty - ? WHERE id = ? AND qty >= ?`), optimistic lock (`version`), hoặc `SELECT ... FOR UPDATE`; khi khóa nhiều dòng phải khóa theo **thứ tự ID tăng dần**.
- ✅ Thao tác hàng loạt: chia lô (1k–5k dòng), có thể resume, idempotent.
- ✅ Giới hạn kích thước kết quả (`LIMIT`), cấu hình **connection pool** vừa tải và đặt **query timeout** (`statement_timeout`).

## Migration an toàn
- Thêm cột: nullable hoặc có default nhẹ → backfill theo lô → mới đặt `NOT NULL`.
- Tạo index trên bảng lớn: `CREATE INDEX CONCURRENTLY` (PostgreSQL) hoặc tương đương.
- Đổi/xóa cột: mô hình **expand → migrate → contract** qua ≥ 2 release.
- Luôn có `down` + đã thử trên bản sao.

## Checklist review
- [ ] Có EXPLAIN & không Seq Scan trên bảng lớn không chủ ý
- [ ] Không N+1 · Có giới hạn/phân trang · Index phù hợp & không thừa
- [ ] Migration an toàn, đảo ngược được
