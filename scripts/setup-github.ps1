# scripts/setup-github.ps1 — Cấu hình GitHub cho CI/CD tự động (mặc định DRY-RUN: chỉ in lệnh). Cần GitHub CLI `gh` đã đăng nhập.
# Dùng: ./scripts/setup-github.ps1            (xem trước)
#       ./scripts/setup-github.ps1 -Apply     (áp dụng thật)
#       [-Reviews 0|1] số approval bắt buộc (mặc định 0 = solo; auto-merge vẫn cần PR + CI xanh)
param([switch]$Apply, [int]$Reviews = 0)
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
[Environment]::CurrentDirectory = $root
$main = 'main'
if (Test-Path '.sdd/project.conf') {
    $m = [regex]::Match([System.IO.File]::ReadAllText((Join-Path $root '.sdd/project.conf')), '(?m)^MAIN_BRANCH=(\S+)')
    if ($m.Success) { $main = $m.Groups[1].Value }
}
if ($Apply -and -not (Get-Command gh -ErrorAction SilentlyContinue)) { Write-Host '✖ Cần cài GitHub CLI: https://cli.github.com' -ForegroundColor Red; exit 1 }

$protection = @{
    required_status_checks = @{ strict = $true; contexts = @('verify', 'secrets') }
    enforce_admins = $false
    required_pull_request_reviews = @{ required_approving_review_count = $Reviews; dismiss_stale_reviews = $true }
    restrictions = $null
    required_linear_history = $true
    allow_force_pushes = $false
    allow_deletions = $false
    required_conversation_resolution = $true
} | ConvertTo-Json -Depth 6

Write-Host "=== Cấu hình sẽ áp dụng cho nhánh '$main' ===" -ForegroundColor Cyan
Write-Host "1) gh repo edit --enable-auto-merge --delete-branch-on-merge --enable-squash-merge --enable-merge-commit=false --enable-rebase-merge=false"
Write-Host "2) Branch protection (PUT repos/{owner}/{repo}/branches/$main/protection):`n$protection"
Write-Host "3) Tạo environment 'production' (PUT repos/{owner}/{repo}/environments/production) — thêm Required reviewers thủ công trên GitHub nếu cần duyệt deploy"
if (-not $Apply) { Write-Host "`nℹ Dry-run. Thêm -Apply để áp dụng." -ForegroundColor Gray; exit 0 }

gh repo edit --enable-auto-merge --delete-branch-on-merge --enable-squash-merge --enable-merge-commit=false --enable-rebase-merge=false
if ($LASTEXITCODE -ne 0) { Write-Host '⚠ gh repo edit lỗi (quyền admin?)' -ForegroundColor Yellow }
$protection | gh api -X PUT "repos/{owner}/{repo}/branches/$main/protection" --input -
if ($LASTEXITCODE -ne 0) { Write-Host '⚠ Chưa đặt được branch protection (repo private cần GitHub Pro/Team; hoặc nhánh chưa tồn tại — push main trước).' -ForegroundColor Yellow }
gh api -X PUT "repos/{owner}/{repo}/environments/production" | Out-Null
if ($LASTEXITCODE -ne 0) { Write-Host '⚠ Chưa tạo được environment production.' -ForegroundColor Yellow }
Write-Host '✔ Xong. Kiểm tra lại trong Settings → Branches / Environments.' -ForegroundColor Green
