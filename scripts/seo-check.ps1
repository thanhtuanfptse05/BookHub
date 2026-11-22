# scripts/seo-check.ps1 — Kiểm SEO/AEO/GEO tĩnh trên HTML ĐÃ BUILD (trung lập stack).
# Cấu hình .sdd/project.conf: SEO_CHECK=off|warn|error (mặc định warn) · SEO_BUILD_DIR (thư mục HTML đã build) · AI_CRAWLERS.
# Bỏ qua (exit 0) nếu SEO_BUILD_DIR trống/không tồn tại. Luật: .sdd/constraints/seo.md (SE-01..SE-11).
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
[Environment]::CurrentDirectory = $root

$conf = @{}
$confFile = Join-Path $root '.sdd/project.conf'
if (Test-Path $confFile) {
    foreach ($line in [System.IO.File]::ReadAllLines($confFile, [System.Text.Encoding]::UTF8)) {
        if ($line -match '^\s*([A-Z0-9_]+)\s*=\s*(.*)$') { $conf[$Matches[1]] = $Matches[2].Trim() }
    }
}
$mode   = if ($conf['SEO_CHECK']) { $conf['SEO_CHECK'] } else { 'warn' }
$dir    = $conf['SEO_BUILD_DIR']
$policy = if ($conf['AI_CRAWLERS']) { $conf['AI_CRAWLERS'].ToLower() } else { 'allow' }
if ($mode -eq 'off' -or [string]::IsNullOrWhiteSpace($dir) -or -not (Test-Path (Join-Path $root $dir))) {
    Write-Host "ℹ seo-check: bỏ qua (SEO_CHECK=off hoặc SEO_BUILD_DIR trống/chưa build)" -ForegroundColor Gray
    exit 0
}
$dirPath = (Resolve-Path (Join-Path $root $dir)).Path
$findings = New-Object System.Collections.ArrayList
function Add-Finding($file, $id, $msg) {
    [void]$findings.Add("${file} [$id] $msg")
    Write-Host "⚠ ${file} [$id] $msg" -ForegroundColor Yellow
}
function Get-Attr($tag, $name) {
    $m = [regex]::Match($tag, '(?is)\b' + $name + '\s*=\s*(?:"([^"]*)"|''([^'']*)'')')
    if ($m.Success) { if ($m.Groups[1].Success) { return $m.Groups[1].Value } else { return $m.Groups[2].Value } }
    return $null
}

