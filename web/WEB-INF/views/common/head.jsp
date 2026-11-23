<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><c:out value="${not empty pageTitle ? pageTitle : 'BookHub — Hiệu sách trực tuyến'}" /></title>
<c:if test="${not empty metaDescription}">
  <meta name="description" content="<c:out value='${metaDescription}' />">
  <meta property="og:description" content="<c:out value='${metaDescription}' />">
</c:if>
<meta property="og:title" content="<c:out value='${not empty pageTitle ? pageTitle : "BookHub — Hiệu sách trực tuyến"}' />">
<meta property="og:type" content="<c:out value='${not empty ogType ? ogType : "website"}' />">
<c:if test="${not empty canonicalUrl}">
  <link rel="canonical" href="<c:out value='${canonicalUrl}' />">
  <meta property="og:url" content="<c:out value='${canonicalUrl}' />">
</c:if>
<c:if test="${not empty ogImage}">
  <meta property="og:image" content="<c:out value='${ogImage}' />">
</c:if>
<c:if test="${noindex}">
  <meta name="robots" content="noindex, nofollow">
</c:if>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700&family=Newsreader:opsz,wght@6..72,500;6..72,600;6..72,700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/vendor/bootstrap/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/tokens.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bookhub.css">
