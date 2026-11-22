# scripts/init-project.ps1 — Khởi tạo dự án từ template: điền placeholder + .sdd/project.conf theo preset.
# Dùng: ./scripts/init-project.ps1 -Name "Acme Shop" -OneLiner "Nền tảng B2B" -Preset web-ts -Profile full
#   Preset : generic | web-ts | python-api | frontend-spa   (xem .sdd/presets/)
#   Profile: full | lite (lite bỏ .sdd/rfcs và .sdd/shared_context.md — cho solo/prototype)
param(
    [string]$Name,
    [string]$OneLiner,
    [string]$Preset = 'generic',
    [ValidateSet('full', 'lite')][string]$Profile = 'full'
)
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
[Environment]::CurrentDirectory = $root   # .NET dùng cwd riêng; đồng bộ để đường dẫn tương đối đúng
$utf8 = New-Object System.Text.UTF8Encoding($false)

function Read-Conf($path) {
    $h = [ordered]@{}
    foreach ($line in [System.IO.File]::ReadAllLines($path, $utf8)) {
        if ($line -match '^\s*([A-Z0-9_]+)\s*=\s*(.*)$') { $h[$Matches[1]] = $Matches[2].Trim() }
    }
    return $h
}

$presetPath = ".sdd/presets/$Preset.conf"
if (-not (Test-Path $presetPath)) { Write-Error "Không có preset '$Preset' ($presetPath)"; exit 1 }
if (-not $Name)     { $Name = Read-Host 'Tên dự án' }
if (-not $OneLiner) { $OneLiner = Read-Host 'Mô tả 1 dòng' }

$conf = Read-Conf '.sdd/project.conf'
$presetConf = Read-Conf $presetPath
foreach ($k in $presetConf.Keys) { $conf[$k] = $presetConf[$k] }
if ($Name)     { $conf['PROJECT_NAME'] = $Name }
if ($OneLiner) { $conf['PROJECT_ONE_LINER'] = $OneLiner }

# 1) Ghi .sdd/project.conf (nguồn duy nhất cho verify)
$header = "# Cấu hình dự án — nguồn duy nhất cho lệnh & thông số. Sửa tay khi stack đổi.`n"
$body = ($conf.Keys | ForEach-Object { "$_=$($conf[$_])" }) -join "`n"
[System.IO.File]::WriteAllText((Join-Path $root '.sdd/project.conf'), $header + $body + "`n", $utf8)

# 2) Thay {{KEY}} trong tài liệu
$today = (Get-Date).ToString('yyyy-MM-dd')
$files = Get-ChildItem -Recurse -File -Force | Where-Object {
    $_.Extension -in '.md', '.mdc', '.yml', '.yaml' -and
    $_.FullName -notmatch '[\\/]\.git[\\/]' -and
    $_.FullName -notmatch '[\\/]\.sdd[\\/]presets[\\/]' -and
    $_.FullName -notmatch '[\\/]scripts[\\/]'
}
foreach ($f in $files) {
    $text = [System.IO.File]::ReadAllText($f.FullName, $utf8)
    $orig = $text
    foreach ($k in $conf.Keys) {
        if ($conf[$k]) { $text = $text.Replace('{{' + $k + '}}', $conf[$k]) }
    }
    if ($f.Name -in 'CLAUDE.md', 'constitution.md') { $text = $text.Replace('{{YYYY-MM-DD}}', $today) }
    if ($text -ne $orig) { [System.IO.File]::WriteAllText($f.FullName, $text, $utf8) }
}

# 3) Profile lite
if ($Profile -eq 'lite') {
    Remove-Item -Recurse -Force '.sdd/rfcs' -ErrorAction SilentlyContinue
    Remove-Item -Force '.sdd/shared_context.md' -ErrorAction SilentlyContinue
}

# 4) Git hooks
if (Test-Path '.git') { git config core.hooksPath .githooks; Write-Host '✔ Đã bật git hooks (.githooks)' }
else { Write-Host 'ℹ Chưa có .git — chạy `git init` rồi ./scripts/setup-hooks.ps1' }

# 5) Báo placeholder còn lại ở file nền tảng
$left = @('CLAUDE.md', '.sdd/constraints/global.md', '.sdd/constraints/business.md') |
    Where-Object { Test-Path $_ } |
    ForEach-Object { $c = ([regex]::Matches([System.IO.File]::ReadAllText((Join-Path $root $_), $utf8), '\{\{[^}]+\}\}')).Count; "$_ : $c" }
Write-Host "✔ Init xong (preset=$Preset, profile=$Profile). Placeholder còn cần tự điền:"
$left | ForEach-Object { Write-Host "   $_" }
Write-Host 'Bước tiếp: ./scripts/setup-tools.ps1 → điền 3 file trên → ./scripts/verify.ps1 → ./scripts/new-feature.ps1 <slug>'
