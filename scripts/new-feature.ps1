# scripts/new-feature.ps1 — Tạo thư mục feature dạng <NN>-feat-<slug> từ .sdd/specs/_template
# Dùng: ./scripts/new-feature.ps1 user-login [-Tier Full|Standard] [-Ui] [-Number 09]
#   Mặc định tự động tính số thứ tự 2 chữ số tiếp theo (01, 02, ...) dựa trên .sdd/specs/ và _archive/.
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidatePattern('^[a-z0-9]+(-[a-z0-9]+)*$')]
    [string]$Slug,
    [ValidateSet('Standard', 'Full')]
    [string]$Tier = 'Full',
    [switch]$Ui,         # feature có giao diện: giữ DESIGN.md và đặt UI=Có trong SPEC
    [ValidatePattern('^\d{2}$')]
    [string]$Number      # tùy chọn: chỉ định số 2 chữ số (vd: 01, 09)
)
$root     = Split-Path -Parent $PSScriptRoot
$specsDir = Join-Path $root '.sdd/specs'
$tpl      = Join-Path $specsDir '_template'
$utf8     = New-Object System.Text.UTF8Encoding($false)

if (-not (Test-Path $tpl)) { Write-Error "Không thấy template: $tpl"; exit 1 }

if (-not $Number) {
    $max = 0
    foreach ($scanDir in @($specsDir, (Join-Path $specsDir '_archive'))) {
        if (Test-Path $scanDir) {
            Get-ChildItem $scanDir -Directory | ForEach-Object {
                if ($_.Name -match '^(\d{2})-feat-') {
                    $n = [int]$Matches[1]
                    if ($n -gt $max) { $max = $n }
                }
            }
        }
    }
    $Number = '{0:D2}' -f ($max + 1)
}

$featureId = "$Number-feat-$Slug"
$target    = Join-Path $specsDir $featureId
if (Test-Path $target) { Write-Error "Đã tồn tại: $target"; exit 1 }

Copy-Item -Recurse $tpl $target
if ($Tier -eq 'Standard') { Remove-Item (Join-Path $target 'PLAN.md') -Force }
if (-not $Ui) { Remove-Item (Join-Path $target 'DESIGN.md') -Force }
Get-ChildItem $target -Filter *.md | ForEach-Object {
    $t = [System.IO.File]::ReadAllText($_.FullName, $utf8)
    $t = $t.Replace('{{num}}', $Number).Replace('{{slug}}', $Slug).Replace('{{Tên feature}}', $Slug).Replace('{{Standard/Full}}', $Tier)
    if ($_.Name -eq 'SPEC.md') {
        $t = $t.Replace('| Tier | Full |', "| Tier | $Tier |")
        if ($Ui) { $t = $t.Replace('| UI | Không |', '| UI | Có |') }
    }
    [System.IO.File]::WriteAllText($_.FullName, $t, $utf8)
}

Write-Host "✅ Đã tạo $target (tier $Tier)"
Write-Host "Bước tiếp: điền CONTEXT.md (người) → dán .sdd/prompts/01-specify.md cho Agent."
