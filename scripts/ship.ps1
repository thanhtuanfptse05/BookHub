# scripts/ship.ps1 — "Tự động push an toàn": verify đầy đủ → commit → push NHÁNH FEATURE (không force) → mở PR + bật auto-merge.
# Dùng: ./scripts/ship.ps1 -Message "feat(auth): add refresh rotation"   (có thay đổi chưa commit)
#       ./scripts/ship.ps1                                                (chỉ push các commit đã có)
#       [-NoPR] không tạo PR · [-NoAutoMerge] tạo PR nhưng không bật auto-merge
# Agent chỉ được push bằng script này. KHÔNG push main/release/prod, KHÔNG force, KHÔNG --no-verify, KHÔNG deploy.
param([Parameter(Position = 0)][string]$Message, [switch]$NoPR, [switch]$NoAutoMerge)
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
[Environment]::CurrentDirectory = $root
function Fail($m) { Write-Host "✖ $m" -ForegroundColor Red; exit 1 }

git rev-parse --is-inside-work-tree 2>$null | Out-Null
if ($LASTEXITCODE -ne 0) { Fail 'Không phải git repo (chạy git init).' }
$branch = (git rev-parse --abbrev-ref HEAD).Trim()
$main = 'main'
if (Test-Path '.sdd/project.conf') {
    $m = [regex]::Match([System.IO.File]::ReadAllText((Join-Path $root '.sdd/project.conf')), '(?m)^MAIN_BRANCH=(\S+)')
    if ($m.Success) { $main = $m.Groups[1].Value }
}
if ($branch -in @($main, 'main', 'master', 'HEAD') -or $branch -match '^(release/|prod)') { Fail "Cấm ship từ '$branch'. Tạo nhánh: git switch -c feat/<slug>" }
if ($branch -notmatch '^(feat|fix|chore|docs|refactor|perf|test|build|ci)/[a-z0-9._/-]+$') { Fail "Tên nhánh '$branch' sai quy ước type/slug (vd feat/user-login)." }

$dirty = ((git status --porcelain) -join "`n").Trim()
if ($dirty -and -not $Message) { Fail 'Có thay đổi chưa commit: cần -Message "type(scope): mô tả".' }
if ($Message -and $Message -notmatch '^(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\([a-z0-9._/-]+\))?!?: .{1,100}$') { Fail 'Message sai Conventional Commits (vd feat(auth): add x).' }

Write-Host "▶ verify (đầy đủ) trước khi ship" -ForegroundColor Cyan
& "$PSScriptRoot/verify.ps1"
if ($LASTEXITCODE -ne 0) { Fail 'verify chưa xanh — không ship.' }

if ($dirty) {
    git add -A
    $bad = git diff --cached --name-only | Where-Object { (($_ -match '(^|/)\.env(\..*)?$') -and ($_ -notmatch '\.example$')) -or ($_ -match '\.(pem|key|p12|pfx)$') }
    if ($bad) { git reset -q; Fail ("Phát hiện file nhạy cảm sắp commit: " + ($bad -join ', ')) }
    git commit -m $Message
    if ($LASTEXITCODE -ne 0) { Fail 'commit thất bại (hook chặn?).' }
}

git push -u origin $branch
if ($LASTEXITCODE -ne 0) { Fail 'push thất bại (không dùng force; hãy pull/rebase rồi thử lại).' }
Write-Host "✔ Đã push nhánh $branch" -ForegroundColor Green

if ($NoPR) { exit 0 }
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) { Write-Host "ℹ Chưa cài GitHub CLI (gh) — mở PR thủ công." -ForegroundColor Gray; exit 0 }
gh pr view --json url -q .url 2>$null
if ($LASTEXITCODE -ne 0) {
    gh pr create --fill --base $main
    if ($LASTEXITCODE -ne 0) { Write-Host "⚠ Không tạo được PR tự động." -ForegroundColor Yellow; exit 0 }
}
if (-not $NoAutoMerge) {
    gh pr merge --auto --squash --delete-branch
    if ($LASTEXITCODE -ne 0) { Write-Host "⚠ Chưa bật được auto-merge (cần bật Allow auto-merge + branch protection: ./scripts/setup-github.ps1 -Apply)." -ForegroundColor Yellow }
    else { Write-Host "✔ Auto-merge đã bật: PR tự merge khi CI xanh." -ForegroundColor Green }
}
exit 0
