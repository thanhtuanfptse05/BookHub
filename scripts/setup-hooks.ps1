# scripts/setup-hooks.ps1 — Bật git hooks của dự án (chặn commit lên main, sai format commit, lint đỏ, secret)
if (-not (Test-Path '.git')) { Write-Error 'Chạy trong thư mục gốc repo (đã git init).'; exit 1 }
git config core.hooksPath .githooks
Write-Host '✔ core.hooksPath = .githooks'
