#!/usr/bin/env bash
# scripts/verify.sh — Cổng kiểm tra duy nhất: spec-check + lint + ui-check + test + build + audit + unused + secret-scan.
# Dùng: ./scripts/verify.sh [--fast] [--strict]
#   --fast   spec-check + lint + ui-check + secret-scan (cho pre-commit)
#   --strict lệnh cốt lõi chưa cấu hình = FAIL (cho CI)
set -uo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$root"
fast=0; strict=0
for a in "$@"; do case "$a" in --fast) fast=1;; --strict) strict=1;; esac; done

conf() { grep -E "^$1=" .sdd/project.conf 2>/dev/null | head -1 | cut -d= -f2- | tr -d '\r'; }
failed=(); skipped=()

step() {
  local name="$1" cmd="$2"
  if [[ -z "${cmd// /}" || "$cmd" == *"{{"* ]]; then
    skipped+=("$name"); echo "⚠ $name: chưa cấu hình trong .sdd/project.conf — bỏ qua"; return
  fi
  echo "▶ $name: $cmd"
  if bash -c "$cmd"; then echo "✔ $name"; else echo "✖ $name FAIL"; failed+=("$name"); fi
}
opt_step() { [[ -n "${2// /}" ]] && step "$1" "$2" || true; }

echo "▶ spec-check"
bash scripts/spec-check.sh || failed+=("spec-check")

step lint "$(conf LINT_CMD)"

echo "▶ ui-check"
bash scripts/ui-check.sh || failed+=("ui-check")

if [[ $fast -eq 0 ]]; then
  step test "$(conf TEST_CMD)"
  step build "$(conf BUILD_CMD)"
  opt_step audit "$(conf AUDIT_CMD)"     # quét CVE dependency (pnpm audit / pip-audit...)
  opt_step unused "$(conf UNUSED_CMD)"   # quét dead code / package thừa (knip / vulture...)
  echo "▶ seo-check"                      # SEO/AEO/GEO trên HTML đã build (bỏ qua nếu SEO_BUILD_DIR trống)
  bash scripts/seo-check.sh || failed+=("seo-check")
  opt_step a11y "$(conf A11Y_CMD)"       # tùy chọn: chỉ chạy khi dự án có UI và đã cấu hình
  opt_step visual "$(conf VISUAL_CMD)"   # tùy chọn: ảnh chụp/so sánh trực quan
fi

if command -v gitleaks >/dev/null 2>&1; then
  step secret-scan "gitleaks detect --no-banner --redact"
else
  echo "▶ secret-scan (regex dự phòng — chưa cài gitleaks)"
  if [[ -d .git ]] && command -v git >/dev/null 2>&1; then
    cands="$(git ls-files --cached --others --exclude-standard)"
  else
    cands="$(find . -type f -not -path './.git/*' -not -path '*/node_modules/*' -not -path '*/.venv/*' -not -path '*/venv/*' \
      -not -path './dist/*' -not -path './build/*' -not -path './out/*' -not -name '.env' -not -name '.env.*' | sed 's|^\./||')"
  fi
  cands="$(printf '%s\n' "$cands" | grep -vE '^\.sdd/|^scripts/verify\.(ps1|sh)$|\.example$|package-lock\.json$|\.(png|jpg|jpeg|gif|webp|ico|pdf|zip|tar|gz|7z|exe|dll|so|dylib|wasm|woff2?|ttf|map|lock|sum)$' || true)"
  re='-----BEGIN (RSA |EC |DSA |OPENSSH |PGP )?PRIVATE KEY-----|AKIA[0-9A-Z]{16}|gh[pousr]_[A-Za-z0-9_]{36,}|xox[baprs]-[A-Za-z0-9-]{10,}|sk_live_[A-Za-z0-9]{20,}|sk-(proj-)?[A-Za-z0-9_-]{28,}|(postgres|postgresql|mysql|mongodb(\+srv)?|redis)://[^:/[:space:]]+:[^@[:space:]]{6,}@'
  hits=0
  if [[ -n "$cands" ]]; then
    while IFS= read -r f; do
      [[ -f "$f" ]] || continue
      m="$(grep -nE -e "$re" "$f" 2>/dev/null | grep -vE 'change-me|placeholder|example|user:password@' | cut -d: -f1 || true)"
      for ln in $m; do echo "✖ secret-scan: $f:$ln nghi lộ secret (không in nội dung)"; hits=$((hits+1)); done
    done <<<"$cands"
  fi
  if [[ $hits -gt 0 ]]; then failed+=("secret-scan"); else echo "✔ secret-scan (fallback)"; fi
fi

if [[ $strict -eq 1 && ${#skipped[@]} -gt 0 ]]; then
  for s in "${skipped[@]}"; do failed+=("$s (chưa cấu hình)"); done
fi
if [[ ${#failed[@]} -gt 0 ]]; then echo; echo "✖ VERIFY FAIL: ${failed[*]}"; exit 1; fi
echo; echo "✔ VERIFY OK"
