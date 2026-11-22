# plan.md — Con trỏ session + sổ nháp tier Quick

> Đọc file này **sau** `AGENTS.md`/`CLAUDE.md`. Agent sở hữu. Tiến độ chi tiết của Standard/Full nằm ở `.sdd/specs/feat-<slug>/PROGRESS.md` (tránh đè nhau khi làm song song).
> Hết việc: xóa nội dung, trả file về template.

## 1. Đang làm
| Trường | Giá trị |
|---|---|
| Feature | `feat-{{slug}}` hoặc `none` |
| Tier | `Quick \| Standard \| Full` |
| Tiến độ chi tiết | `.sdd/specs/feat-{{slug}}/PROGRESS.md` (Standard/Full) |
| Nhánh Git | `feat/{{slug}}` |
| Cập nhật lần cuối | {{YYYY-MM-DD HH:mm}} |

## 2. Sổ nháp tier Quick (bỏ trống nếu không phải Quick)
- **Mục tiêu (1 câu, đo được):** {{...}}
- **Phạm vi file:** {{...}}
- [ ] Tái hiện lỗi bằng test đỏ
- [ ] Sửa tối thiểu → test xanh
- [ ] `scripts/verify` xanh
- [ ] Ghi bài học vào `CLAUDE.md §5` nếu lỗi có thể lặp lại

## 3. Blockers / câu hỏi cho người
- ⬜ {{...}}

## 4. Handoff cho session kế tiếp
- **Đã xong:** {{...}} · **Làm tiếp:** {{...}} · **Cẩn thận:** {{...}}
