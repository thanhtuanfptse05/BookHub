# scripts/ui-check.ps1 — Kiểm UI tĩnh, trung lập stack: hardcode màu, inline style, <img> thiếu alt.
# Đọc UI_CHECK trong .sdd/project.conf: off | warn (mặc định) | error. Bỏ qua dòng có "ui-check:ignore".
# Bỏ qua: file token/theme, test, story, node_modules. Chỉ quét src/ (đổi UI_SRC trong conf nếu cần).
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
[Environment]::CurrentDirectory = $root

$conf = @{}
if (Test-Path '.sdd/project.conf') {
    foreach ($line in [System.IO.File]::ReadAllLines((Join-Path $root '.sdd/project.conf'), [System.Text.Encoding]::UTF8)) {
        if ($line -match '^\s*([A-Z0-9_]+)\s*=\s*(.*)$') { $conf[$Matches[1]] = $Matches[2].Trim() }
    }
}
$mode = if ($conf['UI_CHECK']) { $conf['UI_CHECK'] } else { 'warn' }
$srcDir = if ($conf['UI_SRC']) { $conf['UI_SRC'] } else { 'src' }
if ($mode -eq 'off' -or -not (Test-Path $srcDir)) { exit 0 }

$exts = '.css', '.scss', '.sass', '.less', '.html', '.htm', '.vue', '.svelte', '.jsx', '.tsx', '.astro'
$files = Get-ChildItem $srcDir -Recurse -File | Where-Object {
    $_.Extension -in $exts -and
    $_.FullName -notmatch '[\\/]node_modules[\\/]' -and
    $_.Name -notmatch '(?i)tokens|theme|\.test\.|\.spec\.|\.stories\.' -and
    $_.FullName -notmatch '(?i)[\\/](__tests__|tests?)[\\/]'
}

$findings = New-Object System.Collections.ArrayList
$rules = @(
    @{ id = 'U-01'; msg = 'màu hardcode (dùng token var(--color-*))'; re = '(?<![&\w/#-])#[0-9a-fA-F]{3,8}\b(?![\w-])|\b(?:rgb|rgba|hsl|hsla)\(' ; skip = 'href=|url\(#|xlink:' },
    @{ id = 'U-03'; msg = 'inline style (dùng class + token)'; re = '\bstyle\s*=\s*["{]' ; skip = '' },
    @{ id = 'U-06'; msg = '<img> thiếu alt'; re = '<img\b(?![^>]*\balt\s*=)' ; skip = '' }
)
foreach ($f in $files) {
    $n = 0
    foreach ($line in [System.IO.File]::ReadLines($f.FullName)) {
        $n++
        if ($line.Contains('ui-check:ignore')) { continue }
        foreach ($r in $rules) {
            if ($line -match $r.re -and (-not $r.skip -or $line -notmatch $r.skip)) {
                $rel = $f.FullName.Substring($root.Length + 1)
                [void]$findings.Add("${rel}:${n} [$($r.id)] $($r.msg)")
            }
        }
    }
}

foreach ($x in $findings) { Write-Host "⚠ $x" -ForegroundColor Yellow }
Write-Host ("ui-check: {0} phát hiện (mode={1})" -f $findings.Count, $mode)
if ($mode -eq 'error' -and $findings.Count -gt 0) { exit 1 } else { exit 0 }
