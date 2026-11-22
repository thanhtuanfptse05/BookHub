#!/usr/bin/env bash
# scripts/ship.sh — "Tự động push an toàn": verify đầy đủ → commit → push NHÁNH FEATURE (không force) → mở PR + bật auto-merge.
# Dùng: ./scripts/ship.sh "feat(auth): add refresh rotation"   (có thay đổi chưa commit)
#       ./scripts/ship.sh                                       (chỉ push các commit đã có)
#       NO_PR=1 không tạo PR · NO_AUTOMERGE=1 tạo PR nhưng không bật auto-merge
# Agent chỉ được push bằng script này. KHÔNG push main/release/prod, KHÔNG force, KHÔNG --no-verify, KHÔNG deploy.
set -uo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$root"
msg="${1:-}"
fail() { echo "✖ $*" >&2; exit 1; }

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || fail "Không phải git repo (chạy git init)."
branch="$(git rev-parse --abbrev-ref HEAD)"
main="$(grep -E '^MAIN_BRANCH=' .sdd/project.conf 2>/dev/null | head -1 | cut -d= -f2- | tr -d '\r')"; main="${main:-main}"
if [[ "$branch" == "$main" || "$branch" == main || "$branch" == master || "$branch" == HEAD || "$branch" == release/* || "$branch" == prod* ]]; then
  fail "Cấm ship từ '$branch'. Tạo nhánh: git switch -c feat/<slug>"
fi
[[ "$branch" =~ ^(feat|fix|chore|docs|refactor|perf|test|build|ci)/[a-z0-9._/-]+$ ]] || fail "Tên nhánh '$branch' sai quy ước type/slug (vd feat/user-login)."

dirty="$(git status --porcelain)"
[[ -z "$dirty" || -n "$msg" ]] || fail 'Có thay đổi chưa commit: cần truyền message: ./scripts/ship.sh "type(scope): mô tả"'
if [[ -n "$msg" ]]; then
  [[ "$msg" =~ ^(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\([a-z0-9._/-]+\))?!?:\ .{1,100}$ ]] || fail "Message sai Conventional Commits (vd feat(auth): add x)."
fi

echo "▶ verify (đầy đủ) trước khi ship"
bash scripts/verify.sh || fail "verify chưa xanh — không ship."

if [[ -n "$dirty" ]]; then
  git add -A
  bad="$(git diff --cached --name-only | grep -E '(^|/)\.env(\..*)?$|\.(pem|key|p12|pfx)$' | grep -vE '\.example$' || true)"
  if [[ -n "$bad" ]]; then git reset -q; fail "Phát hiện file nhạy cảm sắp commit: $(echo "$bad" | tr '\n' ' ')"; fi
  git commit -m "$msg" || fail "commit thất bại (hook chặn?)."
fi

git push -u origin "$branch" || fail "push thất bại (không dùng force; hãy pull/rebase rồi thử lại)."
echo "✔ Đã push nhánh $branch"

[[ "${NO_PR:-0}" == "1" ]] && exit 0
command -v gh >/dev/null 2>&1 || { echo "ℹ Chưa cài GitHub CLI (gh) — mở PR thủ công."; exit 0; }
if ! gh pr view --json url -q .url >/dev/null 2>&1; then
  gh pr create --fill --base "$main" || { echo "⚠ Không tạo được PR tự động."; exit 0; }
fi
if [[ "${NO_AUTOMERGE:-0}" != "1" ]]; then
  if gh pr merge --auto --squash --delete-branch; then echo "✔ Auto-merge đã bật: PR tự merge khi CI xanh."
  else echo "⚠ Chưa bật được auto-merge (cần bật Allow auto-merge + branch protection: ./scripts/setup-github.sh --apply)."; fi
fi
