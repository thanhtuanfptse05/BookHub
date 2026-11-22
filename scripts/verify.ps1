# scripts/verify.ps1 — Cổng kiểm tra duy nhất: spec-check + lint + ui-check + test + build + audit + unused + secret-scan.
# Dùng: ./scripts/verify.ps1            (đủ)
#       ./scripts/verify.ps1 -Fast      (spec-check + lint + ui-check + secret-scan; cho pre-commit)
#       ./scripts/verify.ps1 -Strict    (lệnh cốt lõi chưa cấu hình = FAIL; cho CI)
param([switch]$Fast, [switch]$Strict)
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
[Environment]::CurrentDirectory = $root   # .NET dùng cwd riêng; đồng bộ để đường dẫn tương đối đúng

$conf = @{}
$confPath = '.sdd/project.conf'
if (Test-Path $confPath) {
    foreach ($line in [System.IO.File]::ReadAllLines($confPath, [System.Text.Encoding]::UTF8)) {
        if ($line -match '^\s*([A-Z0-9_]+)\s*=\s*(.*)$') { $conf[$Matches[1]] = $Matches[2].Trim() }
    }
}
$failed = New-Object System.Collections.ArrayList
$skipped = New-Object System.Collections.ArrayList

function Step($name, $cmd) {
    if ([string]::IsNullOrWhiteSpace($cmd) -or $cmd.Contains('{{')) {
        [void]$skipped.Add($name)
        Write-Host "⚠ ${name}: chưa cấu hình trong .sdd/project.conf — bỏ qua" -ForegroundColor Yellow
        return
    }
    Write-Host "▶ ${name}: $cmd" -ForegroundColor Cyan
    cmd /c $cmd
    if ($LASTEXITCODE -ne 0) { [void]$failed.Add($name); Write-Host "✖ $name FAIL" -ForegroundColor Red }
    else { Write-Host "✔ $name" -ForegroundColor Green }
}
function OptStep($name, $cmd) { if (-not [string]::IsNullOrWhiteSpace($cmd)) { Step $name $cmd } }

Write-Host "▶ spec-check" -ForegroundColor Cyan
& "$PSScriptRoot/spec-check.ps1"
if ($LASTEXITCODE -ne 0) { [void]$failed.Add('spec-check') }

Step 'lint' $conf['LINT_CMD']

Write-Host "▶ ui-check" -ForegroundColor Cyan
& "$PSScriptRoot/ui-check.ps1"
if ($LASTEXITCODE -ne 0) { [void]$failed.Add('ui-check') }

if (-not $Fast) {
    Step 'test' $conf['TEST_CMD']
    Step 'build' $conf['BUILD_CMD']
    OptStep 'audit' $conf['AUDIT_CMD']     # quét CVE dependency (pnpm audit / pip-audit...)
    OptStep 'unused' $conf['UNUSED_CMD']   # quét dead code / package thừa (knip / vulture...)
    Write-Host "▶ seo-check" -ForegroundColor Cyan   # SEO/AEO/GEO trên HTML đã build (bỏ qua nếu SEO_BUILD_DIR trống)
    & "$PSScriptRoot/seo-check.ps1"
    if ($LASTEXITCODE -ne 0) { [void]$failed.Add('seo-check') }
    OptStep 'a11y' $conf['A11Y_CMD']       # tùy chọn: chỉ chạy khi dự án có UI và đã cấu hình
    OptStep 'visual' $conf['VISUAL_CMD']   # tùy chọn: ảnh chụp/so sánh trực quan
}

if (Get-Command gitleaks -ErrorAction SilentlyContinue) {
    Step 'secret-scan' 'gitleaks detect --no-banner --redact'
} else {
    Write-Host "▶ secret-scan (regex dự phòng — chưa cài gitleaks)" -ForegroundColor Cyan
    $skipExts = '.png','.jpg','.jpeg','.gif','.webp','.ico','.pdf','.zip','.tar','.gz','.7z','.exe','.dll','.so','.dylib','.wasm','.woff','.woff2','.ttf','.map','.lock','.sum'
    $candidates = if ((Test-Path '.git') -and (Get-Command git -ErrorAction SilentlyContinue)) {
        git ls-files --cached --others --exclude-standard | Where-Object { Test-Path $_ -PathType Leaf } | ForEach-Object { Get-Item $_ }
    } else {
        Get-ChildItem -Recurse -File -Force | Where-Object { $_.FullName -notmatch '[\\/](\.git|node_modules|vendor|\.venv|venv|dist|build|out|\.next|coverage)[\\/]' -and $_.Name -notmatch '^\.env($|\.)' }
    }
    $scanFiles = $candidates | Where-Object {
        $rel = $_.FullName.Substring($root.Length + 1).Replace('\', '/')
        $_.Extension -notin $skipExts -and
        $rel -notmatch '^(?:\.sdd/|scripts/verify\.(?:ps1|sh)$)' -and
        $_.Name -notmatch '\.example$|package-lock\.json$'
    }
    $secretRe = '-----BEGIN (?:RSA |EC |DSA |OPENSSH |PGP )?PRIVATE KEY-----|AKIA[0-9A-Z]{16}|gh[pousr]_[A-Za-z0-9_]{36,}|xox[baprs]-[A-Za-z0-9-]{10,}|sk_live_[A-Za-z0-9]{20,}|sk-(?:proj-)?[A-Za-z0-9_-]{28,}|(?:postgres|postgresql|mysql|mongodb(?:\+srv)?|redis)://[^:\s/]+:[^@\s]{6,}@'
    $hits = 0
    foreach ($f in $scanFiles) {
        $ln = 0
        foreach ($line in [System.IO.File]::ReadLines($f.FullName)) {
            $ln++
            if ($line -match $secretRe -and $line -notmatch 'change-me|placeholder|example|user:password@') {
                $rel = $f.FullName.Substring($root.Length + 1)
                Write-Host "✖ secret-scan: ${rel}:${ln} nghi lộ secret (không in nội dung)" -ForegroundColor Red
                $hits++
            }
        }
    }
    if ($hits -gt 0) { [void]$failed.Add('secret-scan') } else { Write-Host "✔ secret-scan (fallback)" -ForegroundColor Green }
}

if ($Strict -and $skipped.Count -gt 0) { foreach ($s in $skipped) { [void]$failed.Add("$s (chưa cấu hình)") } }
if ($failed.Count -gt 0) { Write-Host ("`n✖ VERIFY FAIL: " + ($failed -join ', ')) -ForegroundColor Red; exit 1 }
Write-Host "`n✔ VERIFY OK" -ForegroundColor Green
exit 0
