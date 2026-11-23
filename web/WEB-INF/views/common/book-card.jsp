<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<article class="bh-book-card">
  <a class="bh-book-cover-wrap d-block" href="${pageContext.request.contextPath}/books/detail?id=${bookCard.bookId}">
    <c:set var="imgSrc" value="${bookCard.coverUrl}" />
    <c:choose>
      <c:when test="${not empty imgSrc and (imgSrc.startsWith('http://') or imgSrc.startsWith('https://'))}">
        <img src="<c:out value='${imgSrc}' />"
             alt="Bìa sách <c:out value='${bookCard.title}' />"
             data-fallback="${pageContext.request.contextPath}/assets/images/book-placeholder.svg"
             loading="lazy">
      </c:when>
      <c:otherwise>
        <img src="${pageContext.request.contextPath}<c:out value='${not empty imgSrc ? imgSrc : "/assets/images/book-placeholder.svg"}' />"
             alt="Bìa sách <c:out value='${bookCard.title}' />"
             data-fallback="${pageContext.request.contextPath}/assets/images/book-placeholder.svg"
             loading="lazy">
      </c:otherwise>
    </c:choose>
  </a>
  <div class="p-3 d-flex flex-column flex-grow-1 gap-2">
    <div class="small bh-muted"><c:out value="${bookCard.categoryName}" /></div>
    <h3 class="m-0">
      <a class="bh-book-title" href="${pageContext.request.contextPath}/books/detail?id=${bookCard.bookId}">
        <c:out value="${bookCard.title}" />
      </a>
    </h3>
    <div class="small bh-muted text-truncate"><c:out value="${bookCard.author}" /></div>
    <div class="mt-auto pt-2 d-flex align-items-center justify-content-between gap-2 flex-wrap">
      <span class="bh-price"><c:out value="${bookCard.formattedPrice}" /></span>
      <c:choose>
        <c:when test="${bookCard.stockQty > 0}">
          <span class="bh-badge bh-badge-success">Còn hàng</span>
        </c:when>
        <c:otherwise>
          <span class="bh-badge bh-badge-neutral">Hết hàng</span>
        </c:otherwise>
      </c:choose>
    </div>
    <a class="btn bh-btn-outline-teal w-100 mt-1" href="${pageContext.request.contextPath}/books/detail?id=${bookCard.bookId}">
      Xem chi tiết
    </a>
  </div>
</article>
