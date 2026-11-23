---
name: testing
description: Chiến lược & quy tắc viết test (TDD, kim tự tháp test, mock, fixture). Kích hoạt khi viết/sửa test hoặc implement task có Acceptance Criteria.
---

# Skill: Testing

## Chiến lược
- Kim tự tháp: **nhiều unit** (nhanh, domain/usecase) → **vừa integration** (DB/HTTP thật hoặc container) → **ít e2e** (luồng sống còn).
- **TDD khi có thể:** viết test đỏ từ Acceptance Criteria (`AC-xx`) → code tối thiểu để xanh → refactor.
- Mỗi test truy vết được về `FR/AC/EC` trong SPEC (ghi ID trong tên hoặc comment).

## Quy tắc
- Cấu trúc **Arrange – Act – Assert**; một hành vi/test; tên mô tả hành vi: `should_reject_order_when_stock_is_zero`.
- Test **hành vi công khai**, không test chi tiết cài đặt nội bộ.
- Độc lập & lặp lại được: không phụ thuộc thứ tự, giờ hệ thống, mạng, dữ liệu dùng chung. Đóng băng thời gian & random seed.
- Mock **ranh giới** (port/HTTP/queue), không mock domain. Ưu tiên fake in-memory hơn mock phức tạp.
- Fixture/factory dữ liệu giả; **không** dữ liệu thật/PII.
- Bug fix ⇒ test hồi quy **đỏ trước, xanh sau**.
- Phải có test cho **đường lỗi/từ chối** (validation, 401/403, timeout), không chỉ happy path.
- ❌ Cấm: xóa/skip test, `sleep` để chờ, test phụ thuộc kết quả test khác, hạ ngưỡng coverage.

## Coverage
- ≥ 80% cho code mới; coverage là *chỉ báo*, không thay thế assertion có ý nghĩa.

## Checklist
- [ ] Mọi AC có test · [ ] Có test đường lỗi · [ ] Không flaky (chạy 3 lần liên tiếp) · [ ] Chạy nhanh
