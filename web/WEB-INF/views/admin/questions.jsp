<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <jsp:include page="/WEB-INF/views/common/head.jsp" />
</head>
<body>
  <jsp:include page="/WEB-INF/views/admin/layout-start.jsp" />

  <h1 class="h2 mb-3">Quản lý Hỏi đáp (Q&amp;A)</h1>

  <div class="d-flex flex-wrap gap-2 mb-4">
    <a class="btn btn-sm ${empty selectedStatus ? 'bh-btn-primary' : 'bh-btn-outline-neutral'}"
       href="${pageContext.request.contextPath}/admin/questions">Tất cả (<c:out value="${counts.ALL}" />)</a>
    <a class="btn btn-sm ${selectedStatus == 'OPEN' ? 'bh-btn-primary' : 'bh-btn-outline-neutral'}"
       href="${pageContext.request.contextPath}/admin/questions?status=OPEN">Chưa trả lời (<c:out value="${counts.OPEN}" />)</a>
    <a class="btn btn-sm ${selectedStatus == 'ANSWERED' ? 'bh-btn-primary' : 'bh-btn-outline-neutral'}"
       href="${pageContext.request.contextPath}/admin/questions?status=ANSWERED">Đã trả lời (<c:out value="${counts.ANSWERED}" />)</a>
    <a class="btn btn-sm ${selectedStatus == 'HIDDEN' ? 'bh-btn-primary' : 'bh-btn-outline-neutral'}"
       href="${pageContext.request.contextPath}/admin/questions?status=HIDDEN">Đã ẩn (<c:out value="${counts.HIDDEN}" />)</a>
  </div>

  <div class="d-flex flex-column gap-3">
    <c:forEach var="q" items="${pageResult.items}">
      <article class="bh-card p-4">
        <div class="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-2">
          <div>
            <div class="small bh-muted">Sách: <strong><c:out value="${q.bookTitle}" /></strong> &middot; Hỏi bởi <c:out value="${q.askerName}" /> (<c:out value="${q.askerEmail}" />) &middot; <c:out value="${q.createdAtFormatted}" /></div>
            <h2 class="h5 mt-1 mb-1"><c:out value="${q.subject}" /></h2>
          </div>
          <c:choose>
            <c:when test="${q.status.name() == 'ANSWERED'}"><span class="bh-badge bh-badge-success">Đã trả lời</span></c:when>
            <c:when test="${q.status.name() == 'OPEN'}"><span class="bh-badge bh-badge-warning">Chờ phản hồi</span></c:when>
            <c:otherwise><span class="bh-badge bh-badge-neutral">Đã ẩn</span></c:otherwise>
          </c:choose>
        </div>
        <p class="small mb-3"><c:out value="${q.content}" /></p>

        <c:if test="${q.status.name() != 'HIDDEN'}">
          <form action="${pageContext.request.contextPath}/admin/questions" method="post" class="mb-2">
            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
            <input type="hidden" name="questionId" value="${q.questionId}">
            <input type="hidden" name="action" value="${q.answered ? 'updateAnswer' : 'answer'}">
            <label class="form-label small fw-semibold" for="ans_${q.questionId}">
              ${q.answered ? 'Phản hồi hiện tại của BookHub (có thể chỉnh sửa):' : 'Viết phản hồi chính thức của BookHub:'}
            </label>
            <textarea id="ans_${q.questionId}" name="content" rows="2" maxlength="2000" required
                      class="form-control mb-2" placeholder="Nhập câu trả lời..."><c:out value="${q.answerContent}" /></textarea>
            <div class="d-flex flex-wrap gap-2">
              <button type="submit" class="btn btn-sm bh-btn-primary">
                ${q.answered ? 'Cập nhật phản hồi' : 'Gửi phản hồi'}
              </button>
            </div>
          </form>

          <form action="${pageContext.request.contextPath}/admin/questions" method="post" class="m-0">
            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}' />">
            <input type="hidden" name="questionId" value="${q.questionId}">
            <input type="hidden" name="action" value="hide">
            <button type="submit" class="btn btn-sm bh-btn-danger-outline"
                    data-confirm-title="Ẩn câu hỏi"
                    data-confirm-message="Ẩn câu hỏi này khỏi trang chi tiết sách?"
                    data-confirm-ok="Ẩn câu hỏi">Ẩn câu hỏi</button>
          </form>
        </c:if>
      </article>
    </c:forEach>
    <c:if test="${pageResult.empty}">
      <section class="bh-empty-state"><p class="bh-muted mb-0">Chưa có câu hỏi nào ở mục này.</p></section>
    </c:if>
  </div>
  <jsp:include page="/WEB-INF/views/common/pagination.jsp" />

  <jsp:include page="/WEB-INF/views/admin/layout-end.jsp" />
</body>
</html>
