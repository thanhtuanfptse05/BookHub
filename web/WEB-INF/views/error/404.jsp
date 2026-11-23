<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <jsp:include page="/WEB-INF/views/common/head.jsp" />
</head>
<body>
  <jsp:include page="/WEB-INF/views/common/header.jsp" />
  <main class="bh-main">
    <div class="bh-container">
      <section class="bh-empty-state mx-auto">
        <div class="bh-badge bh-badge-warning mb-3">HTTP 404</div>
        <h1 class="h2 mb-2">Không tìm thấy trang</h1>
        <p class="bh-muted mb-4">Trang hoặc cuốn sách bạn đang tìm không tồn tại hoặc đã ngừng kinh doanh.</p>
        <div class="d-flex justify-content-center gap-2 flex-wrap">
          <a class="btn bh-btn-primary" href="${pageContext.request.contextPath}/">Về trang chủ</a>
          <a class="btn bh-btn-outline-teal" href="${pageContext.request.contextPath}/books">Xem danh mục sách</a>
        </div>
      </section>
    </div>
  </main>
  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
