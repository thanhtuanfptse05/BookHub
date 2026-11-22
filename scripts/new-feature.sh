#!/usr/bin/env bash
# scripts/new-feature.sh — Tạo thư mục feature từ .sdd/specs/_template
# Dùng: ./scripts/new-feature.sh user-login [full|standard]
#   standard: bỏ PLAN.md (thiết kế ghi ở Phụ lục A của SPEC).
set -euo pipefail

slug="${1:-}"; tier="${2:-full}"; ui="${3:-}"   # arg 3 = "ui" nếu feature có giao diện
[[ "$slug" =~ ^[a-z0-9]+(-[a-z0-9]+)*$ ]] || { echo "Slug phải là kebab-case, vd: user-login" >&2; exit 1; }
[[ "$tier" == "full" || "$tier" == "standard" ]] || { echo "Tier phải là full|standard" >&2; exit 1; }
tier_name="$([[ "$tier" == "full" ]] && echo Full || echo Standard)"

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
tpl="$root/.sdd/specs/_template"
target="$root/.sdd/specs/feat-$slug"

[[ -d "$tpl" ]] || { echo "Không thấy template: $tpl" >&2; exit 1; }
[[ ! -e "$target" ]] || { echo "Đã tồn tại: $target" >&2; exit 1; }

cp -r "$tpl" "$target"
[[ "$tier" == "standard" ]] && rm -f "$target/PLAN.md"
[[ "$ui" == "ui" ]] || rm -f "$target/DESIGN.md"
for f in "$target"/*.md; do
  sed -i.bak -e "s/{{slug}}/$slug/g" -e "s/{{Tên feature}}/$slug/g" -e "s|{{Standard/Full}}|$tier_name|g" "$f"
  if [[ "$(basename "$f")" == "SPEC.md" ]]; then
    sed -i.bak "s/| Tier | Full |/| Tier | $tier_name |/" "$f"
    [[ "$ui" == "ui" ]] && sed -i.bak "s/| UI | Không |/| UI | Có |/" "$f"
  fi
  rm -f "$f.bak"
done

echo "✅ Đã tạo $target (tier $tier_name)"
echo "Bước tiếp: điền CONTEXT.md (người) → dán .sdd/prompts/01-specify.md cho Agent."
