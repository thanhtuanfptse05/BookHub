#!/usr/bin/env bash
# scripts/setup-hooks.sh — Bật git hooks của dự án
set -euo pipefail
[[ -d .git ]] || { echo "Chạy trong thư mục gốc repo (đã git init)." >&2; exit 1; }
git config core.hooksPath .githooks
chmod +x .githooks/* scripts/*.sh 2>/dev/null || true
echo "✔ core.hooksPath = .githooks"
