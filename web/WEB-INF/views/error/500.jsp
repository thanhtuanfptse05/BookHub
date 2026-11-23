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
        <div class="bh-badge bh-badge-danger mb-3">HTTP 500</div>
        <h1 class="h2 mb-2">Đã có lỗi hệ thống</h1>
        <p class="bh-muted mb-4">Hệ thống gặp sự cố tạm thời khi xử lý yêu cầu của bạn. Vui lòng thử lại sau ít phút.</p>
        <div class="d-flex justify-content-center gap-2 flex-wrap">
          <a class="btn bh-btn-primary" href="${pageContext.request.contextPath}/">Về trang chủ</a>
          <a class="btn bh-btn-outline-neutral" href="${pageContext.request.contextPath}/books">Danh mục sách</a>
        </div>
      </section>
    </div>
  </main>
  <jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
