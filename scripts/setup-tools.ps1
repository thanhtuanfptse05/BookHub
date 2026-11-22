# scripts/setup-tools.ps1 — Cài & khởi tạo bộ công cụ AI cho dự án:
#   Spec Kit (specify) + CodeGraph + Graphify + 3 UI Skills (hallmark, design-taste-frontend, impeccable)
# Dùng: ./scripts/setup-tools.ps1 [-LocalSkills] [-Strict]
param(
    [switch]$LocalSkills,   # cài 3 UI skill vào .agents/skills/ của dự án thay vì global (~/.gemini/config/skills)
    [switch]$Strict         # thoát lỗi nếu thiếu CLI bắt buộc (specify / npx)
)
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
[Environment]::CurrentDirectory = $root
$err = 0

function Has($cmd) { return [bool](Get-Command $cmd -ErrorAction SilentlyContinue) }

Write-Host "=== 1. Spec Kit (specify) ===" -ForegroundColor Cyan
if (Has 'specify') {
    if (-not (Test-Path '.specify')) {
        specify init . --integration agy --force --ignore-agent-tools --script ps
        if ($LASTEXITCODE -ne 0) { Write-Host "✖ specify init lỗi" -ForegroundColor Red; $err++ }
    } else {
        Write-Host "✔ .specify/ đã tồn tại" -ForegroundColor Green
    }
    if (Test-Path '.sdd/project.conf') {
        $c = [IO.File]::ReadAllText((Join-Path $root '.sdd/project.conf'), [Text.Encoding]::UTF8)
        $c = $c -replace '(?m)^REQUIRE_SPECKIT=.*$', 'REQUIRE_SPECKIT=error'
        [IO.File]::WriteAllText((Join-Path $root '.sdd/project.conf'), $c, (New-Object System.Text.UTF8Encoding($false)))
        Write-Host "✔ Đã bật REQUIRE_SPECKIT=error trong .sdd/project.conf" -ForegroundColor Green
    }
} else {
    Write-Host "⚠ Chưa cài Spec Kit CLI (`specify`). Cài: uv tool install specify-cli --from git+https://github.com/github/spec-kit.git" -ForegroundColor Yellow
    if ($Strict) { $err++ }
}

Write-Host "`n=== 2. CodeGraph ===" -ForegroundColor Cyan
if (Has 'codegraph') {
    if (-not (Test-Path '.codegraph')) {
        codegraph init -y
        if ($LASTEXITCODE -ne 0) { Write-Host "⚠ codegraph init chưa chạy được (có thể chưa có source code)" -ForegroundColor Yellow }
    } else {
        Write-Host "✔ .codegraph/ đã tồn tại — chạy 'codegraph sync' để đồng bộ" -ForegroundColor Green
    }
} else {
    Write-Host "⚠ Chưa cài codegraph. Cài: npm i -g @colbymchenry/codegraph" -ForegroundColor Yellow
}

Write-Host "`n=== 3. Graphify ===" -ForegroundColor Cyan
if (Has 'graphify') {
    Write-Host "✔ Đã có graphify CLI — dùng '/graphify . --no-viz' khi cần dựng knowledge graph" -ForegroundColor Green
} else {
    Write-Host "⚠ Chưa cài graphify. Cài: uv tool install graphifyy && graphify install --platform antigravity" -ForegroundColor Yellow
}

Write-Host "`n=== 4. 3 UI Skills (hallmark · design-taste-frontend · impeccable) ===" -ForegroundColor Cyan
$skillPairs = @(
    @{ pkg = 'nutlope/hallmark';   skill = 'hallmark' },
    @{ pkg = 'leonxlnx/taste-skill'; skill = 'design-taste-frontend' },
    @{ pkg = 'pbakaus/impeccable'; skill = 'impeccable' }
)
$globalDir = Join-Path $env:USERPROFILE '.gemini\config\skills'
$localDir  = Join-Path $root '.agents\skills'
$missing = @()
foreach ($s in $skillPairs) {
    $hasG = Test-Path (Join-Path $globalDir "$($s.skill)\SKILL.md")
    $hasL = Test-Path (Join-Path $localDir  "$($s.skill)\SKILL.md")
    if ((-not $LocalSkills -and $hasG) -or $hasL) {
        Write-Host "✔ $($s.skill) đã cài sẵn" -ForegroundColor Green
    } else {
        $missing += $s
    }
}
if ($missing.Count -gt 0) {
    if (Has 'npx') {
        $scope = if ($LocalSkills) { @() } else { @('-g') }
        foreach ($s in $missing) {
            Write-Host "▶ Cài $($s.skill)..." -ForegroundColor Cyan
            & npx skills add $s.pkg --skill $s.skill @scope -a antigravity -y
            if ($LASTEXITCODE -ne 0) { Write-Host "✖ Lỗi cài $($s.skill)" -ForegroundColor Red; $err++ }
        }
    } else {
        Write-Host "⚠ Thiếu npx (Node.js) để cài UI skills" -ForegroundColor Yellow
        if ($Strict) { $err++ }
    }
}

Write-Host "`nXong setup-tools (lỗi: $err). Đọc .sdd/skills/ui-craft-routing.md & code-intelligence.md để dùng đúng rule."
if ($err -gt 0) { exit 1 } else { exit 0 }
