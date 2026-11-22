#!/usr/bin/env bash
# scripts/setup-github.sh — Cấu hình GitHub cho CI/CD tự động (mặc định DRY-RUN: chỉ in lệnh). Cần GitHub CLI `gh` đã đăng nhập.
# Dùng: ./scripts/setup-github.sh            (xem trước)
#       ./scripts/setup-github.sh --apply    (áp dụng thật)
#       REVIEWS=1 ./scripts/setup-github.sh --apply   (yêu cầu 1 approval; mặc định 0 = solo)
set -uo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$root"
apply=0; [[ "${1:-}" == "--apply" ]] && apply=1
reviews="${REVIEWS:-0}"
main="$(grep -E '^MAIN_BRANCH=' .sdd/project.conf 2>/dev/null | head -1 | cut -d= -f2- | tr -d '\r')"; main="${main:-main}"
if [[ $apply -eq 1 ]] && ! command -v gh >/dev/null 2>&1; then echo "✖ Cần cài GitHub CLI: https://cli.github.com" >&2; exit 1; fi

protection="$(cat <<JSON
{"required_status_checks":{"strict":true,"contexts":["verify","secrets"]},"enforce_admins":false,
 "required_pull_request_reviews":{"required_approving_review_count":$reviews,"dismiss_stale_reviews":true},
 "restrictions":null,"required_linear_history":true,"allow_force_pushes":false,"allow_deletions":false,
 "required_conversation_resolution":true}
JSON
)"
echo "=== Cấu hình sẽ áp dụng cho nhánh '$main' ==="
echo "1) gh repo edit --enable-auto-merge --delete-branch-on-merge --enable-squash-merge --enable-merge-commit=false --enable-rebase-merge=false"
echo "2) Branch protection (PUT repos/{owner}/{repo}/branches/$main/protection):"; echo "$protection"
echo "3) Tạo environment 'production' (PUT repos/{owner}/{repo}/environments/production) — thêm Required reviewers thủ công trên GitHub nếu cần duyệt deploy"
if [[ $apply -eq 0 ]]; then echo; echo "ℹ Dry-run. Thêm --apply để áp dụng."; exit 0; fi

gh repo edit --enable-auto-merge --delete-branch-on-merge --enable-squash-merge --enable-merge-commit=false --enable-rebase-merge=false || echo "⚠ gh repo edit lỗi (quyền admin?)"
echo "$protection" | gh api -X PUT "repos/{owner}/{repo}/branches/$main/protection" --input - || echo "⚠ Chưa đặt được branch protection (repo private cần GitHub Pro/Team; hoặc nhánh chưa tồn tại — push main trước)."
gh api -X PUT "repos/{owner}/{repo}/environments/production" >/dev/null || echo "⚠ Chưa tạo được environment production."
echo "✔ Xong. Kiểm tra lại trong Settings → Branches / Environments."