$pages = Get-ChildItem $dirPath -Recurse -File -Filter *.html | Where-Object {
    $_.FullName -notmatch '[\\/]node_modules[\\/]' -and $_.Name -notin '404.html', '500.html' -and $_.Name -notmatch '^google[0-9a-f]+\.html$'
}
foreach ($p in $pages) {
    $html = [System.IO.File]::ReadAllText($p.FullName, [System.Text.Encoding]::UTF8)
    if ($html -notmatch '(?i)<html') { continue }
    $rel = $p.FullName.Substring($dirPath.Length + 1).Replace('\', '/')
    $clean = [regex]::Replace($html, '(?is)<(script|style)\b.*?</\1>', '')

    $t = [regex]::Match($html, '(?is)<title[^>]*>(.*?)</title>')
    $title = if ($t.Success) { ($t.Groups[1].Value -replace '\s+', ' ').Trim() } else { '' }
    if (-not $title) { Add-Finding $rel 'SE-02' 'thiếu <title>' }
    elseif ($title.Length -gt 60) { Add-Finding $rel 'SE-02' "title dài $($title.Length) ký tự (> 60)" }

    $metas = [regex]::Matches($html, '(?is)<meta\b[^>]*>') | ForEach-Object { $_.Value }
    $desc = $null
    foreach ($m in $metas) { if ((Get-Attr $m 'name') -eq 'description') { $desc = Get-Attr $m 'content' } }
    if (-not $desc) { Add-Finding $rel 'SE-02' 'thiếu meta description' }
    elseif ($desc.Length -lt 50 -or $desc.Length -gt 160) { Add-Finding $rel 'SE-02' "meta description dài $($desc.Length) ký tự (nên 50–160)" }

    if ($html -notmatch '(?is)<html\b[^>]*\blang\s*=\s*["''][^"'']+["'']') { Add-Finding $rel 'SE-02' 'thiếu <html lang>' }
    if (-not ($metas | Where-Object { (Get-Attr $_ 'name') -eq 'viewport' })) { Add-Finding $rel 'SE-02' 'thiếu meta viewport' }

    $canon = [regex]::Matches($html, '(?is)<link\b[^>]*>') | ForEach-Object { $_.Value } | Where-Object { (Get-Attr $_ 'rel') -eq 'canonical' } | Select-Object -First 1
    if (-not $canon) { Add-Finding $rel 'SE-02' 'thiếu link canonical' }
    elseif ((Get-Attr $canon 'href') -notmatch '^https?://') { Add-Finding $rel 'SE-02' 'canonical phải là URL tuyệt đối' }

    $h1 = ([regex]::Matches($clean, '(?i)<h1\b')).Count
    if ($h1 -ne 1) { Add-Finding $rel 'SE-02' "có $h1 thẻ <h1> (cần đúng 1)" }

    $noAlt = ([regex]::Matches($clean, '(?is)<img\b[^>]*>') | Where-Object { $_.Value -notmatch '(?i)\balt\s*=' }).Count
    if ($noAlt -gt 0) { Add-Finding $rel 'SE-04' "$noAlt thẻ <img> thiếu alt" }

    foreach ($og in 'og:title', 'og:description', 'og:image') {
        if (-not ($metas | Where-Object { (Get-Attr $_ 'property') -eq $og })) { Add-Finding $rel 'SE-03' "thiếu $og" }
    }

    $ld = [regex]::Matches($html, '(?is)<script\b[^>]*type\s*=\s*["'']application/ld\+json["''][^>]*>(.*?)</script>')
    if ($ld.Count -eq 0) { Add-Finding $rel 'SE-06' 'chưa có JSON-LD (structured data)' }
    foreach ($b in $ld) {
        try { $null = $b.Groups[1].Value | ConvertFrom-Json -ErrorAction Stop }
        catch { Add-Finding $rel 'SE-06' 'JSON-LD không phải JSON hợp lệ' }
    }

    if ($metas | Where-Object { (Get-Attr $_ 'name') -eq 'robots' -and (Get-Attr $_ 'content') -match 'noindex' }) {
        Add-Finding $rel 'SE-05' 'trang có noindex (đúng chủ ý? production công khai không được noindex nhầm)'
    }

    $text = ([regex]::Replace($clean, '(?s)<[^>]+>', ' ') -replace '\s+', ' ').Trim()
    if ($text.Length -lt 200) { Add-Finding $rel 'SE-01' "HTML ban đầu gần như trống ($($text.Length) ký tự chữ) — nội dung có thể chỉ hiện nhờ JS; AI crawler sẽ không thấy" }
}
if (@($pages).Count -eq 0) { Add-Finding $dir 'SE-01' 'không tìm thấy trang HTML nào trong SEO_BUILD_DIR' }

# Site-level: robots.txt, sitemap, chính sách AI crawler
$robotsPath = Join-Path $dirPath 'robots.txt'
if (-not (Test-Path $robotsPath)) { Add-Finding 'robots.txt' 'SE-05' 'thiếu robots.txt ở gốc' }
else {
    $robots = [System.IO.File]::ReadAllText($robotsPath, [System.Text.Encoding]::UTF8)
    if ($robots -notmatch '(?im)^\s*sitemap\s*:') { Add-Finding 'robots.txt' 'SE-05' 'chưa khai báo Sitemap:' }
    $blocked = @{}; $agents = @(); $lastRule = $false
    foreach ($raw in ($robots -split "`r?`n")) {
        $ln = ($raw -replace '#.*$', '').Trim()
        if (-not $ln) { continue }
        if ($ln -match '^(?i)user-agent\s*:\s*(.+)$') {
            if ($lastRule) { $agents = @(); $lastRule = $false }
            $agents += $Matches[1].Trim().ToLower()
        } elseif ($ln -match '^(?i)disallow\s*:\s*/\s*$') {
            foreach ($a in $agents) { $blocked[$a] = $true }; $lastRule = $true
        } else { $lastRule = $true }
    }
    if ($blocked['*']) { Add-Finding 'robots.txt' 'SE-05' 'Disallow: / cho mọi bot — production sẽ không được lập chỉ mục' }
    $training = 'gptbot', 'claudebot', 'google-extended', 'applebot-extended', 'ccbot'
    $search = 'oai-searchbot', 'claude-searchbot', 'perplexitybot'
    switch ($policy) {
        'allow' { foreach ($a in ($training + $search)) { if ($blocked[$a]) { Add-Finding 'robots.txt' 'SE-11' "AI_CRAWLERS=allow nhưng robots.txt chặn $a" } } }
        'block-training' {
            foreach ($a in $training) { if (-not $blocked[$a]) { Add-Finding 'robots.txt' 'SE-11' "AI_CRAWLERS=block-training nhưng chưa chặn $a" } }
            foreach ($a in $search) { if ($blocked[$a]) { Add-Finding 'robots.txt' 'SE-11' "AI_CRAWLERS=block-training nhưng lại chặn bot search $a (mất trích dẫn AI)" } }
        }
        'block-all' { foreach ($a in ($training + $search)) { if (-not $blocked[$a]) { Add-Finding 'robots.txt' 'SE-11' "AI_CRAWLERS=block-all nhưng chưa chặn $a" } } }
    }
}
if (-not ('sitemap.xml', 'sitemap-index.xml', 'sitemap_index.xml' | Where-Object { Test-Path (Join-Path $dirPath $_) })) {
    Add-Finding 'sitemap.xml' 'SE-05' 'thiếu sitemap.xml ở gốc'
}

Write-Host ("seo-check: {0} phát hiện trên {1} trang (mode={2}, AI_CRAWLERS={3})" -f $findings.Count, @($pages).Count, $mode, $policy)
if ($mode -eq 'error' -and $findings.Count -gt 0) { exit 1 } else { exit 0 }
