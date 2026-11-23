#!/usr/bin/env bash
# scripts/new-feature.sh — Tạo thư mục feature dạng <NN>-feat-<slug> từ .sdd/specs/_template
# Dùng: ./scripts/new-feature.sh user-login [full|standard] [ui|-] [09]
set -euo pipefail

slug="${1:-}"; tier="${2:-full}"; ui="${3:-}"; num="${4:-}"
[[ "$slug" =~ ^[a-z0-9]+(-[a-z0-9]+)*$ ]] || { echo "Slug phải là kebab-case, vd: user-login" >&2; exit 1; }
[[ "$tier" == "full" || "$tier" == "standard" ]] || { echo "Tier phải là full|standard" >&2; exit 1; }
tier_name="$([[ "$tier" == "full" ]] && echo Full || echo Standard)"

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
specs_dir="$root/.sdd/specs"
tpl="$specs_dir/_template"
[[ -d "$tpl" ]] || { echo "Không thấy template: $tpl" >&2; exit 1; }

if [[ -z "$num" ]]; then
  max=0
  for scan in "$specs_dir" "$specs_dir/_archive"; do
    [[ -d "$scan" ]] || continue
    for d in "$scan"/*/; do
      [[ -d "$d" ]] || continue
      bn="$(basename "$d")"
      if [[ "$bn" =~ ^([0-9]{2})-feat- ]]; then
        n=$((10#${BASH_REMATCH[1]}))
        (( n > max )) && max=$n
      fi
    done
  done
  num="$(printf '%02d' $((max + 1)))"
else
  [[ "$num" =~ ^[0-9]{2}$ ]] || { echo "Số thứ tự phải là 2 chữ số, vd: 01" >&2; exit 1; }
fi

target="$specs_dir/${num}-feat-${slug}"
[[ ! -e "$target" ]] || { echo "Đã tồn tại: $target" >&2; exit 1; }

cp -r "$tpl" "$target"
[[ "$tier" == "standard" ]] && rm -f "$target/PLAN.md"
[[ "$ui" == "ui" ]] || rm -f "$target/DESIGN.md"
for f in "$target"/*.md; do
  sed -i.bak -e "s/{{num}}/$num/g" -e "s/{{slug}}/$slug/g" -e "s/{{Tên feature}}/$slug/g" -e "s|{{Standard/Full}}|$tier_name|g" "$f"
  if [[ "$(basename "$f")" == "SPEC.md" ]]; then
    sed -i.bak "s/| Tier | Full |/| Tier | $tier_name |/" "$f"
    [[ "$ui" == "ui" ]] && sed -i.bak "s/| UI | Không |/| UI | Có |/" "$f"
  fi
  rm -f "$f.bak"
done

echo "✅ Đã tạo $target (tier $tier_name)"
echo "Bước tiếp: điền CONTEXT.md (người) → dán .sdd/prompts/01-specify.md cho Agent."
