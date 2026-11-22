#!/usr/bin/env bash
# scripts/seo-check.sh — Kiểm SEO/AEO/GEO tĩnh trên HTML ĐÃ BUILD (trung lập stack).
# Cấu hình .sdd/project.conf: SEO_CHECK=off|warn|error (mặc định warn) · SEO_BUILD_DIR · AI_CRAWLERS.
# Bỏ qua (exit 0) nếu SEO_BUILD_DIR trống/không tồn tại. Luật: .sdd/constraints/seo.md (SE-01..SE-11).
set -uo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$root"
conf() { grep -E "^$1=" .sdd/project.conf 2>/dev/null | head -1 | cut -d= -f2- | tr -d '\r'; }
mode="$(conf SEO_CHECK)"; mode="${mode:-warn}"
dir="$(conf SEO_BUILD_DIR)"
policy="$(conf AI_CRAWLERS | tr 'A-Z' 'a-z')"; policy="${policy:-allow}"
if [[ "$mode" == "off" || -z "$dir" || ! -d "$dir" ]]; then
  echo "ℹ seo-check: bỏ qua (SEO_CHECK=off hoặc SEO_BUILD_DIR trống/chưa build)"; exit 0
fi

findings=0
add() { echo "⚠ $1 [$2] $3"; findings=$((findings+1)); }
has_node=0; command -v node >/dev/null 2>&1 && has_node=1
pages=0

