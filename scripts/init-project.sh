#!/usr/bin/env bash
# scripts/init-project.sh — Khởi tạo dự án từ template: điền placeholder + .sdd/project.conf theo preset.
# Dùng: ./scripts/init-project.sh "Acme Shop" "Nền tảng B2B" [preset] [profile]
#   preset : generic | web-ts | python-api | frontend-spa (mặc định generic)
#   profile: full | lite (lite bỏ .sdd/rfcs và .sdd/shared_context.md)
set -euo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$root"
name="${1:-}"; oneliner="${2:-}"; preset="${3:-generic}"; profile="${4:-full}"
[[ -n "$name" ]]     || read -rp "Tên dự án: " name
[[ -n "$oneliner" ]] || read -rp "Mô tả 1 dòng: " oneliner
[[ -f ".sdd/presets/$preset.conf" ]] || { echo "Không có preset '$preset'" >&2; exit 1; }
[[ "$profile" == "full" || "$profile" == "lite" ]] || { echo "profile phải là full|lite" >&2; exit 1; }

getv() { grep -E "^$2=" "$1" 2>/dev/null | head -1 | cut -d= -f2- | tr -d '\r' || true; }
esc()  { printf '%s' "$1" | sed -e 's/[\/&|]/\\&/g'; }

keys="$(grep -oE '^[A-Z0-9_]+=' .sdd/project.conf | tr -d '=')"
tmp="$(mktemp)"
{ echo "# Cấu hình dự án — nguồn duy nhất cho lệnh & thông số. Sửa tay khi stack đổi."
  for k in $keys; do
    v="$(getv .sdd/project.conf "$k")"
    pv="$(getv ".sdd/presets/$preset.conf" "$k")"; [[ -n "$pv" ]] && v="$pv"
    [[ "$k" == "PROJECT_NAME" ]] && v="$name"
    [[ "$k" == "PROJECT_ONE_LINER" ]] && v="$oneliner"
    echo "$k=$v"
  done; } > "$tmp"
mv "$tmp" .sdd/project.conf

today="$(date +%Y-%m-%d)"
# Gom mọi phép thay thế vào MỘT script sed, áp dụng 1 lần/file (nhanh, nhất là trên Windows/Git Bash)
sedfile="$(mktemp)"
while IFS='=' read -r k v; do
  [[ "$k" =~ ^[A-Z0-9_]+$ && -n "$v" ]] || continue
  printf 's|{{%s}}|%s|g\n' "$k" "$(esc "$v")" >> "$sedfile"
done < <(grep -E '^[A-Z0-9_]+=' .sdd/project.conf | tr -d '\r')
datefile="$(mktemp)"; { cat "$sedfile"; printf 's|{{YYYY-MM-DD}}|%s|g\n' "$today"; } > "$datefile"

while IFS= read -r -d '' f; do
  case "$(basename "$f")" in CLAUDE.md|constitution.md) sed -i.bak -f "$datefile" "$f";; *) sed -i.bak -f "$sedfile" "$f";; esac
  rm -f "$f.bak"
done < <(find . -type f \( -name '*.md' -o -name '*.mdc' -o -name '*.yml' -o -name '*.yaml' \) \
          -not -path './.git/*' -not -path './.sdd/presets/*' -not -path './scripts/*' -print0)
rm -f "$sedfile" "$datefile"

if [[ "$profile" == "lite" ]]; then rm -rf .sdd/rfcs .sdd/shared_context.md; fi
if [[ -d .git ]]; then git config core.hooksPath .githooks; chmod +x .githooks/* scripts/*.sh 2>/dev/null || true; echo "✔ Đã bật git hooks (.githooks)"
else echo "ℹ Chưa có .git — chạy 'git init' rồi ./scripts/setup-hooks.sh"; fi

echo "✔ Init xong (preset=$preset, profile=$profile). Placeholder còn cần tự điền:"
for f in CLAUDE.md .sdd/constraints/global.md .sdd/constraints/business.md; do
  [[ -f "$f" ]] && echo "   $f : $(grep -o '{{[^}]*}}' "$f" | wc -l | tr -d ' ')"
done
echo "Bước tiếp: ./scripts/setup-tools.sh → điền 3 file trên → ./scripts/verify.sh → ./scripts/new-feature.sh <slug>"
