# scripts/spec-check.ps1 — Kiểm tra SPEC "máy kiểm được" + ngân sách ngữ cảnh. Exit 1 nếu có lỗi.
# Chỉ kiểm sâu feature có Status=LOCKED. Thư mục bắt đầu bằng "_" (template, ví dụ) bị bỏ qua.
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
[Environment]::CurrentDirectory = $root   # .NET dùng cwd riêng; đồng bộ để đường dẫn tương đối đúng
$script:errors = 0; $script:warns = 0
function Err($m)  { Write-Host "✖ $m" -ForegroundColor Red;    $script:errors++ }
function Warn($m) { Write-Host "⚠ $m" -ForegroundColor Yellow; $script:warns++ }
function Info($m) { Write-Host "ℹ $m" -ForegroundColor Gray }
function Read-Text($p) { [System.IO.File]::ReadAllText($p, [System.Text.Encoding]::UTF8) }

$testText = ''
if (Test-Path 'tests') {
    $files = Get-ChildItem 'tests' -Recurse -File | Where-Object { $_.Name -notin '.gitkeep', 'README.md' }
    $testText = ($files | ForEach-Object { Read-Text $_.FullName }) -join "`n"
}

if (Test-Path '.sdd/specs') {
    foreach ($d in (Get-ChildItem '.sdd/specs' -Directory | Where-Object { $_.Name -like 'feat-*' })) {
        $name = $d.Name; $slug = $name.Substring(5)
        $specPath = Join-Path $d.FullName 'SPEC.md'
        if (-not (Test-Path $specPath)) { Warn "${name}: thiếu SPEC.md"; continue }
        $t = Read-Text $specPath
        if ($t -notmatch '(?m)^\|\s*Status\s*\|\s*`?LOCKED`?\s*\|') { Info "${name}: SPEC chưa LOCKED — bỏ qua kiểm sâu"; continue }

        if ($t.Contains('{{')) { Err "${name}: SPEC còn placeholder {{...}}" }
        if ($t -match '(?m)^- \[ \] Q') { Err "${name}: còn câu hỏi mở chưa giải quyết" }

        $frs = [regex]::Matches($t, '(?m)^\|\s*(FR-\d+)\s*\|') | ForEach-Object { $_.Groups[1].Value }
        $acs = [regex]::Matches($t, '(?m)^\|\s*(AC-\d+)\s*\|') | ForEach-Object { $_.Groups[1].Value }
        if (-not $frs) { Err "${name}: không có FR nào" }
        if (-not $acs) { Err "${name}: không có AC nào" }
        $acRows = ([regex]::Matches($t, '(?m)^\|\s*AC-\d+\s*\|.*$') | ForEach-Object { $_.Value }) -join "`n"
        foreach ($fr in $frs) { if ($acRows -notmatch "(?<![0-9A-Za-z])$fr(?![0-9A-Za-z])") { Err "${name}: $fr chưa được AC nào kiểm chứng" } }

        $oos = [regex]::Match($t, '(?s)##\s*8\.\s*Out of Scope(.*?)(?=\r?\n---|\r?\n## |\z)')
        if (-not $oos.Success -or $oos.Groups[1].Value -notmatch '(?m)^- \S') { Err "${name}: Out of Scope trống" }

        $tier = 'Full'
        $m = [regex]::Match($t, '(?m)^\|\s*Tier\s*\|\s*`?(\w+)`?\s*\|')
        if ($m.Success) { $tier = $m.Groups[1].Value }
        if ($tier -eq 'Full' -and -not (Test-Path (Join-Path $d.FullName 'PLAN.md'))) { Err "${name}: tier Full nhưng thiếu PLAN.md" }

        $designPath = Join-Path $d.FullName 'DESIGN.md'
        if ($t -match '(?m)^\|\s*UI\s*\|\s*Có\s*\|' -and -not (Test-Path $designPath)) { Err "${name}: SPEC khai báo UI=Có nhưng thiếu DESIGN.md" }
        if (Test-Path $designPath) {
            $dt = Read-Text $designPath
            if ($dt.Contains('{{')) { Err "${name}: DESIGN.md còn placeholder {{...}}" }
            if ($dt -notmatch '(?m)^\|\s*Status\s*\|\s*`?LOCKED`?\s*\|') { Err "${name}: SPEC đã LOCKED nhưng DESIGN.md chưa LOCKED" }
        }

        $tasksPath = Join-Path $d.FullName 'TASKS.md'
        $allDone = $false
        if (Test-Path $tasksPath) {
            $rows = [regex]::Matches((Read-Text $tasksPath), '(?m)^\|\s*T-\d+.*$') | ForEach-Object { $_.Value }
            if ($rows -and -not ($rows | Where-Object { $_ -match '⬜|🔄|⛔' })) { $allDone = $true }
        } else { Warn "${name}: chưa có TASKS.md" }

        foreach ($ac in $acs) {
            if (-not $testText.Contains("$slug/$ac")) {
                if ($allDone) { Err "${name}: $ac chưa có test gắn thẻ '@spec $slug/$ac' (mọi task đã xong)" }
                else { Warn "${name}: $ac chưa có test gắn thẻ '@spec $slug/$ac'" }
            }
        }
        if ($allDone -and $tier -eq 'Full') {
            $rv = Join-Path $d.FullName 'REVIEW.md'
            if (-not (Test-Path $rv) -or (Read-Text $rv) -notmatch '(?m)^\|\s*\*{0,2}Kết luận\*{0,2}\s*\|\s*\*{0,2}`?APPROVED`?') { Err "${name}: tier Full đã xong task nhưng REVIEW.md chưa APPROVED" }
        }
    }
}

