# scripts/new-feature.ps1 — Tạo thư mục feature từ .sdd/specs/_template
# Dùng: ./scripts/new-feature.ps1 user-login [-Tier Full|Standard]
#   Standard: bỏ PLAN.md (thiết kế ghi ở Phụ lục A của SPEC).
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidatePattern('^[a-z0-9]+(-[a-z0-9]+)*$')]
    [string]$Slug,
    [ValidateSet('Standard', 'Full')]
    [string]$Tier = 'Full',
    [switch]$Ui          # feature có giao diện: giữ DESIGN.md và đặt UI=Có trong SPEC
)
$root   = Split-Path -Parent $PSScriptRoot
$tpl    = Join-Path $root '.sdd/specs/_template'
$target = Join-Path $root ".sdd/specs/feat-$Slug"
$utf8   = New-Object System.Text.UTF8Encoding($false)

if (-not (Test-Path $tpl)) { Write-Error "Không thấy template: $tpl"; exit 1 }
if (Test-Path $target)     { Write-Error "Đã tồn tại: $target"; exit 1 }

Copy-Item -Recurse $tpl $target
if ($Tier -eq 'Standard') { Remove-Item (Join-Path $target 'PLAN.md') -Force }
if (-not $Ui) { Remove-Item (Join-Path $target 'DESIGN.md') -Force }
Get-ChildItem $target -Filter *.md | ForEach-Object {
    $t = [System.IO.File]::ReadAllText($_.FullName, $utf8)
    $t = $t.Replace('{{slug}}', $Slug).Replace('{{Tên feature}}', $Slug).Replace('{{Standard/Full}}', $Tier)
    if ($_.Name -eq 'SPEC.md') {
        $t = $t.Replace('| Tier | Full |', "| Tier | $Tier |")
        if ($Ui) { $t = $t.Replace('| UI | Không |', '| UI | Có |') }
    }
    [System.IO.File]::WriteAllText($_.FullName, $t, $utf8)
}

Write-Host "✅ Đã tạo $target (tier $Tier)"
Write-Host "Bước tiếp: điền CONTEXT.md (người) → dán .sdd/prompts/01-specify.md cho Agent."
