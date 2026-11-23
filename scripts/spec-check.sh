#!/usr/bin/env bash
# scripts/spec-check.sh — Kiểm tra SPEC "máy kiểm được" + ngân sách ngữ cảnh. Exit 1 nếu có lỗi.
# Chỉ kiểm sâu feature có Status=LOCKED. Thư mục bắt đầu bằng "_" (template, ví dụ) bị bỏ qua.
set -uo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$root"
errors=0; warns=0
err()  { echo "✖ $*"; errors=$((errors+1)); }
warn() { echo "⚠ $*"; warns=$((warns+1)); }
info() { echo "ℹ $*"; }

test_text=""
if [[ -d tests ]]; then
  test_text="$(find tests -type f ! -name .gitkeep ! -name README.md -print0 | xargs -0 cat 2>/dev/null || true)"
fi

for d in .sdd/specs/*/; do
  [[ -d "$d" ]] || continue
  d="${d%/}"; name="$(basename "$d")"
  [[ "$name" == _* ]] && continue
  if [[ ! "$name" =~ ^[0-9]{2}-feat-[a-z0-9]+(-[a-z0-9]+)*$ ]]; then
    err "$name: tên thư mục spec phải có tiền tố số 2 chữ số dạng '<NN>-feat-<slug>' (vd: 01-feat-foundation-jpa)"
    continue
  fi
  slug="${name#[0-9][0-9]-feat-}"
  [[ -f "$d/SPEC.md" ]] || { warn "$name: thiếu SPEC.md"; continue; }
  t="$(tr -d '\r' < "$d/SPEC.md")"
  if ! grep -qE '^\|[[:space:]]*Status[[:space:]]*\|[[:space:]]*`?LOCKED`?[[:space:]]*\|' <<<"$t"; then
    info "$name: SPEC chưa LOCKED — bỏ qua kiểm sâu"; continue
  fi

  grep -qF '{{' <<<"$t" && err "$name: SPEC còn placeholder {{...}}"
  grep -qE '^- \[ \] Q' <<<"$t" && err "$name: còn câu hỏi mở chưa giải quyết"

  frs="$(grep -oE '^\|[[:space:]]*FR-[0-9]+' <<<"$t" | grep -oE 'FR-[0-9]+' || true)"
  acs="$(grep -oE '^\|[[:space:]]*AC-[0-9]+' <<<"$t" | grep -oE 'AC-[0-9]+' || true)"
  [[ -n "$frs" ]] || err "$name: không có FR nào"
  [[ -n "$acs" ]] || err "$name: không có AC nào"
  acrows="$(grep -E '^\|[[:space:]]*AC-[0-9]+[[:space:]]*\|' <<<"$t" || true)"
  for fr in $frs; do
    grep -qE "(^|[^0-9A-Za-z])${fr}([^0-9A-Za-z]|$)" <<<"$acrows" || err "$name: $fr chưa được AC nào kiểm chứng"
  done

  awk '/^##[[:space:]]*8\.[[:space:]]*Out of Scope/{f=1;next} f&&(/^---/||/^## /){f=0} f' <<<"$t" | grep -qE '^- [^[:space:]]' \
    || err "$name: Out of Scope trống"

  tier="$(grep -E '^\|[[:space:]]*Tier[[:space:]]*\|' <<<"$t" | head -1 | awk -F'|' '{gsub(/[[:space:]`]/,"",$3); print $3}')"
  tier="${tier:-Full}"
  [[ "$tier" == "Full" && ! -f "$d/PLAN.md" ]] && err "$name: tier Full nhưng thiếu PLAN.md"

  ui_flag="$(grep -E '^\|[[:space:]]*UI[[:space:]]*\|' <<<"$t" | head -1 | awk -F'|' '{gsub(/[[:space:]]/,"",$3); print $3}')"
  if [[ "$ui_flag" == "Có" && ! -f "$d/DESIGN.md" ]]; then err "$name: SPEC khai báo UI=Có nhưng thiếu DESIGN.md"; fi
  if [[ -f "$d/DESIGN.md" ]]; then
    dt="$(tr -d '\r' < "$d/DESIGN.md")"
    grep -qF '{{' <<<"$dt" && err "$name: DESIGN.md còn placeholder {{...}}"
    grep -qE '^\|[[:space:]]*Status[[:space:]]*\|[[:space:]]*`?LOCKED`?[[:space:]]*\|' <<<"$dt" || err "$name: SPEC đã LOCKED nhưng DESIGN.md chưa LOCKED"
  fi

  all_done=0
  if [[ -f "$d/TASKS.md" ]]; then
    rows="$(tr -d '\r' < "$d/TASKS.md" | grep -E '^\|[[:space:]]*T-[0-9]+' || true)"
    if [[ -n "$rows" ]] && ! grep -qE '⬜|🔄|⛔' <<<"$rows"; then all_done=1; fi
  else
    warn "$name: chưa có TASKS.md"
  fi

  for ac in $acs; do
    if ! grep -qF "$slug/$ac" <<<"$test_text"; then
      if [[ $all_done -eq 1 ]]; then err "$name: $ac chưa có test gắn thẻ '@spec $slug/$ac' (mọi task đã xong)"
      else warn "$name: $ac chưa có test gắn thẻ '@spec $slug/$ac'"; fi
    fi
  done

  if [[ $all_done -eq 1 && "$tier" == "Full" ]]; then
    if [[ ! -f "$d/REVIEW.md" ]] || ! tr -d '\r' < "$d/REVIEW.md" | grep -qE '^\|[[:space:]]*\*{0,2}Kết luận\*{0,2}[[:space:]]*\|[[:space:]]*\*{0,2}`?APPROVED`?'; then
      err "$name: tier Full đã xong task nhưng REVIEW.md chưa APPROVED"
    fi
  fi
done

budget() { [[ -f "$1" ]] || return 0; n="$(wc -l < "$1" | tr -d ' ')"; [[ "$n" -le "$2" ]] || warn "$1: $n dòng (> $2) — rút gọn/tách bớt"; }
budget AGENTS.md 150
budget CLAUDE.md 300
for f in .sdd/skills/*.md; do [[ -f "$f" ]] && budget "$f" 150; done

src_max="$(grep -E '^SRC_MAX_LINES=' .sdd/project.conf 2>/dev/null | head -1 | cut -d= -f2- | tr -d '\r')"
src_max="${src_max:-300}"
if [[ "$src_max" =~ ^[0-9]+$ && "$src_max" -gt 0 && -d src ]]; then
  src_count=0
  while IFS= read -r -d '' f; do
    src_count=$((src_count+1))
    n="$(wc -l < "$f" | tr -d ' ')"
    [[ "$n" -le "$src_max" ]] || warn "${f#./}: $n dòng (> $src_max) — tách module theo S-04"
  done < <(find src -type f ! -name .gitkeep ! -name README.md ! -name '*.min.*' ! -name '*.map' -not -path '*/node_modules/*' -print0)
  if [[ $src_count -gt 0 && ! -d .codegraph ]]; then
    info "src/ đã có code nhưng chưa index CodeGraph — chạy 'codegraph init -y' (hoặc ./scripts/setup-tools.sh)"
  fi
fi

req_speckit="$(grep -E '^REQUIRE_SPECKIT=' .sdd/project.conf 2>/dev/null | head -1 | cut -d= -f2- | tr -d '\r')"
req_speckit="${req_speckit:-warn}"
has_features=0
for d in .sdd/specs/*/; do
  [[ -d "$d" ]] || continue
  bn="$(basename "${d%/}")"
  [[ "$bn" == _* ]] && continue
  has_features=1; break
done
if [[ "$req_speckit" != "off" && ( "$has_features" -eq 1 || "$req_speckit" == "error" ) && ! -d .specify ]]; then
  if [[ "$req_speckit" == "error" ]]; then err "Chưa setup Spec Kit (.specify/) — chạy ./scripts/setup-tools.sh (specify init)"
  else warn "Chưa setup Spec Kit (.specify/) — chạy ./scripts/setup-tools.sh trước khi viết Spec"; fi
fi

echo "spec-check: $errors lỗi, $warns cảnh báo"
[[ $errors -eq 0 ]]
