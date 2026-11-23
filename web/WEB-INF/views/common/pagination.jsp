<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty pageResult and pageResult.totalPages > 1}">
  <nav class="d-flex justify-content-center align-items-center gap-2 mt-4 flex-wrap" aria-label="Phân trang">
    <c:url var="prevUrl" value="${paginationPath}">
      <c:if test="${not empty param.keyword}"><c:param name="keyword" value="${param.keyword}" /></c:if>
      <c:if test="${not empty param.categoryId}"><c:param name="categoryId" value="${param.categoryId}" /></c:if>
      <c:if test="${not empty param.minPrice}"><c:param name="minPrice" value="${param.minPrice}" /></c:if>
      <c:if test="${not empty param.maxPrice}"><c:param name="maxPrice" value="${param.maxPrice}" /></c:if>
      <c:if test="${not empty param.sort}"><c:param name="sort" value="${param.sort}" /></c:if>
      <c:if test="${not empty param.status}"><c:param name="status" value="${param.status}" /></c:if>
      <c:if test="${not empty param.orderCode}"><c:param name="orderCode" value="${param.orderCode}" /></c:if>
      <c:if test="${not empty param.fromDate}"><c:param name="fromDate" value="${param.fromDate}" /></c:if>
      <c:if test="${not empty param.toDate}"><c:param name="toDate" value="${param.toDate}" /></c:if>
      <c:param name="page" value="${pageResult.page - 1}" />
    </c:url>
    <a class="bh-page-link ${pageResult.page <= 1 ? 'disabled' : ''}" href="${prevUrl}">Trước</a>

    <c:forEach begin="1" end="${pageResult.totalPages}" var="p">
      <c:url var="pUrl" value="${paginationPath}">
        <c:if test="${not empty param.keyword}"><c:param name="keyword" value="${param.keyword}" /></c:if>
        <c:if test="${not empty param.categoryId}"><c:param name="categoryId" value="${param.categoryId}" /></c:if>
        <c:if test="${not empty param.minPrice}"><c:param name="minPrice" value="${param.minPrice}" /></c:if>
        <c:if test="${not empty param.maxPrice}"><c:param name="maxPrice" value="${param.maxPrice}" /></c:if>
        <c:if test="${not empty param.sort}"><c:param name="sort" value="${param.sort}" /></c:if>
        <c:if test="${not empty param.status}"><c:param name="status" value="${param.status}" /></c:if>
        <c:if test="${not empty param.orderCode}"><c:param name="orderCode" value="${param.orderCode}" /></c:if>
        <c:if test="${not empty param.fromDate}"><c:param name="fromDate" value="${param.fromDate}" /></c:if>
        <c:if test="${not empty param.toDate}"><c:param name="toDate" value="${param.toDate}" /></c:if>
        <c:param name="page" value="${p}" />
      </c:url>
      <a class="bh-page-link ${p == pageResult.page ? 'active' : ''}" href="${pUrl}">${p}</a>
    </c:forEach>

    <c:url var="nextUrl" value="${paginationPath}">
      <c:if test="${not empty param.keyword}"><c:param name="keyword" value="${param.keyword}" /></c:if>
      <c:if test="${not empty param.categoryId}"><c:param name="categoryId" value="${param.categoryId}" /></c:if>
      <c:if test="${not empty param.minPrice}"><c:param name="minPrice" value="${param.minPrice}" /></c:if>
      <c:if test="${not empty param.maxPrice}"><c:param name="maxPrice" value="${param.maxPrice}" /></c:if>
      <c:if test="${not empty param.sort}"><c:param name="sort" value="${param.sort}" /></c:if>
      <c:if test="${not empty param.status}"><c:param name="status" value="${param.status}" /></c:if>
      <c:if test="${not empty param.orderCode}"><c:param name="orderCode" value="${param.orderCode}" /></c:if>
      <c:if test="${not empty param.fromDate}"><c:param name="fromDate" value="${param.fromDate}" /></c:if>
      <c:if test="${not empty param.toDate}"><c:param name="toDate" value="${param.toDate}" /></c:if>
      <c:param name="page" value="${pageResult.page + 1}" />
    </c:url>
    <a class="bh-page-link ${pageResult.page >= pageResult.totalPages ? 'disabled' : ''}" href="${nextUrl}">Sau</a>
  </nav>
</c:if>
