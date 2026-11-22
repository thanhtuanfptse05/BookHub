# 08 — Refactor an toàn

```text
Mục tiêu refactor: <mô tả> · phạm vi file: <liệt kê>. Hành vi bên ngoài KHÔNG được đổi.
1) Xác nhận có test bao phủ vùng này; nếu thiếu → viết test đặc tả hành vi hiện tại (characterization test) TRƯỚC.
2) Chạy scripts/verify để có baseline xanh.
3) Đi từng bước nhỏ, mỗi bước verify xanh, mỗi bước 1 commit (`refactor(scope): …`).
4) Không đổi contract, schema, dependency, không mở rộng phạm vi. Gặp nhu cầu đó → dừng và hỏi.
5) Báo cáo: trước/sau (độ phức tạp, số dòng, phụ thuộc), xác nhận test không bị sửa ý nghĩa.
```
