#!/usr/bin/env bash
# scripts/setup-tools.sh — Cài & khởi tạo bộ công cụ AI cho dự án:
#   Spec Kit (specify) + CodeGraph + Graphify + 3 UI Skills (hallmark, design-taste-frontend, impeccable)
# Dùng: ./scripts/setup-tools.sh [--local-skills] [--strict]
set -uo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$root"
local_skills=0; strict=0; err=0
for a in "$@"; do case "$a" in --local-skills) local_skills=1;; --strict) strict=1;; esac; done
has() { command -v "$1" >/dev/null 2>&1; }

echo "=== 1. Spec Kit (specify) ==="
if has specify; then
  if [[ ! -d .specify ]]; then
    specify init . --integration agy --force --ignore-agent-tools --script sh || { echo "✖ specify init lỗi"; err=$((err+1)); }
  else
    echo "✔ .specify/ đã tồn tại"
  fi
  if [[ -f .sdd/project.conf ]]; then
    sed -i.bak 's/^REQUIRE_SPECKIT=.*/REQUIRE_SPECKIT=error/' .sdd/project.conf && rm -f .sdd/project.conf.bak
    echo "✔ Đã bật REQUIRE_SPECKIT=error trong .sdd/project.conf"
  fi
else
  echo "⚠ Chưa cài Spec Kit CLI (specify). Cài: uv tool install specify-cli --from git+https://github.com/github/spec-kit.git"
  [[ $strict -eq 1 ]] && err=$((err+1))
fi

echo; echo "=== 2. CodeGraph ==="
if has codegraph; then
  if [[ ! -d .codegraph ]]; then
    codegraph init -y || echo "⚠ codegraph init chưa chạy được (có thể chưa có source code)"
  else
    echo "✔ .codegraph/ đã tồn tại — chạy 'codegraph sync' để đồng bộ"
  fi
else
  echo "⚠ Chưa cài codegraph. Cài: npm i -g @colbymchenry/codegraph"
fi

echo; echo "=== 3. Graphify ==="
if has graphify; then
  echo "✔ Đã có graphify CLI — dùng '/graphify . --no-viz' khi cần dựng knowledge graph"
else
  echo "⚠ Chưa cài graphify. Cài: uv tool install graphifyy && graphify install --platform antigravity"
fi

echo; echo "=== 4. 3 UI Skills (hallmark · design-taste-frontend · impeccable) ==="
global_dir="$HOME/.gemini/config/skills"
local_dir="$root/.agents/skills"
scope=(-g); [[ $local_skills -eq 1 ]] && scope=()

install_skill() {
  local pkg="$1" skill="$2"
  if { [[ $local_skills -eq 0 && -f "$global_dir/$skill/SKILL.md" ]]; } || [[ -f "$local_dir/$skill/SKILL.md" ]]; then
    echo "✔ $skill đã cài sẵn"
  elif has npx; then
    echo "▶ Cài $skill..."
    npx skills add "$pkg" --skill "$skill" "${scope[@]}" -a antigravity -y || { echo "✖ Lỗi cài $skill"; err=$((err+1)); }
  else
    echo "⚠ Thiếu npx (Node.js) để cài $skill"
    [[ $strict -eq 1 ]] && err=$((err+1))
  fi
}
install_skill nutlope/hallmark hallmark
install_skill leonxlnx/taste-skill design-taste-frontend
install_skill pbakaus/impeccable impeccable

echo; echo "Xong setup-tools (lỗi: $err). Đọc .sdd/skills/ui-craft-routing.md & code-intelligence.md để dùng đúng rule."
[[ $err -eq 0 ]]
