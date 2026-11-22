---
name: error-handling
description: Chuẩn xử lý lỗi, format lỗi API, retry/timeout, logging lỗi. Kích hoạt khi viết usecase, gọi I/O ngoài, hoặc thiết kế response lỗi.
---

# Skill: Error Handling

## Nguyên tắc
- **Fail fast, fail loud, fail safe.** Không nuốt lỗi (`catch {}` rỗng bị cấm). Bắt lỗi chỉ khi có thể **xử lý** hoặc **bổ sung ngữ cảnh**.
- Phân loại lỗi: `ValidationError (4xx)`, `NotFound`, `Conflict`, `Unauthorized/Forbidden`, `DomainRuleViolation`, `ExternalServiceError`, `Unexpected (5xx)`.
- `domain/usecase` ném lỗi **domain** (không biết HTTP); `interface/` ánh xạ sang mã HTTP/exit code.

## Format lỗi API chuẩn
```json
{ "error": { "code": "ORDER_NOT_CANCELLABLE", "message": "Đơn đã được giao.", "details": {"orderId": "..."}, "requestId": "req_abc123" } }
```
- `code`: ổn định, UPPER_SNAKE, để client xử lý. `message`: thân thiện, **không** lộ nội bộ.
- Mã HTTP: 400/422 validate · 401 chưa xác thực · 403 cấm · 404 không có · 409 xung đột · 429 rate limit · 5xx lỗi hệ thống.

## I/O ngoài
- Luôn **timeout**; retry chỉ với thao tác **idempotent** & lỗi tạm thời, dùng exponential backoff + jitter, tối đa N lần.
- Circuit breaker cho dịch vụ không ổn định; fallback rõ ràng hoặc lỗi rõ ràng.
- Giữ nguyên `cause` khi bọc lỗi; không mất stack gốc.

## Logging lỗi
- Log **một lần** ở biên (tránh log lặp ở mọi tầng). Kèm `requestId`, user/tenant (đã mask), tham số quan trọng (không PII/secret).
- Mức: `warn` cho lỗi do client; `error` cho lỗi hệ thống cần người xem.

## Test
- Mỗi nhánh lỗi trong SPEC (Unwanted/EC-xx) có test riêng; test cả timeout/retry bằng mock.
