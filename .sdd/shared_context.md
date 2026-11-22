# shared_context.md — Nguồn sự thật chung

> Dùng khi có **nhiều Agent / nhiều session / nhiều người** cùng làm. Ai đổi contract ⇒ **cập nhật file này trong cùng PR** và thêm dòng vào mục 4.
> Chỉ ghi những gì **đang chạy thật** hoặc **đã chốt**. Chi tiết dài → link sang `docs/` (OpenAPI).

## 1. API Contracts đang hiệu lực
| ID | Method + Path | Version | Owner (feature/module) | Request → Response (tóm tắt) | Trạng thái |
|---|---|---|---|---|---|
| API-001 | `GET /api/v1/health` | v1 | platform | — → `200 {status:"ok"}` | stable |

> Nguồn chi tiết: `docs/openapi.yaml`

## 2. Kiểu dữ liệu dùng chung
```text
# Ví dụ — thay bằng kiểu thật của dự án
UserId   = string (UUID v7)
Money    = { amount: integer (đơn vị nhỏ nhất), currency: ISO-4217 }
ApiError = { code: string, message: string, details?: object, requestId: string }
Page<T>  = { items: T[], nextCursor?: string }
```

## 3. Sự kiện / Message dùng chung (nếu có queue)
| Event | Producer | Consumer(s) | Schema version | Ghi chú (idempotency key…) |
|---|---|---|---|---|
| | | | | |

## 4. 🔴 Breaking Changes Log (mới nhất ở trên)
| Ngày | Thay đổi | Ảnh hưởng tới | Cách migrate | Hạn chót | Người/Agent |
|---|---|---|---|---|---|
| | | | | | |

## 5. Tài nguyên đang bị "khóa" (tránh đụng độ giữa Agent)
| File/Module | Ai đang sửa | Feature/Task | Từ lúc |
|---|---|---|---|
| | | | |

## 6. Biến môi trường dùng chung (chỉ TÊN, không giá trị)
`DATABASE_URL`, `JWT_SECRET`, … → xem `.env.example`