# Ngân sách ngữ cảnh & kích thước file src/ (S-04)
function Budget($p, $max) { if (Test-Path $p) { $n = (Get-Content $p).Count; if ($n -gt $max) { Warn "${p}: $n dòng (> $max) — rút gọn/tách bớt" } } }
Budget 'AGENTS.md' 150
Budget 'CLAUDE.md' 300
if (Test-Path '.sdd/skills') { Get-ChildItem '.sdd/skills' -Filter *.md | ForEach-Object { Budget $_.FullName 150 } }

$srcMax = 300
if (Test-Path '.sdd/project.conf') {
    $m = [regex]::Match((Read-Text '.sdd/project.conf'), '(?m)^SRC_MAX_LINES=(\d+)')
    if ($m.Success) { $srcMax = [int]$m.Groups[1].Value }
}
if ($srcMax -gt 0 -and (Test-Path 'src')) {
    $srcCode = Get-ChildItem 'src' -Recurse -File | Where-Object {
        $_.Name -notin '.gitkeep', 'README.md' -and $_.Name -notmatch '\.min\.|\.map$' -and $_.FullName -notmatch '[\\/]node_modules[\\/]'
    }
    $srcCode | ForEach-Object {
        $n = (Get-Content $_.FullName).Count
        if ($n -gt $srcMax) { $rel = $_.FullName.Substring($root.Length + 1); Warn "${rel}: $n dòng (> $srcMax) — tách module theo S-04" }
    }
    if ($srcCode.Count -gt 0 -and -not (Test-Path '.codegraph')) {
        Info "src/ đã có code nhưng chưa index CodeGraph — chạy 'codegraph init -y' (hoặc ./scripts/setup-tools.ps1)"
    }
}

$reqSpeckit = 'warn'
if (Test-Path '.sdd/project.conf') {
    $m = [regex]::Match((Read-Text '.sdd/project.conf'), '(?m)^REQUIRE_SPECKIT=(\w+)')
    if ($m.Success) { $reqSpeckit = $m.Groups[1].Value }
}
$hasFeatures = (Test-Path '.sdd/specs') -and ((Get-ChildItem '.sdd/specs' -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -like 'feat-*' }).Count -gt 0)
if ($reqSpeckit -ne 'off' -and ($hasFeatures -or $reqSpeckit -eq 'error') -and -not (Test-Path '.specify')) {
    if ($reqSpeckit -eq 'error') { Err "Chưa setup Spec Kit (.specify/) — chạy ./scripts/setup-tools.ps1 (specify init)" }
    else { Warn "Chưa setup Spec Kit (.specify/) — chạy ./scripts/setup-tools.ps1 trước khi viết Spec" }
}

Write-Host ("spec-check: {0} lỗi, {1} cảnh báo" -f $script:errors, $script:warns)
if ($script:errors -gt 0) { exit 1 } else { exit 0 }
