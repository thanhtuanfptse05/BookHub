#!/usr/bin/env bash
# scripts/ui-check.sh — Kiểm UI tĩnh, trung lập stack: hardcode màu, inline style, <img> thiếu alt.
# Đọc UI_CHECK trong .sdd/project.conf: off | warn (mặc định) | error. Bỏ qua dòng có "ui-check:ignore".
# Bỏ qua: file token/theme, test, story, node_modules. Chỉ quét src/ (đổi UI_SRC trong conf nếu cần).
set -uo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$root"
conf() { grep -E "^$1=" .sdd/project.conf 2>/dev/null | head -1 | cut -d= -f2- | tr -d '\r'; }
mode="$(conf UI_CHECK)"; mode="${mode:-warn}"
src="$(conf UI_SRC)"; src="${src:-src}"
[[ "$mode" == "off" || ! -d "$src" ]] && exit 0

files="$(find "$src" -type f \( -name '*.css' -o -name '*.scss' -o -name '*.sass' -o -name '*.less' -o -name '*.html' -o -name '*.htm' \
  -o -name '*.vue' -o -name '*.svelte' -o -name '*.jsx' -o -name '*.tsx' -o -name '*.astro' \) \
  -not -path '*/node_modules/*' -not -path '*/__tests__/*' -not -path '*/tests/*' -not -path '*/test/*' \
  ! -iname '*tokens*' ! -iname '*theme*' ! -name '*.test.*' ! -name '*.spec.*' ! -name '*.stories.*')"

scan() { # $1=id $2=msg $3=regex $4=skip-regex(optional) — in kết quả ra stdout
  echo "$files" | tr '\n' '\0' | xargs -0 grep -nE "$3" 2>/dev/null \
    | grep -v 'ui-check:ignore' | { if [[ -n "${4:-}" ]]; then grep -vE "$4" || true; else cat; fi; } \
    | sed "s|\$| [$1] $2|"
}

out=""
if [[ -n "$files" ]]; then
  out="$(
    scan U-01 'màu hardcode (dùng token var(--color-*))' '(^|[^&[:alnum:]_/#-])#[0-9a-fA-F]{3,8}([^[:alnum:]_-]|$)|(rgb|rgba|hsl|hsla)\(' 'href=|url\(#|xlink:'
    scan U-03 'inline style (dùng class + token)' 'style[[:space:]]*=[[:space:]]*["{]'
    scan U-06 '<img> thiếu alt' '<img[[:space:]]' 'alt[[:space:]]*='
  )"
fi
count=0
if [[ -n "$out" ]]; then
  while IFS= read -r l; do echo "⚠ $l"; count=$((count+1)); done <<<"$out"
fi

echo "ui-check: $count phát hiện (mode=$mode)"
[[ "$mode" == "error" && $count -gt 0 ]] && exit 1
exit 0
