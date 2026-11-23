<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <jsp:include page="/WEB-INF/views/common/head.jsp" />
</head>
<body>
  <jsp:include page="/WEB-INF/views/admin/layout-start.jsp" />

  <h1 class="h2 mb-3">Kiểm duyệt đánh giá sách</h1>

  <div class="d-flex flex-wrap gap-2 mb-4">
    <a class="btn btn-sm ${empty selectedStatus ? 'bh-btn-primary' : 'bh-btn-outline-neutral'}"
       href="${pageContext.request.contextPath}/admin/reviews">Tất cả (<c:out value="${counts.ALL}" />)</a>
    <a class="btn btn-sm ${selectedStatus == 'PENDING' ? 'bh-btn-primary' : 'bh-btn-outline-neutral'}"
       href="${pageContext.request.contextPath}/admin/reviews?status=PENDING">Chờ duyệt (<c:out value="${counts.PENDING}" />)</a>
    <a class="btn btn-sm ${selectedStatus == 'APPROVED' ? 'bh-btn-primary' : 'bh-btn-outline-neutral'}"
       href="${pageContext.request.contextPath}/admin/reviews?status=APPROVED">Đã duyệt (<c:out value="${counts.APPROVED}" />)</a>
    <a class="btn btn-sm ${selectedStatus == 'HIDDEN' ? 'bh-btn-primary' : 'bh-btn-outline-neutral'}"
       href="${pageContext.request.contextPath}/admin/reviews?status=HIDDEN">Đã ẩn (<c:out value="${counts.HIDDEN}" />)</a>
  </div>

  <section class="bh-card p-3 p-md-4">
    <div class="table-responsive">
      <table class="table bh-table mb-0">
        <thead>
          <tr>
            <th>Sách &amp; Khách hàng</th>
            <th>Đánh giá</th>
            <th>Trạng thái</th>
            <th class="text-end">Kiểm duyệt</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="r" items="${pageResult.items}">
            <tr>
              <td>
                <div class="fw-semibold"><c:out value="${r.bookTitle}" /></div>
                <div class="small bh-muted"><c:out value="${r.reviewerName}" /> (<c:out value="${r.reviewerEmail}" />) &middot; <c:out value="${r.createdAtFormatted}" /></div>
              </td>
              <td>
                <div class="bh-star small mb-1"><c:forEach begin="1" end="${r.rating}">&#9733;</c:forEach> (<c:out value="${r.rating}" />/5)</div>
                <c:if test="${not empty r.title}"><div class="fw-semibold small"><c:out value="${r.title}" /></div></c:if>
                <div class="small"><c:out value="${r.content}" /></div>
              </td>
              <td>
                <c:choose>
                  <c:when test="${r.status.name() == 'APPROVED'}"><span class="bh-badge bh-badge-success">Đã duyệt</span></c:when>
                  <c:when test="${r.status.name() == 'PENDING'}"><span class="bh-badge bh-badge-warning">Chờ duyệt</span></c:when>
                  <c:otherwise><span class="bh-badge bh-badge-neutral">Đã ẩn</span></c:otherwise>
                </c:choose>
              </td>
              <td class="text-end bh-nowrap">
                <c:if test="${r.status.name() != 'APPROVED'}">
                  <form action="${pageContext.request.contextPath}/admin/reviews" method="post" class="d-inline me-1">
                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
                    <input type="hidden" name="reviewId" value="${r.reviewId}">
                    <input type="hidden" name="action" value="approve">
                    <button type="submit" class="btn btn-sm bh-btn-outline-teal">Duyệt</button>
                  </form>
                </c:if>
                <c:if test="${r.status.name() != 'HIDDEN'}">
                  <form action="${pageContext.request.contextPath}/admin/reviews" method="post" class="d-inline">
                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
                    <input type="hidden" name="reviewId" value="${r.reviewId}">
                    <input type="hidden" name="action" value="hide">
                    <button type="submit" class="btn btn-sm bh-btn-danger-outline"
                            data-confirm-title="Ẩn đánh giá"
                            data-confirm-message="Ẩn đánh giá này khỏi trang sách công khai và loại khỏi điểm trung bình?"
                            data-confirm-ok="Ẩn đánh giá">Ẩn</button>
                  </form>
                </c:if>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${pageResult.empty}">
            <tr><td colspan="4" class="text-center py-4 bh-muted">Chưa có đánh giá nào ở mục này.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
    <jsp:include page="/WEB-INF/views/common/pagination.jsp" />
  </section>

  <jsp:include page="/WEB-INF/views/admin/layout-end.jsp" />
</body>
</html>
