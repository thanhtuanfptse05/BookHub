# 09 — Quick fix (tier Quick)

```text
Việc: <mô tả lỗi/tinh chỉnh>. Tier Quick: ≲ 30 dòng diff, ≤ 2 file.
1) Ghi mục tiêu + phạm vi vào plan.md §2.
2) Kiểm tra: có chạm DB schema / auth / tiền / PII / contract công khai / CI không? Có → DỪNG, nâng lên tier Full.
3) Viết test hồi quy đỏ → sửa tối thiểu → xanh → scripts/verify.
4) Báo cáo ngắn: nguyên nhân · file đổi · test thêm · nên ghi bài học vào CLAUDE.md §5 không.
```