while IFS= read -r -d '' f; do
  raw="$(tr '\n\r\t' '   ' < "$f")"
  lc="$(printf '%s' "$raw" | tr 'A-Z' 'a-z')"
  grep -q '<html' <<<"$lc" || continue
  pages=$((pages+1)); rel="${f#"$dir"/}"
  body="$(sed -E 's/<script[^>]*>[^<]*<\/script>//g; s/<style[^>]*>[^<]*<\/style>//g' <<<"$lc")"

  title="$(grep -oE '<title[^>]*>[^<]*</title>' <<<"$lc" | head -1 | sed -E 's/<[^>]*>//g; s/^ +//; s/ +$//')"
  if [[ -z "$title" ]]; then add "$rel" SE-02 "thiếu <title>"
  elif [[ ${#title} -gt 60 ]]; then add "$rel" SE-02 "title dài ${#title} ký tự (> 60)"; fi

  dtag="$(grep -oE '<meta[^>]*name=["'\'']description["'\''][^>]*>' <<<"$lc" | head -1)"
  if [[ -z "$dtag" ]]; then add "$rel" SE-02 "thiếu meta description"
  else
    dc="$(sed -E 's/.*content=["'\'']([^"'\'']*)["'\''].*/\1/' <<<"$dtag")"
    if [[ ${#dc} -lt 50 || ${#dc} -gt 160 ]]; then add "$rel" SE-02 "meta description dài ${#dc} ký tự (nên 50–160)"; fi
  fi

  grep -qE '<html[^>]*lang=["'\''][^"'\'']+' <<<"$lc" || add "$rel" SE-02 "thiếu <html lang>"
  grep -qE 'name=["'\'']viewport["'\'']' <<<"$lc" || add "$rel" SE-02 "thiếu meta viewport"

  ctag="$(grep -oE '<link[^>]*rel=["'\'']canonical["'\''][^>]*>' <<<"$lc" | head -1)"
  if [[ -z "$ctag" ]]; then add "$rel" SE-02 "thiếu link canonical"
  elif ! grep -qE 'href=["'\'']https?://' <<<"$ctag"; then add "$rel" SE-02 "canonical phải là URL tuyệt đối"; fi

  h1="$(grep -oE '<h1[ >]' <<<"$body" | wc -l | tr -d ' ')"
  [[ "$h1" -eq 1 ]] || add "$rel" SE-02 "có $h1 thẻ <h1> (cần đúng 1)"

  noalt="$(grep -oE '<img[^>]*>' <<<"$body" | grep -vc 'alt=' || true)"
  [[ "${noalt:-0}" -eq 0 ]] || add "$rel" SE-04 "$noalt thẻ <img> thiếu alt"

  for og in og:title og:description og:image; do
    grep -qE "property=[\"']$og[\"']" <<<"$lc" || add "$rel" SE-03 "thiếu $og"
  done

  blocks="$(grep -oE '<script[^>]*application/ld\+json[^>]*>[^<]*</script>' <<<"$raw" || true)"
  if [[ -z "$blocks" ]]; then add "$rel" SE-06 "chưa có JSON-LD (structured data)"
  elif [[ $has_node -eq 1 ]]; then
    while IFS= read -r b; do
      printf '%s' "$b" | sed -E 's/<[^>]*>//g' | node -e 'JSON.parse(require("fs").readFileSync(0,"utf8"))' >/dev/null 2>&1 \
        || add "$rel" SE-06 "JSON-LD không phải JSON hợp lệ"
    done <<<"$blocks"
  fi

  if grep -qE 'name=["'\'']robots["'\''][^>]*noindex|noindex[^>]*name=["'\'']robots["'\'']' <<<"$lc"; then
    add "$rel" SE-05 "trang có noindex (đúng chủ ý? production công khai không được noindex nhầm)"
  fi

  txt="$(sed -E 's/<[^>]*>/ /g' <<<"$body" | tr -s ' ')"
  if [[ ${#txt} -lt 200 ]]; then add "$rel" SE-01 "HTML ban đầu gần như trống (${#txt} ký tự chữ) — nội dung có thể chỉ hiện nhờ JS; AI crawler sẽ không thấy"; fi
done < <(find "$dir" -type f -name '*.html' ! -name 404.html ! -name 500.html -not -path '*/node_modules/*' -print0)
[[ $pages -gt 0 ]] || add "$dir" SE-01 "không tìm thấy trang HTML nào trong SEO_BUILD_DIR"

robots="$dir/robots.txt"
if [[ ! -f "$robots" ]]; then add robots.txt SE-05 "thiếu robots.txt ở gốc"
else
  grep -qiE '^[[:space:]]*sitemap[[:space:]]*:' "$robots" || add robots.txt SE-05 "chưa khai báo Sitemap:"
  blocked="$(awk '
    { sub(/#.*/,""); gsub(/\r/,""); line=$0; gsub(/^[ \t]+|[ \t]+$/,"",line); if (line=="") next; l=tolower(line)
      if (l ~ /^user-agent[ \t]*:/) { if (last) { n=0; last=0 } sub(/^[^:]*:[ \t]*/,"",l); agents[++n]=l }
      else if (l ~ /^disallow[ \t]*:[ \t]*\/[ \t]*$/) { for (i=1;i<=n;i++) print agents[i]; last=1 }
      else last=1 }' "$robots" | sort -u)"
  isb() { grep -qxF "$1" <<<"$blocked"; }
  isb '*' && add robots.txt SE-05 "Disallow: / cho mọi bot — production sẽ không được lập chỉ mục"
  training="gptbot claudebot google-extended applebot-extended ccbot"
  search="oai-searchbot claude-searchbot perplexitybot"
  case "$policy" in
    allow) for a in $training $search; do isb "$a" && add robots.txt SE-11 "AI_CRAWLERS=allow nhưng robots.txt chặn $a"; done ;;
    block-training)
      for a in $training; do isb "$a" || add robots.txt SE-11 "AI_CRAWLERS=block-training nhưng chưa chặn $a"; done
      for a in $search; do isb "$a" && add robots.txt SE-11 "AI_CRAWLERS=block-training nhưng lại chặn bot search $a (mất trích dẫn AI)"; done ;;
    block-all) for a in $training $search; do isb "$a" || add robots.txt SE-11 "AI_CRAWLERS=block-all nhưng chưa chặn $a"; done ;;
  esac
fi
[[ -f "$dir/sitemap.xml" || -f "$dir/sitemap-index.xml" || -f "$dir/sitemap_index.xml" ]] || add sitemap.xml SE-05 "thiếu sitemap.xml ở gốc"

echo "seo-check: $findings phát hiện trên $pages trang (mode=$mode, AI_CRAWLERS=$policy)"
[[ "$mode" == "error" && $findings -gt 0 ]] && exit 1
exit 0
